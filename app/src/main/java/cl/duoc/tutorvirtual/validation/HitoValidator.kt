package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.EstadoHito
import java.time.LocalDate

/** Error de cada campo del formulario de hito (null = sin error). */
data class HitoErrores(
    val titulo: String? = null,
    val descripcion: String? = null,
    val formato: String? = null,
    val fecha: String? = null,
    val estado: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(titulo, descripcion, formato, fecha, estado).any { it != null }
}

/**
 * Reglas de validación del formulario de hito.
 * Es Kotlin puro: NO conoce Compose ni Android. Así la lógica queda separada
 * de la interfaz (rúbrica IE 2.2.1) y se puede probar con JUnit.
 * Tipos de validación (2.3.2): obligatorios, largo, rango de fechas y dependencia entre campos.
 */
object HitoValidator {

    const val TITULO_MIN = 5
    const val TITULO_MAX = 60
    const val DESCRIPCION_MIN = 10
    const val DESCRIPCION_MAX = 300

    fun validarTitulo(titulo: String): String? {
        val t = titulo.trim()
        return when {
            t.isEmpty() -> "El título es obligatorio"
            t.length < TITULO_MIN -> "Debe tener al menos $TITULO_MIN caracteres"
            t.length > TITULO_MAX -> "Máximo $TITULO_MAX caracteres"
            else -> null
        }
    }

    fun validarDescripcion(descripcion: String): String? {
        val d = descripcion.trim()
        return when {
            d.isEmpty() -> "La descripción es obligatoria"
            d.length < DESCRIPCION_MIN -> "Describe la entrega con al menos $DESCRIPCION_MIN caracteres"
            d.length > DESCRIPCION_MAX -> "Máximo $DESCRIPCION_MAX caracteres"
            else -> null
        }
    }

    fun validarFormato(formato: String): String? {
        val f = formato.trim()
        return when {
            f.isEmpty() -> "Indica el formato requerido (ej: PDF, Word)"
            f.length < 3 -> "Formato demasiado corto"
            else -> null
        }
    }

    /**
     * Dependencia entre campos: un hito que aún NO se entrega no puede tener
     * una fecha límite pasada. Solo se revisa si la fecha es nueva o cambió,
     * para poder editar otros datos de un hito que ya venció.
     */
    fun validarFecha(
        fecha: LocalDate?,
        estado: EstadoHito,
        hoy: LocalDate,
        fechaOriginal: LocalDate? = null
    ): String? = when {
        fecha == null -> "Selecciona la fecha límite"
        fecha != fechaOriginal && !estado.estaTerminado && fecha.isBefore(hoy) ->
            "Un hito pendiente no puede tener una fecha pasada"
        else -> null
    }

    /** Regla de negocio del caso: solo el profesor guía aprueba hitos. */
    fun validarEstado(
        estado: EstadoHito,
        esProfesor: Boolean,
        estadoOriginal: EstadoHito? = null
    ): String? =
        if (estado == EstadoHito.APROBADO && estadoOriginal != EstadoHito.APROBADO && !esProfesor)
            "Solo el profesor guía puede marcar un hito como Aprobado"
        else null

    /** Valida todos los campos de una vez (al presionar "Guardar"). */
    fun validarTodo(
        titulo: String,
        descripcion: String,
        formato: String,
        fecha: LocalDate?,
        estado: EstadoHito,
        hoy: LocalDate,
        esProfesor: Boolean,
        fechaOriginal: LocalDate? = null,
        estadoOriginal: EstadoHito? = null
    ) = HitoErrores(
        titulo = validarTitulo(titulo),
        descripcion = validarDescripcion(descripcion),
        formato = validarFormato(formato),
        fecha = validarFecha(fecha, estado, hoy, fechaOriginal),
        estado = validarEstado(estado, esProfesor, estadoOriginal)
    )
}
