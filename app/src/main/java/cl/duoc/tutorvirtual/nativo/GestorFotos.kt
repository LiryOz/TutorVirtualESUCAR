package cl.duoc.tutorvirtual.nativo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import java.io.File

/**
 * Manejo de archivos de foto (cámara y galería).
 * Las fotos se guardan en la carpeta PRIVADA de la app (filesDir/fotos):
 * otras apps no pueden leerlas y no se necesita permiso de almacenamiento.
 */
object GestorFotos {

    private fun carpeta(context: Context) = File(context.filesDir, "fotos").apply { mkdirs() }

    /** Archivo vacío donde la cámara escribirá la foto. */
    fun crearArchivo(context: Context): File =
        File(carpeta(context), "foto_${System.currentTimeMillis()}.jpg")

    /**
     * La cámara es otra app: no puede escribir en nuestra carpeta privada directamente.
     * FileProvider le entrega una dirección (content://) temporal y segura.
     */
    fun uriPara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    /**
     * La imagen elegida en la galería solo se puede leer mientras dure el permiso
     * temporal, así que la copiamos a nuestra carpeta para conservarla.
     */
    fun copiarDesdeGaleria(context: Context, origen: Uri): File? {
        return try {
            val destino = crearArchivo(context)
            val entrada = context.contentResolver.openInputStream(origen) ?: return null
            entrada.use { e -> destino.outputStream().use { salida -> e.copyTo(salida) } }
            destino
        } catch (e: Exception) {
            null   // si algo falla, simplemente no hay foto
        }
    }

    fun eliminar(ruta: String?) {
        if (ruta != null) File(ruta).delete()
    }

    /**
     * Carga la foto reducida (para no gastar memoria) y girada correctamente
     * según la orientación con que se tomó (dato EXIF de la cámara).
     */
    fun cargarMiniatura(ruta: String, ladoMaximo: Int = 800): ImageBitmap? {
        if (!File(ruta).exists()) return null
        return try {
            leerYGirar(ruta, ladoMaximo)
        } catch (e: Exception) {
            null   // archivo dañado o ilegible: se muestra el ícono por defecto
        }
    }

    private fun leerYGirar(ruta: String, ladoMaximo: Int): ImageBitmap? {
        // 1) Leer solo el tamaño
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, opciones)
        // 2) Calcular cuánto reducir (potencias de 2)
        var escala = 1
        while (opciones.outWidth / (escala * 2) >= ladoMaximo && opciones.outHeight / (escala * 2) >= ladoMaximo) {
            escala *= 2
        }
        val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = escala })
            ?: return null
        // 3) Girar si la foto venía rotada
        val grados = when (ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val final = if (grados == 0f) bitmap
        else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
        return final.asImageBitmap()
    }
}
