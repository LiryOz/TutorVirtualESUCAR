package cl.duoc.tutorvirtual.data.model

/** Perfiles del caso 3.1: estudiante (principal) y profesor guía (supervisor). */
enum class Rol(val etiqueta: String) {
    ESTUDIANTE("Estudiante"),
    PROFESOR_GUIA("Profesor guía")
}

/** Asignaturas del caso: Trabajo de Título I, II y III. */
enum class Asignatura(val etiqueta: String) {
    TT1("Trabajo de Título I"),
    TT2("Trabajo de Título II"),
    TT3("Trabajo de Título III")
}

/** Datos del usuario guardados en DataStore. */
data class Perfil(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val asignatura: Asignatura? = null,
    val profesorGuia: String = "",
    val rol: Rol = Rol.ESTUDIANTE,
    val foto: String? = null          // ruta de la foto de perfil (Etapa 8)
) {
    val estaCompleto: Boolean get() = nombre.isNotBlank() && asignatura != null
}

/** Ajustes de la app (se guardan en DataStore). */
data class Ajustes(
    val recordatorios: Boolean = true,   // notificaciones de fechas límite
    val textoGrande: Boolean = false     // accesibilidad
)
