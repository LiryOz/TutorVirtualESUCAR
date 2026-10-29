package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tutorvirtual.data.repository.GuiaRepository

@Composable
fun GuiaMetodologicaScreen() {
    var etapaSeleccionada by remember { mutableStateOf<Int?>(null) }
    val etapas = GuiaRepository.etapas
    val etapa = etapas.firstOrNull { it.id == etapaSeleccionada }

    if (etapa == null) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Guía metodológica", style = MaterialTheme.typography.headlineSmall)
            Text("Selecciona una etapa para revisar sus orientaciones.")
            etapas.forEach { item ->
                OutlinedButton(
                    onClick = { etapaSeleccionada = item.id },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${item.id}. ${item.titulo}")
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(etapa.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(etapa.descripcion)
            Text("Orientaciones", style = MaterialTheme.typography.titleMedium)
            etapa.orientaciones.forEachIndexed { index, orientacion ->
                Text("${index + 1}. $orientacion")
            }
            OutlinedButton(onClick = { etapaSeleccionada = null }) {
                Text("Volver a las etapas")
            }
        }
    }
}