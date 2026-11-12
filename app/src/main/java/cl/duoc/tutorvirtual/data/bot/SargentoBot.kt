package cl.duoc.tutorvirtual.data.bot

import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import cl.duoc.tutorvirtual.data.model.PreguntaFrecuente
import cl.duoc.tutorvirtual.data.repository.FaqRepository

/** Qué tipo de respuesta dio el bot (sirve para la interfaz y para las pruebas). */
enum class TipoRespuesta { SALUDO, PRESENTACION, AGRADECIMIENTO, DESPEDIDA, RESPUESTA, VARIAS, CATEGORIA, SIN_RESPUESTA }

/** Lo que responde el Sargento: texto, título opcional y preguntas sugeridas para tocar. */
data class RespuestaBot(
    val tipo: TipoRespuesta,
    val texto: String,
    val titulo: String? = null,
    val sugerencias: List<PreguntaFrecuente> = emptyList(),
    val pregunta: PreguntaFrecuente? = null
)

/**
 * "Sargento": perro de Carabineros de Chile que responde las dudas del trabajo de título.
 *
 * Funciona SIN internet y sin inteligencia artificial: compara las palabras del mensaje
 * con el banco de preguntas frecuentes (FaqRepository) y elige la que más se parece.
 *  1. Normaliza el texto (minúsculas, sin tildes) y quita palabras vacías ("de", "como"...).
 *  2. Da puntos a cada pregunta: palabra clave = 3 puntos, palabra de la pregunta = 2.
 *     Se compara la "raíz" (primeras 5 letras) para aceptar variaciones: "libros" ≈ "libro".
 *  3. Si nada alcanza el mínimo → "No tengo respuesta para eso".
 */
class SargentoBot(private val banco: List<PreguntaFrecuente> = FaqRepository.obtenerTodas()) {

    fun bienvenida(nombre: String): RespuestaBot {
        val saludo = nombre.split(" ").firstOrNull().orEmpty().ifBlank { "cadete" }
        return RespuestaBot(
            TipoRespuesta.SALUDO,
            "¡Guau, $saludo! Soy el Sargento, perro guía de Carabineros de Chile. " +
                "Estoy de servicio para ayudarte con tu trabajo de título: citación, metodología, formato y entregas. " +
                "Escríbeme tu duda o toca una de estas preguntas.",
            sugerencias = sugerenciasIniciales()
        )
    }

    fun responder(mensaje: String): RespuestaBot {
        val todas = tokens(mensaje)
        val utiles = todas.filter { it.length >= 3 && it !in PALABRAS_VACIAS }

        // ¿Escribió solo el nombre de una categoría? ("citación", "formato")
        val categorias = utiles.mapNotNull { p -> CategoriaFaq.entries.find { raiz(FaqRepository.normalizar(it.etiqueta)) == raiz(p) } }
        if (utiles.isNotEmpty() && categorias.size == utiles.size) return preguntasDeCategoria(categorias.first())

        val puntajes = banco.map { it to puntaje(utiles, it) }.filter { it.second >= PUNTAJE_MINIMO }
        val mejor = puntajes.maxOfOrNull { it.second }

        if (mejor != null) {
            val empatadas = puntajes.filter { it.second == mejor }.map { it.first }
            return if (empatadas.size == 1) responderPregunta(empatadas.first())
            else RespuestaBot(
                TipoRespuesta.VARIAS,
                "¡Olfateé varias pistas! ¿Cuál de estas preguntas es la tuya?",
                sugerencias = empatadas.take(4)
            )
        }

        // Conversación básica (solo si no era una duda del banco)
        return when {
            todas.any { it in SALUDOS } -> RespuestaBot(
                TipoRespuesta.SALUDO,
                "¡Guau! ¡Hola! Aquí el Sargento, listo para ayudarte. ¿Qué duda tienes de tu trabajo de título?",
                sugerencias = sugerenciasIniciales()
            )
            esPresentacion(todas) -> RespuestaBot(
                TipoRespuesta.PRESENTACION,
                "Soy el Sargento, perro de Carabineros de Chile y tu tutor virtual. " +
                    "Conozco las preguntas frecuentes sobre citación, metodología, formato y entregas del trabajo de título.",
                sugerencias = sugerenciasIniciales()
            )
            todas.any { it in AGRADECIMIENTOS } -> RespuestaBot(
                TipoRespuesta.AGRADECIMIENTO,
                "¡De nada! *mueve la cola* Si te surge otra duda, aquí estaré de guardia."
            )
            todas.any { it in DESPEDIDAS } -> RespuestaBot(
                TipoRespuesta.DESPEDIDA,
                "¡Hasta pronto! Mucho éxito con tu trabajo de título. ¡Guau!"
            )
            else -> RespuestaBot(
                TipoRespuesta.SIN_RESPUESTA,
                "No tengo respuesta para eso. Mi olfato solo encuentra temas de tu trabajo de título: " +
                    "citación, metodología, formato y entregas. Prueba con una de estas preguntas " +
                    "o consulta directamente a tu profesor guía.",
                sugerencias = sugerenciasIniciales()
            )
        }
    }

