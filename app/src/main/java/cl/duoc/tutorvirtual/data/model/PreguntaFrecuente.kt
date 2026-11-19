package cl.duoc.tutorvirtual.data.model

/** Categorías del banco de preguntas (caso 3.2: "ej. Citación, Metodología, Formato"). */
enum class CategoriaFaq(val etiqueta: String) {
    CITACION("Citación"),
    METODOLOGIA("Metodología"),
    FORMATO("Formato"),
    ENTREGAS("Entregas")
}

/**
 * Una pregunta frecuente con su respuesta instructiva.
 * [palabrasClave]: otras formas en que alguien podría preguntarlo (las usa el bot Sargento).
 */
data class PreguntaFrecuente(
    val id: Int,
    val pregunta: String,
    val categoria: CategoriaFaq,
    val respuesta: String,
    val palabrasClave: List<String> = emptyList()
)
