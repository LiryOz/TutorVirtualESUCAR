package cl.duoc.tutorvirtual.data.repository

import cl.duoc.tutorvirtual.data.model.CriterioPauta
import cl.duoc.tutorvirtual.data.model.EtapaMetodologica

/**
 * Fuente de datos de la guía metodológica.
 * El contenido viene incluido en la app, por eso se puede leer SIN conexión (caso 3.3).
 * Contenido didáctico de ejemplo: reemplazar por el material autorizado de ESUCAR.
 */
object GuiaRepository {

    fun obtenerEtapas(): List<EtapaMetodologica> = etapas

    fun obtenerEtapa(id: Int): EtapaMetodologica? = etapas.firstOrNull { it.id == id }

    private val etapas = listOf(
        EtapaMetodologica(
            id = 1,
            titulo = "Planteamiento del problema",
            descripcion = "Identificar el problema y delimitar el tema de estudio.",
            orientaciones = listOf(
                "Describe la situación que quieres investigar y dónde ocurre.",
                "Explica por qué es un problema y a quién afecta.",
                "Formula una pregunta de investigación clara.",
                "Redacta un objetivo general y 2 o 3 objetivos específicos."
            ),
            pauta = listOf(
                CriterioPauta("Descripción del problema", "Presenta antecedentes concretos y delimita lugar y tiempo."),
                CriterioPauta("Pregunta de investigación", "Es una sola pregunta, clara y posible de responder."),
                CriterioPauta("Objetivos", "Comienzan con verbo en infinitivo y se relacionan con la pregunta.")
            )
        ),
        EtapaMetodologica(
            id = 2,
            titulo = "Marco teórico",
            descripcion = "Organizar antecedentes y conceptos que sustentan el estudio.",
            orientaciones = listOf(
                "Busca fuentes confiables: libros, artículos, normativa institucional.",
                "Registra cada referencia en formato APA desde el principio.",
                "Define los conceptos clave de tu pregunta de investigación.",
                "Relaciona las teorías con tu problema, no solo las copies."
            ),
            pauta = listOf(
                CriterioPauta("Pertinencia de fuentes", "Las fuentes son confiables y se relacionan con el tema."),
                CriterioPauta("Citación APA", "Todas las ideas ajenas están citadas y aparecen en la bibliografía."),
                CriterioPauta("Análisis propio", "Se conectan los conceptos con el problema planteado.")
            )
        ),
        EtapaMetodologica(
            id = 3,
            titulo = "Metodología",
            descripcion = "Explicar cómo se desarrollará la investigación.",
            orientaciones = listOf(
                "Elige el enfoque: cuantitativo, cualitativo o mixto.",
                "Define la población y la muestra.",
                "Selecciona técnicas e instrumentos (encuesta, entrevista, etc.).",
                "Indica los resguardos éticos (consentimiento, confidencialidad)."
            ),
            pauta = listOf(
                CriterioPauta("Enfoque y diseño", "El enfoque elegido es coherente con los objetivos."),
                CriterioPauta("Población y muestra", "Se describen con claridad y se justifica su selección."),
                CriterioPauta("Instrumentos", "Se explica qué instrumento se usará y para qué objetivo.")
            )
        ),
        EtapaMetodologica(
            id = 4,
            titulo = "Recogida de información",
            descripcion = "Planificar y registrar la obtención de datos.",
            orientaciones = listOf(
                "Prueba tus instrumentos con 2 o 3 personas antes de aplicarlos.",
                "Calendariza la aplicación de instrumentos.",
                "Registra los datos de forma ordenada (planilla o transcripción).",
                "Resguarda la información y la identidad de los participantes."
            ),
            pauta = listOf(
                CriterioPauta("Aplicación", "Los instrumentos se aplicaron según lo planificado."),
                CriterioPauta("Registro", "Los datos están ordenados y son verificables."),
                CriterioPauta("Ética", "Se respetan el consentimiento y la confidencialidad.")
            )
        ),
        EtapaMetodologica(
            id = 5,
            titulo = "Análisis de resultados",
            descripcion = "Examinar los datos y relacionarlos con los objetivos.",
            orientaciones = listOf(
                "Ordena los resultados según cada objetivo específico.",
                "Usa tablas o gráficos simples cuando ayuden a entender.",
                "Interpreta: explica qué significa cada resultado.",
                "Compara tus hallazgos con el marco teórico."
            ),
            pauta = listOf(
                CriterioPauta("Presentación", "Tablas y gráficos tienen título, fuente y se explican."),
                CriterioPauta("Interpretación", "Se explica el significado de los datos, no solo se describen."),
                CriterioPauta("Relación con teoría", "Los hallazgos se contrastan con el marco teórico.")
            )
        ),
        EtapaMetodologica(
            id = 6,
            titulo = "Conclusiones y entrega final",
            descripcion = "Cerrar el trabajo respondiendo la pregunta de investigación.",
            orientaciones = listOf(
                "Responde la pregunta de investigación con base en tus resultados.",
                "Indica si se cumplieron los objetivos.",
                "Señala limitaciones del estudio y propone recomendaciones.",
                "Revisa formato, citas y bibliografía antes de enviar."
            ),
            pauta = listOf(
                CriterioPauta("Respuesta a la pregunta", "Las conclusiones responden la pregunta planteada."),
                CriterioPauta("Cumplimiento de objetivos", "Se revisa cada objetivo específico."),
                CriterioPauta("Formato", "El documento cumple la estructura y la norma APA.")
            )
        )
    )
}
