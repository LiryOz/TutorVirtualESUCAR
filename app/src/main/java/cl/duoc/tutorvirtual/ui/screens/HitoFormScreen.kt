package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.ui.components.CampoValidado
import cl.duoc.tutorvirtual.ui.components.MensajeError
import cl.duoc.tutorvirtual.ui.components.SelectorFoto
import cl.duoc.tutorvirtual.validation.HitoValidator
import cl.duoc.tutorvirtual.viewmodel.HitoFormViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Formulario para crear o editar un hito. */
@Composable
fun HitoFormScreen(
    hitoId: Long?,
    alTerminar: () -> Unit,
    viewModel: HitoFormViewModel = viewModel(factory = HitoFormViewModel.factory(hitoId))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    // Al guardar con éxito volvemos a la lista
    LaunchedEffect(estado.guardado) { if (estado.guardado) alTerminar() }
    // Mensajes del ViewModel → Snackbar
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 700.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (estado.esEdicion) "Editar hito" else "Nuevo hito",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Los campos con * son obligatorios.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CampoValidado(
                    valor = estado.titulo,
                    alCambiar = viewModel::onTituloChange,
                    etiqueta = "Título del hito *",
                    error = estado.errores.titulo,
                    icono = R.drawable.ic_hito,
                    ayuda = "Ej: Avance 2: Marco teórico",
                    maxCaracteres = HitoValidator.TITULO_MAX
                )
                CampoValidado(
                    valor = estado.descripcion,
                    alCambiar = viewModel::onDescripcionChange,
                    etiqueta = "Descripción *",
                    error = estado.errores.descripcion,
                    icono = R.drawable.ic_pauta,
                    ayuda = "Qué se debe entregar",
                    maxCaracteres = HitoValidator.DESCRIPCION_MAX,
                    unaLinea = false
                )
                CampoValidado(
                    valor = estado.formato,
                    alCambiar = viewModel::onFormatoChange,
                    etiqueta = "Formato requerido *",
                    error = estado.errores.formato,
                    icono = R.drawable.ic_guia,
                    ayuda = "Ej: PDF, norma APA 7"
                )

                CampoFecha(
                    fecha = estado.fechaLimite,
                    error = estado.errores.fecha,
                    alTocar = { mostrarCalendario = true }
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SelectorEstado(
                    seleccionado = estado.estado,
                    error = estado.errores.estado,
                    alElegir = viewModel::onEstadoChange
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                // Evidencia del avance: foto con cámara o desde galería (recursos nativos)
                SelectorFoto(
                    ruta = estado.fotoEvidencia,
                    alCambiar = viewModel::onFotoChange,
                    titulo = "Foto de evidencia (opcional)"
                )

                Spacer8()
                // Botón con animación: el texto cambia a un loader mientras guarda
                Button(
                    onClick = viewModel::guardar,
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    AnimatedContent(targetState = estado.guardando, label = "botonGuardar") { guardando ->
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(painterResource(R.drawable.ic_check), contentDescription = null)
                                Text(if (estado.esEdicion) "Guardar cambios" else "Crear hito")
                            }
                        }
                    }
                }
                TextButton(onClick = alTerminar, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
            }
        }
    }

    if (mostrarCalendario) {
        SelectorFecha(
            inicial = estado.fechaLimite,
            alElegir = { viewModel.onFechaChange(it); mostrarCalendario = false },
            alCerrar = { mostrarCalendario = false }
        )
    }
}

@Composable
private fun Spacer8() = Box(Modifier.height(8.dp))

/** Campo de solo lectura que abre el calendario al tocarlo. */
@Composable
private fun CampoFecha(fecha: LocalDate?, error: String?, alTocar: () -> Unit) {
    Box {
        OutlinedTextField(
            value = fecha?.format(formatoFecha) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha límite *") },
            isError = error != null,
            leadingIcon = { Icon(painterResource(R.drawable.ic_calendario), contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(painterResource(R.drawable.ic_error), contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error)
                else if (fecha != null) Icon(painterResource(R.drawable.ic_ok), contentDescription = "Campo correcto", tint = MaterialTheme.colorScheme.primary)
            },
            supportingText = { Text(error ?: "Toca para elegir en el calendario") },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente encima para que todo el campo responda al toque
        Box(
            Modifier
                .matchParentSize()
                .clickable(role = Role.Button, onClickLabel = "Elegir fecha", onClick = alTocar)
        )
    }
}

/** Calendario Material 3 (2.3.1: DatePicker). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(inicial: LocalDate?, alElegir: (LocalDate) -> Unit, alCerrar: () -> Unit) {
    // El DatePicker trabaja en milisegundos UTC; convertimos desde/hacia LocalDate
    val estadoCalendario = rememberDatePickerState(
        initialSelectedDateMillis = (inicial ?: LocalDate.now())
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = alCerrar,
        confirmButton = {
            TextButton(onClick = {
                estadoCalendario.selectedDateMillis?.let { ms ->
                    alElegir(Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate())
                } ?: alCerrar()
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    ) {
        DatePicker(state = estadoCalendario)
    }
}

/** Grupo de RadioButton: solo se puede elegir un estado (2.3.1). */
@Composable
private fun SelectorEstado(seleccionado: EstadoHito, error: String?, alElegir: (EstadoHito) -> Unit) {
    Text("Estado del entregable *", style = MaterialTheme.typography.titleMedium)
    Column(Modifier.selectableGroup()) {
        EstadoHito.entries.forEach { opcion ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = opcion == seleccionado,
                        onClick = { alElegir(opcion) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = opcion == seleccionado, onClick = null)
                Text(
                    opcion.etiqueta + if (opcion == EstadoHito.APROBADO) " (solo profesor guía)" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
    AnimatedVisibility(visible = error != null) { MensajeError(error) }
}
