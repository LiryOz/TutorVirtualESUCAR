package cl.duoc.tutorvirtual.data.model

data class EtapaMetodologica(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val orientaciones: List<String>
)