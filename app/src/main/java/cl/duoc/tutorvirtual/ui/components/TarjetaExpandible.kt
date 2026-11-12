package cl.duoc.tutorvirtual.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.duoc.tutorvirtual.R

/**
 * Tarjeta con título que se abre/cierra al tocarla.
 * Animaciones (2.4.1):
 *  - AnimatedVisibility: el contenido aparece/desaparece deslizándose.
 *  - animateFloatAsState: la flecha gira 180° para indicar si está abierta.
 * El estado "expandida" lo maneja quien la usa (patrón state hoisting).
 */
@Composable
fun TarjetaExpandible(
    titulo: String,
    expandida: Boolean,
    alCambiar: () -> Unit,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    @DrawableRes icono: Int? = null,
    contenido: @Composable () -> Unit
) {
    val giroFlecha by animateFloatAsState(if (expandida) 180f else 0f, label = "giroFlecha")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (expandida) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = alCambiar)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icono != null) Icon(painterResource(icono), contentDescription = null)
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                if (subtitulo != null) {
                    Text(subtitulo, style = MaterialTheme.typography.labelMedium)
                }
            }
            Icon(
                painterResource(R.drawable.ic_expandir),
                contentDescription = if (expandida) "Contraer" else "Expandir",
                modifier = Modifier.rotate(giroFlecha)
            )
        }
        AnimatedVisibility(
            visible = expandida,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                contenido()
            }
        }
    }
}
