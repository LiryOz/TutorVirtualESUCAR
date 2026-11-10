package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.ui.components.ChipEstado
import cl.duoc.tutorvirtual.ui.components.ProgresoAnimado
import cl.duoc.tutorvirtual.ui.navigation.Rutas
import cl.duoc.tutorvirtual.viewmodel.InicioViewModel

/** Panel de inicio: saludo, alertas de plazos, avance, próximo hito y accesos rápidos. */
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

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.widthIn(max = 700.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Saludo (jerarquía visual: lo más importante arriba y más grande)
            val nombre = estado.perfil.nombre.split(" ").firstOrNull().orEmpty()
            Text(
                if (nombre.isBlank()) "¡Bienvenido/a!" else "¡Hola, $nombre!",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                when {
                    estado.perfil.rol == Rol.PROFESOR_GUIA -> "Modo profesor guía: puedes aprobar hitos."
                    estado.perfil.asignatura != null -> estado.perfil.asignatura!!.etiqueta
                    else -> "Tu tutor para Trabajo de Título I, II y III."
                },
                style = MaterialTheme.typography.bodyLarge
            )

            if (!estado.perfil.estaCompleto) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_persona), contentDescription = null)
                        Text("Completa tu perfil para personalizar la app.", Modifier.weight(1f).padding(horizontal = 12.dp))
                        TextButton(onClick = { irA(Rutas.PERFIL) }) { Text("Ir") }
                    }
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
                            Text("Atención con tus plazos", style = MaterialTheme.typography.titleMedium)
                        }
                        estado.alertas.forEach { hito ->
                            TextButton(onClick = { abrirHito(hito.id) }) {
                                Text("${hito.titulo}: ${textoPlazo(hito, estado.hoy)}", Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            ProgresoAnimado(
                progreso = estado.resumen.progreso,
                terminados = estado.resumen.terminados,
                total = estado.resumen.hitos.size
            )

            estado.resumen.proximoHito?.let { hito ->
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

            Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
            AccesoRapido("Guía metodológica paso a paso", R.drawable.ic_guia) { irA(Rutas.GUIA) }
            AccesoRapido("Buscar una duda frecuente", R.drawable.ic_buscar) { irA(Rutas.FAQ) }
            AccesoRapido("Revisar mis hitos", R.drawable.ic_hito) { irA(Rutas.HITOS) }
        }
    }
}

@Composable
private fun AccesoRapido(texto: String, icono: Int, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(painterResource(icono), contentDescription = null)
        Text(texto, Modifier.weight(1f).padding(start = 12.dp))
    }
}
