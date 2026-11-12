package cl.duoc.tutorvirtual.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cl.duoc.tutorvirtual.ui.theme.VerdeAcento
import cl.duoc.tutorvirtual.ui.theme.VerdeInstitucional
import cl.duoc.tutorvirtual.ui.theme.VerdeSecundario
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.tutorvirtual.R
import cl.duoc.tutorvirtual.ui.components.RevisarRecordatoriosAlIniciar
import cl.duoc.tutorvirtual.ui.navigation.AppNavHost
import cl.duoc.tutorvirtual.ui.navigation.DestinoPrincipal
import cl.duoc.tutorvirtual.ui.navigation.Rutas
import cl.duoc.tutorvirtual.ui.navigation.navegarPrincipal
import cl.duoc.tutorvirtual.ui.navigation.tituloDeRuta

/**
 * Estructura general de la app: TopAppBar + navegación + contenido.
 * Diseño adaptable (2.2.1 Window Size Classes):
 *  - Teléfono (Compact): barra inferior NavigationBar.
 *  - Tablet (Medium/Expanded): barra lateral NavigationRail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorVirtualApp(anchoVentana: WindowWidthSizeClass, cerrarSesion: () -> Unit) {
    val navController = rememberNavController()
    var confirmarCierre by rememberSaveable { mutableStateOf(false) }
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // ¿La pantalla actual es una de las 5 principales? Si no, mostramos "volver".
    val esPrincipal = DestinoPrincipal.entries.any { it.ruta == rutaActual }
    val usarRail = anchoVentana != WindowWidthSizeClass.Compact

    // Al abrir la app: aviso de hitos próximos a vencer (notificación local)
    RevisarRecordatoriosAlIniciar()

    // Qué ítem de la barra se marca: las sub-pantallas marcan a su "padre"
    fun estaSeleccionado(destino: DestinoPrincipal) = when (rutaActual) {
        Rutas.GUIA_DETALLE -> destino == DestinoPrincipal.GUIA
        Rutas.HITO_FORM, Rutas.HITO_DETALLE -> destino == DestinoPrincipal.HITOS
        else -> destino.ruta == rutaActual
    }

    // Colores de los ítems de la barra: íconos y letras blancas sobre el verde;
    // la pestaña elegida se marca con el verde acento.
    val coloresBarra = NavigationBarItemDefaults.colors(
        selectedIconColor = VerdeSecundario,
        selectedTextColor = Color.White,
        indicatorColor = VerdeAcento,
        unselectedIconColor = Color.White,
        unselectedTextColor = Color.White
    )
    val coloresRail = NavigationRailItemDefaults.colors(
        selectedIconColor = VerdeSecundario,
        selectedTextColor = Color.White,
        indicatorColor = VerdeAcento,
        unselectedIconColor = Color.White,
        unselectedTextColor = Color.White
    )

    Row(Modifier.fillMaxSize()) {
        if (usarRail) {
            NavigationRail(containerColor = VerdeInstitucional) {
                DestinoPrincipal.entries.forEach { destino ->
                    NavigationRailItem(
                        selected = estaSeleccionado(destino),
                        onClick = { navController.navegarPrincipal(destino.ruta) },
                        icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                        label = { Text(destino.etiqueta) },
                        colors = coloresRail
                    )
                }
            }
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(tituloDeRuta(rutaActual)) },
                    navigationIcon = {
                        if (!esPrincipal) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(painterResource(R.drawable.ic_volver), contentDescription = "Volver")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { confirmarCierre = true }) {
                            Icon(painterResource(R.drawable.ic_salir), contentDescription = "Cerrar sesión")
                        }
                    },
                    // Barra superior verde con el título y la flecha "volver" en blanco
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VerdeInstitucional,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                if (!usarRail) {
                    NavigationBar(containerColor = VerdeInstitucional) {
                        DestinoPrincipal.entries.forEach { destino ->
                            NavigationBarItem(
                                selected = estaSeleccionado(destino),
                                onClick = { navController.navegarPrincipal(destino.ruta) },
                                icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                                label = { Text(destino.etiqueta) },
                                colors = coloresBarra
                            )
                        }
                    }
                }
            }
        ) { padding ->
            AppNavHost(navController, pedirCierreSesion = { confirmarCierre = true }, Modifier.padding(padding))
        }
    }

    // Confirmación antes de cerrar la sesión
    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            icon = { Icon(painterResource(R.drawable.ic_salir), contentDescription = null) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus hitos y tu perfil quedan guardados en este dispositivo.") },
            confirmButton = {
                TextButton(onClick = { confirmarCierre = false; cerrarSesion() }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierre = false }) { Text("Cancelar") }
            }
        )
    }
}
