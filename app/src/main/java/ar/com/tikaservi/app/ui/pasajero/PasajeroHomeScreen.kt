package ar.com.tikaservi.app.ui.pasajero

import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.api.LocalidadesCache
import ar.com.tikaservi.app.data.model.Viaje
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.LocalidadSelector
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasajeroHomeScreen(
    onCerrarSesion: () -> Unit,
    onVerViaje: (Int) -> Unit,
    onVerMisReservas: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var localidades by remember { mutableStateOf<List<String>>(emptyList()) }
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var tipoEncomienda by remember { mutableStateOf(false) }
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
                    destino = destino.ifBlank { null },
                    fecha = fecha.ifBlank { null },
                    tipo = if (tipoEncomienda) "encomienda" else "viaje"
                )
                if (viajes.isEmpty()) mensaje = "No hay viajes activos con esos filtros"
            } catch (e: Exception) {
                mensaje = "Error al buscar: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            localidades = LocalidadesCache.obtener()
        } catch (e: Exception) {
            mensaje = "No se pudo cargar la lista de localidades: ${e.message}"
        }
        buscar()
    }

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

        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onVerMisReservas) { Text("Mis reservas") }

        Spacer(Modifier.height(8.dp))

        LocalidadSelector(
            etiqueta = "Origen",
            localidades = localidades,
            valorSeleccionado = origen,
            permitirVacio = true,
            onSeleccion = { origen = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        LocalidadSelector(
            etiqueta = "Destino",
            localidades = localidades,
            valorSeleccionado = destino,
            permitirVacio = true,
            onSeleccion = { destino = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha (AAAA-MM-DD, opcional)") },
            placeholder = { Text("2026-09-20") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = tipoEncomienda, onCheckedChange = { tipoEncomienda = it })
            Text("Buscar encomiendas en vez de pasajes")
        }
        Spacer(Modifier.height(8.dp))
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
            items(viajes) { viaje -> ViajeCard(viaje, onClick = { onVerViaje(viaje.id) }) }
        }
    }
}

@Composable
private fun ViajeCard(viaje: Viaje, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${viaje.origen} - ${viaje.destino}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("${viaje.fecha} - ${viaje.hora_salida}")
            viaje.precio?.let { Text("$ $it") }
            Text("Asientos disponibles: ${viaje.asientos_disponibles}")
            viaje.conductor_nombre?.let { Text("Conductor: $it") }
            if (viaje.acepta_encomiendas == 1) Text("Acepta encomiendas")
        }
    }
}
