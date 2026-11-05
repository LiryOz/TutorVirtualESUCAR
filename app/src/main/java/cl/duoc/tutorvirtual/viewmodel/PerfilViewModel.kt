package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.model.Ajustes
import cl.duoc.tutorvirtual.data.model.Asignatura
import cl.duoc.tutorvirtual.data.model.Perfil
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.repository.PreferenciasRepository
import cl.duoc.tutorvirtual.validation.PerfilErrores
import cl.duoc.tutorvirtual.validation.PerfilValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val asignatura: Asignatura? = null,
    val profesorGuia: String = "",
    val rol: Rol = Rol.ESTUDIANTE,
    val errores: PerfilErrores = PerfilErrores(),
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val mensaje: String? = null
)

/** ViewModel del perfil: formulario validado + ajustes, todo guardado en DataStore. */
class PerfilViewModel(private val preferencias: PreferenciasRepository) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUiState())
    val estado: StateFlow<PerfilUiState> = _estado.asStateFlow()

    /** Los ajustes se guardan apenas se tocan los Switch, por eso se observan directo. */
    val ajustes: StateFlow<Ajustes> = preferencias.ajustes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Ajustes())

    /** La foto viene de DataStore (se guarda apenas se toma, ver Etapa 8). */
    val foto: StateFlow<String?> = preferencias.perfil.map { it.foto }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Cargar una vez el perfil guardado en el formulario
        viewModelScope.launch {
            val p = preferencias.perfil.first()
            _estado.update {
                it.copy(
                    nombre = p.nombre, rut = p.rut, correo = p.correo,
                    asignatura = p.asignatura, profesorGuia = p.profesorGuia,
                    rol = p.rol, cargando = false
                )
            }
        }
    }

    fun onNombreChange(v: String) = _estado.update {
        it.copy(nombre = v, errores = it.errores.copy(nombre = PerfilValidator.validarNombre(v)))
    }

    fun onRutChange(v: String) = _estado.update {
        it.copy(rut = v, errores = it.errores.copy(rut = PerfilValidator.validarRut(v)))
    }

    fun onCorreoChange(v: String) = _estado.update {
        it.copy(correo = v, errores = it.errores.copy(correo = PerfilValidator.validarCorreo(v)))
    }

    fun onAsignaturaChange(v: Asignatura) = _estado.update {
        it.copy(asignatura = v, errores = it.errores.copy(asignatura = null))
    }

    fun onProfesorGuiaChange(v: String) = _estado.update {
        it.copy(profesorGuia = v, errores = it.errores.copy(profesorGuia = PerfilValidator.validarProfesorGuia(v, it.rol)))
    }

    /** Cambiar el rol afecta qué campos son obligatorios: se re-validan. */
    fun onRolChange(v: Rol) = _estado.update {
        it.copy(
            rol = v,
            errores = it.errores.copy(
                asignatura = if (it.errores.asignatura != null) PerfilValidator.validarAsignatura(it.asignatura, v) else null,
                profesorGuia = if (it.errores.profesorGuia != null) PerfilValidator.validarProfesorGuia(it.profesorGuia, v) else null
            )
        )
    }

    fun guardar() {
        val s = _estado.value
        val errores = PerfilValidator.validarTodo(s.nombre, s.rut, s.correo, s.asignatura, s.profesorGuia, s.rol)
        if (errores.hayErrores) {
            _estado.update { it.copy(errores = errores, mensaje = "Revisa los campos marcados en rojo") }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(errores = errores, guardando = true) }
            val fotoActual = preferencias.perfil.first().foto
            preferencias.guardarPerfil(
                Perfil(
                    nombre = s.nombre.trim(), rut = s.rut.trim().uppercase(), correo = s.correo.trim(),
                    asignatura = s.asignatura, profesorGuia = s.profesorGuia.trim(),
                    rol = s.rol, foto = fotoActual
                )
            )
            _estado.update { it.copy(guardando = false, mensaje = "Perfil guardado") }
        }
    }

    fun cambiarRecordatorios(activo: Boolean) {
        viewModelScope.launch { preferencias.cambiarRecordatorios(activo) }
    }

    fun cambiarTextoGrande(activo: Boolean) {
        viewModelScope.launch { preferencias.cambiarTextoGrande(activo) }
    }

    fun guardarFoto(ruta: String?) {
        viewModelScope.launch { preferencias.guardarFoto(ruta) }
    }

    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                PerfilViewModel(app.contenedor.preferencias)
            }
        }
    }
}
