package cl.duoc.tutorvirtual.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cl.duoc.tutorvirtual.R

/**
 * Campo de texto con retroalimentación visual por campo (rúbrica IE 2.1.2):
 *  - Ícono a la izquierda que indica qué se pide.
 *  - Con error: borde rojo, ícono de error y mensaje debajo.
 *  - Correcto y con texto: ícono verde de "ok".
 * El campo NO valida nada: solo muestra el [error] que le entrega el ViewModel.
 */
@Composable
fun CampoValidado(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String?,
    modifier: Modifier = Modifier,
    @DrawableRes icono: Int? = null,
    ayuda: String? = null,
    maxCaracteres: Int? = null,
    unaLinea: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val conError = error != null
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        isError = conError,
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        keyboardOptions = keyboardOptions,
        leadingIcon = icono?.let { { Icon(painterResource(it), contentDescription = null) } },
        trailingIcon = {
            when {
                conError -> Icon(
                    painterResource(R.drawable.ic_error),
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
                valor.isNotBlank() -> Icon(
                    painterResource(R.drawable.ic_ok),
                    contentDescription = "Campo correcto",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        supportingText = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // AnimatedContent: el texto de ayuda cambia suavemente al mensaje de error
                AnimatedContent(targetState = error ?: ayuda ?: "", label = "mensajeCampo", modifier = Modifier.weight(1f)) { texto ->
                    Text(texto)
                }
                if (maxCaracteres != null) Text("${valor.length}/$maxCaracteres")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (error != null) error(error) }   // lector de pantalla anuncia el error
    )
}

/** Mensaje de error para controles que no son campos de texto (radios, fecha, foto). */
@Composable
fun MensajeError(error: String?) {
    if (error == null) return
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp)
        )
        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}
