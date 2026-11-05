package cl.duoc.tutorvirtual.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.ui.theme.EstadoAprobado
import cl.duoc.tutorvirtual.ui.theme.EstadoEnDesarrollo
import cl.duoc.tutorvirtual.ui.theme.EstadoEnviado
import cl.duoc.tutorvirtual.ui.theme.EstadoPorIniciar

fun colorDeEstado(estado: EstadoHito): Color = when (estado) {
    EstadoHito.POR_INICIAR -> EstadoPorIniciar
    EstadoHito.EN_DESARROLLO -> EstadoEnDesarrollo
    EstadoHito.ENVIADO -> EstadoEnviado
    EstadoHito.APROBADO -> EstadoAprobado
}

/**
 * Etiqueta de color con el estado del hito.
 * animateColorAsState (2.4.1): al cambiar el estado, el color pasa suavemente
 * de uno a otro, dando retroalimentación visual del cambio.
 */
@Composable
fun ChipEstado(estado: EstadoHito, modifier: Modifier = Modifier) {
    val color by animateColorAsState(colorDeEstado(estado), label = "colorEstado")
    Surface(color = color, contentColor = Color.White, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            estado.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
