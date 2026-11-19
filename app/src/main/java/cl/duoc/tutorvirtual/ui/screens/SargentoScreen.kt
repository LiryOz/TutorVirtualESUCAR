package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.CategoriaFaq
import cl.duoc.tutorvirtual.data.model.PreguntaFrecuente
import cl.duoc.tutorvirtual.ui.components.AvatarSargento
import cl.duoc.tutorvirtual.ui.components.EntradaEscalonada
import cl.duoc.tutorvirtual.viewmodel.MensajeChat
import cl.duoc.tutorvirtual.viewmodel.SargentoViewModel

/**
 * Chat con el Sargento, el perro de Carabineros que responde las dudas frecuentes.
 * Las preguntas y respuestas vienen del banco de preguntas (FaqRepository).
 */
@Composable
fun SargentoScreen(
    verBancoPreguntas: () -> Unit,
    viewModel: SargentoViewModel = viewModel(factory = SargentoViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val lista = rememberLazyListState()

    // Cada mensaje nuevo (o los "puntitos") lleva la lista hasta abajo con animación
    val totalItems = estado.mensajes.size + if (estado.escribiendo) 1 else 0
    LaunchedEffect(totalItems) {
        if (totalItems > 0) lista.animateScrollToItem(totalItems - 1)
    }

    // El Sargento "salta" de alegría cada vez que responde
    val salto = remember { Animatable(0f) }
    val respuestasDelBot = estado.mensajes.count { it.deBot }
    LaunchedEffect(respuestasDelBot) {
        if (respuestasDelBot > 0) {
            salto.animateTo(-10f, tween(150, easing = FastOutSlowInEasing))
            salto.animateTo(0f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
        }
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Encabezado(
            escribiendo = estado.escribiendo,
            salto = salto.value,
            alVerBanco = verBancoPreguntas,
            alReiniciar = viewModel::reiniciar
        )

        LazyColumn(
            state = lista,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(estado.mensajes, key = { it.id }) { m ->
                Box(Modifier.widthIn(max = 700.dp).animateItem()) {
                    EntradaEscalonada(0) {
                        if (m.deBot) MensajeBot(m, alElegir = viewModel::elegirPregunta)
                        else MensajeUsuario(m.texto)
                    }
                }
            }
            if (estado.escribiendo) {
                item(key = "escribiendo") { Box(Modifier.widthIn(max = 700.dp).fillMaxWidth().animateItem()) { Escribiendo() } }
            }
        }

        // Atajos por categoría
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoriaFaq.entries.forEach { c ->
                AssistChip(
                    onClick = { viewModel.elegirCategoria(c) },
                    enabled = !estado.escribiendo,
                    label = { Text(c.etiqueta) }
                )
            }
        }

        Redactor(
            texto = estado.texto,
            habilitado = !estado.escribiendo,
            alCambiar = viewModel::onTextoChange,
            alEnviar = viewModel::enviar
        )
    }
}

@Composable
private fun Encabezado(escribiendo: Boolean, salto: Float, alVerBanco: () -> Unit, alReiniciar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                AvatarSargento(52.dp, Modifier.graphicsLayer { translationY = salto.dp.toPx() }, borde = MaterialTheme.colorScheme.primary)
                // Punto verde "en servicio"
                Box(
                    Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).size(14.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer).padding(2.dp)
                        .clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                )
            }
            Column(Modifier.weight(1f)) {
                Text("Sargento", style = MaterialTheme.typography.titleLarge)
                AnimatedContent(
                    escribiendo,
                    transitionSpec = { (fadeIn() + slideInVertically { it }) togetherWith (fadeOut() + slideOutVertically { -it }) },
                    label = "estadoSargento"
                ) { e ->
                    Text(
                        if (e) "Olfateando la respuesta…" else "Perro de Carabineros · En servicio",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            IconButton(onClick = alVerBanco) {
                Icon(painterResource(R.drawable.ic_buscar), contentDescription = "Ver banco de preguntas")
            }
            IconButton(onClick = alReiniciar) {
                Icon(painterResource(R.drawable.ic_eliminar), contentDescription = "Borrar conversación")
            }
        }
    }
    HorizontalDivider()
}

/** Burbuja del Sargento: avatar, título (si responde una pregunta), texto y sugerencias. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MensajeBot(m: MensajeChat, alElegir: (PreguntaFrecuente) -> Unit) {
    val colores = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AvatarSargento(34.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                color = if (m.sinRespuesta) colores.errorContainer else colores.surfaceContainerHigh,
                shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    m.titulo?.let {
                        Text(it, style = MaterialTheme.typography.titleMedium, color = colores.primary)
                    }
                    Text(
                        m.texto,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (m.sinRespuesta) colores.onErrorContainer else colores.onSurface
                    )
                }
            }
            if (m.sugerencias.isNotEmpty()) {
                if (m.titulo != null) Text("Preguntas relacionadas", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    m.sugerencias.forEach { p ->
                        SuggestionChip(
                            onClick = { alElegir(p) },
                            label = { Text(p.pregunta, style = MaterialTheme.typography.labelLarge) },
                            icon = { Icon(painterResource(R.drawable.ic_ayuda), contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeUsuario(texto: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(12.dp))
        }
    }
}

/** Tres puntitos que saltan uno tras otro mientras el Sargento "escribe". */
@Composable
private fun Escribiendo() {
    val t = rememberInfiniteTransition(label = "puntos")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AvatarSargento(34.dp)
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    val y by t.animateFloat(
                        0f, -6f,
                        infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse, StartOffset(i * 120)),
                        label = "punto$i"
                    )
                    Box(
                        Modifier.graphicsLayer { translationY = y.dp.toPx() }.size(8.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun Redactor(texto: String, habilitado: Boolean, alCambiar: (String) -> Unit, alEnviar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = alCambiar,
            placeholder = { Text("Pregúntale al Sargento…") },
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { alEnviar() }),
            modifier = Modifier.weight(1f)
        )
        FilledIconButton(
            onClick = alEnviar,
            enabled = habilitado && texto.isNotBlank(),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(painterResource(R.drawable.ic_enviar), contentDescription = "Enviar pregunta")
        }
    }
}
