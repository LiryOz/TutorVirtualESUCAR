package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import cl.duoc.tutorvirtual.ui.components.TarjetaExpandible
import cl.duoc.tutorvirtual.viewmodel.FaqViewModel

/** Buscador de preguntas frecuentes con filtro por categoría. */
@Composable
fun FaqScreen(viewModel: FaqViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(Modifier.widthIn(max = 700.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = estado.texto,
                    onValueChange = viewModel::onTextoChange,
                    label = { Text("Buscar duda") },
                    placeholder = { Text("Ej: citar libro, muestra, formato") },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) },
                    trailingIcon = {
                        if (estado.texto.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onTextoChange("") }) {
                                Icon(painterResource(R.drawable.ic_cerrar), contentDescription = "Borrar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth()
                )
                // Filtro por categoría (chips que se pueden marcar/desmarcar)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoriaFaq.entries.forEach { categoria ->
                        FilterChip(
                            selected = estado.categoria == categoria,
                            onClick = { viewModel.onCategoriaChange(categoria) },
                            label = { Text(categoria.etiqueta) },
                            leadingIcon = if (estado.categoria == categoria) {
                                { Icon(painterResource(R.drawable.ic_check), contentDescription = null) }
                            } else null
                        )
                    }
                }
                Text(
                    "${estado.resultados.size} resultado(s)",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (estado.resultados.isEmpty()) {
            item {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No encontramos preguntas con esa búsqueda.", style = MaterialTheme.typography.bodyLarge)
                    Text("Prueba con otra palabra o consulta a tu profesor guía.")
                    TextButton(onClick = viewModel::limpiarBusqueda) { Text("Ver todas") }
                }
            }
        }

        items(estado.resultados, key = { it.id }) { pregunta ->
            TarjetaExpandible(
                titulo = pregunta.pregunta,
                subtitulo = pregunta.categoria.etiqueta,
                expandida = estado.abiertaId == pregunta.id,
                alCambiar = { viewModel.alternarPregunta(pregunta.id) },
                // animateItem: las tarjetas se reacomodan suavemente al filtrar
                modifier = Modifier.widthIn(max = 700.dp).animateItem()
            ) {
                Text(pregunta.respuesta, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
