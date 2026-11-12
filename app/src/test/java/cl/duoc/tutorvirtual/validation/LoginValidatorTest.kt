package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.CuentasDemo
import cl.duoc.tutorvirtual.data.model.Perfil
import cl.duoc.tutorvirtual.data.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del inicio de sesión con las cuentas genéricas (Kotlin puro, sin emulador). */
class LoginValidatorTest {

    @Test
    fun `campos vacios piden usuario y contrasena`() {
        val r = LoginValidator.intentar("", "", Rol.ESTUDIANTE)
        assertTrue(r is ResultadoLogin.CamposInvalidos)
        r as ResultadoLogin.CamposInvalidos
        assertNotNull(r.usuario)
        assertNotNull(r.clave)
    }

    @Test
    fun `contrasena corta da error`() {
        assertNotNull(LoginValidator.validarClave("123"))
        assertNull(LoginValidator.validarClave("123456"))
    }

    @Test
    fun `cuenta de alumno ingresa como estudiante`() {
        val r = LoginValidator.intentar("alumno", "alumno123", Rol.ESTUDIANTE)
        assertEquals(ResultadoLogin.Exito(CuentasDemo.ESTUDIANTE), r)
    }

    @Test
    fun `cuenta de profesor ingresa como profesor guia`() {
        val r = LoginValidator.intentar("profesor", "profesor123", Rol.PROFESOR_GUIA)
        assertEquals(ResultadoLogin.Exito(CuentasDemo.PROFESOR), r)
    }

    @Test
    fun `usuario no distingue mayusculas ni espacios`() {
        val r = LoginValidator.intentar("  ALUMNO ", "alumno123", Rol.ESTUDIANTE)
        assertTrue(r is ResultadoLogin.Exito)
    }

    @Test
    fun `contrasena incorrecta es rechazada`() {
        assertEquals(
            ResultadoLogin.CredencialesIncorrectas,
            LoginValidator.intentar("alumno", "otraClave", Rol.ESTUDIANTE)
        )
    }

    @Test
    fun `no se puede entrar como profesor con la cuenta de alumno`() {
        assertEquals(
            ResultadoLogin.PerfilDistinto(Rol.ESTUDIANTE),
            LoginValidator.intentar("alumno", "alumno123", Rol.PROFESOR_GUIA)
        )
    }

    @Test
    fun `perfil de profesor esta completo sin asignatura`() {
        assertTrue(Perfil(nombre = "Profesor Demo", rol = Rol.PROFESOR_GUIA).estaCompleto)
        assertTrue(!Perfil(nombre = "Alumno Demo", rol = Rol.ESTUDIANTE).estaCompleto)
    }
}
