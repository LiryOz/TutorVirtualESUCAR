package cl.duoc.tutorvirtual.nativo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import cl.duoc.tutorvirtual.data.model.Adjunto
import java.io.File

/**
 * Archivos adjuntos a los comentarios (PDF, Word, imágenes...).
 * Igual que las fotos, se copian a la carpeta PRIVADA de la app (filesDir/adjuntos):
 * así se pueden leer sin conexión y no se necesita permiso de almacenamiento.
 */
object GestorArchivos {

    /** Límite de tamaño para no llenar el teléfono (10 MB). */
    const val TAMANO_MAXIMO = 10L * 1024 * 1024

    sealed interface Resultado {
        data class Ok(val adjunto: Adjunto) : Resultado
        data object MuyGrande : Resultado
        data object Error : Resultado
    }

    private fun carpeta(context: Context) = File(context.filesDir, "adjuntos").apply { mkdirs() }

    /** Copia el archivo elegido por el usuario (selector de documentos del sistema). */
    fun copiar(context: Context, origen: Uri): Resultado = try {
        var nombre = "archivo"
        var tamano = -1L
        context.contentResolver.query(origen, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val iNombre = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val iTamano = c.getColumnIndex(OpenableColumns.SIZE)
                if (iNombre >= 0) nombre = c.getString(iNombre) ?: nombre
                if (iTamano >= 0 && !c.isNull(iTamano)) tamano = c.getLong(iTamano)
            }
        }
        if (tamano > TAMANO_MAXIMO) {
            Resultado.MuyGrande
        } else {
            // Prefijo con la hora para que dos archivos con el mismo nombre no se pisen
            val destino = File(carpeta(context), "${System.currentTimeMillis()}_${nombre.replace('/', '_')}")
            val entrada = context.contentResolver.openInputStream(origen)
            if (entrada == null) Resultado.Error
            else {
                entrada.use { e -> destino.outputStream().use { s -> e.copyTo(s) } }
                Resultado.Ok(Adjunto(destino.absolutePath, nombre))
            }
        }
    } catch (e: Exception) {
        Resultado.Error
    }

    /** Abre el adjunto con la app que corresponda. Devuelve false si no hay ninguna app capaz. */
    fun abrir(context: Context, adjunto: Adjunto): Boolean {
        val archivo = File(adjunto.ruta)
        if (!archivo.exists()) return false
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val extension = adjunto.nombre.substringAfterLast('.', "").lowercase()
        val tipo = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, tipo)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    fun eliminar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }
}
