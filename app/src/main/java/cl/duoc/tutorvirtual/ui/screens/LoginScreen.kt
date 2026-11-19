package cl.duoc.tutorvirtual.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.data.model.CuentasDemo
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.ui.components.FondoAnimado
import cl.duoc.tutorvirtual.ui.components.LogoAnimado
import cl.duoc.tutorvirtual.viewmodel.LoginViewModel

/**
 * Pantalla de inicio de sesión con los dos perfiles del caso 3.1.
 * Detalles visuales: fondo con burbujas animadas, logo que "respira",
 * tarjeta que entra deslizándose y se sacude si los datos son incorrectos.
 */
@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current
    var verClave by rememberSaveable { mutableStateOf(false) }

    // Entrada inicial: el encabezado y la tarjeta aparecen al abrir la pantalla
    val aparecer = remember { MutableTransitionState(false).apply { targetState = true } }

    // "Sacudida" horizontal de la tarjeta en cada intento fallido
    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(estado.intentosFallidos) {
        if (estado.intentosFallidos > 0) {
            for (x in listOf(-14f, 14f, -10f, 10f, -5f, 5f, 0f)) sacudida.animateTo(x, tween(45))
        }
    }

    Box(Modifier.fillMaxSize()) {
        FondoAnimado()

        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Encabezado ----------
            AnimatedVisibility(
                visibleState = aparecer,
                enter = fadeIn(tween(600)) + scaleIn(tween(600, easing = FastOutSlowInEasing), initialScale = 0.7f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LogoAnimado()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tutor Virtual ESUCAR",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Tu guía para el Trabajo de Título",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Tarjeta del formulario ----------
            AnimatedVisibility(
                visibleState = aparecer,
                enter = fadeIn(tween(600, delayMillis = 200)) +
                    slideInVertically(tween(700, delayMillis = 200, easing = FastOutSlowInEasing)) { it / 2 }
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .graphicsLayer { translationX = sacudida.value.dp.toPx() }
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
                        Text("¿Con qué perfil ingresas?", style = MaterialTheme.typography.bodyMedium)

                        // Selector de perfil (dos tarjetas)
                        Row(
                            Modifier.fillMaxWidth().selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TarjetaRol(
                                titulo = "Estudiante",
                                detalle = "Desarrolla tu tesis con el tutor",
                                icono = R.drawable.ic_escuela,
                                seleccionada = estado.rol == Rol.ESTUDIANTE,
                                alElegir = { viewModel.onRolChange(Rol.ESTUDIANTE) },
                                modifier = Modifier.weight(1f)
                            )
                            TarjetaRol(
                                titulo = "Profesor guía",
                                detalle = "Supervisa, aprueba y evalúa",
                                icono = R.drawable.ic_supervisor,
                                seleccionada = estado.rol == Rol.PROFESOR_GUIA,
                                alElegir = { viewModel.onRolChange(Rol.PROFESOR_GUIA) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        CampoLogin(
                            valor = estado.usuario,
                            alCambiar = viewModel::onUsuarioChange,
                            etiqueta = "Usuario",
                            icono = R.drawable.ic_persona,
                            error = estado.errorUsuario,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { foco.moveFocus(FocusDirection.Down) })
                        )
                        CampoLogin(
                            valor = estado.clave,
                            alCambiar = viewModel::onClaveChange,
                            etiqueta = "Contraseña",
                            icono = R.drawable.ic_candado,
                            error = estado.errorClave,
                            visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { foco.clearFocus(); viewModel.ingresar() }),
                            accion = {
                                IconButton(onClick = { verClave = !verClave }) {
                                    // El ícono del ojo cambia con un fundido
                                    AnimatedContent(verClave, label = "ojo") { visible ->
                                        Icon(
                                            painterResource(if (visible) R.drawable.ic_ojo_cerrado else R.drawable.ic_ojo),
                                            contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                                        )
                                    }
                                }
                            }
                        )

                        // Error general (credenciales incorrectas / perfil distinto)
                        AnimatedVisibility(
                            visible = estado.errorGeneral != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(painterResource(R.drawable.ic_error), contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Text(
                                        estado.errorGeneral.orEmpty(),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { foco.clearFocus(); viewModel.ingresar() },
                            enabled = !estado.ingresando,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            AnimatedContent(
                                estado.ingresando,
                                transitionSpec = { fadeIn(tween(200)) + scaleIn(initialScale = 0.8f) togetherWith fadeOut(tween(150)) },
                                label = "botonIngresar"
                            ) { cargando ->
                                if (cargando) {
                                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("Ingresar", style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }

                        CuentaDePrueba(rol = estado.rol, alUsar = viewModel::usarCuentaDemo)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Versión MVP · tus datos se guardan solo en este dispositivo",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Tarjeta seleccionable de perfil: cambia de color, borde y tamaño con animación. */
@Composable
private fun TarjetaRol(
    titulo: String,
    detalle: String,
    @DrawableRes icono: Int,
    seleccionada: Boolean,
    alElegir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val fondo by animateColorAsState(if (seleccionada) colores.primaryContainer else colores.surfaceContainerLow, label = "fondoRol")
    val borde by animateColorAsState(if (seleccionada) colores.primary else colores.outline.copy(alpha = 0.35f), label = "bordeRol")
    val grosor by animateDpAsState(if (seleccionada) 2.dp else 1.dp, label = "grosorRol")
    val escala by animateFloatAsState(
        if (seleccionada) 1f else 0.96f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "escalaRol"
    )
    val fondoIcono by animateColorAsState(if (seleccionada) colores.primary else colores.surfaceVariant, label = "fondoIcono")
    val colorIcono by animateColorAsState(if (seleccionada) colores.onPrimary else colores.onSurfaceVariant, label = "colorIcono")

    Column(
        modifier
            .scale(escala)
            .clip(RoundedCornerShape(20.dp))
            .border(BorderStroke(grosor, borde), RoundedCornerShape(20.dp))
            .background(fondo)
            .selectable(selected = seleccionada, onClick = alElegir, role = Role.RadioButton)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(fondoIcono),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icono), contentDescription = null, tint = colorIcono)
        }
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(detalle, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CampoLogin(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    @DrawableRes icono: Int,
    error: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    accion: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(painterResource(icono), contentDescription = null) },
        trailingIcon = accion,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { if (error != null) error(error) }
    )
}

/** Muestra la cuenta genérica del perfil elegido y permite rellenarla con un toque. */
@Composable
private fun CuentaDePrueba(rol: Rol, alUsar: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                rol,
                transitionSpec = {
                    (fadeIn(tween(250)) + slideInVertically { it / 2 }) togetherWith fadeOut(tween(150))
                },
                label = "cuentaDemo",
                modifier = Modifier.weight(1f)
            ) { r ->
                val cuenta = CuentasDemo.deRol(r)
                Column {
                    Text("Cuenta de prueba (${r.etiqueta})", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${cuenta.usuario}  ·  ${cuenta.clave}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            TextButton(onClick = alUsar) { Text("Usar") }
        }
    }
}
