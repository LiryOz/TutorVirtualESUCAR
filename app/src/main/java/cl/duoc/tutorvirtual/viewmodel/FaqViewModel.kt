package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import cl.duoc.tutorvirtual.data.model.PreguntaFrecuente
import cl.duoc.tutorvirtual.data.repository.FaqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Todo lo que la pantalla de FAQ necesita para dibujarse. */
data class FaqUiState(
    val texto: String = "",
    val categoria: CategoriaFaq? = null,       // null = todas
    val resultados: List<PreguntaFrecuente> = emptyList(),
    val abiertaId: Int? = null                  // pregunta con la respuesta visible
)

/**
 * ViewModel del buscador. Cada vez que cambia el texto o la categoría,
 * vuelve a buscar y emite un nuevo estado; la pantalla solo lo dibuja.
 */
class FaqViewModel(
    private val repositorio: FaqRepository = FaqRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(FaqUiState(resultados = repositorio.obtenerTodas()))
    val estado: StateFlow<FaqUiState> = _estado.asStateFlow()

    fun onTextoChange(nuevo: String) {
        _estado.update { it.copy(texto = nuevo) }
        buscar()
    }

    /** Tocar la categoría ya elegida la quita (vuelve a "todas"). */
    fun onCategoriaChange(categoria: CategoriaFaq?) {
        _estado.update { it.copy(categoria = if (it.categoria == categoria) null else categoria) }
        buscar()
    }

    fun limpiarBusqueda() {
        _estado.update { it.copy(texto = "", categoria = null) }
        buscar()
    }

    fun alternarPregunta(id: Int) {
        _estado.update { it.copy(abiertaId = if (it.abiertaId == id) null else id) }
    }

    private fun buscar() {
        _estado.update { it.copy(resultados = repositorio.buscar(it.texto, it.categoria)) }
    }
}
