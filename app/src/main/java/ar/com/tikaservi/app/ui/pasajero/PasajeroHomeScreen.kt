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
import ar.com.tikaservi.app.data.model.Viaje
import ar.com.tikaservi.app.data.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun PasajeroHomeScreen(onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var viajes by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun buscar() {
        cargando = true
        mensaje = null
        scope.launch {
            try {
                viajes = ApiClient.api.buscarViajes(
                    origen = origen.ifBlank { null },
                    destino = destino.ifBlank { null }
                )
                if (viajes.isEmpty()) mensaje = "No hay viajes activos con esos filtros"
            } catch (e: Exception) {
                mensaje = "Error al buscar: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { buscar() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Hola, ${session.nombrePasajero ?: "pasajero"}",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { session.cerrarSesion(); onCerrarSesion() }) {
                Text("Salir")
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = origen,
            onValueChange = { origen = it },
            label = { Text("Origen") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = destino,
            onValueChange = { destino = it },
            label = { Text("Destino") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { buscar() }, modifier = Modifier.fillMaxWidth()) {
            Text("Buscar viajes")
        }

        Spacer(Modifier.height(16.dp))

        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        mensaje?.let {
            Text(it, modifier = Modifier.padding(vertical = 8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viajes) { viaje -> ViajeCard(viaje) }
        }
    }
}

@Composable
private fun ViajeCard(viaje: Viaje) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${viaje.origen} - ${viaje.destino}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("${viaje.fecha} - ${viaje.hora_salida}")
            viaje.precio?.let { Text("$ $it") }
            Text("Asientos disponibles: ${viaje.asientos_disponibles}")
            viaje.nombre_conductor?.let { Text("Conductor: $it") }
        }
    }
}
