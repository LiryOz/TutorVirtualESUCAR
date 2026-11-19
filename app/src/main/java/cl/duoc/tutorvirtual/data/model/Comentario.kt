package cl.duoc.tutorvirtual.data.model

import java.time.LocalDateTime

/** Qué tipo de mensaje es: el estudiante comenta avances o dudas; el profesor responde. */
enum class TipoComentario(val etiqueta: String) {
    AVANCE("Avance"),
    DUDA("Duda"),
    RESPUESTA("Respuesta")
}

/** Archivo adjunto a un comentario (copiado dentro de la app para leerlo sin conexión). */
data class Adjunto(val ruta: String, val nombre: String)

/** Mensaje dentro de un hito: avance o duda del estudiante, o respuesta del profesor guía. */
data class Comentario(
    val id: Long = 0,
    val hitoId: Long,
    val autorRol: Rol,
    val autorNombre: String,
    val tipo: TipoComentario,
    val texto: String,
    val fechaHora: LocalDateTime,
    val adjunto: Adjunto? = null
)

/** Resumen de la conversación de un hito (para la lista y el panel del profesor). */
data class ResumenConversacion(val total: Int, val ultimoRol: Rol?) {
    /** El estudiante escribió último: el profesor tiene algo por responder. */
    val esperaProfesor: Boolean get() = ultimoRol == Rol.ESTUDIANTE

    /** El profesor escribió último: hay una respuesta para el estudiante. */
    val esperaEstudiante: Boolean get() = ultimoRol == Rol.PROFESOR_GUIA
}
