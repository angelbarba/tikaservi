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
import ar.com.tikaservi.app.BuildConfig
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.TabPillRow
import kotlinx.coroutines.launch

private val TABS = listOf("Mis viajes", "Pasajeros", "Datos")

@Composable
fun ChoferHomeScreen(
    onCerrarSesion: () -> Unit,
    onVerVehiculos: () -> Unit,
    onPublicarViaje: () -> Unit,
    onVerReservas: (Int) -> Unit
) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var tab by remember { mutableStateOf(TABS[0]) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Hola, ${session.nombreConductor ?: "chofer"}",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = { session.cerrarSesion(); onCerrarSesion() }) { Text("Salir") }
        }
        Text(
            "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        TabPillRow(opciones = TABS, seleccionado = tab, onSeleccionar = { tab = it })
        Spacer(Modifier.height(12.dp))

        when (tab) {
            "Mis viajes" -> MisViajesTab(session, onVerVehiculos, onPublicarViaje, onVerReservas)
            "Pasajeros" -> ChoferPasajerosTab()
            "Datos" -> ChoferDatosTab(session)
        }
    }
}

@Composable
private fun MisViajesTab(
    session: SessionManager,
    onVerVehiculos: () -> Unit,
    onPublicarViaje: () -> Unit,
    onVerReservas: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onVerVehiculos) { Text("Mis vehiculos") }
            Button(onClick = onPublicarViaje) { Text("Publicar viaje") }
        }

        Spacer(Modifier.height(16.dp))
        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        mensaje?.let { Text(it, modifier = Modifier.padding(vertical = 8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viajes) { viaje ->
                ViajePublicadoCard(
                    viaje = viaje,
                    onCancelar = {
                        scope.launch {
                            try {
                                ApiClient.api.cancelarViaje(viaje.id, session.conductorId)
                                cargar()
                            } catch (e: Exception) {
                                mensaje = "No se pudo cancelar: ${e.message}"
                            }
                        }
                    },
                    onVerReservas = { onVerReservas(viaje.id) }
                )
            }
        }
    }
}

@Composable
private fun ViajePublicadoCard(viaje: Viaje, onCancelar: () -> Unit, onVerReservas: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${viaje.origen} - ${viaje.destino}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("${viaje.fecha} - ${viaje.hora_salida} - estado: ${viaje.estado}")
            Text("Asientos disponibles: ${viaje.asientos_disponibles}")
            viaje.reservas_confirmadas_total?.let { Text("Reservas confirmadas: $it") }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onVerReservas) { Text("Ver reservas") }
                if (viaje.estado == "activo") {
                    OutlinedButton(onClick = onCancelar) { Text("Cancelar viaje") }
                }
            }
        }
    }
}
