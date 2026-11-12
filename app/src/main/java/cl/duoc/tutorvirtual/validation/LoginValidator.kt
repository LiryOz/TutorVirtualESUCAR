package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.Cuenta
import cl.duoc.tutorvirtual.data.model.CuentasDemo
import cl.duoc.tutorvirtual.data.model.Rol

/** Resultado del intento de inicio de sesión. */
sealed interface ResultadoLogin {
    data class Exito(val cuenta: Cuenta) : ResultadoLogin
    data class CamposInvalidos(val usuario: String?, val clave: String?) : ResultadoLogin
    data object CredencialesIncorrectas : ResultadoLogin
    /** La cuenta existe, pero pertenece al otro perfil (ej: entra como profesor con la cuenta de alumno). */
    data class PerfilDistinto(val rolDeLaCuenta: Rol) : ResultadoLogin
}

/** Reglas del login en Kotlin puro (se prueban sin emulador). */
object LoginValidator {

    fun validarUsuario(usuario: String): String? = when {
        usuario.isBlank() -> "Ingresa tu usuario"
        usuario.trim().length < 3 -> "El usuario es muy corto"
        else -> null
    }

    fun validarClave(clave: String): String? = when {
        clave.isEmpty() -> "Ingresa tu contraseña"
        clave.length < 6 -> "La contraseña tiene al menos 6 caracteres"
        else -> null
    }

    fun intentar(usuario: String, clave: String, rolElegido: Rol): ResultadoLogin {
        val errUsuario = validarUsuario(usuario)
        val errClave = validarClave(clave)
        if (errUsuario != null || errClave != null) return ResultadoLogin.CamposInvalidos(errUsuario, errClave)

        val cuenta = CuentasDemo.autenticar(usuario, clave) ?: return ResultadoLogin.CredencialesIncorrectas
        if (cuenta.rol != rolElegido) return ResultadoLogin.PerfilDistinto(cuenta.rol)
        return ResultadoLogin.Exito(cuenta)
    }
}
