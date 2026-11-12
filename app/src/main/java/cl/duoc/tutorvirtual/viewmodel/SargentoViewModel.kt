package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.bot.RespuestaBot
import cl.duoc.tutorvirtual.data.bot.SargentoBot
import cl.duoc.tutorvirtual.data.bot.TipoRespuesta
import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import cl.duoc.tutorvirtual.data.model.PreguntaFrecuente
import cl.duoc.tutorvirtual.data.repository.PreferenciasRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Un mensaje del chat: del usuario o del Sargento. */
data class MensajeChat(
    val id: Long,
    val deBot: Boolean,
    val texto: String,
    val titulo: String? = null,
    val sugerencias: List<PreguntaFrecuente> = emptyList(),
    val sinRespuesta: Boolean = false
)

data class SargentoUiState(
    val mensajes: List<MensajeChat> = emptyList(),
    val texto: String = "",
    val escribiendo: Boolean = false      // muestra los "tres puntitos" mientras el Sargento responde
)

/** Chat con el Sargento. Todo ocurre en el teléfono: funciona sin conexión. */
class SargentoViewModel(
    private val preferencias: PreferenciasRepository,
    private val bot: SargentoBot = SargentoBot()
) : ViewModel() {

    private val _estado = MutableStateFlow(SargentoUiState())
    val estado: StateFlow<SargentoUiState> = _estado.asStateFlow()

    private var siguienteId = 0L

    init {
        saludar()
    }

    private fun saludar() {
        viewModelScope.launch {
            val nombre = preferencias.perfil.first().nombre
            responderCon(pausa = 400) { bot.bienvenida(nombre) }
        }
    }

    fun onTextoChange(v: String) = _estado.update { it.copy(texto = v.take(200)) }

    fun enviar() {
        val texto = _estado.value.texto.trim()
        if (texto.isEmpty() || _estado.value.escribiendo) return
        agregar(MensajeChat(siguienteId++, deBot = false, texto = texto))
        _estado.update { it.copy(texto = "") }
        responderCon { bot.responder(texto) }
    }

    fun elegirPregunta(p: PreguntaFrecuente) {
        if (_estado.value.escribiendo) return
        agregar(MensajeChat(siguienteId++, deBot = false, texto = p.pregunta))
        responderCon { bot.responderPregunta(p) }
    }

    fun elegirCategoria(c: CategoriaFaq) {
        if (_estado.value.escribiendo) return
        agregar(MensajeChat(siguienteId++, deBot = false, texto = "Muéstrame preguntas de ${c.etiqueta}"))
        responderCon { bot.preguntasDeCategoria(c) }
    }

    /** Borra la conversación y el Sargento vuelve a saludar. */
    fun reiniciar() {
        if (_estado.value.escribiendo) return
        _estado.update { SargentoUiState() }
        saludar()
    }

    private fun agregar(m: MensajeChat) = _estado.update { it.copy(mensajes = it.mensajes + m) }

    /** Simula que el Sargento "escribe": la pausa depende del largo de la respuesta. */
    private fun responderCon(pausa: Long? = null, respuesta: () -> RespuestaBot) {
        viewModelScope.launch {
            _estado.update { it.copy(escribiendo = true) }
            val r = respuesta()
            delay(pausa ?: (600L + (r.texto.length * 4L).coerceAtMost(900L)))
            agregar(
                MensajeChat(
                    id = siguienteId++,
                    deBot = true,
                    texto = r.texto,
                    titulo = r.titulo,
                    sugerencias = r.sugerencias,
                    sinRespuesta = r.tipo == TipoRespuesta.SIN_RESPUESTA
                )
            )
            _estado.update { it.copy(escribiendo = false) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                SargentoViewModel(app.contenedor.preferencias)
            }
        }
    }
}
