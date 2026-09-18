package ar.com.tikaservi.app.ui.chofer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.ReservaChofer
import ar.com.tikaservi.app.data.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ChoferReservasViajeScreen(viajeId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var reservas by remember { mutableStateOf<List<ReservaChofer>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viajeId) {
        try {
            reservas = ApiClient.api.getReservasDeViaje(viajeId, session.conductorId)
            if (reservas.isEmpty()) mensaje = "Todavia no tenes reservas para este viaje"
        } catch (e: Exception) {
            mensaje = "Error al cargar: ${e.message}"
        } finally {
            cargando = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Reservas del viaje", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(Modifier.height(12.dp))

        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        mensaje?.let { Text(it, modifier = Modifier.padding(vertical = 8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reservas) { r ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(r.nombre, style = MaterialTheme.typography.titleMedium)
                        r.dni?.let { Text("DNI: $it") }
                        r.telefono?.let { Text("Telefono: $it") }
                        Text("Tipo: ${r.tipo}${r.tamano_encomienda?.let { " ($it)" } ?: ""}")
                        Text("Asientos: ${r.asientos_reservados} - Estado: ${r.estado}")
                        r.pasajero_viajes_realizados?.let { Text("Viajes anteriores del pasajero: $it") }
                        if (r.origen_deseado != null || r.destino_deseado != null) {
                            Text(
                                "Prefiere: ${r.origen_deseado ?: "-"} -> ${r.destino_deseado ?: "-"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (r.punto_retiro_lat != null && r.punto_retiro_lng != null) {
                            Spacer(Modifier.height(6.dp))
                            OutlinedButton(onClick = {
                                val uri = Uri.parse("geo:${r.punto_retiro_lat},${r.punto_retiro_lng}?q=${r.punto_retiro_lat},${r.punto_retiro_lng}(${Uri.encode(r.nombre)})")
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                } catch (e: Exception) {
                                    mensaje = "No se encontro una app de mapas instalada"
                                }
                            }) { Text("Ver punto de retiro") }
                        }
                    }
                }
            }
        }
    }
}
