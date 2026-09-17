package ar.com.tikaservi.app.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Selector de localidad por dropdown (no texto libre). El backend solo
 * acepta como origen/destino uno de los valores exactos de GET /localidades,
 * por eso la busqueda con texto libre no funcionaba antes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalidadSelector(
    etiqueta: String,
    localidades: List<String>,
    valorSeleccionado: String,
    permitirVacio: Boolean = false,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = valorSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            if (permitirVacio) {
                DropdownMenuItem(
                    text = { Text("(cualquiera)") },
                    onClick = { onSeleccion(""); expandido = false }
                )
            }
            localidades.forEach { loc ->
                DropdownMenuItem(
                    text = { Text(loc) },
                    onClick = { onSeleccion(loc); expandido = false }
                )
            }
        }
    }
}
