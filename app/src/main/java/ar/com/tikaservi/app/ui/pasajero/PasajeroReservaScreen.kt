package ar.com.tikaservi.app.ui.pasajero

import ar.com.tikaservi.app.ui.common.mensajeDeError

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
import ar.com.tikaservi.app.data.model.ReservaRequest
import ar.com.tikaservi.app.data.model.Viaje
import ar.com.tikaservi.app.data.session.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasajeroReservaScreen(
    viajeId: Int,
    onReservaExitosa: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var viaje by remember { mutableStateOf<Viaje?>(null) }
    var cargandoViaje by remember { mutableStateOf(true) }
    var esEncomienda by remember { mutableStateOf(false) }
    var asientos by remember { mutableStateOf("1") }
    var tamanoEncomienda by remember { mutableStateOf("chico") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viajeId) {
        try {
            viaje = ApiClient.api.getViaje(viajeId)
        } catch (e: Exception) {
            error = "No se pudo cargar el viaje: ${e.message}"
        } finally {
            cargandoViaje = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Detalle del viaje", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (cargandoViaje) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        viaje?.let { v ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${v.origen} - ${v.destino}", style = MaterialTheme.typography.titleMedium)
                    Text("${v.fecha} - ${v.hora_salida}")
                    v.precio?.let { Text("Precio: $ $it") }
                    Text("Asientos disponibles: ${v.asientos_disponibles}")
                    v.conductor_nombre?.let { Text("Conductor: $it") }
                    v.marca_modelo?.let { Text("Vehiculo: $it${v.patente?.let { p -> " ($p)" } ?: ""}") }
                    if (v.acepta_encomiendas == 1) Text("Este viaje acepta encomiendas")
                }
            }

            Spacer(Modifier.height(20.dp))

            if (v.acepta_encomiendas == 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = esEncomienda, onCheckedChange = { esEncomienda = it })
                    Text("Quiero reservar una encomienda (no un asiento)")
                }
                Spacer(Modifier.height(8.dp))
            }

            if (esEncomienda) {
                Text("Tamano de la encomienda", style = MaterialTheme.typography.labelLarge)
                Row {
                    listOf("chico", "mediano", "grande").forEach { opcion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            RadioButton(
                                selected = tamanoEncomienda == opcion,
                                onClick = { tamanoEncomienda = opcion }
                            )
                            Text(opcion)
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = asientos,
                    onValueChange = { asientos = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad de asientos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            exito?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (!session.haySesionPasajero) {
                        error = "Iniciá sesión como pasajero para reservar"
                        return@Button
                    }
                    cargando = true
                    error = null
                    scope.launch {
                        try {
                            val resp = ApiClient.api.crearReserva(
                                ReservaRequest(
                                    viaje_id = viajeId,
                                    pasajero_id = session.pasajeroId,
                                    asientos = asientos.toIntOrNull() ?: 1,
                                    tipo = if (esEncomienda) "encomienda" else "pasajero",
                                    tamano_encomienda = if (esEncomienda) tamanoEncomienda else null
                                )
                            )
                            if (resp.isSuccessful && resp.body() != null) {
                                exito = "Reserva confirmada (n° ${resp.body()!!.reserva_id})"
                                onReservaExitosa()
                            } else {
                                error = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
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
                else Text(if (esEncomienda) "Reservar encomienda" else "Reservar asiento")
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}
