package cl.duoc.tutorvirtual.nativo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.tutorvirtual.MainActivity
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.hitosQueRequierenAviso
import java.time.LocalDate

/**
 * Notificaciones locales para recordar fechas límite (caso 3.3).
 * No usa internet ni servidores: la app revisa sus hitos guardados en Room
 * al abrirse y, si hay plazos cercanos, muestra una notificación del sistema.
 */
object Notificador {

    private const val CANAL_ID = "recordatorios_hitos"
    private const val NOTIFICACION_ID = 1001

    /** Desde Android 13 (API 33) el permiso POST_NOTIFICATIONS se pide en tiempo de ejecución. */
    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Android 8+ exige un "canal" para agrupar notificaciones; el usuario puede silenciarlo. */
    fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ID,
            "Recordatorios de hitos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Avisos de entregas próximas a vencer" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    /**
     * Revisa los hitos y, si alguno vence pronto o ya venció, muestra UNA notificación resumen.
     * Devuelve cuántos hitos se avisaron.
     */
    fun avisarSiCorresponde(context: Context, hitos: List<Hito>, hoy: LocalDate = LocalDate.now()): Int {
        val urgentes = hitosQueRequierenAviso(hitos, hoy)
        if (urgentes.isEmpty() || !tienePermiso(context)) return 0

        crearCanal(context)
        val primero = urgentes.first()
        val dias = primero.diasRestantes(hoy)
        val detalle = when {
            dias < 0 -> "\"${primero.titulo}\" está vencido."
            dias == 0L -> "\"${primero.titulo}\" vence hoy."
            else -> "\"${primero.titulo}\" vence en $dias día(s)."
        }
        val texto = if (urgentes.size > 1) "$detalle Y ${urgentes.size - 1} hito(s) más." else detalle

        // Al tocar la notificación se abre la app
        val abrirApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Tienes entregas próximas")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICACION_ID, notificacion)
        } catch (e: SecurityException) {
            return 0   // el usuario quitó el permiso justo ahora
        }
        return urgentes.size
    }
}
