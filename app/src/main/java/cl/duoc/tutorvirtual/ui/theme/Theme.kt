package cl.duoc.tutorvirtual.ui.theme

import cl.duoc.tutorvirtual.data.model.TamanoTexto
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
    secondary = VerdeSecundario,
    onSecondary = Color.White,
    tertiary = VerdeAcento,
    onTertiary = VerdeOscuro,
    secondaryContainer = Color(0xFFD7EBD3),
    onSecondaryContainer = VerdeOscuro,
    background = Color.White,          // fondo blanco en modo claro
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFEEF2EC),
    // Superficies de tarjetas y barras: grises verdosos (evita el lila por defecto de Material)
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF4F6F2),
    surfaceContainer = Color(0xFFEEF1EC),
    surfaceContainerHigh = Color(0xFFE8ECE6),
    surfaceContainerHighest = Color(0xFFE2E6E0),
    onSurfaceVariant = TextoOscuro,
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
    tertiary = VerdeAcento,
    onTertiary = VerdeOscuro,
    secondaryContainer = Dorado,
    onSecondaryContainer = Color.White,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro,
    surfaceVariant = Color(0xFF2A302A),
    surfaceContainerLowest = Color(0xFF0B0F0B),
    surfaceContainerLow = Color(0xFF181D18),
    surfaceContainer = Color(0xFF1C211C),
    surfaceContainerHigh = Color(0xFF262B26),
    surfaceContainerHighest = Color(0xFF313631),
    onSurfaceVariant = Color(0xFFDADADA),
    outline = Color(0xFFB0B0B0),
    error = RojoErrorClaro,
    onError = Color(0xFF690005)
)

/**
 * Tema de la app (2.2.1 MaterialTheme: colores + tipografía).
 * No usamos "dynamic color" de Android 12 porque podría bajar el contraste
 * que pide el caso. [modoOscuro] y [tamanoTexto] vienen de los ajustes guardados
 * en DataStore: por defecto la app es clara (fondo blanco) aunque el teléfono esté en modo oscuro.
 */
@Composable
fun TutorVirtualESUCARTheme(
    modoOscuro: Boolean = false,
    tamanoTexto: TamanoTexto = TamanoTexto.NORMAL,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
        typography = tipografia(tamanoTexto.escala),
        content = content
    )
}
