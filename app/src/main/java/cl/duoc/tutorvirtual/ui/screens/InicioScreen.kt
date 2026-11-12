package cl.duoc.tutorvirtual.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.ui.components.ChipEstado
import cl.duoc.tutorvirtual.ui.components.EntradaEscalonada
import cl.duoc.tutorvirtual.ui.components.FondoAnimado
import cl.duoc.tutorvirtual.ui.components.ProgresoAnimado
import cl.duoc.tutorvirtual.ui.navigation.Rutas
import cl.duoc.tutorvirtual.viewmodel.InicioUiState
import cl.duoc.tutorvirtual.viewmodel.InicioViewModel

/**
 * Panel de inicio. Cambia según el perfil con que se inició sesión:
 *  - Estudiante: alertas de plazos, avance y próxima entrega.
 *  - Profesor guía: resumen y entregas "Enviadas" que esperan aprobación.
 * Las tarjetas entran una tras otra (EntradaEscalonada).
 */
@Composable
fun InicioScreen(
    irA: (String) -> Unit,
    abrirHito: (Long) -> Unit,
    viewModel: InicioViewModel = viewModel(factory = InicioViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    if (estado.cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val esProfesor = estado.perfil.rol == Rol.PROFESOR_GUIA

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.widthIn(max = 700.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            var i = 0
            EntradaEscalonada(i++) { Encabezado(estado, esProfesor) }

            if (!estado.perfil.estaCompleto) {
                EntradaEscalonada(i++) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
                            Text("Completa tu perfil para personalizar la app.", Modifier.weight(1f).padding(horizontal = 12.dp))
                            TextButton(onClick = { irA(Rutas.PERFIL) }) { Text("Ir") }
                        }
                    }
                }
            }

            if (esProfesor) {
                EntradaEscalonada(i++) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Indicador("Por revisar", estado.porRevisar.size, R.drawable.ic_pauta, Modifier.weight(1f))
                        Indicador("Consultas", estado.resumen.consultasPendientes, R.drawable.ic_comentario, Modifier.weight(1f))
                        Indicador("Aprobados", estado.aprobados, R.drawable.ic_check, Modifier.weight(1f))
                    }
                }
                EntradaEscalonada(i++) { PorRevisar(estado, abrirHito) }
                val consultas = estado.resumen.hitos.filter { estado.resumen.conversaciones[it.id]?.esperaProfesor == true }
                if (consultas.isNotEmpty()) {
                    EntradaEscalonada(i++) {
                        TarjetaConversaciones("Comentarios por responder", consultas, abrirHito)
                    }
                }
            } else if (estado.conRespuesta.isNotEmpty()) {
                EntradaEscalonada(i++) {
                    TarjetaConversaciones("Tu profesor guía te respondió", estado.conRespuesta, abrirHito)
                }
            }

            // Alertas de plazo: aparecen con animación solo si hay hitos urgentes
            AnimatedVisibility(
                visible = estado.alertas.isNotEmpty(),
                enter = expandVertically() + fadeIn()
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(painterResource(R.drawable.ic_notificacion), contentDescription = null)
                            Text(
                                if (esProfesor) "Plazos cercanos del estudiante" else "Atención con tus plazos",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        estado.alertas.forEach { hito ->
                            TextButton(onClick = { abrirHito(hito.id) }) {
                                Text("${hito.titulo}: ${textoPlazo(hito, estado.hoy)}", Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            EntradaEscalonada(i++) {
                ProgresoAnimado(
                    progreso = estado.resumen.progreso,
                    terminados = estado.resumen.terminados,
                    total = estado.resumen.hitos.size
                )
            }

            if (!esProfesor) {
                estado.resumen.proximoHito?.let { hito ->
                    EntradaEscalonada(i++) {
                        Card(onClick = { abrirHito(hito.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Próxima entrega", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Text(hito.titulo, style = MaterialTheme.typography.titleLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    ChipEstado(hito.estado)
                                    Text("${hito.fechaLimite.format(formatoFecha)} · ${textoPlazo(hito, estado.hoy)}")
                                }
                                Text("Formato: ${hito.formatoRequerido}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            EntradaEscalonada(i++) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
                    if (esProfesor) {
                        AccesoRapido("Revisar y aprobar hitos", "Cambia el estado a \"Aprobado\"", R.drawable.ic_hito) { irA(Rutas.HITOS) }
                        AccesoRapido("Pautas y rúbricas", "Criterios de evaluación por etapa", R.drawable.ic_guia) { irA(Rutas.GUIA) }
                        AccesoRapido("Preguntarle al Sargento", "Citación, metodología y formato", R.drawable.ic_ayuda) { irA(Rutas.FAQ) }
                    } else {
                        AccesoRapido("Guía metodológica", "Paso a paso de tu tesis", R.drawable.ic_guia) { irA(Rutas.GUIA) }
                        AccesoRapido("Preguntarle al Sargento", "Tu perro guía resuelve tus dudas", R.drawable.ic_ayuda) { irA(Rutas.FAQ) }
                        AccesoRapido("Mis hitos", "Entregas y fechas límite", R.drawable.ic_hito) { irA(Rutas.HITOS) }
                    }
                }
            }
        }
    }
}

/** Encabezado con el fondo animado institucional, saludo e insignia del perfil. */
@Composable
private fun Encabezado(estado: InicioUiState, esProfesor: Boolean) {
    val nombre = estado.perfil.nombre.split(" ").firstOrNull().orEmpty()
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))) {
        FondoAnimado(Modifier.matchParentSize())
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painterResource(if (esProfesor) R.drawable.ic_supervisor else R.drawable.ic_escuela),
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)
                    )
                    Text(estado.perfil.rol.etiqueta, color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                if (nombre.isBlank()) "¡Bienvenido/a!" else "¡Hola, $nombre!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                when {
                    esProfesor -> "Supervisa el avance, aprueba hitos y evalúa con las rúbricas."
                    estado.perfil.asignatura != null -> estado.perfil.asignatura!!.etiqueta
                    else -> "Tu tutor para Trabajo de Título I, II y III."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f)
            )
        }
    }
}

