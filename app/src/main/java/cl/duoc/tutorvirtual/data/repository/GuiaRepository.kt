package cl.duoc.tutorvirtual.data.repository

import cl.duoc.tutorvirtual.data.model.EtapaMetodologica

object GuiaRepository {
    // Contenido didáctico de ejemplo; reemplazar por material autorizado.
    val etapas = listOf(
        EtapaMetodologica(
            1, "Planteamiento del problema",
            "Identificar el problema y delimitar el tema de estudio.",
            listOf("Describir la situación a investigar.",
                "Formular una pregunta de investigación.",
                "Proponer un objetivo general.")
        ),
        EtapaMetodologica(
            2, "Marco teórico",
            "Organizar antecedentes y conceptos relevantes.",
            listOf("Buscar fuentes pertinentes.",
                "Registrar las referencias consultadas.",
                "Relacionar conceptos con el problema.")
        ),
        EtapaMetodologica(
            3, "Metodología",
            "Explicar cómo se desarrollará la investigación.",
            listOf("Seleccionar un enfoque apropiado.",
                "Definir técnicas e instrumentos.",
                "Considerar aspectos éticos.")
        ),
        EtapaMetodologica(
            4, "Recogida de información",
            "Planificar y registrar la obtención de datos.",
            listOf("Preparar instrumentos.",
                "Organizar el registro de información.",
                "Resguardar los datos recolectados.")
        ),
        EtapaMetodologica(
            5, "Análisis de resultados",
            "Examinar los datos y relacionarlos con los objetivos.",
            listOf("Ordenar los resultados.",
                "Interpretar los hallazgos.",
                "Redactar conclusiones sustentadas.")
        )
    )
}