package cl.duoc.tutorvirtual.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.tutorvirtual.ui.navigation.DestinoPrincipal

// Pantallas temporales de la Etapa 2: se reemplazan en las etapas siguientes.

@Composable
private fun EnConstruccion(titulo: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Text("Módulo en construcción.")
    }
}

@Composable
fun InicioScreen(irA: (String) -> Unit, abrirHito: (Long) -> Unit) {
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DestinoPrincipal.entries.drop(1).forEach { destino ->
            Button(onClick = { irA(destino.ruta) }, modifier = Modifier.fillMaxWidth()) {
                Text(destino.etiqueta)
            }
        }
    }
}

@Composable
fun GuiaScreen(abrirEtapa: (Int) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
        GuiaMetodologicaScreen()
    }
}

@Composable fun GuiaDetalleScreen(etapaId: Int) = EnConstruccion("Etapa $etapaId")
@Composable fun FaqScreen() = EnConstruccion("Preguntas frecuentes")
@Composable fun HitosScreen(nuevoHito: () -> Unit, editarHito: (Long) -> Unit) = EnConstruccion("Mis hitos")
@Composable fun HitoFormScreen(hitoId: Long?, alTerminar: () -> Unit) = EnConstruccion("Formulario de hito")
@Composable fun PerfilScreen() = EnConstruccion("Mi perfil")
