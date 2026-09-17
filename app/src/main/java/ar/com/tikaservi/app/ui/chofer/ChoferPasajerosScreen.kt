package ar.com.tikaservi.app.ui.chofer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.api.LocalidadesCache
import ar.com.tikaservi.app.data.model.SolicitudPublica
import ar.com.tikaservi.app.ui.common.LocalidadSelector
import kotlinx.coroutines.launch

@Composable
fun ChoferPasajerosTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var localidades by remember { mutableStateOf<List<String>>(emptyList()) }
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var solicitudes by remember { mutableStateOf<List<SolicitudPublica>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    fun buscar() {
        cargando = true
        mensaje = null
        scope.launch {
            try {
                solicitudes = ApiClient.api.buscarSolicitudes(
                    origen = origen.ifBlank { null },
                    destino = destino.ifBlank { null }
                )
                if (solicitudes.isEmpty()) mensaje = "No hay pasajeros pidiendo viaje con esos filtros"
            } catch (e: Exception) {
                mensaje = "Error al buscar: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try { localidades = LocalidadesCache.obtener() } catch (_: Exception) {}
        buscar()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Pasajeros pidiendo viaje", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LocalidadSelector("Origen", localidades, origen, permitirVacio = true, onSeleccion = { origen = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        LocalidadSelector("Destino", localidades, destino, permitirVacio = true, onSeleccion = { destino = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Button(onClick = { buscar() }, modifier = Modifier.fillMaxWidth()) { Text("Buscar") }

        Spacer(Modifier.height(12.dp))
        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        mensaje?.let { Text(it, modifier = Modifier.padding(vertical = 8.dp)) }

        solicitudes.forEach { s ->
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(s.pasajero_nombre, style = MaterialTheme.typography.titleMedium)
                    Text("${s.origen} - ${s.destino}")
                    Text("${s.fecha} - ${s.franjas_horarias.joinToString()}")
                    Text("Tipo: ${s.tipo}${s.tamano_encomienda?.let { " ($it)" } ?: ""}")
                    s.pasajero_viajes_realizados?.let { Text("Viajes anteriores: $it") }
                    s.comentario?.let { if (it.isNotBlank()) Text("\"$it\"") }
                    s.whatsapp_link?.let { link ->
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                        }) { Text("Contactar por WhatsApp") }
                    }
                }
            }
        }
    }
}
