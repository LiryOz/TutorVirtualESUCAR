package cl.duoc.tutorvirtual.data.model

/**
 * Una etapa del trabajo de título (ej.: "Marco teórico").
 * [orientaciones] = el paso a paso que debe seguir el estudiante.
 * [pauta] = criterios de evaluación simplificados de la etapa (caso 3.2 "Pautas y rúbricas").
 */
data class EtapaMetodologica(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val orientaciones: List<String>,
    val pauta: List<CriterioPauta>
)

/** Un criterio de la rúbrica: qué se evalúa y qué se espera para lograrlo. */
data class CriterioPauta(
    val criterio: String,
    val queSeEspera: String
)
