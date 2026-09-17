package ar.com.tikaservi.app.ui.chofer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.Vehiculo
import ar.com.tikaservi.app.data.model.VehiculoRequest
import ar.com.tikaservi.app.data.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ChoferVehiculosScreen(onVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var vehiculos by remember { mutableStateOf<List<Vehiculo>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    var mostrarForm by remember { mutableStateOf(false) }
    var marcaModelo by remember { mutableStateOf("") }
    var patente by remember { mutableStateOf("") }
    var asientos by remember { mutableStateOf("4") }
    var errorForm by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    fun cargar() {
        cargando = true
        scope.launch {
            try {
                vehiculos = ApiClient.api.getVehiculosDeConductor(session.conductorId)
                mensaje = if (vehiculos.isEmpty()) "Todavia no cargaste ningun vehiculo" else null
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
            Text("Mis vehiculos", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onVolver) { Text("Volver") }
        }

        Spacer(Modifier.height(8.dp))
        if (cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        mensaje?.let { Text(it, modifier = Modifier.padding(vertical = 8.dp)) }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vehiculos) { v ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(v.marca_modelo, style = MaterialTheme.typography.titleMedium)
                        Text("Patente: ${v.patente}")
                        Text("Asientos totales: ${v.asientos_totales}")
                        Text(if (v.activo == 1) "Activo" else "Dado de baja")
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        if (!mostrarForm) {
            Button(onClick = { mostrarForm = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar vehiculo")
            }
        } else {
            Text("Nuevo vehiculo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                marcaModelo, { marcaModelo = it; errorForm = null },
                label = { Text("Marca y modelo") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                patente, { patente = it.uppercase(); errorForm = null },
                label = { Text("Patente (AAA123 o AA123BB)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                asientos, { asientos = it.filter { c -> c.isDigit() } },
                label = { Text("Asientos totales") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            errorForm?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val asientosInt = asientos.toIntOrNull()
                        if (marcaModelo.isBlank() || patente.isBlank() || asientosInt == null || asientosInt !in 1..8) {
                            errorForm = "Completa todos los campos (asientos entre 1 y 8)"
                            return@Button
                        }
                        guardando = true
                        scope.launch {
                            try {
                                val resp = ApiClient.api.crearVehiculo(
                                    VehiculoRequest(session.conductorId, marcaModelo.trim(), patente.trim(), asientosInt)
                                )
                                if (resp.isSuccessful) {
                                    mostrarForm = false
                                    marcaModelo = ""; patente = ""; asientos = "4"
                                    cargar()
                                } else {
                                    errorForm = resp.errorBody()?.string()?.take(200) ?: "Error del servidor"
                                }
                            } catch (e: Exception) {
                                errorForm = "No se pudo conectar: ${e.message}"
                            } finally {
                                guardando = false
                            }
                        }
                    },
                    enabled = !guardando
                ) { Text("Guardar") }
                OutlinedButton(onClick = { mostrarForm = false }) { Text("Cancelar") }
            }
        }
    }
}
