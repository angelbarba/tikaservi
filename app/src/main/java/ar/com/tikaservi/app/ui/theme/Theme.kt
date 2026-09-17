package ar.com.tikaservi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta calcada del portal web real (pasajero-viajes.html / conductor-viajes.html):
// blanco y negro, pastillas grises para lo no seleccionado.
val TikaserviNegro = Color(0xFF111111)
val TikaserviGrisClaro = Color(0xFFF1F1F1)
val TikaserviGrisMedio = Color(0xFFE3E3E3)
val TikaserviFondoClaro = Color(0xFFFAFAFA)
val TikaserviFondoOscuro = Color(0xFF121212)

private val LightColors = lightColorScheme(
    primary = TikaserviNegro,
    onPrimary = Color.White,
    secondary = TikaserviGrisMedio,
    onSecondary = TikaserviNegro,
    background = TikaserviFondoClaro,
    surface = Color.White,
    surfaceVariant = TikaserviGrisClaro
)

private val DarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = TikaserviNegro,
    secondary = Color(0xFF3A3A3A),
    onSecondary = Color.White,
    background = TikaserviFondoOscuro,
    surface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFF2A2A2A)
)

@Composable
fun TikaserviTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
