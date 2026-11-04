package cl.duoc.tutorvirtual.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = VerdeInstitucional,
    onPrimary = Color.White,
    primaryContainer = VerdeClaro,
    onPrimaryContainer = VerdeOscuro,
    secondary = Dorado,
    onSecondary = Color.White,
    secondaryContainer = DoradoClaro,
    onSecondaryContainer = TextoOscuro,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFEEF2EC),
    onSurfaceVariant = Color(0xFF2E2E2E),
    outline = GrisBorde,
    error = RojoError,
    onError = Color.White
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeNocturno,
    onPrimary = VerdeOscuro,
    primaryContainer = VerdeInstitucional,
    onPrimaryContainer = Color.White,
    secondary = DoradoClaro,
    onSecondary = TextoOscuro,
    secondaryContainer = Dorado,
    onSecondaryContainer = Color.White,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = Color(0xFF2A302A),
    onSurfaceVariant = Color(0xFFDADADA),
    outline = Color(0xFFB0B0B0),
    error = RojoErrorClaro,
    onError = Color(0xFF690005)
)

/**
 * Tema de la app (2.2.1 MaterialTheme: colores + tipografía).
 * No usamos "dynamic color" de Android 12 porque podría bajar el contraste
 * que pide el caso. [textoGrande] viene de los ajustes guardados en DataStore.
 */
@Composable
fun TutorVirtualESUCARTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textoGrande: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = tipografia(if (textoGrande) 1.2f else 1f),
        content = content
    )
}
