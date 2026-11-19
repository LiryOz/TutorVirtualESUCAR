package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.local.room.ComentarioEntity
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.ResumenConversacion
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.repository.HitoRepository
import cl.duoc.tutorvirtual.data.repository.PreferenciasRepository
import cl.duoc.tutorvirtual.validation.PermisosHito
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Estado de la lista de hitos. */
data class HitosUiState(
    val cargando: Boolean = true,
    val hitos: List<Hito> = emptyList(),
    val hoy: LocalDate = LocalDate.now(),
    val rol: Rol = Rol.ESTUDIANTE,
    val conversaciones: Map<Long, ResumenConversacion> = emptyMap()
) {
    val esProfesor: Boolean get() = rol == Rol.PROFESOR_GUIA

    val terminados: Int get() = hitos.count { it.estado.estaTerminado }

    /** Avance entre 0 y 1 (hitos enviados o aprobados / total). */
    val progreso: Float get() = if (hitos.isEmpty()) 0f else terminados.toFloat() / hitos.size

    /** El hito pendiente más cercano a vencer (para la pantalla de inicio). */
    val proximoHito: Hito? get() = hitos.filter { !it.estado.estaTerminado }.minByOrNull { it.fechaLimite }

    /** Hitos con un mensaje del estudiante que el profesor aún no responde. */
    val consultasPendientes: Int get() = hitos.count { conversaciones[it.id]?.esperaProfesor == true }
}

/**
 * ViewModel de la lista de hitos.
 * Observa Room y el perfil: la lista, los permisos y los contadores de comentarios
 * se actualizan solos.
 */
class HitosViewModel(
    private val repositorio: HitoRepository,
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    val estado: StateFlow<HitosUiState> = combine(
        repositorio.observarHitos(),
        repositorio.observarResumenComentarios(),
        preferencias.perfil
    ) { hitos, conversaciones, perfil ->
        HitosUiState(cargando = false, hitos = hitos, hoy = LocalDate.now(), rol = perfil.rol, conversaciones = conversaciones)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HitosUiState())

    private var ultimoEliminado: Pair<Hito, List<ComentarioEntity>>? = null

    /**
     * Checklist del estudiante: marcar = "Enviado", desmarcar = "En desarrollo".
     * Un hito "Aprobado" no se puede desmarcar.
     */
    fun marcarEntregado(hito: Hito, entregado: Boolean) {
        val nuevo = if (entregado) EstadoHito.ENVIADO else EstadoHito.EN_DESARROLLO
        viewModelScope.launch {
            if (!PermisosHito.puedeCambiarEstado(preferencias.perfil.first().rol, hito.estado, nuevo)) return@launch
            repositorio.cambiarEstado(hito, nuevo)
        }
    }

    /** Solo el profesor guía puede eliminar hitos. */
    fun eliminar(hito: Hito) {
        viewModelScope.launch {
            if (!PermisosHito.puedeAdministrar(preferencias.perfil.first().rol)) return@launch
            ultimoEliminado = hito to repositorio.eliminar(hito)
        }
    }

    fun deshacerEliminar() {
        val (hito, comentarios) = ultimoEliminado ?: return
        viewModelScope.launch {
            repositorio.restaurar(hito, comentarios)
            ultimoEliminado = null
        }
    }

    companion object {
        /** Fábrica: le entrega al ViewModel los repositorios desde TutorApp. */
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                HitosViewModel(app.contenedor.hitoRepository, app.contenedor.preferencias)
            }
        }
    }
}
