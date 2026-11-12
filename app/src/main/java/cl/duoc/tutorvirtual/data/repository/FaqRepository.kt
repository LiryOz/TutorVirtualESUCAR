package cl.duoc.tutorvirtual.data.repository

import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import cl.duoc.tutorvirtual.data.model.PreguntaFrecuente
import java.text.Normalizer

/**
 * Banco de preguntas frecuentes, incluido en la app (funciona sin conexión).
 * Contenido de ejemplo: reemplazar por el material autorizado de ESUCAR.
 */
object FaqRepository {

    fun obtenerTodas(): List<PreguntaFrecuente> = preguntas

    /**
     * Busca preguntas por texto y/o categoría.
     * Ignora mayúsculas y tildes: "citacion" encuentra "Citación".
     */
    fun buscar(texto: String, categoria: CategoriaFaq?): List<PreguntaFrecuente> {
        val busqueda = normalizar(texto.trim())
        return preguntas.filter { p ->
            val coincideCategoria = categoria == null || p.categoria == categoria
            val coincideTexto = busqueda.isEmpty() ||
                normalizar(p.pregunta).contains(busqueda) ||
                normalizar(p.respuesta).contains(busqueda)
            coincideCategoria && coincideTexto
        }
    }

    /** Pasa a minúsculas y quita tildes (á → a, ñ se mantiene como n). */
    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")

    /**
     * Palabras clave por pregunta (id → palabras). Le permiten al bot Sargento
     * reconocer la duda aunque se escriba distinto: "referencia de un libro" ≈ pregunta 1.
     */
    private val palabrasClave = mapOf(
        1 to listOf("libro", "libros", "apa", "cito", "citar", "cita", "referencia", "referencias", "bibliografia", "autor"),
        2 to listOf("web", "pagina", "internet", "url", "sitio", "link", "enlace", "online"),
        3 to listOf("textual", "parafrasis", "parafrasear", "comillas", "copiar", "plagio"),
        4 to listOf("ley", "leyes", "normativa", "norma", "reglamento", "decreto", "institucional"),
        5 to listOf("investigacion", "problema", "tema", "formular", "planteamiento"),
        6 to listOf("enfoque", "cuantitativo", "cualitativo", "mixto", "encuesta", "entrevista", "metodo"),
        7 to listOf("muestra", "personas", "participantes", "encuestados", "poblacion", "cantidad"),
        8 to listOf("objetivo", "objetivos", "especificos", "general", "verbo", "infinitivo"),
        9 to listOf("estructura", "informe", "final", "partes", "capitulos", "indice", "portada"),
        10 to listOf("letra", "fuente", "espaciado", "interlineado", "margen", "margenes", "tamano", "times", "arial"),
        11 to listOf("tabla", "tablas", "figura", "figuras", "grafico", "graficos", "imagen", "imagenes"),
        12 to listOf("atraso", "atrasado", "plazo", "fecha", "tarde", "prorroga", "llego", "vencido"),
        13 to listOf("enviar", "envio", "avance", "avances", "word", "pdf", "archivo", "subir"),
        14 to listOf("estado", "estados", "iniciar", "desarrollo", "enviado", "aprobado", "significa")
    )

    private val preguntas = listOf(
        PreguntaFrecuente(1, "¿Cómo cito un libro en norma APA?", CategoriaFaq.CITACION,
            "Apellido, Inicial. (Año). Título del libro en cursiva. Editorial. En el texto usa (Apellido, año)."),
        PreguntaFrecuente(2, "¿Cómo cito una página web?", CategoriaFaq.CITACION,
            "Autor u organización. (Año, día mes). Título de la página. Sitio. URL. Si no hay fecha, usa (s.f.)."),
        PreguntaFrecuente(3, "¿Qué diferencia hay entre cita textual y paráfrasis?", CategoriaFaq.CITACION,
            "La cita textual copia las palabras exactas entre comillas e indica la página. La paráfrasis explica la idea con tus palabras, pero igual debe citar al autor."),
        PreguntaFrecuente(4, "¿Cómo cito una ley o normativa institucional?", CategoriaFaq.CITACION,
            "Nombre de la norma, número y año. Ejemplo: Ley N° 18.961 (1990). Orgánica Constitucional de Carabineros."),
        PreguntaFrecuente(5, "¿Cómo formulo una buena pregunta de investigación?", CategoriaFaq.METODOLOGIA,
            "Debe ser una sola pregunta, clara, delimitada en lugar y tiempo, y posible de responder con los datos que puedes obtener."),
        PreguntaFrecuente(6, "¿Qué enfoque elijo: cuantitativo o cualitativo?", CategoriaFaq.METODOLOGIA,
            "Cuantitativo si quieres medir o comparar con números (encuestas). Cualitativo si quieres comprender opiniones o experiencias (entrevistas). Mixto combina ambos."),
        PreguntaFrecuente(7, "¿Cuántas personas necesito en mi muestra?", CategoriaFaq.METODOLOGIA,
            "Depende del enfoque. En cualitativo se trabaja con pocos casos en profundidad; en cuantitativo se calcula según el tamaño de la población. Consulta con tu profesor guía."),
        PreguntaFrecuente(8, "¿Cómo redacto los objetivos específicos?", CategoriaFaq.METODOLOGIA,
            "Comienza con un verbo en infinitivo (identificar, describir, analizar). Cada objetivo específico debe aportar a cumplir el objetivo general."),
        PreguntaFrecuente(9, "¿Qué estructura debe tener el informe final?", CategoriaFaq.FORMATO,
            "Portada, índice, introducción, planteamiento del problema, marco teórico, metodología, resultados, conclusiones, referencias y anexos."),
        PreguntaFrecuente(10, "¿Qué letra y espaciado uso?", CategoriaFaq.FORMATO,
            "Según APA 7: fuente legible (Times New Roman 12 o Arial 11), interlineado doble y márgenes de 2,54 cm. Verifica la pauta de tu sección."),
        PreguntaFrecuente(11, "¿Cómo presento tablas y figuras?", CategoriaFaq.FORMATO,
            "Numera cada una (Tabla 1, Figura 1), pon un título arriba y la fuente abajo. Menciónalas en el texto antes de que aparezcan."),
        PreguntaFrecuente(12, "¿Qué hago si no llego a la fecha de entrega?", CategoriaFaq.ENTREGAS,
            "Avisa a tu profesor guía antes del plazo y explica el motivo. Registra el hito como \"En desarrollo\" en la app para no perderlo de vista."),
        PreguntaFrecuente(13, "¿En qué formato envío mis avances?", CategoriaFaq.ENTREGAS,
            "Revisa el \"formato requerido\" de cada hito en la sección Hitos. Generalmente se pide documento Word o PDF."),
        PreguntaFrecuente(14, "¿Qué significa cada estado de un hito?", CategoriaFaq.ENTREGAS,
            "Por iniciar: aún no empiezas. En desarrollo: estás trabajando. Enviado: ya lo entregaste. Aprobado: el profesor guía lo aprobó.")
    ).map { it.copy(palabrasClave = palabrasClave[it.id].orEmpty()) }
}