    /** Respuesta directa a una pregunta del banco (cuando se toca una sugerencia). */
    fun responderPregunta(p: PreguntaFrecuente) = RespuestaBot(
        TipoRespuesta.RESPUESTA,
        p.respuesta,
        titulo = p.pregunta,
        sugerencias = banco.filter { it.categoria == p.categoria && it.id != p.id }.take(2),
        pregunta = p
    )

    fun preguntasDeCategoria(categoria: CategoriaFaq) = RespuestaBot(
        TipoRespuesta.CATEGORIA,
        "Estas son las preguntas que conozco sobre ${categoria.etiqueta}. Toca la que necesites:",
        sugerencias = banco.filter { it.categoria == categoria }
    )

    /** Una pregunta de cada categoría, para empezar. */
    fun sugerenciasIniciales(): List<PreguntaFrecuente> =
        CategoriaFaq.entries.mapNotNull { c -> banco.firstOrNull { it.categoria == c } }

    internal fun puntaje(palabras: List<String>, p: PreguntaFrecuente): Int {
        val raicesClave = p.palabrasClave.map { raiz(FaqRepository.normalizar(it)) }.toSet()
        val raicesPregunta = tokens(p.pregunta).filter { it.length >= 3 && it !in PALABRAS_VACIAS }.map(::raiz).toSet()
        return palabras.map(::raiz).distinct().sumOf { r ->
            when (r) {
                in raicesClave -> 3
                in raicesPregunta -> 2
                else -> 0
            }
        }
    }

    private fun esPresentacion(t: List<String>): Boolean {
        val texto = t.joinToString(" ")
        return "quien eres" in texto || "que eres" in texto || "como te llamas" in texto ||
            "tu nombre" in texto || t == listOf("sargento")
    }

    private fun tokens(texto: String): List<String> =
        FaqRepository.normalizar(texto).split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }

    private fun raiz(palabra: String) = palabra.take(5)

    companion object {
        const val PUNTAJE_MINIMO = 2

        private val SALUDOS = setOf("hola", "holi", "buenas", "buenos", "saludos", "hey", "alo")
        private val AGRADECIMIENTOS = setOf("gracias", "agradezco", "agradecido", "agradecida", "thanks")
        private val DESPEDIDAS = setOf("chao", "chau", "adios", "hasta")

        /** Palabras muy comunes que no ayudan a reconocer la duda (ya sin tildes). */
        private val PALABRAS_VACIAS = setOf(
            "que", "como", "cual", "cuales", "cuando", "donde", "quien", "cuanto", "cuantos", "cuantas",
            "para", "por", "con", "sin", "una", "uno", "unos", "unas", "los", "las", "del", "mis", "tus", "sus",
            "este", "esta", "esto", "ese", "esa", "eso", "hay", "hago", "hacer", "debo", "deberia", "puedo",
            "puede", "tengo", "tiene", "tener", "necesito", "quiero", "saber", "sobre", "entre", "muy", "mas",
            "pero", "porque", "segun", "cada", "todo", "toda", "son", "ser", "estar", "estoy", "sea", "algo",
            "bien", "favor", "ayuda", "ayudame", "dime", "explica", "explicame", "sargento", "hola", "buenas",
            "gracias", "duda", "dudas", "consulta", "pregunta", "preguntas", "usar", "uso", "hace", "hacen", "les"
        )
    }
}
