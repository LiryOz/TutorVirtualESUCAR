package cl.duoc.tutorvirtual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.tutorvirtual.data.model.Ajustes
import cl.duoc.tutorvirtual.ui.TutorVirtualApp
import cl.duoc.tutorvirtual.ui.components.FondoAnimado
import cl.duoc.tutorvirtual.ui.components.LogoAnimado
import cl.duoc.tutorvirtual.ui.screens.LoginScreen
import cl.duoc.tutorvirtual.ui.theme.TutorVirtualESUCARTheme
import kotlinx.coroutines.launch

/** Punto de entrada: calcula el tamaño de pantalla, aplica el tema y decide si mostrar el login. */
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
            // Sesión guardada en DataStore: null = leyendo, "" = sin sesión, otro = usuario
            val sesion by preferencias.sesion.collectAsStateWithLifecycle(initialValue = null)
            val scope = rememberCoroutineScope()

            TutorVirtualESUCARTheme(modoOscuro = ajustes.modoOscuro, tamanoTexto = ajustes.tamanoTexto) {
                // Cambio animado entre carga, login y la app (al entrar o al cerrar sesión)
                AnimatedContent(
                    targetState = sesion,
                    transitionSpec = {
                        (fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.94f)) togetherWith fadeOut(tween(300))
                    },
                    label = "sesion"
                ) { usuario ->
                    when {
                        usuario == null -> PantallaCarga()
                        usuario.isEmpty() -> LoginScreen()
                        else -> TutorVirtualApp(
                            anchoVentana = tamanoVentana.widthSizeClass,
                            cerrarSesion = { scope.launch { preferencias.cerrarSesion() } }
                        )
                    }
                }
            }
        }
    }
}

/** Pantalla breve mientras se lee la sesión guardada. */
@Composable
private fun PantallaCarga() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        FondoAnimado()
        LogoAnimado()
    }
}
