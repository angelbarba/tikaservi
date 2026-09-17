package ar.com.tikaservi.app.ui.chofer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.api.LocalidadesCache
import ar.com.tikaservi.app.data.model.Vehiculo
import ar.com.tikaservi.app.data.model.ViajeRequest
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.LocalidadSelector
import ar.com.tikaservi.app.ui.common.FechaSelector
import kotlinx.coroutines.launch

private val HORAS = (0..23).map { String.format("%02d:00", it) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoferPublicarViajeScreen(
    onPublicadoExitoso: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var localidades by remember { mutableStateOf<List<String>>(emptyList()) }
    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var vehiculoSeleccionado by remember { mutableStateOf<Vehiculo?>(null) }
    var expandidoVehiculo by remember { mutableStateOf(false) }

    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("08:00") }
    var expandidoHora by remember { mutableStateOf(false) }
    var precio by remember { mutableStateOf("") }
    var paradas by remember { mutableStateOf("") }
    var aceptaEncomiendas by remember { mutableStateOf(false) }
    var asientosDisponibles by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var cargandoDatos by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            localidades = LocalidadesCache.obtener()
            vehiculos = ApiClient.api.getVehiculosDeConductor(session.conductorId)
                .filter { it.activo == 1 }
            vehiculoSeleccionado = vehiculos.firstOrNull()
        } catch (e: Exception) {
            error = "No se pudieron cargar los datos: ${e.message}"
        } finally {
            cargandoDatos = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Text("Publicar viaje", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (cargandoDatos) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        if (!cargandoDatos && vehiculos.isEmpty()) {
            Text(
                "Todavia no tenes ningun vehiculo cargado. Anda a \"Mis vehiculos\" primero.",
                color = MaterialTheme.colorScheme.error
            )
        } else if (!cargandoDatos) {
            ExposedDropdownMenuBox(
                expanded = expandidoVehiculo,
                onExpandedChange = { expandidoVehiculo = it }
            ) {
                OutlinedTextField(
                    value = vehiculoSeleccionado?.let { "${it.marca_modelo} (${it.patente})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vehiculo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoVehiculo) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandidoVehiculo, onDismissRequest = { expandidoVehiculo = false }) {
                    vehiculos.forEach { v ->
                        DropdownMenuItem(
                            text = { Text("${v.marca_modelo} (${v.patente})") },
                            onClick = { vehiculoSeleccionado = v; expandidoVehiculo = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            LocalidadSelector("Origen", localidades, origen, onSeleccion = { origen = it }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            LocalidadSelector("Destino", localidades, destino, onSeleccion = { destino = it }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            FechaSelector(etiqueta = "Fecha", valorIso = fecha, onSeleccion = { fecha = it }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))

            ExposedDropdownMenuBox(expanded = expandidoHora, onExpandedChange = { expandidoHora = it }) {
                OutlinedTextField(
                    value = hora, onValueChange = {}, readOnly = true,
                    label = { Text("Hora de salida (en punto)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoHora) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandidoHora, onDismissRequest = { expandidoHora = false }) {
                    HORAS.forEach { h ->
                        DropdownMenuItem(text = { Text(h) }, onClick = { hora = h; expandidoHora = false })
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                precio, { precio = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Precio (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                paradas, { paradas = it }, label = { Text("Paradas (opcional)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                asientosDisponibles, { asientosDisponibles = it.filter { c -> c.isDigit() } },
                label = { Text("Asientos a ofrecer (opcional, por defecto todos)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = aceptaEncomiendas, onCheckedChange = { aceptaEncomiendas = it })
                Text("Acepto encomiendas en este viaje")
            }

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val vehiculo = vehiculoSeleccionado
                    if (vehiculo == null) { error = "Elegi un vehiculo"; return@Button }
                    if (origen.isBlank() || destino.isBlank() || fecha.isBlank()) {
                        error = "Completa origen, destino y fecha"; return@Button
                    }
                    if (origen == destino) { error = "Origen y destino deben ser distintos"; return@Button }
                    cargando = true
                    error = null
                    scope.launch {
                        try {
                            val resp = ApiClient.api.publicarViaje(
                                ViajeRequest(
                                    conductor_id = session.conductorId,
                                    vehiculo_id = vehiculo.id,
                                    origen = origen,
                                    destino = destino,
                                    fecha = fecha.trim(),
                                    hora_salida = hora,
                                    precio = precio.toDoubleOrNull(),
                                    paradas = paradas.ifBlank { "" },
                                    acepta_encomiendas = aceptaEncomiendas,
                                    asientos_disponibles = asientosDisponibles.toIntOrNull()
                                )
                            )
                            if (resp.isSuccessful) {
                                onPublicadoExitoso()
                            } else {
                                error = resp.errorBody()?.string()?.take(250) ?: "Error del servidor (${resp.code()})"
                            }
                        } catch (e: Exception) {
                            error = "No se pudo conectar: ${e.message}"
                        } finally {
                            cargando = false
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                else Text("Publicar viaje")
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}
