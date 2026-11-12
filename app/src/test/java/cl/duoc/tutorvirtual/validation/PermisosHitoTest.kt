package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.ResumenConversacion
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.model.TipoComentario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de los permisos por perfil y de la validación de comentarios. */
class PermisosHitoTest {

    @Test
    fun `solo el profesor administra hitos`() {
        assertTrue(PermisosHito.puedeAdministrar(Rol.PROFESOR_GUIA))
        assertFalse(PermisosHito.puedeAdministrar(Rol.ESTUDIANTE))
    }

    @Test
    fun `el estudiante no puede aprobar`() {
        assertFalse(PermisosHito.puedeCambiarEstado(Rol.ESTUDIANTE, EstadoHito.ENVIADO, EstadoHito.APROBADO))
        assertFalse(EstadoHito.APROBADO in PermisosHito.estadosPermitidos(Rol.ESTUDIANTE))
    }

    @Test
    fun `el estudiante avanza su entrega`() {
        assertTrue(PermisosHito.puedeCambiarEstado(Rol.ESTUDIANTE, EstadoHito.POR_INICIAR, EstadoHito.EN_DESARROLLO))
        assertTrue(PermisosHito.puedeCambiarEstado(Rol.ESTUDIANTE, EstadoHito.EN_DESARROLLO, EstadoHito.ENVIADO))
    }

    @Test
    fun `el estudiante no modifica un hito aprobado`() {
        assertFalse(PermisosHito.puedeCambiarEstado(Rol.ESTUDIANTE, EstadoHito.APROBADO, EstadoHito.EN_DESARROLLO))
    }

    @Test
    fun `el profesor puede aprobar y reabrir`() {
        assertTrue(PermisosHito.puedeCambiarEstado(Rol.PROFESOR_GUIA, EstadoHito.ENVIADO, EstadoHito.APROBADO))
        assertTrue(PermisosHito.puedeCambiarEstado(Rol.PROFESOR_GUIA, EstadoHito.APROBADO, EstadoHito.EN_DESARROLLO))
    }

    @Test
    fun `tipos de comentario por perfil`() {
        assertEquals(listOf(TipoComentario.AVANCE, TipoComentario.DUDA), PermisosHito.tiposDeComentario(Rol.ESTUDIANTE))
        assertEquals(listOf(TipoComentario.RESPUESTA), PermisosHito.tiposDeComentario(Rol.PROFESOR_GUIA))
    }

    @Test
    fun `comentario vacio sin archivo da error`() {
        assertNotNull(ComentarioValidator.validar("   ", tieneAdjunto = false))
    }

    @Test
    fun `comentario vacio con archivo es valido`() {
        assertNull(ComentarioValidator.validar("", tieneAdjunto = true))
    }

    @Test
    fun `comentario demasiado largo da error`() {
        assertNotNull(ComentarioValidator.validar("a".repeat(ComentarioValidator.MAX + 1), tieneAdjunto = false))
        assertNull(ComentarioValidator.validar("Avancé el marco teórico", tieneAdjunto = false))
    }

    @Test
    fun `quien escribio ultimo define lo pendiente`() {
        assertTrue(ResumenConversacion(2, Rol.ESTUDIANTE).esperaProfesor)
        assertTrue(ResumenConversacion(3, Rol.PROFESOR_GUIA).esperaEstudiante)
        assertFalse(ResumenConversacion(3, Rol.PROFESOR_GUIA).esperaProfesor)
    }
}
