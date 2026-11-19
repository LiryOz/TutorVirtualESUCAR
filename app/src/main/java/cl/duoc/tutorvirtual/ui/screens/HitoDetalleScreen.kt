package cl.duoc.tutorvirtual.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.Adjunto
import cl.duoc.tutorvirtual.data.model.Comentario
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.model.TipoComentario
import cl.duoc.tutorvirtual.nativo.GestorArchivos
import cl.duoc.tutorvirtual.ui.components.ChipEstado
import cl.duoc.tutorvirtual.ui.components.EntradaEscalonada
import cl.duoc.tutorvirtual.validation.ComentarioValidator
import cl.duoc.tutorvirtual.validation.PermisosHito
import cl.duoc.tutorvirtual.viewmodel.HitoDetalleUiState
import cl.duoc.tutorvirtual.viewmodel.HitoDetalleViewModel
import cl.duoc.tutorvirtual.viewmodel.NuevoComentarioUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

private val formatoFechaHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

/**
 * Detalle de un hito, para ambos perfiles:
 *  - Estudiante: avanza el estado (sin "Aprobado"), comenta avances o dudas y adjunta archivos.
 *  - Profesor guía: aprueba, edita, elimina y responde los comentarios.
 */
@Composable
fun HitoDetalleScreen(
    hitoId: Long,
    editar: (Long) -> Unit,
    alVolver: () -> Unit,
    viewModel: HitoDetalleViewModel = viewModel(factory = HitoDetalleViewModel.factory(hitoId))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val nuevo by viewModel.nuevo.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(nuevo.eliminado) { if (nuevo.eliminado) alVolver() }
    LaunchedEffect(nuevo.mensaje) {
        nuevo.mensaje?.let { snackbar.showSnackbar(it); viewModel.mensajeMostrado() }
    }

    // Selector de documentos del sistema: cualquier tipo de archivo (PDF, Word, imagen...)
    val elegirArchivo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.adjuntando(true)
            scope.launch {
                val r = withContext(Dispatchers.IO) { GestorArchivos.copiar(context, uri) }
                viewModel.onAdjunto(r)
            }
        }
    }
    fun abrirAdjunto(adjunto: Adjunto) {
        if (!GestorArchivos.abrir(context, adjunto)) {
            scope.launch { snackbar.showSnackbar("No hay una app para abrir este archivo") }
        }
    }

    Scaffold(contentWindowInsets = WindowInsets(0), snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        val hito = estado.hito
        if (estado.cargando || hito == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (estado.cargando) CircularProgressIndicator() else Text("El hito ya no existe")
            }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 700.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EntradaEscalonada(0) { TarjetaDatos(hito, estado) }
                EntradaEscalonada(1) { TarjetaEstado(hito, estado.rol, viewModel::cambiarEstado) }

                if (estado.esProfesor) {
                    EntradaEscalonada(2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { editar(hito.id) }, modifier = Modifier.weight(1f).height(48.dp)) {
                                Icon(painterResource(R.drawable.ic_editar), contentDescription = null)
                                Text("Editar", Modifier.padding(start = 8.dp))
                            }
                            OutlinedButton(
                                onClick = { confirmarEliminar = true },
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null)
                                Text("Eliminar", Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }

                EntradaEscalonada(3) {
                    Conversacion(
                        estado = estado,
                        alAbrirAdjunto = ::abrirAdjunto,
                        alEliminar = viewModel::eliminarComentario
                    )
                }
                EntradaEscalonada(4) {
                    Redactor(
                        rol = estado.rol,
                        nuevo = nuevo,
                        alCambiarTexto = viewModel::onTextoChange,
                        alCambiarTipo = viewModel::onTipoChange,
                        alAdjuntar = { elegirArchivo.launch(arrayOf("*/*")) },
                        alQuitarAdjunto = viewModel::quitarAdjunto,
                        alAbrirAdjunto = ::abrirAdjunto,
                        alEnviar = viewModel::enviar
                    )
                }
            }
        }
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            icon = { Icon(painterResource(R.drawable.ic_eliminar), contentDescription = null) },
            title = { Text("¿Eliminar hito?") },
            text = { Text("Se eliminará el hito y toda su conversación.") },
            confirmButton = {
                TextButton(onClick = { confirmarEliminar = false; viewModel.eliminarHito() }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaDatos(hito: Hito, estado: HitoDetalleUiState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(hito.titulo, style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ChipEstado(hito.estado)
                Text(
                    "${hito.fechaLimite.format(formatoFecha)} · ${textoPlazo(hito, estado.hoy)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hito.estaVencido(estado.hoy) || hito.estaPorVencer(estado.hoy)) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(hito.descripcion, style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(painterResource(R.drawable.ic_guia), contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Formato: ${hito.formatoRequerido}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Chips de estado: cada perfil solo puede tocar los estados que le corresponden. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaEstado(hito: Hito, rol: Rol, alCambiar: (EstadoHito) -> Unit) {
    val esProfesor = rol == Rol.PROFESOR_GUIA
    Card(Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Estado del entregable", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EstadoHito.entries.forEach { opcion ->
                    val habilitado = PermisosHito.puedeCambiarEstado(rol, hito.estado, opcion)
                    FilterChip(
                        selected = hito.estado == opcion,
                        onClick = { alCambiar(opcion) },
                        enabled = habilitado || hito.estado == opcion,
                        label = { Text(opcion.etiqueta) },
                        leadingIcon = if (!habilitado && hito.estado != opcion) {
                            { Icon(painterResource(R.drawable.ic_candado), contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else if (hito.estado == opcion) {
                            { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
            when {
                esProfesor && hito.estado == EstadoHito.ENVIADO -> Button(
                    onClick = { alCambiar(EstadoHito.APROBADO) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_check), contentDescription = null)
                    Text("Aprobar entrega", Modifier.padding(start = 8.dp))
                }
                !esProfesor && hito.estado == EstadoHito.APROBADO ->
                    Text("¡Tu profesor guía aprobó este hito!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                !esProfesor ->
                    Text("Solo el profesor guía puede marcarlo como Aprobado.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Lista de mensajes estilo chat: los propios a la derecha, los del otro perfil a la izquierda. */
@Composable
private fun Conversacion(
    estado: HitoDetalleUiState,
    alAbrirAdjunto: (Adjunto) -> Unit,
    alEliminar: (Comentario) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(R.drawable.ic_comentario), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Avances y consultas", style = MaterialTheme.typography.titleLarge)
            if (estado.comentarios.isNotEmpty()) {
                Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                    Text(
                        "${estado.comentarios.size}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (estado.comentarios.isEmpty()) {
            Text(
                if (estado.esProfesor) "El estudiante aún no comenta este hito."
                else "Aún no hay comentarios. Cuéntale a tu profesor cómo vas o deja tu duda.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        estado.comentarios.forEach { c ->
            // key: cada mensaje nuevo entra con su propia animación
            key(c.id) {
                EntradaEscalonada(0) {
                    Burbuja(c, propio = c.autorRol == estado.rol, alAbrirAdjunto = alAbrirAdjunto, alEliminar = { alEliminar(c) })
                }
            }
        }
    }
}

@Composable
private fun Burbuja(comentario: Comentario, propio: Boolean, alAbrirAdjunto: (Adjunto) -> Unit, alEliminar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (propio) colores.primaryContainer else colores.surfaceContainerHigh
    val forma = if (propio) RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp) else RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
    var confirmar by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth(), contentAlignment = if (propio) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(color = fondo, shape = forma, modifier = Modifier.fillMaxWidth(0.88f)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        painterResource(if (comentario.autorRol == Rol.PROFESOR_GUIA) R.drawable.ic_supervisor else R.drawable.ic_escuela),
                        contentDescription = null, modifier = Modifier.size(18.dp)
                    )
                    Text(
                        comentario.autorNombre, style = MaterialTheme.typography.labelLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                    )
                    EtiquetaTipo(comentario.tipo)
                    Box(Modifier.weight(1f))
                    if (propio) {
                        IconButton(onClick = { confirmar = true }, modifier = Modifier.size(32.dp)) {
                            Icon(painterResource(R.drawable.ic_eliminar), contentDescription = "Eliminar comentario", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (comentario.texto.isNotBlank()) Text(comentario.texto, style = MaterialTheme.typography.bodyLarge)
                comentario.adjunto?.let { FilaAdjunto(it, alAbrir = { alAbrirAdjunto(it) }) }
                Text(
                    comentario.fechaHora.format(formatoFechaHora),
                    style = MaterialTheme.typography.labelMedium,
                    color = colores.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Eliminar comentario?") },
            confirmButton = { TextButton(onClick = { confirmar = false; alEliminar() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun EtiquetaTipo(tipo: TipoComentario) {
    val colores = MaterialTheme.colorScheme
    val (fondo, texto) = when (tipo) {
        TipoComentario.AVANCE -> colores.primary to colores.onPrimary
        TipoComentario.DUDA -> colores.tertiary to colores.onTertiary
        TipoComentario.RESPUESTA -> colores.secondary to colores.onSecondary
    }
    Text(
        tipo.etiqueta,
        color = texto,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(fondo).padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/** Archivo adjunto: al tocarlo se abre con la app que corresponda. */
@Composable
private fun FilaAdjunto(adjunto: Adjunto, alAbrir: () -> Unit, accion: (@Composable () -> Unit)? = null) {
    Surface(
        onClick = alAbrir,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(R.drawable.ic_archivo), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                adjunto.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
            )
            accion?.invoke()
        }
    }
}

/** Caja para escribir: tipo (avance/duda), texto, archivo adjunto y botón enviar. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Redactor(
    rol: Rol,
    nuevo: NuevoComentarioUiState,
    alCambiarTexto: (String) -> Unit,
    alCambiarTipo: (TipoComentario) -> Unit,
    alAdjuntar: () -> Unit,
    alQuitarAdjunto: () -> Unit,
    alAbrirAdjunto: (Adjunto) -> Unit,
    alEnviar: () -> Unit
) {
    val tipos = PermisosHito.tiposDeComentario(rol)
    Card(
        Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (rol == Rol.PROFESOR_GUIA) "Responder al estudiante" else "Nuevo comentario",
                style = MaterialTheme.typography.titleMedium
            )
            if (tipos.size > 1) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tipos.forEach { t ->
                        FilterChip(
                            selected = nuevo.tipo == t,
                            onClick = { alCambiarTipo(t) },
                            label = { Text(if (t == TipoComentario.AVANCE) "Comentar avance" else "Tengo una duda") }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = nuevo.texto,
                onValueChange = alCambiarTexto,
                placeholder = {
                    Text(
                        when (nuevo.tipo) {
                            TipoComentario.AVANCE -> "¿Qué avanzaste en este hito?"
                            TipoComentario.DUDA -> "Escribe tu duda para el profesor guía"
                            TipoComentario.RESPUESTA -> "Escribe tu respuesta u observación"
                        }
                    )
                },
                isError = nuevo.error != null,
                supportingText = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(nuevo.error ?: "", Modifier.weight(1f))
                        Text("${nuevo.texto.length}/${ComentarioValidator.MAX}")
                    }
                },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            AnimatedVisibility(
                visible = nuevo.adjunto != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                nuevo.adjunto?.let { adj ->
                    FilaAdjunto(adj, alAbrir = { alAbrirAdjunto(adj) }) {
                        IconButton(onClick = alQuitarAdjunto, modifier = Modifier.size(32.dp)) {
                            Icon(painterResource(R.drawable.ic_cerrar), contentDescription = "Quitar archivo", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = alAdjuntar, enabled = !nuevo.adjuntando) {
                    AnimatedContent(nuevo.adjuntando, label = "adjuntando") { cargando ->
                        if (cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(painterResource(R.drawable.ic_adjunto), contentDescription = null)
                    }
                    Text(if (nuevo.adjunto == null) "Adjuntar archivo" else "Cambiar archivo", Modifier.padding(start = 6.dp))
                }
                Box(Modifier.weight(1f))
                FilledIconButton(onClick = alEnviar, enabled = !nuevo.enviando, modifier = Modifier.size(52.dp)) {
                    AnimatedContent(nuevo.enviando, label = "enviando") { enviando ->
                        if (enviando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Icon(painterResource(R.drawable.ic_enviar), contentDescription = "Enviar comentario")
                    }
                }
            }
        }
    }
}
