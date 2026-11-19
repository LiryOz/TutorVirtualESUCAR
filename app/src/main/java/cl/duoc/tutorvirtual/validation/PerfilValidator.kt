package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.Asignatura
import cl.duoc.tutorvirtual.data.model.Rol

data class PerfilErrores(
    val nombre: String? = null,
    val rut: String? = null,
    val correo: String? = null,
    val asignatura: String? = null,
    val profesorGuia: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, rut, correo, asignatura, profesorGuia).any { it != null }
}

/** Reglas del formulario de perfil (Kotlin puro, sin Compose ni Android). */
object PerfilValidator {

    private val SOLO_LETRAS = Regex("^[\\p{L} .'-]+$")
    private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarNombre(nombre: String): String? {
        val n = nombre.trim()
        return when {
            n.isEmpty() -> "El nombre es obligatorio"
            !SOLO_LETRAS.matches(n) -> "Solo letras y espacios"
            n.length < 3 -> "Mínimo 3 caracteres"
            n.length > 60 -> "Máximo 60 caracteres"
            else -> null
        }
    }

    fun validarRut(rut: String): String? {
        val r = rut.trim()
        return when {
            r.isEmpty() -> "El RUT es obligatorio"
            !Regex("^[0-9.]+-[0-9kK]$").matches(r) -> "Formato: 12345678-9"
            !rutEsValido(r) -> "El dígito verificador no es válido"
            else -> null
        }
    }

    fun validarCorreo(correo: String): String? {
        val c = correo.trim()
        return when {
            c.isEmpty() -> "El correo es obligatorio"
            !CORREO.matches(c) -> "Correo no válido (ej: nombre@dominio.cl)"
            else -> null
        }
    }

    fun validarAsignatura(asignatura: Asignatura?, rol: Rol): String? =
        if (rol == Rol.ESTUDIANTE && asignatura == null) "Selecciona tu asignatura" else null

    /** Dependencia entre campos: el profesor guía solo se pide si el rol es estudiante. */
    fun validarProfesorGuia(profesor: String, rol: Rol): String? {
        if (rol == Rol.PROFESOR_GUIA) return null
        val p = profesor.trim()
        return when {
            p.isEmpty() -> "Indica el nombre de tu profesor guía"
            !SOLO_LETRAS.matches(p) -> "Solo letras y espacios"
            p.length < 3 -> "Mínimo 3 caracteres"
            else -> null
        }
    }

    fun validarTodo(
        nombre: String, rut: String, correo: String,
        asignatura: Asignatura?, profesorGuia: String, rol: Rol
    ) = PerfilErrores(
        nombre = validarNombre(nombre),
        rut = validarRut(rut),
        correo = validarCorreo(correo),
        asignatura = validarAsignatura(asignatura, rol),
        profesorGuia = validarProfesorGuia(profesorGuia, rol)
    )

    /**
     * Valida el RUT chileno con el algoritmo módulo 11:
     * se multiplican los dígitos (de derecha a izquierda) por 2,3,4,5,6,7,2,3...
     * se suman, y 11 - (suma % 11) da el dígito verificador (11 → 0, 10 → K).
     */
    fun rutEsValido(rut: String): Boolean {
        val limpio = rut.replace(".", "").replace("-", "").uppercase()
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return false

        var suma = 0
        var factor = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val esperado = when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
        return dv == esperado
    }
}