/** Tarjeta pequeña con un número que "cuenta" hasta su valor al aparecer. */
@Composable
private fun Indicador(titulo: String, valor: Int, @DrawableRes icono: Int, modifier: Modifier = Modifier) {
    val animado by animateIntAsState(valor, tween(900), label = "indicador")
    Card(modifier) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icono), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Text("$animado", style = MaterialTheme.typography.headlineSmall)
            Text(titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Hitos con mensajes nuevos en su conversación (dudas o respuestas). */
@Composable
private fun TarjetaConversaciones(titulo: String, hitos: List<Hito>, abrirHito: (Long) -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.ic_comentario), contentDescription = null)
                Text(titulo, style = MaterialTheme.typography.titleMedium)
            }
            hitos.forEach { hito ->
                TextButton(onClick = { abrirHito(hito.id) }) {
                    Text(hito.titulo, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** Lista de entregas enviadas por el estudiante que el profesor debe revisar. */
@Composable
private fun PorRevisar(estado: InicioUiState, abrirHito: (Long) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Entregas por revisar", style = MaterialTheme.typography.titleMedium)
            if (estado.porRevisar.isEmpty()) {
                Text("No hay entregas pendientes de aprobación. ¡Todo al día!", style = MaterialTheme.typography.bodyMedium)
            } else {
                estado.porRevisar.forEach { hito ->
                    Surface(
                        onClick = { abrirHito(hito.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(hito.titulo, style = MaterialTheme.typography.titleMedium)
                                Text("Límite: ${hito.fechaLimite.format(formatoFecha)}", style = MaterialTheme.typography.bodyMedium)
                            }
                            ChipEstado(hito.estado)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccesoRapido(titulo: String, detalle: String, @DrawableRes icono: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(icono), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(detalle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(
                painterResource(R.drawable.ic_volver), contentDescription = null,
                modifier = Modifier.size(20.dp).rotate(180f)   // "volver" girado = flecha hacia adelante
            )
        }
    }
}
