package cl.duoc.tutorvirtual.validation

import cl.duoc.tutorvirtual.data.model.EstadoHito
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Pruebas del validador de hitos. Se ejecutan en el computador (sin emulador)
 * porque HitoValidator no depende de Android: así se demuestra que la lógica
 * está separada de la interfaz.
 */
class HitoValidatorTest {

    private val hoy = LocalDate.of(2026, 11, 5)

    @Test
    fun `titulo vacio es obligatorio`() {
        assertEquals("El título es obligatorio", HitoValidator.validarTitulo("   "))
    }

    @Test
    fun `titulo muy corto o muy largo da error`() {
        assertNotNull(HitoValidator.validarTitulo("Abc"))
        assertNotNull(HitoValidator.validarTitulo("a".repeat(61)))
    }

    @Test
    fun `titulo valido no da error`() {
        assertNull(HitoValidator.validarTitulo("Avance 1: Planteamiento"))
    }

    @Test
    fun `descripcion debe tener minimo 10 caracteres`() {
        assertNotNull(HitoValidator.validarDescripcion("Corta"))
        assertNull(HitoValidator.validarDescripcion("Entregar el capítulo uno completo"))
    }

    @Test
    fun `formato requerido es obligatorio`() {
        assertNotNull(HitoValidator.validarFormato(""))
        assertNull(HitoValidator.validarFormato("PDF"))
    }

    @Test
    fun `fecha es obligatoria`() {
        assertEquals("Selecciona la fecha límite", HitoValidator.validarFecha(null, EstadoHito.POR_INICIAR, hoy))
    }

    @Test
    fun `hito pendiente no puede tener fecha pasada`() {
        val ayer = hoy.minusDays(1)
        assertNotNull(HitoValidator.validarFecha(ayer, EstadoHito.EN_DESARROLLO, hoy))
    }

    @Test
    fun `hito enviado si puede tener fecha pasada`() {
        val ayer = hoy.minusDays(1)
        assertNull(HitoValidator.validarFecha(ayer, EstadoHito.ENVIADO, hoy))
    }

    @Test
    fun `al editar se permite mantener una fecha ya vencida`() {
        val vencida = hoy.minusDays(3)
        assertNull(HitoValidator.validarFecha(vencida, EstadoHito.EN_DESARROLLO, hoy, fechaOriginal = vencida))
    }

    @Test
    fun `solo el profesor guia puede aprobar`() {
        assertNotNull(HitoValidator.validarEstado(EstadoHito.APROBADO, esProfesor = false))
        assertNull(HitoValidator.validarEstado(EstadoHito.APROBADO, esProfesor = true))
        assertNull(HitoValidator.validarEstado(EstadoHito.ENVIADO, esProfesor = false))
    }

    @Test
    fun `estudiante puede editar un hito que ya estaba aprobado`() {
        assertNull(HitoValidator.validarEstado(EstadoHito.APROBADO, esProfesor = false, estadoOriginal = EstadoHito.APROBADO))
    }

    @Test
    fun `validarTodo detecta errores y formulario correcto no tiene errores`() {
        val conErrores = HitoValidator.validarTodo("", "", "", null, EstadoHito.POR_INICIAR, hoy, false)
        assertTrue(conErrores.hayErrores)

        val correcto = HitoValidator.validarTodo(
            "Avance 2: Marco teórico", "Entregar el marco teórico con citas APA", "PDF",
            hoy.plusDays(10), EstadoHito.POR_INICIAR, hoy, false
        )
        assertFalse(correcto.hayErrores)
    }
}
