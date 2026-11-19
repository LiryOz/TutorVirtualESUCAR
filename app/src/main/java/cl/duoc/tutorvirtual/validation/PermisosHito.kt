package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.model.TipoComentario

/**
 * Reglas de quién puede hacer qué con los hitos (caso 3.1):
 *  - Profesor guía: define la estructura (crea, edita y borra hitos) y aprueba.
 *  - Estudiante: avanza su entrega (Por iniciar → En desarrollo → Enviado),
 *    comenta avances o dudas y adjunta archivos. No puede aprobar.
 */
object PermisosHito {

    fun puedeAdministrar(rol: Rol): Boolean = rol == Rol.PROFESOR_GUIA

    /** Estados que cada perfil puede elegir. */
    fun estadosPermitidos(rol: Rol): List<EstadoHito> =
        if (rol == Rol.PROFESOR_GUIA) EstadoHito.entries
        else EstadoHito.entries - EstadoHito.APROBADO

    /** El estudiante no puede aprobar ni modificar un hito que ya fue aprobado. */
    fun puedeCambiarEstado(rol: Rol, actual: EstadoHito, nuevo: EstadoHito): Boolean =
        rol == Rol.PROFESOR_GUIA || (actual != EstadoHito.APROBADO && nuevo in estadosPermitidos(rol))

    /** Tipos de comentario: el estudiante escribe avances o dudas; el profesor, respuestas. */
    fun tiposDeComentario(rol: Rol): List<TipoComentario> =
        if (rol == Rol.PROFESOR_GUIA) listOf(TipoComentario.RESPUESTA)
        else listOf(TipoComentario.AVANCE, TipoComentario.DUDA)
}

/** Validación del mensaje que se envía en la conversación del hito. */
object ComentarioValidator {

    const val MAX = 500

    fun validar(texto: String, tieneAdjunto: Boolean): String? {
        val t = texto.trim()
        return when {
            t.isEmpty() && !tieneAdjunto -> "Escribe un comentario o adjunta un archivo"
            t.length > MAX -> "Máximo $MAX caracteres"
            else -> null
        }
    }
}
