package ar.com.tikaservi.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TikaserviVioleta = Color(0xFFC026D3)
val TikaserviVioletaOscuro = Color(0xFF86198F)
val TikaserviFondoClaro = Color(0xFFFFFBFF)
val TikaserviFondoOscuro = Color(0xFF201A1F)

private val LightColors = lightColorScheme(
    primary = TikaserviVioleta,
    onPrimary = Color.White,
    secondary = TikaserviVioletaOscuro,
    background = TikaserviFondoClaro,
    surface = TikaserviFondoClaro
)

private val DarkColors = darkColorScheme(
    primary = TikaserviVioleta,
    onPrimary = Color.White,
    secondary = Color(0xFFE9A6F1),
    background = TikaserviFondoOscuro,
    surface = TikaserviFondoOscuro
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
