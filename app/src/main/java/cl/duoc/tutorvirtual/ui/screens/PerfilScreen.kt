package cl.duoc.tutorvirtual.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import cl.duoc.tutorvirtual.nativo.Notificador
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.Asignatura
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.ui.components.CampoValidado
import cl.duoc.tutorvirtual.ui.components.MensajeError
import cl.duoc.tutorvirtual.ui.components.SelectorFoto
import cl.duoc.tutorvirtual.viewmodel.PerfilViewModel

/** Perfil del usuario (formulario validado) + ajustes de la app. */
@Composable
fun PerfilScreen(viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val ajustes by viewModel.ajustes.collectAsStateWithLifecycle()
    val foto by viewModel.foto.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Al activar recordatorios en Android 13+ se pide el permiso de notificaciones
    val pedirPermisoNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        viewModel.cambiarRecordatorios(concedido)
        if (!concedido) viewModel.avisar("Sin permiso de notificaciones no podemos recordarte los plazos")
    }
    fun alCambiarRecordatorios(activo: Boolean) {
        if (activo && !Notificador.tienePermiso(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirPermisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.cambiarRecordatorios(activo)
        }
    }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { snackbar.showSnackbar(it); viewModel.mensajeMostrado() }
    }

    Scaffold(contentWindowInsets = WindowInsets(0), snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.widthIn(max = 700.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ---------- Foto de perfil (cámara / galería) ----------
                Card(Modifier.fillMaxWidth()) {
                    SelectorFoto(
                        ruta = foto,
                        alCambiar = viewModel::guardarFoto,
                        titulo = "Foto de perfil",
                        circular = true,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // ---------- Datos personales ----------
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Mis datos", style = MaterialTheme.typography.titleLarge)

                        Text("Soy", style = MaterialTheme.typography.titleMedium)
                        GrupoRadio(
                            opciones = Rol.entries,
                            seleccionada = estado.rol,
                            etiqueta = { it.etiqueta },
                            alElegir = viewModel::onRolChange
                        )

                        CampoValidado(
                            valor = estado.nombre, alCambiar = viewModel::onNombreChange,
                            etiqueta = "Nombre completo *", error = estado.errores.nombre,
                            icono = R.drawable.ic_persona, maxCaracteres = 60
                        )
                        CampoValidado(
                            valor = estado.rut, alCambiar = viewModel::onRutChange,
                            etiqueta = "RUT *", error = estado.errores.rut,
                            icono = R.drawable.ic_pauta, ayuda = "Sin puntos, con guion: 12345678-5"
                        )
                        CampoValidado(
                            valor = estado.correo, alCambiar = viewModel::onCorreoChange,
                            etiqueta = "Correo *", error = estado.errores.correo,
                            icono = R.drawable.ic_correo,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        // Campos que solo aplican al estudiante (dependencia entre campos)
                        AnimatedVisibility(visible = estado.rol == Rol.ESTUDIANTE) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                CampoValidado(
                                    valor = estado.profesorGuia, alCambiar = viewModel::onProfesorGuiaChange,
                                    etiqueta = "Profesor guía *", error = estado.errores.profesorGuia,
                                    icono = R.drawable.ic_persona
                                )
                                Text("Asignatura *", style = MaterialTheme.typography.titleMedium)
                                GrupoRadio(
                                    opciones = Asignatura.entries,
                                    seleccionada = estado.asignatura,
                                    etiqueta = { it.etiqueta },
                                    alElegir = viewModel::onAsignaturaChange
                                )
                                MensajeError(estado.errores.asignatura)
                            }
                        }

                        Button(
                            onClick = viewModel::guardar,
                            enabled = !estado.guardando,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp)
                        ) {
                            AnimatedContent(estado.guardando, label = "botonPerfil") { guardando ->
                                if (guardando) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                else Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(R.drawable.ic_check), contentDescription = null)
                                    Text("Guardar perfil")
                                }
                            }
                        }
                    }
                }

                // ---------- Ajustes (DataStore) ----------
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ajustes", style = MaterialTheme.typography.titleLarge)
                        FilaSwitch(
                            titulo = "Recordatorios de fechas límite",
                            detalle = "Aviso cuando un hito vence en 3 días o menos",
                            activo = ajustes.recordatorios,
                            alCambiar = ::alCambiarRecordatorios
                        )
                        HorizontalDivider()
                        FilaSwitch(
                            titulo = "Texto grande",
                            detalle = "Aumenta el tamaño de la letra en toda la app",
                            activo = ajustes.textoGrande,
                            alCambiar = viewModel::cambiarTextoGrande
                        )
                    }
                }
            }
        }
    }
}

/** Grupo de RadioButton genérico (sirve para Rol y Asignatura). */
@Composable
private fun <T> GrupoRadio(
    opciones: List<T>,
    seleccionada: T?,
    etiqueta: (T) -> String,
    alElegir: (T) -> Unit
) {
    Column(Modifier.selectableGroup()) {
        opciones.forEach { opcion ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = opcion == seleccionada, onClick = { alElegir(opcion) }, role = Role.RadioButton)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = opcion == seleccionada, onClick = null)
                Text(etiqueta(opcion), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

/** Fila con texto y Switch; toda la fila es tocable (mejor accesibilidad). */
@Composable
private fun FilaSwitch(titulo: String, detalle: String, activo: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = activo, onValueChange = alCambiar, role = Role.Switch)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(detalle, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = activo, onCheckedChange = null)
    }
}
