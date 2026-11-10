package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.ui.components.ChipEstado
import cl.duoc.tutorvirtual.ui.components.ProgresoAnimado
import cl.duoc.tutorvirtual.viewmodel.HitosViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val formatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

/** Texto amigable del plazo: "Vence en 3 días", "Vencido hace 2 días"... */
fun textoPlazo(hito: Hito, hoy: LocalDate): String {
    val dias = hito.diasRestantes(hoy)
    return when {
        hito.estado.estaTerminado -> "Entregado"
        dias < 0 -> "Vencido hace ${-dias} día(s)"
        dias == 0L -> "Vence hoy"
        dias == 1L -> "Vence mañana"
        else -> "Vence en $dias días"
    }
}

/** Lista de hitos guardados en Room, con checklist y progreso. */
@Composable
fun HitosScreen(
    nuevoHito: () -> Unit,
    editarHito: (Long) -> Unit,
    viewModel: HitosViewModel = viewModel(factory = HitosViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var hitoAEliminar by remember { mutableStateOf<Hito?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),   // el Scaffold principal ya aplica los márgenes
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = nuevoHito,
                icon = { Icon(painterResource(R.drawable.ic_agregar), contentDescription = null) },
                text = { Text("Nuevo hito") }
            )
        }
    ) { padding ->
        if (estado.cargando) {
            // Loader mientras Room entrega los datos (2.4.1 indicadores visuales)
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProgresoAnimado(
                    progreso = estado.progreso,
                    terminados = estado.terminados,
                    total = estado.hitos.size,
                    modifier = Modifier.widthIn(max = 700.dp)
                )
            }
            if (estado.hitos.isEmpty()) {
                item {
                    Text(
                        "Aún no tienes hitos. Toca \"Nuevo hito\" para agregar tu primera entrega.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
            items(estado.hitos, key = { it.id }) { hito ->
                TarjetaHito(
                    hito = hito,
                    hoy = estado.hoy,
                    alMarcar = { viewModel.marcarEntregado(hito, it) },
                    alEditar = { editarHito(hito.id) },
                    alEliminar = { hitoAEliminar = hito },
                    modifier = Modifier.widthIn(max = 700.dp).animateItem()
                )
            }
        }
    }

    // Confirmación antes de borrar (2.3.2: AlertDialog)
    hitoAEliminar?.let { hito ->
        AlertDialog(
            onDismissRequest = { hitoAEliminar = null },
            icon = { Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null) },
            title = { Text("¿Eliminar hito?") },
            text = { Text("Se eliminará \"${hito.titulo}\".") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(hito)
                    hitoAEliminar = null
                    // Snackbar con opción de deshacer
                    scope.launch {
                        val r = snackbar.showSnackbar("Hito eliminado", actionLabel = "Deshacer", withDismissAction = true)
                        if (r == SnackbarResult.ActionPerformed) viewModel.deshacerEliminar()
                    }
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { hitoAEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaHito(
    hito: Hito,
    hoy: LocalDate,
    alMarcar: (Boolean) -> Unit,
    alEditar: () -> Unit,
    alEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vencido = hito.estaVencido(hoy)
    val porVencer = hito.estaPorVencer(hoy)

    // animateDpAsState (2.4.1): los hitos urgentes se "elevan" para llamar la atención;
    // al marcarlos como entregados bajan suavemente.
    val elevacion by animateDpAsState(
        targetValue = if (vencido || porVencer) 6.dp else 1.dp,
        label = "elevacionHito"
    )

    Card(
        onClick = alEditar,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevacion)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Checklist de entrega
            Checkbox(
                checked = hito.estado.estaTerminado,
                onCheckedChange = alMarcar,
                enabled = hito.estado != EstadoHito.APROBADO
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(hito.titulo, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    ChipEstado(hito.estado)
                    Text(hito.fechaLimite.format(formatoFecha), style = MaterialTheme.typography.labelMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (vencido || porVencer) {
                        Icon(
                            painterResource(if (vencido) R.drawable.ic_error else R.drawable.ic_notificacion),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        textoPlazo(hito, hoy),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (vencido || porVencer) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = alEliminar) {
                Icon(painterResource(R.drawable.ic_eliminar), contentDescription = "Eliminar ${hito.titulo}")
            }
        }
    }
}
