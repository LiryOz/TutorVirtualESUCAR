package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.Perfil
import cl.duoc.tutorvirtual.data.model.hitosQueRequierenAviso
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
        get() = hitosQueRequierenAviso(resumen.hitos, hoy)

    /** Panel del profesor guía: entregas enviadas que esperan su aprobación. */
    val porRevisar: List<Hito>
        get() = resumen.hitos.filter { it.estado == EstadoHito.ENVIADO }.sortedBy { it.fechaLimite }

    val aprobados: Int get() = resumen.hitos.count { it.estado == EstadoHito.APROBADO }

    /** Estudiante: hitos donde el profesor guía respondió (último mensaje es suyo). */
    val conRespuesta: List<Hito>
        get() = resumen.hitos.filter { resumen.conversaciones[it.id]?.esperaEstudiante == true }
}

/** Junta (combine) el perfil de DataStore, los hitos y los comentarios de Room para el panel de inicio. */
class InicioViewModel(
    hitos: HitoRepository,
    preferencias: PreferenciasRepository
) : ViewModel() {

    val estado: StateFlow<InicioUiState> =
        combine(
            preferencias.perfil,
            hitos.observarHitos(),
            hitos.observarResumenComentarios()
        ) { perfil, lista, conversaciones ->
            val hoy = LocalDate.now()
            InicioUiState(
                cargando = false,
                perfil = perfil,
                resumen = HitosUiState(
                    cargando = false, hitos = lista, hoy = hoy,
                    rol = perfil.rol, conversaciones = conversaciones
                ),
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
