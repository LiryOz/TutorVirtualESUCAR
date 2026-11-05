package cl.duoc.tutorvirtual.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.tutorvirtual.R
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
fun TutorVirtualApp(anchoVentana: WindowWidthSizeClass) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // ¿La pantalla actual es una de las 5 principales? Si no, mostramos "volver".
    val esPrincipal = DestinoPrincipal.entries.any { it.ruta == rutaActual }
    val usarRail = anchoVentana != WindowWidthSizeClass.Compact

    // Qué ítem de la barra se marca: las sub-pantallas marcan a su "padre"
    fun estaSeleccionado(destino: DestinoPrincipal) = when (rutaActual) {
        Rutas.GUIA_DETALLE -> destino == DestinoPrincipal.GUIA
        Rutas.HITO_FORM -> destino == DestinoPrincipal.HITOS
        else -> destino.ruta == rutaActual
    }

    Row(Modifier.fillMaxSize()) {
        if (usarRail) {
            NavigationRail {
                DestinoPrincipal.entries.forEach { destino ->
                    NavigationRailItem(
                        selected = estaSeleccionado(destino),
                        onClick = { navController.navegarPrincipal(destino.ruta) },
                        icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                        label = { Text(destino.etiqueta) }
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                if (!usarRail) {
                    NavigationBar {
                        DestinoPrincipal.entries.forEach { destino ->
                            NavigationBarItem(
                                selected = estaSeleccionado(destino),
                                onClick = { navController.navegarPrincipal(destino.ruta) },
                                icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                                label = { Text(destino.etiqueta) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            AppNavHost(navController, Modifier.padding(padding))
        }
    }
}
