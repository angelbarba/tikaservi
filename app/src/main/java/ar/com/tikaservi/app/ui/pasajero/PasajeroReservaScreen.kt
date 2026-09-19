package ar.com.tikaservi.app.ui.pasajero

import ar.com.tikaservi.app.ui.common.mensajeDeError

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.api.LocalidadesCache
import ar.com.tikaservi.app.data.model.ReservaRequest
import ar.com.tikaservi.app.data.model.Viaje
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.LocalidadSelector
import ar.com.tikaservi.app.ui.common.MapaPuntoRetiroDialog
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

    // Fix geoposicion: antes el punto de retiro solo se podia cargar DESPUES
    // de reservar (boton aparte en "Mis reservas"), nunca desde aca. Ahora se
    // puede marcar la ubicacion actual en el momento de reservar el asiento
    // o pedir la encomienda, y viaja en el mismo POST /reservas.
    var puntoRetiroLat by remember { mutableStateOf<Double?>(null) }
    var puntoRetiroLng by remember { mutableStateOf<Double?>(null) }
    var errorUbicacion by remember { mutableStateOf<String?>(null) }

    var mostrarMapaPuntoRetiro by remember { mutableStateOf(false) }

    // Fix geoposicion (pedido explicito): dar las dos opciones, geolocalizar
    // automatico O elegir a mano en un mapa - igual que el picker Leaflet de
    // la web. Pedimos el permiso de ubicacion (para el intento automatico
    // dentro del mapa) y despues abrimos el dialogo con el mapa.
    val permisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> mostrarMapaPuntoRetiro = true }

    fun abrirSelectorDeUbicacion() {
        errorUbicacion = null
        val concedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (concedido) mostrarMapaPuntoRetiro = true else permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var viaje by remember { mutableStateOf<Viaje?>(null) }
    var cargandoViaje by remember { mutableStateOf(true) }
    var esEncomienda by remember { mutableStateOf(false) }
    var asientos by remember { mutableStateOf("1") }
    var tamanoEncomienda by remember { mutableStateOf("chico") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var exito by remember { mutableStateOf<String?>(null) }
    var localidades by remember { mutableStateOf<List<String>>(emptyList()) }
    // P-09: dejar subir/bajar en una parada intermedia (distinta al origen/destino del viaje).
    var origenDeseado by remember { mutableStateOf("") }
    var destinoDeseado by remember { mutableStateOf("") }

    LaunchedEffect(viajeId) {
        try {
            viaje = ApiClient.api.getViaje(viajeId)
        } catch (e: Exception) {
            error = "No se pudo cargar el viaje: ${e.message}"
        } finally {
            cargandoViaje = false
        }
        try { localidades = LocalidadesCache.obtener() } catch (_: Exception) {}
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

            Spacer(Modifier.height(12.dp))
            Text(
                "Si subis o bajas en una parada intermedia (no en ${v.origen} ni en ${v.destino}), indicalo aca:",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(4.dp))
            LocalidadSelector(
                "Donde te subis (opcional)", localidades, origenDeseado,
                permitirVacio = true, onSeleccion = { origenDeseado = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            LocalidadSelector(
                "Donde te bajas (opcional)", localidades, destinoDeseado,
                permitirVacio = true, onSeleccion = { destinoDeseado = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            Text("Punto de retiro (opcional)", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            if (puntoRetiroLat != null && puntoRetiroLng != null) {
                Text(
                    "Ubicacion marcada: %.5f, %.5f".format(puntoRetiroLat, puntoRetiroLng),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
            }
            OutlinedButton(
                onClick = { abrirSelectorDeUbicacion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (puntoRetiroLat == null) "Geolocalizar / elegir en el mapa" else "Cambiar ubicacion en el mapa")
            }
            errorUbicacion?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (mostrarMapaPuntoRetiro) {
                MapaPuntoRetiroDialog(
                    latInicial = puntoRetiroLat,
                    lngInicial = puntoRetiroLng,
                    onConfirmar = { lat, lng ->
                        puntoRetiroLat = lat
                        puntoRetiroLng = lng
                        mostrarMapaPuntoRetiro = false
                    },
                    onCancelar = { mostrarMapaPuntoRetiro = false }
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
                                    tamano_encomienda = if (esEncomienda) tamanoEncomienda else null,
                                    origen_deseado = origenDeseado.ifBlank { null },
                                    destino_deseado = destinoDeseado.ifBlank { null },
                                    punto_retiro_lat = puntoRetiroLat,
                                    punto_retiro_lng = puntoRetiroLng
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
