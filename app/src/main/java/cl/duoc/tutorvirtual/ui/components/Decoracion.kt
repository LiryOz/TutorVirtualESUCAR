package cl.duoc.tutorvirtual.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.ui.theme.VerdeAcento
import cl.duoc.tutorvirtual.ui.theme.VerdeInstitucional
import cl.duoc.tutorvirtual.ui.theme.VerdeSecundario

/** Degradado institucional usado en el login, la carga y el encabezado de inicio. */
val DegradadoInstitucional = listOf(VerdeSecundario, VerdeInstitucional, Color(0xFF2E9B76))

/**
 * Fondo verde con degradado y "burbujas" translúcidas que flotan lentamente
 * (rememberInfiniteTransition: animación que se repite sin fin).
 */
@Composable
fun FondoAnimado(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "fondo")
    val a by transicion.animateFloat(
        0f, 1f, infiniteRepeatable(tween(9_000, easing = LinearEasing), RepeatMode.Reverse), label = "a"
    )
    val b by transicion.animateFloat(
        0f, 1f, infiniteRepeatable(tween(13_000, easing = LinearEasing), RepeatMode.Reverse), label = "b"
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.linearGradient(DegradadoInstitucional, start = Offset.Zero, end = Offset(size.width, size.height)))
        val m = size.minDimension
        drawCircle(
            Color.White.copy(alpha = 0.08f), radius = m * 0.55f,
            center = Offset(size.width * (0.05f + 0.25f * a), size.height * (0.10f + 0.06f * b))
        )
        drawCircle(
            VerdeAcento.copy(alpha = 0.22f), radius = m * 0.38f,
            center = Offset(size.width * (0.98f - 0.18f * b), size.height * (0.42f + 0.10f * a))
        )
        drawCircle(
            Color.White.copy(alpha = 0.06f), radius = m * 0.75f,
            center = Offset(size.width * (0.25f + 0.35f * b), size.height * (1.02f - 0.06f * a))
        )
    }
}

/** Logo circular de la app (el mismo del ícono de Android) que "respira" suavemente. */
@Composable
fun LogoAnimado(tamano: Dp = 132.dp, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "logo")
    val escala by transicion.animateFloat(
        1f, 1.06f, infiniteRepeatable(tween(1_600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "escala"
    )
    val halo by transicion.animateFloat(
        0.15f, 0.35f, infiniteRepeatable(tween(1_600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "halo"
    )
    Box(modifier.size(tamano * 1.3f), contentAlignment = Alignment.Center) {
        // Halo que late detrás del logo
        Box(
            Modifier.size(tamano * 1.25f).scale(escala).clip(CircleShape)
                .background(Color.White.copy(alpha = halo))
        )
        Image(
            painterResource(R.drawable.logo_tutor_esucar),
            contentDescription = "Logo Tutor ESUCAR",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(tamano)
                .scale(escala)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
        )
    }
}

/**
 * Hace aparecer su contenido deslizándose desde abajo, con un retraso según [indice]
 * (efecto "escalonado": las tarjetas entran una tras otra).
 */
@Composable
fun EntradaEscalonada(indice: Int, content: @Composable () -> Unit) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    val retraso = indice * 70
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(450, delayMillis = retraso)) +
            slideInVertically(tween(450, delayMillis = retraso, easing = FastOutSlowInEasing)) { it / 3 }
    ) { content() }
}
