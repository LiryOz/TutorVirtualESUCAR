package cl.duoc.tutorvirtual.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.nativo.Notificador
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Se ejecuta una vez al abrir la app:
 *  1. Si los recordatorios están activos (ajuste en DataStore)...
 *  2. ...pide el permiso de notificaciones si falta (Android 13+)...
 *  3. ...y muestra un aviso si hay hitos por vencer (datos de Room).
 * No dibuja nada en pantalla.
 */
@Composable
fun RevisarRecordatoriosAlIniciar() {
    val context = LocalContext.current
    val contenedor = (context.applicationContext as TutorApp).contenedor
    val scope = rememberCoroutineScope()
    // rememberSaveable: al girar la pantalla no se vuelve a revisar ni a pedir permiso
    var yaRevisado by rememberSaveable { mutableStateOf(false) }

    fun revisar() {
        scope.launch {
            val hitos = contenedor.hitoRepository.observarHitos().first()
            Notificador.avisarSiCorresponde(context, hitos)
        }
    }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) revisar()
    }

    LaunchedEffect(Unit) {
        if (yaRevisado) return@LaunchedEffect
        yaRevisado = true
        if (!contenedor.preferencias.ajustes.first().recordatorios) return@LaunchedEffect
        when {
            Notificador.tienePermiso(context) -> revisar()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
