package cl.duoc.tutorvirtual.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Barra de avance del trabajo de título.
 * animateFloatAsState hace que la barra "crezca" hasta el nuevo valor
 * cada vez que se marca o desmarca un hito.
 */
@Composable
fun ProgresoAnimado(progreso: Float, terminados: Int, total: Int, modifier: Modifier = Modifier) {
    val progresoAnimado by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(durationMillis = 800),
        label = "progreso"
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Avance: $terminados de $total hitos entregados (${(progreso * 100).toInt()}%)",
            style = MaterialTheme.typography.titleMedium
        )
        LinearProgressIndicator(
            progress = { progresoAnimado },
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
        )
    }
}
