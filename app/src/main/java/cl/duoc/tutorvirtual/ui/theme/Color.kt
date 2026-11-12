package cl.duoc.tutorvirtual.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta institucional (supuesto: verde y blanco). Todos los pares texto/fondo
// tienen contraste alto, como pide el caso en "Accesibilidad".

// Verdes institucionales
val VerdeInstitucional = Color(0xFF1D7D5F)   // barras de navegación y color principal
val VerdeSecundario = Color(0xFF1D5139)
val VerdeAcento = Color(0xFF8AAA45)          // marca la pestaña seleccionada

// Modo normal
val VerdeClaro = Color(0xFFC8E6C9)
val VerdeOscuro = Color(0xFF0A2E0C)
val Dorado = Color(0xFF7A5900)
val DoradoClaro = Color(0xFFFFE08A)
val TextoOscuro = Color(0xFF505050)          // color de todas las letras "negras"
val GrisBorde = Color(0xFF5A5A5A)
val RojoError = Color(0xFFB3261E)

// Modo oscuro
val VerdeNocturno = Color(0xFF8FD694)
val FondoOscuro = Color(0xFF101410)
val SuperficieOscura = Color(0xFF1C211C)
val TextoClaro = Color(0xFFF2F2F2)
val RojoErrorClaro = Color(0xFFFFB4AB)

// Colores para el estado de cada hito (se animan al cambiar, ver ChipEstado)
val EstadoPorIniciar = Color(0xFF616161)
val EstadoEnDesarrollo = Color(0xFF8A5A00)
val EstadoEnviado = Color(0xFF0D47A1)
val EstadoAprobado = VerdeSecundario
