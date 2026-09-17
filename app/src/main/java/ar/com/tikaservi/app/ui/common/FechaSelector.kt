package ar.com.tikaservi.app.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Campo de fecha con calendario real (Material3 DatePicker), en vez de un
 * campo de texto libre donde el usuario tenia que tipear "AAAA-MM-DD" a mano.
 * El valor que expone sigue siendo un String ISO (AAAA-MM-DD), que es el
 * formato que espera el backend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaSelector(
    etiqueta: String,
    valorIso: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    val estadoPicker = rememberDatePickerState(
        initialSelectedDateMillis = valorIso.takeIf { it.isNotBlank() }?.let {
            runCatching {
                java.time.LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }.getOrNull()
        }
    )

    OutlinedTextField(
        value = valorIso,
        onValueChange = {},
        readOnly = true,
        label = { Text(etiqueta) },
        placeholder = { Text("Elegir fecha") },
        trailingIcon = {
            IconButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Elegir fecha")
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    if (mostrarDialogo) {
        DatePickerDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = estadoPicker.selectedDateMillis
                    if (millis != null) {
                        val fecha = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onSeleccion(fecha.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                    mostrarDialogo = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoPicker)
        }
    }
}
