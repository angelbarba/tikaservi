package ar.com.tikaservi.app.ui.pasajero

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.ReservaCancelarRequest
import ar.com.tikaservi.app.data.model.ReservaPasajero
import ar.com.tikaservi.app.data.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun PasajeroMisReservasScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var reservas by remember { mutableStateOf<List<ReservaPasajero>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun cargar() {
        cargando = true
        mensaje = null
        scope.launch {
            try {
                val token = session.pasajeroToken
                if (token.isNullOrBlank()) {
                    mensaje = "Sesion invalida"
                } else {
                    reservas = ApiClient.api.getReservasDePasajero(session.pasajeroId, token)
                    if (reservas.isEmpty()) mensaje = "Todavia no hiciste ninguna reserva"
                }
            } catch (e: Exception) {
                mensaje = "Error al cargar: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargar() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Mis reservas", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(Modifier.height(12.dp))

        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        mensaje?.let { Text(it, modifier = Modifier.padding(vertical = 8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reservas) { reserva ->
                ReservaCard(
                    reserva = reserva,
                    onCancelar = {
                        scope.launch {
                            try {
                                ApiClient.api.cancelarReserva(
                                    reserva.id, ReservaCancelarRequest(session.pasajeroId)
                                )
                                cargar()
                            } catch (e: Exception) {
                                mensaje = "No se pudo cancelar: ${e.message}"
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ReservaCard(reserva: ReservaPasajero, onCancelar: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${reserva.origen} - ${reserva.destino}", style = MaterialTheme.typography.titleMedium)
            Text("${reserva.fecha} - ${reserva.hora_salida}")
            Text("Tipo: ${reserva.tipo}${reserva.tamano_encomienda?.let { " ($it)" } ?: ""}")
            if (reserva.tipo == "pasajero") Text("Asientos reservados: ${reserva.asientos_reservados}")
            Text("Estado reserva: ${reserva.estado} - Estado viaje: ${reserva.viaje_estado}")
            reserva.conductor_nombre?.let { Text("Conductor: $it") }
            if (reserva.estado == "confirmada") {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onCancelar) { Text("Cancelar reserva") }
            }
        }
    }
}
