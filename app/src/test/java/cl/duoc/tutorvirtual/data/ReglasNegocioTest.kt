package cl.duoc.tutorvirtual.data

import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.hitosQueRequierenAviso
import cl.duoc.tutorvirtual.data.repository.FaqRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Pruebas de reglas de negocio: plazos de hitos y buscador de FAQ. */
class ReglasNegocioTest {

    private val hoy = LocalDate.of(2026, 11, 5)

    private fun hito(dias: Long, estado: EstadoHito = EstadoHito.EN_DESARROLLO) = Hito(
        id = dias, titulo = "Hito $dias", descripcion = "Descripción de prueba",
        formatoRequerido = "PDF", fechaLimite = hoy.plusDays(dias), estado = estado
    )

    @Test
    fun `dias restantes se calculan desde hoy`() {
        assertEquals(3L, hito(3).diasRestantes(hoy))
        assertEquals(-2L, hito(-2).diasRestantes(hoy))
    }

    @Test
    fun `hito pendiente con fecha pasada esta vencido`() {
        assertTrue(hito(-1).estaVencido(hoy))
        assertFalse(hito(-1, EstadoHito.ENVIADO).estaVencido(hoy))
    }

    @Test
    fun `se avisa de hitos vencidos o que vencen en 3 dias o menos`() {
        val hitos = listOf(hito(10), hito(3), hito(-1), hito(1, EstadoHito.APROBADO), hito(0))
        val avisos = hitosQueRequierenAviso(hitos, hoy)
        // Ordenados por fecha: vencido (-1), hoy (0), en 3 días; no el aprobado ni el de 10 días
        assertEquals(listOf(-1L, 0L, 3L), avisos.map { it.id })
    }

    @Test
    fun `buscador ignora mayusculas y tildes`() {
        val conTilde = FaqRepository.buscar("CITACIÓN", null)
        val sinTilde = FaqRepository.buscar("citacion", null)
        assertEquals(conTilde, sinTilde)
    }

    @Test
    fun `filtro por categoria solo devuelve esa categoria`() {
        val resultados = FaqRepository.buscar("", CategoriaFaq.FORMATO)
        assertTrue(resultados.isNotEmpty())
        assertTrue(resultados.all { it.categoria == CategoriaFaq.FORMATO })
    }

    @Test
    fun `busqueda sin coincidencias devuelve lista vacia`() {
        assertTrue(FaqRepository.buscar("xyzxyz", null).isEmpty())
    }
}
