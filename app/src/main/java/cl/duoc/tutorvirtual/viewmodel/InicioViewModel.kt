package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.Perfil
import cl.duoc.tutorvirtual.data.repository.HitoRepository
import cl.duoc.tutorvirtual.data.repository.PreferenciasRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class InicioUiState(
    val cargando: Boolean = true,
    val perfil: Perfil = Perfil(),
    val resumen: HitosUiState = HitosUiState(),
    val hoy: LocalDate = LocalDate.now()
) {
    /** Hitos que necesitan atención: vencidos o por vencer en 3 días. */
    val alertas: List<Hito>
        get() = resumen.hitos.filter { it.estaVencido(hoy) || it.estaPorVencer(hoy) }
}

/** Junta (combine) el perfil de DataStore y los hitos de Room para el panel de inicio. */
class InicioViewModel(
    hitos: HitoRepository,
    preferencias: PreferenciasRepository
) : ViewModel() {

    val estado: StateFlow<InicioUiState> =
        combine(preferencias.perfil, hitos.observarHitos()) { perfil, lista ->
            val hoy = LocalDate.now()
            InicioUiState(
                cargando = false,
                perfil = perfil,
                resumen = HitosUiState(cargando = false, hitos = lista, hoy = hoy),
                hoy = hoy
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InicioUiState())

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                InicioViewModel(app.contenedor.hitoRepository, app.contenedor.preferencias)
            }
        }
    }
}
