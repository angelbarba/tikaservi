package ar.com.tikaservi.app.ui.pasajero

import ar.com.tikaservi.app.ui.common.mensajeDeError

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.api.LocalidadesCache
import ar.com.tikaservi.app.data.model.*
import ar.com.tikaservi.app.BuildConfig
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.LocalidadSelector
import ar.com.tikaservi.app.ui.common.FechaSelector
import ar.com.tikaservi.app.ui.common.TabPillRow
import ar.com.tikaservi.app.push.RegistrarPushAlEntrar
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val TABS = listOf("Buscar viaje", "Solicitar viaje", "Mis reservas", "Historial", "Datos")
private val HORAS = (0..23).map { String.format("%02d:00", it) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasajeroHomeScreen(
    onCerrarSesion: () -> Unit,
    onVerViaje: (Int) -> Unit
) {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    var tab by remember { mutableStateOf(TABS[0]) }
    var localidades by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        try { localidades = LocalidadesCache.obtener() } catch (_: Exception) {}
    }
    RegistrarPushAlEntrar()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Hola, ${session.nombrePasajero ?: "pasajero"}",
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
            "Buscar viaje" -> BuscarViajeTab(localidades, onVerViaje)
            "Solicitar viaje" -> SolicitarViajeTab(localidades, session)
            "Mis reservas" -> MisReservasTab(session, soloHistorial = false)
            "Historial" -> MisReservasTab(session, soloHistorial = true)
            "Datos" -> PasajeroDatosTab(session)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuscarViajeTab(localidades: List<String>, onVerViaje: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var tipoEncomienda by remember { mutableStateOf(false) }
    var cantidad by remember { mutableStateOf(1) }
    var expandidoCantidad by remember { mutableStateOf(false) }
    var viajes by remember { mutableStateOf<List<Viaje>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val hoy = remember { LocalDate.now() }
    val fmt = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    fun buscar() {
        cargando = true
        mensaje = null
        scope.launch {
            try {
                var resultado = ApiClient.api.buscarViajes(
                    origen = origen.ifBlank { null },
                    destino = destino.ifBlank { null },
                    fecha = fecha.ifBlank { null },
                    tipo = if (tipoEncomienda) "encomienda" else "viaje"
                )
                if (!tipoEncomienda) resultado = resultado.filter { it.asientos_disponibles >= cantidad }
                viajes = resultado
                if (viajes.isEmpty()) mensaje = "No hay viajes activos con esos filtros"
            } catch (e: Exception) {
                mensaje = "Error al buscar: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { buscar() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Viaje", "Encomienda").forEach { opcion ->
                val activo = (opcion == "Encomienda") == tipoEncomienda
                if (activo) {
                    Button(onClick = { tipoEncomienda = opcion == "Encomienda" }) { Text(opcion) }
                } else {
                    OutlinedButton(onClick = { tipoEncomienda = opcion == "Encomienda" }) { Text(opcion) }
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        LocalidadSelector("Origen", localidades, origen, permitirVacio = true, onSeleccion = { origen = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        LocalidadSelector("Destino", localidades, destino, permitirVacio = true, onSeleccion = { destino = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { fecha = hoy.format(fmt) }) { Text("Hoy") }
            OutlinedButton(onClick = { fecha = hoy.plusDays(1).format(fmt) }) { Text("Manana") }
            OutlinedButton(onClick = { fecha = "" }) { Text("Cualquier fecha") }
        }
        Spacer(Modifier.height(8.dp))
        FechaSelector(
            etiqueta = "Fecha (opcional)",
            valorIso = fecha,
            onSeleccion = { fecha = it },
            modifier = Modifier.fillMaxWidth()
        )
        if (!tipoEncomienda) {
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expandidoCantidad, onExpandedChange = { expandidoCantidad = it }) {
                OutlinedTextField(
                    value = "$cantidad pasajero${if (cantidad > 1) "s" else ""}",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Cantidad de pasajeros") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCantidad) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandidoCantidad, onDismissRequest = { expandidoCantidad = false }) {
                    (1..4).forEach { n ->
                        DropdownMenuItem(text = { Text("$n") }, onClick = { cantidad = n; expandidoCantidad = false })
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = { buscar() }, modifier = Modifier.fillMaxWidth()) { Text("Buscar") }

        Spacer(Modifier.height(16.dp))
        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        mensaje?.let { Text(it, modifier = Modifier.padding(vertical = 8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viajes) { viaje -> ViajeCard(viaje, onClick = { onVerViaje(viaje.id) }) }
        }
    }
}

@Composable
private fun ViajeCard(viaje: Viaje, onClick: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${viaje.origen} - ${viaje.destino}", style = MaterialTheme.typography.titleMedium)
            Text("${viaje.fecha} - ${viaje.hora_salida}")
            viaje.precio?.let { Text("$ $it") }
            Text("Asientos disponibles: ${viaje.asientos_disponibles}")
            viaje.conductor_nombre?.let { Text("Conductor: $it") }
            if (viaje.acepta_encomiendas == 1) Text("Acepta encomiendas")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SolicitarViajeTab(localidades: List<String>, session: SessionManager) {
    val scope = rememberCoroutineScope()
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("08:00") }
    var expandidoHora by remember { mutableStateOf(false) }
    var comentario by remember { mutableStateOf("") }
    var tipoEncomienda by remember { mutableStateOf(false) }
    var asientos by remember { mutableStateOf("1") }
    var tamanoEncomienda by remember { mutableStateOf("chico") }
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    var misSolicitudes by remember { mutableStateOf<List<Solicitud>>(emptyList()) }
    var cargandoLista by remember { mutableStateOf(true) }

    fun cargarSolicitudes() {
        cargandoLista = true
        scope.launch {
            try {
                misSolicitudes = ApiClient.api.getSolicitudesDePasajero(session.pasajeroId)
            } catch (e: Exception) {
                mensaje = "Error al cargar tus solicitudes: ${e.message}"
            } finally {
                cargandoLista = false
            }
        }
    }
    LaunchedEffect(Unit) { cargarSolicitudes() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Pedir un viaje", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Si no encontras un viaje publicado, dejá tu pedido: los choferes lo ven y te contactan por WhatsApp.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Viaje", "Encomienda").forEach { opcion ->
                val activo = (opcion == "Encomienda") == tipoEncomienda
                if (activo) Button(onClick = { tipoEncomienda = opcion == "Encomienda" }) { Text(opcion) }
                else OutlinedButton(onClick = { tipoEncomienda = opcion == "Encomienda" }) { Text(opcion) }
            }
        }
        Spacer(Modifier.height(10.dp))
        LocalidadSelector("Origen", localidades, origen, onSeleccion = { origen = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        LocalidadSelector("Destino", localidades, destino, onSeleccion = { destino = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        FechaSelector(etiqueta = "Fecha", valorIso = fecha, onSeleccion = { fecha = it }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expandidoHora, onExpandedChange = { expandidoHora = it }) {
            OutlinedTextField(
                value = hora, onValueChange = {}, readOnly = true,
                label = { Text("Franja horaria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoHora) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandidoHora, onDismissRequest = { expandidoHora = false }) {
                HORAS.forEach { h -> DropdownMenuItem(text = { Text(h) }, onClick = { hora = h; expandidoHora = false }) }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (tipoEncomienda) {
            Text("Tamano de la encomienda", style = MaterialTheme.typography.labelLarge)
            Row {
                listOf("chico", "mediano", "grande").forEach { opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        RadioButton(selected = tamanoEncomienda == opcion, onClick = { tamanoEncomienda = opcion })
                        Text(opcion)
                    }
                }
            }
        } else {
            OutlinedTextField(
                asientos, { asientos = it.filter { c -> c.isDigit() } },
                label = { Text("Cantidad de pasajeros (1 a 4)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            comentario, { comentario = it }, label = { Text("Comentario (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        mensaje?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (origen.isBlank() || destino.isBlank() || fecha.isBlank()) {
                    error = "Completa origen, destino y fecha"; return@Button
                }
                enviando = true; error = null
                scope.launch {
                    try {
                        val resp = ApiClient.api.crearSolicitud(
                            SolicitudRequest(
                                pasajero_id = session.pasajeroId,
                                origen = origen, destino = destino, fecha = fecha.trim(),
                                franjas_horarias = listOf(hora),
                                comentario = comentario.ifBlank { null },
                                tipo = if (tipoEncomienda) "encomienda" else "viaje",
                                asientos = if (!tipoEncomienda) asientos.toIntOrNull() ?: 1 else null,
                                tamano_encomienda = if (tipoEncomienda) tamanoEncomienda else null
                            )
                        )
                        if (resp.isSuccessful) {
                            mensaje = "Solicitud enviada"
                            cargarSolicitudes()
                        } else {
                            error = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                        }
                    } catch (e: Exception) {
                        error = "No se pudo conectar: ${e.message}"
                    } finally {
                        enviando = false
                    }
                }
            },
            enabled = !enviando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (enviando) CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            else Text("Enviar solicitud")
        }

        Spacer(Modifier.height(20.dp))
        Text("Mis solicitudes", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (cargandoLista) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        misSolicitudes.forEach { s ->
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${s.origen} - ${s.destino}", style = MaterialTheme.typography.titleSmall)
                    Text("${s.fecha} - ${s.franjas_horarias.joinToString()}")
                    Text("Tipo: ${s.tipo}${s.tamano_encomienda?.let { " ($it)" } ?: ""} - Estado: ${s.estado}")
                    s.comentario?.let { if (it.isNotBlank()) Text(it) }
                    if (s.estado == "activa") {
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = {
                            scope.launch {
                                try {
                                    // Fix B-01: antes se ignoraba isSuccessful.
                                    val resp = ApiClient.api.cancelarSolicitud(s.id, CancelarSolicitudRequest(session.pasajeroId))
                                    if (resp.isSuccessful) {
                                        cargarSolicitudes()
                                    } else {
                                        mensaje = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                                    }
                                } catch (e: Exception) {
                                    mensaje = "No se pudo cancelar: ${e.message}"
                                }
                            }
                        }) { Text("Cancelar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun MisReservasTab(session: SessionManager, soloHistorial: Boolean) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
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
                    val todas = ApiClient.api.getReservasDePasajero(session.pasajeroId, token)
                    reservas = if (soloHistorial) {
                        todas.filter { it.estado == "cancelada" || it.viaje_estado in listOf("finalizado", "cancelado") }
                    } else {
                        todas.filter { it.estado == "confirmada" && it.viaje_estado !in listOf("finalizado", "cancelado") }
                    }
                    if (reservas.isEmpty()) {
                        mensaje = if (soloHistorial) "Todavia no tenes historial" else "Todavia no hiciste ninguna reserva"
                    }
                }
            } catch (e: Exception) {
                mensaje = "Error al cargar: ${e.message}"
            } finally {
                cargando = false
            }
        }
    }
    LaunchedEffect(soloHistorial) { cargar() }

    Column(modifier = Modifier.fillMaxSize()) {
        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        mensaje?.let { Text(it, modifier = Modifier.padding(vertical = 8.dp)) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reservas) { reserva ->
                ReservaCard(
                    reserva = reserva,
                    soloHistorial = soloHistorial,
                    session = session,
                    context = context,
                    onCancelar = {
                        scope.launch {
                            try {
                                // Fix B-01: antes se ignoraba isSuccessful.
                                val resp = ApiClient.api.cancelarReserva(reserva.id, ReservaCancelarRequest(session.pasajeroId))
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
                    onGuardarAsientos = { nuevaCantidad, onListo ->
                        // P-07
                        scope.launch {
                            try {
                                val resp = ApiClient.api.editarReserva(
                                    reserva.id,
                                    ReservaEditRequest(pasajero_id = session.pasajeroId, asientos = nuevaCantidad)
                                )
                                if (resp.isSuccessful) {
                                    onListo(null)
                                    cargar()
                                } else {
                                    onListo(mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})"))
                                }
                            } catch (e: Exception) {
                                onListo("No se pudo guardar: ${e.message}")
                            }
                        }
                    },
                    onGuardarPuntoRetiro = { lat, lng, onListo ->
                        // P-08
                        scope.launch {
                            try {
                                val resp = ApiClient.api.actualizarPuntoRetiro(
                                    reserva.id,
                                    PuntoRetiroRequest(pasajero_id = session.pasajeroId, lat = lat, lng = lng)
                                )
                                if (resp.isSuccessful) {
                                    onListo(null)
                                    cargar()
                                } else {
                                    onListo(mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})"))
                                }
                            } catch (e: Exception) {
                                onListo("No se pudo guardar: ${e.message}")
                            }
                        }
                    }
                )
            }
        }
    }
}

// P-08: sin dependencia de Google Play Services; usa el LocationManager del sistema.
private fun obtenerUbicacionActual(
    context: android.content.Context,
    onResultado: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    val proveedor = when {
        lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }
    if (proveedor == null) {
        onError("Activa la ubicacion del dispositivo para marcar el punto de retiro")
        return
    }
    try {
        lm.requestSingleUpdate(proveedor, object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onResultado(location.latitude, location.longitude)
            }
        }, Looper.getMainLooper())
    } catch (e: SecurityException) {
        onError("Sin permiso de ubicacion")
    }
}

@Composable
private fun ReservaCard(
    reserva: ReservaPasajero,
    soloHistorial: Boolean,
    session: SessionManager,
    context: android.content.Context,
    onCancelar: () -> Unit,
    onGuardarAsientos: (Int, (String?) -> Unit) -> Unit,
    onGuardarPuntoRetiro: (Double, Double, (String?) -> Unit) -> Unit
) {
    var editandoAsientos by remember { mutableStateOf(false) }
    var nuevaCantidad by remember(reserva.id) { mutableStateOf(reserva.asientos_reservados.toString()) }
    var guardandoAsientos by remember { mutableStateOf(false) }
    var buscandoUbicacion by remember { mutableStateOf(false) }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    val permisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            buscandoUbicacion = true
            obtenerUbicacionActual(
                context,
                onResultado = { lat, lng ->
                    onGuardarPuntoRetiro(lat, lng) { err ->
                        buscandoUbicacion = false
                        errorLocal = err
                    }
                },
                onError = { err -> buscandoUbicacion = false; errorLocal = err }
            )
        } else {
            errorLocal = "Sin permiso de ubicacion no se puede marcar el punto de retiro"
        }
    }

    fun pedirUbicacionYGuardar() {
        errorLocal = null
        val concedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (concedido) {
            buscandoUbicacion = true
            obtenerUbicacionActual(
                context,
                onResultado = { lat, lng ->
                    onGuardarPuntoRetiro(lat, lng) { err ->
                        buscandoUbicacion = false
                        errorLocal = err
                    }
                },
                onError = { err -> buscandoUbicacion = false; errorLocal = err }
            )
        } else {
            permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val puedeModificar = !soloHistorial && reserva.estado == "confirmada" && reserva.viaje_estado == "activo"

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${reserva.origen} - ${reserva.destino}", style = MaterialTheme.typography.titleMedium)
            Text("${reserva.fecha} - ${reserva.hora_salida}")
            Text("Tipo: ${reserva.tipo}${reserva.tamano_encomienda?.let { " ($it)" } ?: ""}")
            if (reserva.tipo == "pasajero") Text("Asientos reservados: ${reserva.asientos_reservados}")
            Text("Estado reserva: ${reserva.estado} - Estado viaje: ${reserva.viaje_estado}")
            reserva.conductor_nombre?.let { Text("Conductor: $it") }
            if (reserva.origen_deseado != null || reserva.destino_deseado != null) {
                Text(
                    "Reservaste desde: ${reserva.origen_deseado ?: reserva.origen} -> ${reserva.destino_deseado ?: reserva.destino}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (reserva.punto_retiro_lat != null && reserva.punto_retiro_lng != null) {
                Text("Punto de retiro guardado", style = MaterialTheme.typography.bodySmall)
            }

            errorLocal?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            if (!soloHistorial && reserva.estado == "confirmada") {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (reserva.punto_retiro_lat != null && reserva.punto_retiro_lng != null) {
                        OutlinedButton(onClick = {
                            val uri = Uri.parse("geo:${reserva.punto_retiro_lat},${reserva.punto_retiro_lng}?q=${reserva.punto_retiro_lat},${reserva.punto_retiro_lng}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }) { Text("Ver punto de retiro") }
                    }
                    OutlinedButton(enabled = !buscandoUbicacion, onClick = { pedirUbicacionYGuardar() }) {
                        Text(
                            when {
                                buscandoUbicacion -> "Obteniendo ubicacion..."
                                reserva.punto_retiro_lat != null -> "Actualizar punto de retiro"
                                else -> "Marcar punto de retiro (mi ubicacion)"
                            }
                        )
                    }
                }
            }

            if (puedeModificar && reserva.tipo == "pasajero") {
                Spacer(Modifier.height(8.dp))
                if (editandoAsientos) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nuevaCantidad,
                            onValueChange = { nuevaCantidad = it.filter { c -> c.isDigit() } },
                            label = { Text("Asientos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(110.dp),
                            singleLine = true
                        )
                        Button(
                            enabled = !guardandoAsientos,
                            onClick = {
                                val cantidad = nuevaCantidad.toIntOrNull()
                                if (cantidad == null || cantidad < 1) {
                                    errorLocal = "Cantidad invalida"
                                    return@Button
                                }
                                guardandoAsientos = true
                                errorLocal = null
                                onGuardarAsientos(cantidad) { err ->
                                    guardandoAsientos = false
                                    if (err == null) editandoAsientos = false else errorLocal = err
                                }
                            }
                        ) { Text(if (guardandoAsientos) "Guardando..." else "Guardar") }
                        TextButton(onClick = { editandoAsientos = false; errorLocal = null }) { Text("Cancelar") }
                    }
                } else {
                    OutlinedButton(onClick = { editandoAsientos = true }) { Text("Editar asientos") }
                }
            }

            if (!soloHistorial && reserva.estado == "confirmada") {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onCancelar) { Text("Cancelar reserva") }
            }
        }
    }
}

@Composable
private fun PasajeroDatosTab(session: SessionManager) {
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordActual by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    // P-11: antes la pestana arrancaba siempre vacia.
    LaunchedEffect(Unit) {
        try {
            val token = session.pasajeroToken
            if (!token.isNullOrBlank()) {
                val perfil = ApiClient.api.getPasajero(session.pasajeroId, token)
                nombre = perfil.nombre
                dni = perfil.dni ?: ""
                telefono = perfil.telefono ?: ""
                email = perfil.email ?: ""
                usuario = perfil.usuario ?: ""
            }
        } catch (e: Exception) {
            error = "No se pudieron cargar tus datos: ${e.message}"
        } finally {
            cargando = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Mis datos", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "Deja en blanco lo que no quieras cambiar. Necesitas tu contrasena actual para confirmar.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(dni, { dni = it }, label = { Text("DNI (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(telefono, { telefono = it }, label = { Text("Telefono (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(usuario, { usuario = it }, label = { Text("Usuario (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            passwordNueva, { passwordNueva = it }, label = { Text("Nueva contrasena (opcional)") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            passwordActual, { passwordActual = it; error = null }, label = { Text("Tu contrasena actual") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.error) }
        mensaje?.let { Spacer(Modifier.height(8.dp)); Text(it, color = MaterialTheme.colorScheme.primary) }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (passwordActual.isBlank()) { error = "Ingresa tu contrasena actual"; return@Button }
                guardando = true; error = null
                scope.launch {
                    try {
                        val resp = ApiClient.api.editarPasajero(
                            session.pasajeroId,
                            PasajeroEditRequest(
                                password_actual = passwordActual,
                                nombre = nombre.ifBlank { null },
                                dni = dni.ifBlank { null },
                                telefono = telefono.ifBlank { null },
                                email = email.ifBlank { null },
                                usuario = usuario.ifBlank { null },
                                password = passwordNueva.ifBlank { null }
                            )
                        )
                        if (resp.isSuccessful) {
                            mensaje = "Datos actualizados"
                            if (nombre.isNotBlank()) session.nombrePasajero = nombre
                            passwordActual = ""; passwordNueva = ""
                        } else {
                            error = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                        }
                    } catch (e: Exception) {
                        error = "No se pudo conectar: ${e.message}"
                    } finally {
                        guardando = false
                    }
                }
            },
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (guardando) CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            else Text("Guardar cambios")
        }
    }
}
