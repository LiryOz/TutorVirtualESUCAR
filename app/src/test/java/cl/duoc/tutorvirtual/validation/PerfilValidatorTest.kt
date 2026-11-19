package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.Asignatura
import cl.duoc.tutorvirtual.data.model.Rol
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerfilValidatorTest {

    @Test
    fun `nombre solo acepta letras`() {
        assertNotNull(PerfilValidator.validarNombre("Juan123"))
        assertNotNull(PerfilValidator.validarNombre(""))
        assertNull(PerfilValidator.validarNombre("María José Pérez"))
    }

    @Test
    fun `rut con digito verificador correcto es valido`() {
        assertTrue(PerfilValidator.rutEsValido("12345678-5"))
        assertTrue(PerfilValidator.rutEsValido("11.111.111-1"))
        assertTrue(PerfilValidator.rutEsValido("10000013-K"))
    }

    @Test
    fun `rut con digito verificador incorrecto no es valido`() {
        assertFalse(PerfilValidator.rutEsValido("12345678-9"))
        assertNotNull(PerfilValidator.validarRut("12345678-9"))
    }

    @Test
    fun `rut sin guion tiene formato invalido`() {
        assertNotNull(PerfilValidator.validarRut("123456785"))
    }

    @Test
    fun `correo debe tener formato valido`() {
        assertNotNull(PerfilValidator.validarCorreo("nombre@duoc"))
        assertNotNull(PerfilValidator.validarCorreo("sin-arroba.cl"))
        assertNull(PerfilValidator.validarCorreo("nombre@duocuc.cl"))
    }

    @Test
    fun `estudiante debe indicar asignatura y profesor guia`() {
        assertNotNull(PerfilValidator.validarAsignatura(null, Rol.ESTUDIANTE))
        assertNotNull(PerfilValidator.validarProfesorGuia("", Rol.ESTUDIANTE))
        assertNull(PerfilValidator.validarAsignatura(Asignatura.TT1, Rol.ESTUDIANTE))
    }

    @Test
    fun `profesor guia no necesita asignatura ni profesor`() {
        assertNull(PerfilValidator.validarAsignatura(null, Rol.PROFESOR_GUIA))
        assertNull(PerfilValidator.validarProfesorGuia("", Rol.PROFESOR_GUIA))
    }
}
