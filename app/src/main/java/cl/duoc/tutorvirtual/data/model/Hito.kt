package cl.duoc.tutorvirtual.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Estado del entregable (caso 3.2). */
enum class EstadoHito(val etiqueta: String) {
    POR_INICIAR("Por iniciar"),
    EN_DESARROLLO("En desarrollo"),
    ENVIADO("Enviado"),
    APROBADO("Aprobado");

    /** Un hito enviado o aprobado ya no genera alertas de plazo. */
    val estaTerminado: Boolean get() = this == ENVIADO || this == APROBADO
}

/**
 * Hito / entrega del trabajo de título (caso 3.2:
 * título, descripción, formato requerido y fecha límite).
 * Es el modelo que usa la app; Room guarda una copia como HitoEntity.
 */
data class Hito(
    val id: Long = 0,
    val titulo: String,
    val descripcion: String,
    val formatoRequerido: String,
    val fechaLimite: LocalDate,
    val estado: EstadoHito = EstadoHito.POR_INICIAR,
    val fotoEvidencia: String? = null      // ruta de la foto tomada o elegida (Etapa 8)
) {
    /** Días que faltan para la fecha límite (negativo = ya pasó). */
    fun diasRestantes(hoy: LocalDate): Long = ChronoUnit.DAYS.between(hoy, fechaLimite)

    fun estaVencido(hoy: LocalDate): Boolean =
        !estado.estaTerminado && fechaLimite.isBefore(hoy)

    /** Vence hoy o dentro de [dias] días y aún no se envía. */
    fun estaPorVencer(hoy: LocalDate, dias: Long = 3): Boolean =
        !estado.estaTerminado && diasRestantes(hoy) in 0..dias
}

/**
 * Regla de negocio de las alertas: se avisa de los hitos vencidos o que vencen
 * en 3 días o menos, ordenados por fecha. La usan la pantalla de inicio y las notificaciones.
 */
fun hitosQueRequierenAviso(hitos: List<Hito>, hoy: LocalDate): List<Hito> =
    hitos.filter { it.estaVencido(hoy) || it.estaPorVencer(hoy) }
        .sortedBy { it.fechaLimite }
