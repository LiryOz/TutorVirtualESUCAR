package cl.duoc.tutorvirtual.data.model


enum class EstadoHito {
    POR_INICIAR,
    EN_DESARROLLO,
    ENVIADO,
    APROBADO
}

data class Hito(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val fechaLimite: String,
    val estado: EstadoHito
)