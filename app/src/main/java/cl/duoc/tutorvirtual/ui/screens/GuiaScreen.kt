package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.EtapaMetodologica
import cl.duoc.tutorvirtual.ui.components.TarjetaExpandible
import cl.duoc.tutorvirtual.viewmodel.GuiaViewModel

/** Lista de etapas de la guía (paso a paso). */
@Composable
fun GuiaScreen(
    abrirEtapa: (Int) -> Unit,
    viewModel: GuiaViewModel = viewModel()
) {
    // collectAsState(): la pantalla se redibuja si cambian las etapas (2.3.2)
    val etapas by viewModel.etapas.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "Sigue las etapas en orden. Cada una tiene un paso a paso y su pauta de evaluación.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.widthIn(max = 700.dp)
            )
        }
        items(etapas, key = { it.id }) { etapa ->
            TarjetaEtapa(etapa, onClick = { abrirEtapa(etapa.id) })
        }
    }
}

@Composable
private fun TarjetaEtapa(etapa: EtapaMetodologica, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().widthIn(max = 700.dp)) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NumeroEtapa(etapa.id)
            Column(Modifier.weight(1f)) {
                Text(etapa.titulo, style = MaterialTheme.typography.titleMedium)
                Text(etapa.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                painterResource(R.drawable.ic_expandir),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun NumeroEtapa(numero: Int) {
    Box(
        Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$numero",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Detalle de una etapa: paso a paso + pauta + botones anterior/siguiente. */
@Composable
fun GuiaDetalleScreen(
    etapaId: Int,
    irAEtapa: (Int) -> Unit,
    viewModel: GuiaViewModel = viewModel()
) {
    val etapa = viewModel.etapa(etapaId) ?: return
    // rememberSaveable: la pauta sigue abierta aunque se gire el teléfono (2.4.1)
    var pautaAbierta by rememberSaveable(etapaId) { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.widthIn(max = 700.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Etapa ${etapa.id} de ${viewModel.totalEtapas}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(etapa.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(etapa.descripcion, style = MaterialTheme.typography.bodyLarge)

            HorizontalDivider()
            Text("Paso a paso", style = MaterialTheme.typography.titleMedium)
            etapa.orientaciones.forEachIndexed { indice, paso ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${indice + 1}.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(paso, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.padding(4.dp))
            TarjetaExpandible(
                titulo = "Pauta de evaluación",
                subtitulo = "${etapa.pauta.size} criterios",
                icono = R.drawable.ic_pauta,
                expandida = pautaAbierta,
                alCambiar = { pautaAbierta = !pautaAbierta }
            ) {
                etapa.pauta.forEach { criterio ->
                    Text(criterio.criterio, style = MaterialTheme.typography.titleSmall)
                    Text(
                        criterio.queSeEspera,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Navegación entre etapas (flujo paso a paso)
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (etapa.id > 1) {
                    OutlinedButton(onClick = { irAEtapa(etapa.id - 1) }) { Text("Anterior") }
                } else Spacer(Modifier)
                if (etapa.id < viewModel.totalEtapas) {
                    OutlinedButton(onClick = { irAEtapa(etapa.id + 1) }) { Text("Siguiente") }
                }
            }
        }
    }
}
