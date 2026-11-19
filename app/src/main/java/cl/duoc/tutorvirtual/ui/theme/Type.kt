package cl.duoc.tutorvirtual.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Tipografía un poco más grande que la estándar para que el texto sea legible.
// Si "texto grande" está activo (ajuste de accesibilidad), se escala todo.
fun tipografia(escala: Float = 1f) = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = (28 * escala).sp, lineHeight = (36 * escala).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = (24 * escala).sp, lineHeight = (32 * escala).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (22 * escala).sp, lineHeight = (28 * escala).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (18 * escala).sp, lineHeight = (24 * escala).sp),
    bodyLarge = TextStyle(fontSize = (17 * escala).sp, lineHeight = (25 * escala).sp),
    bodyMedium = TextStyle(fontSize = (15 * escala).sp, lineHeight = (22 * escala).sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (15 * escala).sp, lineHeight = (20 * escala).sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = (13 * escala).sp, lineHeight = (18 * escala).sp)
)
