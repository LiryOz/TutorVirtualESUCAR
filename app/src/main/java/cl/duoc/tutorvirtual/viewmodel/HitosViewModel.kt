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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Estado de la lista de hitos. */
data class HitosUiState(
    val cargando: Boolean = true,
    val hitos: List<Hito> = emptyList(),
    val hoy: LocalDate = LocalDate.now()
) {
    val terminados: Int get() = hitos.count { it.estado.estaTerminado }

    /** Avance entre 0 y 1 (hitos enviados o aprobados / total). */
    val progreso: Float get() = if (hitos.isEmpty()) 0f else terminados.toFloat() / hitos.size

    /** El hito pendiente más cercano a vencer (para la pantalla de inicio). */
    val proximoHito: Hito? get() = hitos.filter { !it.estado.estaTerminado }.minByOrNull { it.fechaLimite }
}

/**
 * ViewModel de "Mis hitos".
 * Observa la tabla de Room: cuando un hito cambia, la lista se actualiza sola.
 */
class HitosViewModel(private val repositorio: HitoRepository) : ViewModel() {

    val estado: StateFlow<HitosUiState> = repositorio.observarHitos()
        .map { HitosUiState(cargando = false, hitos = it, hoy = LocalDate.now()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HitosUiState())

    private var ultimoEliminado: Hito? = null

    /**
     * Checklist: marcar = "Enviado", desmarcar = "En desarrollo".
     * Un hito "Aprobado" no se puede desmarcar desde aquí.
     */
    fun marcarEntregado(hito: Hito, entregado: Boolean) {
        if (hito.estado == EstadoHito.APROBADO) return
        viewModelScope.launch {
            repositorio.cambiarEstado(hito, if (entregado) EstadoHito.ENVIADO else EstadoHito.EN_DESARROLLO)
        }
    }

    fun eliminar(hito: Hito) {
        viewModelScope.launch {
            repositorio.eliminar(hito)
            ultimoEliminado = hito
        }
    }

    fun deshacerEliminar() {
        val hito = ultimoEliminado ?: return
        viewModelScope.launch {
            repositorio.restaurar(hito)
            ultimoEliminado = null
        }
    }

    companion object {
        /** Fábrica: le entrega al ViewModel el repositorio desde TutorApp. */
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                HitosViewModel(app.contenedor.hitoRepository)
            }
        }
    }
}
