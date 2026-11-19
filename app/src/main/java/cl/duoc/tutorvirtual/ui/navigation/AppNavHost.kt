package cl.duoc.tutorvirtual.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.tutorvirtual.ui.screens.FaqScreen
import cl.duoc.tutorvirtual.ui.screens.GuiaDetalleScreen
import cl.duoc.tutorvirtual.ui.screens.GuiaScreen
import cl.duoc.tutorvirtual.ui.screens.HitoDetalleScreen
import cl.duoc.tutorvirtual.ui.screens.HitoFormScreen
import cl.duoc.tutorvirtual.ui.screens.HitosScreen
import cl.duoc.tutorvirtual.ui.screens.InicioScreen
import cl.duoc.tutorvirtual.ui.screens.PerfilScreen
import cl.duoc.tutorvirtual.ui.screens.SargentoScreen

/**
 * NavHost = el "escenario" donde se muestra la pantalla actual (2.2.3).
 * Cada composable(ruta) registra una pantalla.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    pedirCierreSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.INICIO,
        modifier = modifier,
        // Transiciones entre pantallas (2.4.1): la nueva entra deslizándose y aparece suavemente
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 8 } },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 8 } },
        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 8 } }
    ) {
        composable(Rutas.INICIO) {
            InicioScreen(
                irA = { ruta -> navController.navegarPrincipal(ruta) },
                abrirHito = { id -> navController.navigate(Rutas.hitoDetalle(id)) }
            )
        }
        composable(Rutas.GUIA) {
            GuiaScreen(abrirEtapa = { id -> navController.navigate(Rutas.guiaDetalle(id)) })
        }
        composable(
            Rutas.GUIA_DETALLE,
            arguments = listOf(navArgument("etapaId") { type = NavType.IntType })
        ) { entrada ->
            GuiaDetalleScreen(
                etapaId = entrada.arguments?.getInt("etapaId") ?: 1,
                // Al pasar a otra etapa reemplazamos la actual, así "volver" lleva a la lista
                irAEtapa = { id ->
                    navController.navigate(Rutas.guiaDetalle(id)) {
                        popUpTo(Rutas.GUIA)
                    }
                }
            )
        }
        // Pestaña "Sargento": chat con preguntas y respuestas del banco de dudas
        composable(Rutas.FAQ) {
            SargentoScreen(verBancoPreguntas = { navController.navigate(Rutas.FAQ_LISTA) })
        }
        composable(Rutas.FAQ_LISTA) { FaqScreen() }
        composable(Rutas.HITOS) {
            HitosScreen(
                nuevoHito = { navController.navigate(Rutas.hitoForm()) },
                abrirHito = { id -> navController.navigate(Rutas.hitoDetalle(id)) }
            )
        }
        // Detalle: estado, conversación y archivos (ambos perfiles)
        composable(
            Rutas.HITO_DETALLE,
            arguments = listOf(navArgument("hitoId") { type = NavType.LongType })
        ) { entrada ->
            HitoDetalleScreen(
                hitoId = entrada.arguments?.getLong("hitoId") ?: -1L,
                editar = { id -> navController.navigate(Rutas.hitoForm(id)) },
                alVolver = { navController.popBackStack() }
            )
        }
        composable(
            Rutas.HITO_FORM,
            arguments = listOf(navArgument("hitoId") {
                type = NavType.LongType
                defaultValue = -1L   // -1 significa "hito nuevo"
            })
        ) { entrada ->
            val id = entrada.arguments?.getLong("hitoId") ?: -1L
            HitoFormScreen(
                hitoId = id.takeIf { it > 0 },
                alTerminar = { navController.popBackStack() }
            )
        }
        composable(Rutas.PERFIL) { PerfilScreen(cerrarSesion = pedirCierreSesion) }
    }
}

/**
 * Navega a un destino de la barra sin apilar pantallas repetidas:
 * vuelve al inicio de la pila y recuerda el estado de cada pestaña.
 */
fun NavHostController.navegarPrincipal(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
