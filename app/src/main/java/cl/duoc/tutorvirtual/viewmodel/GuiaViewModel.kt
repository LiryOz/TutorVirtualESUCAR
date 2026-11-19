package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.tutorvirtual.data.model.EtapaMetodologica
import cl.duoc.tutorvirtual.data.repository.GuiaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel de la guía: entrega las etapas a la pantalla.
 * La pantalla no sabe de dónde vienen los datos; solo observa [etapas].
 */
class GuiaViewModel(
    private val repositorio: GuiaRepository = GuiaRepository
) : ViewModel() {

    private val _etapas = MutableStateFlow(repositorio.obtenerEtapas())
    val etapas: StateFlow<List<EtapaMetodologica>> = _etapas.asStateFlow()

    val totalEtapas: Int get() = _etapas.value.size

    fun etapa(id: Int): EtapaMetodologica? = repositorio.obtenerEtapa(id)
}
