package cl.duoc.tutorvirtual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tutorvirtual.data.model.Ajustes
import cl.duoc.tutorvirtual.ui.TutorVirtualApp
import cl.duoc.tutorvirtual.ui.theme.TutorVirtualESUCARTheme

/** Punto de entrada: calcula el tamaño de pantalla y aplica el tema. */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferencias = (application as TutorApp).contenedor.preferencias
        setContent {
            // 2.2.1: se calcula una sola vez y se pasa hacia abajo
            val tamanoVentana = calculateWindowSizeClass(this)
            // Ajuste de accesibilidad guardado en DataStore
            val ajustes by preferencias.ajustes.collectAsStateWithLifecycle(initialValue = Ajustes())
            TutorVirtualESUCARTheme(textoGrande = ajustes.textoGrande) {
                TutorVirtualApp(anchoVentana = tamanoVentana.widthSizeClass)
            }
        }
    }
}
