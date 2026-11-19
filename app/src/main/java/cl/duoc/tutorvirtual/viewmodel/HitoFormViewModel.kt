package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.repository.HitoRepository
import cl.duoc.tutorvirtual.validation.HitoErrores
import cl.duoc.tutorvirtual.validation.HitoValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Estado completo del formulario: valores + errores + banderas de la UI (2.3.1). */
data class HitoFormUiState(
    val id: Long = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val formato: String = "",
    val fechaLimite: LocalDate? = null,
    val estado: EstadoHito = EstadoHito.POR_INICIAR,
    val fotoEvidencia: String? = null,
    val errores: HitoErrores = HitoErrores(),
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val mensaje: String? = null          // mensaje para Snackbar
)

/**
 * ViewModel del formulario de hito (crear o editar).
 * Cada cambio de campo → se valida ese campo → se emite un nuevo estado →
 * la pantalla muestra el error al instante ("tiempo real", 2.3.2).
 */
class HitoFormViewModel(
    private val repositorio: HitoRepository,
    private val hitoId: Long?,
    private val esProfesor: Flow<Boolean> = flowOf(false),
    private val hoy: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _estado = MutableStateFlow(HitoFormUiState(esEdicion = hitoId != null))
    val estado: StateFlow<HitoFormUiState> = _estado.asStateFlow()

    // Valores originales al editar: se usan en las reglas que dependen de "si cambió"
    private var original: Hito? = null

    init {
        if (hitoId != null) cargarHito(hitoId)
    }

    private fun cargarHito(id: Long) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val hito = repositorio.obtener(id)
            original = hito
            _estado.update { s ->
                if (hito == null) s.copy(cargando = false, mensaje = "No se encontró el hito")
                else s.copy(
                    id = hito.id,
                    titulo = hito.titulo,
                    descripcion = hito.descripcion,
                    formato = hito.formatoRequerido,
                    fechaLimite = hito.fechaLimite,
                    estado = hito.estado,
                    fotoEvidencia = hito.fotoEvidencia,
                    cargando = false
                )
            }
        }
    }

    // --- Cambios de cada campo (la vista solo llama a estas funciones) ---

    fun onTituloChange(valor: String) = _estado.update {
        it.copy(titulo = valor, errores = it.errores.copy(titulo = HitoValidator.validarTitulo(valor)))
    }

    fun onDescripcionChange(valor: String) = _estado.update {
        it.copy(descripcion = valor, errores = it.errores.copy(descripcion = HitoValidator.validarDescripcion(valor)))
    }

    fun onFormatoChange(valor: String) = _estado.update {
        it.copy(formato = valor, errores = it.errores.copy(formato = HitoValidator.validarFormato(valor)))
    }

    fun onFechaChange(fecha: LocalDate) = _estado.update {
        it.copy(
            fechaLimite = fecha,
            errores = it.errores.copy(
                fecha = HitoValidator.validarFecha(fecha, it.estado, hoy(), original?.fechaLimite)
            )
        )
    }

    /** Cambiar el estado también re-valida la fecha (dependencia entre campos). */
    fun onEstadoChange(nuevo: EstadoHito) {
        viewModelScope.launch {
            val profesor = esProfesor.first()
            _estado.update {
                val errorEstado = HitoValidator.validarEstado(nuevo, profesor, original?.estado)
                // Si ya hay fecha elegida, se vuelve a revisar con el nuevo estado
                val errorFecha = if (it.fechaLimite != null) {
                    HitoValidator.validarFecha(it.fechaLimite, nuevo, hoy(), original?.fechaLimite)
                } else {
                    it.errores.fecha
                }
                it.copy(
                    estado = nuevo,
                    errores = it.errores.copy(estado = errorEstado, fecha = errorFecha)
                )
            }
        }
    }

    fun onFotoChange(ruta: String?) = _estado.update { it.copy(fotoEvidencia = ruta) }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    /** Valida todo; si está correcto, guarda en Room. */
    fun guardar() {
        if (_estado.value.guardando) return
        viewModelScope.launch {
            // Solo el profesor guía define la estructura: crea y edita hitos
            if (!esProfesor.first()) {
                _estado.update { it.copy(mensaje = "Solo el profesor guía puede crear o editar hitos") }
                return@launch
            }
            val s = _estado.value
            val errores = HitoValidator.validarTodo(
                titulo = s.titulo,
                descripcion = s.descripcion,
                formato = s.formato,
                fecha = s.fechaLimite,
                estado = s.estado,
                hoy = hoy(),
                esProfesor = esProfesor.first(),
                fechaOriginal = original?.fechaLimite,
                estadoOriginal = original?.estado
            )
            if (errores.hayErrores) {
                _estado.update { it.copy(errores = errores, mensaje = "Revisa los campos marcados en rojo") }
                return@launch
            }
            _estado.update { it.copy(errores = errores, guardando = true) }
            repositorio.guardar(
                Hito(
                    id = s.id,
                    titulo = s.titulo.trim(),
                    descripcion = s.descripcion.trim(),
                    formatoRequerido = s.formato.trim(),
                    fechaLimite = s.fechaLimite!!,     // no es null: ya pasó la validación
                    estado = s.estado,
                    fotoEvidencia = s.fotoEvidencia
                )
            )
            _estado.update { it.copy(guardando = false, guardado = true) }
        }
    }

    companion object {
        fun factory(hitoId: Long?) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                HitoFormViewModel(
                    repositorio = app.contenedor.hitoRepository,
                    hitoId = hitoId,
                    esProfesor = app.contenedor.preferencias.esProfesor
                )
            }
        }
    }
}
