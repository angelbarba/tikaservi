package ar.com.tikaservi.app.ui.chofer

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
fun ChoferHomeScreen(onCerrarSesion: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var viajes by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun cargar() {
        cargando = true
        mensaje = null
        scope.launch {
            try {
                viajes = ApiClient.api.getViajesDeConductor(session.conductorId)
                if (viajes.isEmpty()) mensaje = "Todavia no publicaste ningun viaje"
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
            Text(
                "Hola, ${session.nombreConductor ?: "chofer"}",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { session.cerrarSesion(); onCerrarSesion() }) {
                Text("Salir")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Mis viajes publicados", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        mensaje?.let {
            Text(it, modifier = Modifier.padding(vertical = 8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viajes) { viaje -> ViajePublicadoCard(viaje) }
        }
    }
}

@Composable
private fun ViajePublicadoCard(viaje: Viaje) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${viaje.origen} - ${viaje.destino}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("${viaje.fecha} - ${viaje.hora_salida} - estado: ${viaje.estado}")
            Text("Asientos disponibles: ${viaje.asientos_disponibles}")
        }
    }
}
