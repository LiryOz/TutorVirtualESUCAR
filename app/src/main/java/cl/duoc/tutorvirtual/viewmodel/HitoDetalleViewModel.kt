package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.model.Adjunto
import cl.duoc.tutorvirtual.data.model.Comentario
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.Perfil
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.model.TipoComentario
import cl.duoc.tutorvirtual.data.repository.HitoRepository
import cl.duoc.tutorvirtual.data.repository.PreferenciasRepository
import cl.duoc.tutorvirtual.nativo.GestorArchivos
import cl.duoc.tutorvirtual.validation.ComentarioValidator
import cl.duoc.tutorvirtual.validation.PermisosHito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/** Datos que vienen de Room y DataStore (se actualizan solos). */
data class HitoDetalleUiState(
    val cargando: Boolean = true,
    val hito: Hito? = null,
    val comentarios: List<Comentario> = emptyList(),
    val perfil: Perfil = Perfil(),
    val hoy: LocalDate = LocalDate.now()
) {
    val rol: Rol get() = perfil.rol
    val esProfesor: Boolean get() = rol == Rol.PROFESOR_GUIA
}

/** Lo que el usuario está escribiendo en la conversación. */
data class NuevoComentarioUiState(
    val texto: String = "",
    val tipo: TipoComentario = TipoComentario.AVANCE,
    val adjunto: Adjunto? = null,
    val error: String? = null,
    val adjuntando: Boolean = false,
    val enviando: Boolean = false,
    val mensaje: String? = null,
    val eliminado: Boolean = false       // el profesor borró el hito → volver atrás
)

/**
 * Detalle de un hito: datos, estado y conversación (avances, dudas y respuestas).
 * Aplica las reglas de PermisosHito según el perfil con sesión abierta.
 */
class HitoDetalleViewModel(
    private val hitoId: Long,
    private val repositorio: HitoRepository,
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    val estado: StateFlow<HitoDetalleUiState> = combine(
        repositorio.observarHito(hitoId),
        repositorio.observarComentarios(hitoId),
        preferencias.perfil
    ) { hito, comentarios, perfil ->
        HitoDetalleUiState(cargando = false, hito = hito, comentarios = comentarios, perfil = perfil)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HitoDetalleUiState())

    private val _nuevo = MutableStateFlow(NuevoComentarioUiState())
    val nuevo: StateFlow<NuevoComentarioUiState> = _nuevo.asStateFlow()

    init {
        // El tipo por defecto depende del perfil: el profesor siempre "Responde"
        viewModelScope.launch {
            val rol = preferencias.perfil.first().rol
            _nuevo.update { it.copy(tipo = PermisosHito.tiposDeComentario(rol).first()) }
        }
    }

    fun onTextoChange(v: String) = _nuevo.update {
        it.copy(texto = v, error = if (it.error != null) ComentarioValidator.validar(v, it.adjunto != null) else null)
    }

    fun onTipoChange(t: TipoComentario) = _nuevo.update { it.copy(tipo = t) }

    fun adjuntando(activo: Boolean) = _nuevo.update { it.copy(adjuntando = activo) }

    fun onAdjunto(resultado: GestorArchivos.Resultado) = _nuevo.update {
        when (resultado) {
            is GestorArchivos.Resultado.Ok -> {
                GestorArchivos.eliminar(it.adjunto?.ruta)   // reemplaza el anterior sin enviar
                it.copy(adjunto = resultado.adjunto, error = null, adjuntando = false)
            }
            GestorArchivos.Resultado.MuyGrande -> it.copy(adjuntando = false, mensaje = "El archivo supera los 10 MB")
            GestorArchivos.Resultado.Error -> it.copy(adjuntando = false, mensaje = "No se pudo adjuntar el archivo")
        }
    }

    fun quitarAdjunto() = _nuevo.update {
        GestorArchivos.eliminar(it.adjunto?.ruta)
        it.copy(adjunto = null)
    }

    fun enviar() {
        val s = _nuevo.value
        if (s.enviando) return
        val error = ComentarioValidator.validar(s.texto, s.adjunto != null)
        if (error != null) {
            _nuevo.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            _nuevo.update { it.copy(enviando = true) }
            val perfil = preferencias.perfil.first()
            // Seguridad: el tipo siempre debe corresponder al perfil
            val tipo = s.tipo.takeIf { it in PermisosHito.tiposDeComentario(perfil.rol) }
                ?: PermisosHito.tiposDeComentario(perfil.rol).first()
            repositorio.agregarComentario(
                Comentario(
                    hitoId = hitoId,
                    autorRol = perfil.rol,
                    autorNombre = perfil.nombre.ifBlank { perfil.rol.etiqueta },
                    tipo = tipo,
                    texto = s.texto.trim(),
                    fechaHora = LocalDateTime.now(),
                    adjunto = s.adjunto
                )
            )
            _nuevo.update { it.copy(texto = "", adjunto = null, error = null, enviando = false, mensaje = "Comentario enviado") }
        }
    }

    /** Solo el autor (mismo perfil) puede borrar su comentario. */
    fun eliminarComentario(comentario: Comentario) {
        viewModelScope.launch {
            if (comentario.autorRol != preferencias.perfil.first().rol) return@launch
            repositorio.eliminarComentario(comentario.id)
            GestorArchivos.eliminar(comentario.adjunto?.ruta)
        }
    }

    fun cambiarEstado(nuevo: EstadoHito) {
        viewModelScope.launch {
            val hito = repositorio.obtener(hitoId) ?: return@launch
            val rol = preferencias.perfil.first().rol
            if (!PermisosHito.puedeCambiarEstado(rol, hito.estado, nuevo)) {
                _nuevo.update { it.copy(mensaje = "Solo el profesor guía puede aprobar o modificar un hito aprobado") }
                return@launch
            }
            if (nuevo != hito.estado) {
                repositorio.cambiarEstado(hito, nuevo)
                if (nuevo == EstadoHito.APROBADO) _nuevo.update { it.copy(mensaje = "¡Hito aprobado!") }
            }
        }
    }

    /** Solo el profesor guía puede borrar hitos. */
    fun eliminarHito() {
        viewModelScope.launch {
            if (!PermisosHito.puedeAdministrar(preferencias.perfil.first().rol)) return@launch
            val hito = repositorio.obtener(hitoId) ?: return@launch
            repositorio.eliminar(hito)
            _nuevo.update { it.copy(eliminado = true) }
        }
    }

    fun mensajeMostrado() = _nuevo.update { it.copy(mensaje = null) }

    companion object {
        fun factory(hitoId: Long) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                HitoDetalleViewModel(hitoId, app.contenedor.hitoRepository, app.contenedor.preferencias)
            }
        }
    }
}
