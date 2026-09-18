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
import ar.com.tikaservi.app.data.api.LocalidadesCache
import ar.com.tikaservi.app.data.model.Viaje
import ar.com.tikaservi.app.data.model.ViajeEditRequest
import ar.com.tikaservi.app.BuildConfig
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.TabPillRow
import ar.com.tikaservi.app.ui.common.LocalidadSelector
import ar.com.tikaservi.app.ui.common.FechaSelector
import ar.com.tikaservi.app.ui.common.mensajeDeError
import ar.com.tikaservi.app.push.RegistrarPushAlEntrar
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

    RegistrarPushAlEntrar()

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
    var localidades by remember { mutableStateOf<List<String>>(emptyList()) }
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

    LaunchedEffect(Unit) {
        // P-03: la edicion necesita el mismo selector de localidades que
        // Publicar viaje (el backend solo acepta un valor exacto de esta lista).
        localidades = LocalidadesCache.obtener()
        cargar()
    }

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
                    localidades = localidades,
                    onCancelar = {
                        scope.launch {
                            try {
                                // Fix B-01: antes se ignoraba isSuccessful y se refrescaba
                                // la lista igual aunque el backend hubiera rechazado el
                                // pedido (403 ajeno, 409 ya cancelado, etc).
                                val resp = ApiClient.api.cancelarViaje(viaje.id, session.conductorId)
                                if (resp.isSuccessful) {
                                    cargar()
                                } else {
                                    mensaje = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                                }
                            } catch (e: Exception) {
                                mensaje = "No se pudo cancelar: ${e.message}"
                            }
                        }
                    },
                    onFinalizar = {
                        // P-01
                        scope.launch {
                            try {
                                val resp = ApiClient.api.finalizarViaje(viaje.id, session.conductorId)
                                if (resp.isSuccessful) {
                                    cargar()
                                } else {
                                    mensaje = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                                }
                            } catch (e: Exception) {
                                mensaje = "No se pudo finalizar: ${e.message}"
                            }
                        }
                    },
                    onReactivar = {
                        // P-02
                        scope.launch {
                            try {
                                val resp = ApiClient.api.reactivarViaje(viaje.id, session.conductorId)
                                if (resp.isSuccessful) {
                                    cargar()
                                } else {
                                    mensaje = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                                }
                            } catch (e: Exception) {
                                mensaje = "No se pudo reactivar: ${e.message}"
                            }
                        }
                    },
                    onGuardarEdicion = { origen, destino, fecha, hora, precio, paradas, asientos, onListo ->
                        // P-03
                        scope.launch {
                            try {
                                val resp = ApiClient.api.editarViaje(
                                    viaje.id,
                                    ViajeEditRequest(
                                        conductor_id = session.conductorId,
                                        origen = origen,
                                        destino = destino,
                                        fecha = fecha,
                                        hora_salida = hora,
                                        precio = precio.toDoubleOrNull(),
                                        paradas = paradas,
                                        asientos_disponibles = asientos.toIntOrNull()
                                    )
                                )
                                if (resp.isSuccessful) {
                                    mensaje = null
                                    onListo(null)
                                    cargar()
                                } else {
                                    val err = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                                    onListo(err)
                                }
                            } catch (e: Exception) {
                                onListo("No se pudo guardar: ${e.message}")
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
private fun ViajePublicadoCard(
    viaje: Viaje,
    localidades: List<String>,
    onCancelar: () -> Unit,
    onFinalizar: () -> Unit,
    onReactivar: () -> Unit,
    onGuardarEdicion: (
        origen: String,
        destino: String,
        fecha: String,
        hora: String,
        precio: String,
        paradas: String,
        asientos: String,
        onListo: (String?) -> Unit
    ) -> Unit,
    onVerReservas: () -> Unit
) {
    var editando by remember { mutableStateOf(false) }
    var origen by remember(viaje.id) { mutableStateOf(viaje.origen) }
    var destino by remember(viaje.id) { mutableStateOf(viaje.destino) }
    var fecha by remember(viaje.id) { mutableStateOf(viaje.fecha) }
    var hora by remember(viaje.id) { mutableStateOf(viaje.hora_salida) }
    var precio by remember(viaje.id) { mutableStateOf(viaje.precio?.toString() ?: "") }
    var paradas by remember(viaje.id) { mutableStateOf(viaje.paradas ?: "") }
    var asientos by remember(viaje.id) { mutableStateOf(viaje.asientos_disponibles.toString()) }
    var guardando by remember { mutableStateOf(false) }
    var errorEdicion by remember { mutableStateOf<String?>(null) }

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
                    OutlinedButton(onClick = { editando = !editando }) { Text(if (editando) "Cerrar edicion" else "Editar") }
                }
                if (viaje.estado == "activo" || viaje.estado == "en_curso") {
                    OutlinedButton(onClick = onFinalizar) { Text("Marcar como finalizado") }
                }
                if (viaje.estado == "activo") {
                    OutlinedButton(onClick = onCancelar) { Text("Cancelar viaje") }
                }
                if (viaje.estado == "cancelado") {
                    OutlinedButton(onClick = onReactivar) { Text("Dar de alta") }
                }
            }

            // P-03: panel de edicion inline, mismos campos que conductor-viajes.html
            if (editando) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LocalidadSelector(
                        etiqueta = "Origen",
                        localidades = localidades,
                        valorSeleccionado = origen,
                        onSeleccion = { origen = it }
                    )
                    LocalidadSelector(
                        etiqueta = "Destino",
                        localidades = localidades,
                        valorSeleccionado = destino,
                        onSeleccion = { destino = it }
                    )
                    FechaSelector(
                        etiqueta = "Fecha",
                        valorIso = fecha,
                        onSeleccion = { fecha = it }
                    )
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora de salida") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = paradas,
                        onValueChange = { paradas = it },
                        label = { Text("Paradas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = asientos,
                        onValueChange = { asientos = it },
                        label = { Text("Asientos disponibles") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorEdicion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = !guardando,
                            onClick = {
                                guardando = true
                                errorEdicion = null
                                onGuardarEdicion(origen, destino, fecha, hora, precio, paradas, asientos) { err ->
                                    guardando = false
                                    if (err == null) {
                                        editando = false
                                    } else {
                                        errorEdicion = err
                                    }
                                }
                            }
                        ) { Text(if (guardando) "Guardando..." else "Guardar") }
                        OutlinedButton(onClick = { editando = false; errorEdicion = null }) { Text("Cancelar") }
                    }
                }
            }
        }
    }
}
