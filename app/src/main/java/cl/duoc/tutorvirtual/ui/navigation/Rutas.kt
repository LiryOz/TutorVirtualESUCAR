package cl.duoc.tutorvirtual.ui.navigation

import androidx.annotation.DrawableRes
import cl.duoc.tutorvirtual.R

/**
 * Rutas (direcciones) de cada pantalla. El NavController navega usando estos textos
 * (2.2.3: "Rutas / Destinos").
 */
object Rutas {
    const val INICIO = "inicio"
    const val GUIA = "guia"
    const val GUIA_DETALLE = "guia/{etapaId}"
    const val FAQ = "faq"
    const val HITOS = "hitos"
    const val HITO_FORM = "hito_form?hitoId={hitoId}"
    const val HITO_DETALLE = "hito/{hitoId}"
    const val PERFIL = "perfil"

    fun guiaDetalle(etapaId: Int) = "guia/$etapaId"

    fun hitoDetalle(hitoId: Long) = "hito/$hitoId"

    /** Sin id = crear un hito nuevo; con id = editar uno existente. */
    fun hitoForm(hitoId: Long? = null) =
        if (hitoId == null) "hito_form" else "hito_form?hitoId=$hitoId"
}

/** Los 5 destinos de primer nivel que aparecen en la barra de navegación. */
enum class DestinoPrincipal(val ruta: String, val etiqueta: String, @DrawableRes val icono: Int) {
    INICIO(Rutas.INICIO, "Inicio", R.drawable.ic_home),
    GUIA(Rutas.GUIA, "Guía", R.drawable.ic_guia),
    FAQ(Rutas.FAQ, "Dudas", R.drawable.ic_ayuda),
    HITOS(Rutas.HITOS, "Hitos", R.drawable.ic_hito),
    PERFIL(Rutas.PERFIL, "Perfil", R.drawable.ic_persona)
}

/** Título que se muestra en la TopAppBar según la ruta actual. */
fun tituloDeRuta(ruta: String?): String = when (ruta) {
    Rutas.GUIA -> "Guía metodológica"
    Rutas.GUIA_DETALLE -> "Etapa de la guía"
    Rutas.FAQ -> "Preguntas frecuentes"
    Rutas.HITOS -> "Mis hitos"
    Rutas.HITO_FORM -> "Hito"
    Rutas.HITO_DETALLE -> "Detalle del hito"
    Rutas.PERFIL -> "Mi perfil"
    else -> "Tutor Virtual ESUCAR"
}
