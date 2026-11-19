package cl.duoc.tutorvirtual.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.nativo.GestorFotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Recursos nativos (2.4.3): CÁMARA y GALERÍA.
 * Pasos de la clase:
 *  1. Permiso CAMERA declarado en AndroidManifest.xml.
 *  2. Se pide en tiempo de ejecución (RequestPermission) porque es "peligroso".
 *  3. ActivityResult API: TakePicture (cámara) y GetContent (galería).
 *  4. La foto se muestra en la UI.
 *  5. Si el usuario niega el permiso, se explica amablemente y se ofrece ir a Ajustes.
 * La galería (GetContent) no necesita permiso: el sistema solo entrega la imagen elegida.
 *
 * [ruta] = foto actual (o null). [alCambiar] avisa la nueva ruta al ViewModel.
 */
@Composable
fun SelectorFoto(
    ruta: String?,
    alCambiar: (String?) -> Unit,
    titulo: String,
    modifier: Modifier = Modifier,
    circular: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permisoDenegado by rememberSaveable { mutableStateOf(false) }
    var procesando by remember { mutableStateOf(false) }
    // rememberSaveable: si Android cierra la app mientras la cámara está abierta,
    // al volver todavía sabemos en qué archivo se guardó la foto.
    var archivoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // --- Cámara: TakePicture escribe la foto en el archivo que le damos ---
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val archivo = archivoPendiente
        if (exito && archivo != null) alCambiar(archivo) else GestorFotos.eliminar(archivo)
        archivoPendiente = null
    }

    fun abrirCamara() {
        val archivo = GestorFotos.crearArchivo(context)
        archivoPendiente = archivo.absolutePath
        lanzadorCamara.launch(GestorFotos.uriPara(context, archivo))
    }

    // --- Permiso de cámara en tiempo de ejecución ---
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoDenegado = !concedido
        if (concedido) abrirCamara()
    }

    // --- Galería: GetContent devuelve la imagen elegida (o null si se cancela) ---
    val lanzadorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            procesando = true
            scope.launch {
                // Copiar el archivo en segundo plano para no congelar la pantalla
                val copia = withContext(Dispatchers.IO) { GestorFotos.copiarDesdeGaleria(context, uri) }
                procesando = false
                if (copia != null) alCambiar(copia.absolutePath)
            }
        }
    }

    // Cargar la imagen en segundo plano cada vez que cambia la ruta
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = ruta?.let { withContext(Dispatchers.IO) { GestorFotos.cargarMiniatura(it) } }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())

        val forma = if (circular) CircleShape else RoundedCornerShape(12.dp)
        val tamano = if (circular) Modifier.size(140.dp) else Modifier.fillMaxWidth().height(200.dp)
        Box(
            tamano.clip(forma).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            // Crossfade: transición suave entre "sin foto" y la foto nueva
            Crossfade(targetState = imagen, label = "foto") { bmp ->
                if (bmp != null) {
                    Image(bmp, contentDescription = titulo, contentScale = ContentScale.Crop, modifier = tamano)
                } else {
                    Box(tamano, contentAlignment = Alignment.Center) {
                        Icon(
                            painterResource(if (circular) R.drawable.ic_persona else R.drawable.ic_galeria),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (procesando) CircularProgressIndicator()
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                if (tienePermiso) abrirCamara() else lanzadorPermiso.launch(Manifest.permission.CAMERA)
            }) {
                Icon(painterResource(R.drawable.ic_camara), contentDescription = null)
                Text("Cámara", Modifier.padding(start = 8.dp))
            }
            OutlinedButton(onClick = { lanzadorGaleria.launch("image/*") }) {
                Icon(painterResource(R.drawable.ic_galeria), contentDescription = null)
                Text("Galería", Modifier.padding(start = 8.dp))
            }
        }
        if (ruta != null) {
            TextButton(onClick = { alCambiar(null) }) { Text("Quitar foto") }
        }

        // Permiso negado: explicación + acceso a los ajustes de la app
        AnimatedVisibility(visible = permisoDenegado) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MensajeError("Sin permiso de cámara no podemos tomar la foto. Puedes usar la galería o activarlo en Ajustes.")
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                }) { Text("Abrir ajustes") }
            }
        }
    }
}
