package cl.duoc.tutorvirtual.data

import cl.duoc.tutorvirtual.data.bot.SargentoBot
import cl.duoc.tutorvirtual.data.bot.TipoRespuesta
import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del bot Sargento: reconoce dudas del banco y admite cuando no sabe. */
class SargentoBotTest {

    private val bot = SargentoBot()

    @Test
    fun `reconoce como citar un libro`() {
        val r = bot.responder("¿Cómo cito un libro?")
        assertEquals(TipoRespuesta.RESPUESTA, r.tipo)
        assertEquals(1, r.pregunta?.id)
    }

    @Test
    fun `reconoce la duda escrita con otras palabras y sin tildes`() {
        assertEquals(2, bot.responder("referencia de una pagina de internet").pregunta?.id)
        assertEquals(7, bot.responder("cuantas personas necesito para mi muestra").pregunta?.id)
        assertEquals(10, bot.responder("QUE LETRA Y MARGENES USO").pregunta?.id)
    }

    @Test
    fun `pregunta fuera de contexto no tiene respuesta`() {
        val r = bot.responder("dame una receta de empanadas")
        assertEquals(TipoRespuesta.SIN_RESPUESTA, r.tipo)
        assertTrue(r.texto.startsWith("No tengo respuesta para eso"))
        assertTrue(r.sugerencias.isNotEmpty())
    }

    @Test
    fun `texto sin sentido no tiene respuesta`() {
        assertEquals(TipoRespuesta.SIN_RESPUESTA, bot.responder("asdfgh qwerty").tipo)
    }

    @Test
    fun `saluda, agradece y se despide`() {
        assertEquals(TipoRespuesta.SALUDO, bot.responder("Hola!").tipo)
        assertEquals(TipoRespuesta.AGRADECIMIENTO, bot.responder("muchas gracias").tipo)
        assertEquals(TipoRespuesta.DESPEDIDA, bot.responder("chao").tipo)
        assertEquals(TipoRespuesta.PRESENTACION, bot.responder("¿quién eres?").tipo)
    }

    @Test
    fun `saludo con una duda responde la duda`() {
        assertEquals(TipoRespuesta.RESPUESTA, bot.responder("hola, ¿cómo presento las tablas?").tipo)
    }

    @Test
    fun `nombre de categoria lista sus preguntas`() {
        val r = bot.responder("citación")
        assertEquals(TipoRespuesta.CATEGORIA, r.tipo)
        assertTrue(r.sugerencias.all { it.categoria == CategoriaFaq.CITACION })
    }

    @Test
    fun `bienvenida usa el primer nombre`() {
        assertTrue(bot.bienvenida("Alumno Demo").texto.contains("Alumno"))
    }
}
