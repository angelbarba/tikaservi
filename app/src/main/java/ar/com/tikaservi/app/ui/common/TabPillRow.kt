package ar.com.tikaservi.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Fila de pastillas de navegacion tipo el portal web (Buscar viaje / Solicitar
 * viaje / Mis reservas / Historial / Datos), con la seleccionada en negro y
 * las demas en gris claro.
 */
@Composable
fun TabPillRow(
    opciones: List<String>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(opciones) { opcion ->
            val activa = opcion == seleccionado
            val fondo = if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val texto = if (activa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = opcion,
                color = texto,
                fontWeight = if (activa) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .background(fondo, RoundedCornerShape(20.dp))
                    .clickable { onSeleccionar(opcion) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}
