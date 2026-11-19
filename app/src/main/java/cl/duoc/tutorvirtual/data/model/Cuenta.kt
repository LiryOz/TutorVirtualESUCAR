package cl.duoc.tutorvirtual.data.model

/**
 * Cuenta de acceso de demostración.
 * El caso 3.4 indica que el MVP NO se conecta a servidores de la institución,
 * por eso las cuentas viven dentro de la app (supuesto del MVP).
 */
data class Cuenta(
    val usuario: String,
    val clave: String,
    val rol: Rol,
    val nombre: String,
    val correo: String,
    val asignatura: Asignatura? = null,
    val profesorGuia: String = ""
)

/** Cuentas genéricas: una por cada perfil del caso 3.1. */
object CuentasDemo {

    /** Estudiante de trabajo de título: usuario principal que desarrolla su tesis. */
    val ESTUDIANTE = Cuenta(
        usuario = "alumno",
        clave = "alumno123",
        rol = Rol.ESTUDIANTE,
        nombre = "Alumno Demo",
        correo = "alumno@esucar.cl",
        asignatura = Asignatura.TT1,
        profesorGuia = "Profesor Demo"
    )

    /** Profesor guía: supervisa, aprueba hitos y evalúa los contenidos. */
    val PROFESOR = Cuenta(
        usuario = "profesor",
        clave = "profesor123",
        rol = Rol.PROFESOR_GUIA,
        nombre = "Profesor Demo",
        correo = "profesor@esucar.cl"
    )

    val todas = listOf(ESTUDIANTE, PROFESOR)

    fun deRol(rol: Rol): Cuenta = todas.first { it.rol == rol }

    /** Busca la cuenta (el usuario no distingue mayúsculas; la clave sí). */
    fun autenticar(usuario: String, clave: String): Cuenta? =
        todas.find { it.usuario.equals(usuario.trim(), ignoreCase = true) && it.clave == clave }

    fun buscar(usuario: String): Cuenta? = todas.find { it.usuario == usuario }
}
