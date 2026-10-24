package cl.duoc.tutorvirtual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TutorVirtualApp()
            }
        }
    }
}

@Composable
fun TutorVirtualApp() {
    var pantalla by remember { mutableStateOf("Inicio") }

    BackHandler(enabled = pantalla != "Inicio") {
        pantalla = "Inicio"
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 3.dp) {
                Text(
                    text = "Tutor Virtual ESUCAR",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pantalla == "Inicio") {
                Text(
                    "Bienvenido al Tutor Virtual",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text("Apoyo para Trabajo de Título I, II y III.")

                listOf(
                    "Guía metodológica",
                    "Preguntas frecuentes",
                    "Mis avances",
                    "Mi perfil"
                ).forEach { destino ->
                    Button(
                        onClick = { pantalla = destino },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(destino)
                    }
                }
            } else {
                Text(
                    pantalla,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text("Módulo preparado para próximas actividades.")

                OutlinedButton(
                    onClick = { pantalla = "Inicio" }
                ) {
                    Text("Volver al inicio")
                }
            }
        }
    }
}