package cl.duoc.tutorvirtual.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.tutorvirtual.R

/**
 * Imagen del Sargento (pestaña de la barra, chat y burbujas).
 * Archivo: res/drawable-nodpi/sargento.png (reducido a 384 px para que el chat sea fluido;
 * el original está en app/src/main/sargento-original.png).
 */
val LogoSargento: Int = R.drawable.sargento

/** Foto circular del Sargento. Se dibuja con sus colores reales (sin teñir). */
@Composable
fun AvatarSargento(
    tamano: Dp = 40.dp,
    modifier: Modifier = Modifier,
    borde: Color? = null
) {
    Box(
        modifier
            .size(tamano)
            .clip(CircleShape)
            .then(if (borde != null) Modifier.border(2.dp, borde, CircleShape) else Modifier)
    ) {
        Image(
            painterResource(LogoSargento),
            contentDescription = "Sargento",
            contentScale = ContentScale.Crop,
            // La imagen trae un borde gris fuera del círculo: se amplía un poco para recortarlo
            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = 1.06f; scaleY = 1.06f }
        )
    }
}
