package ar.com.tikaservi.app.ui.chofer

import ar.com.tikaservi.app.ui.common.mensajeDeError

import android.net.Uri
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.SolicitarCodigoActivacionRequest
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.textoAParteTexto
import ar.com.tikaservi.app.ui.common.uriAFotoPart
import ar.com.tikaservi.app.ui.common.TabPillRow
import kotlinx.coroutines.launch

@Composable
fun ChoferRegistroScreen(
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var codigoActivacion by remember { mutableStateOf("") }
    var mostrarSolicitudCodigo by remember { mutableStateOf(false) }
    var solNombre by remember { mutableStateOf("") }
    var solTelefono by remember { mutableStateOf("") }
    var solEmail by remember { mutableStateOf("") }
    var solUsuario by remember { mutableStateOf("") }
    var solDni by remember { mutableStateOf("") }
    var solEnviando by remember { mutableStateOf(false) }
    var solError by remember { mutableStateOf<String?>(null) }
    var solEnviada by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) fotoUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        TabPillRow(
            opciones = listOf("Crear cuenta", "Ya tengo cuenta"),
            seleccionado = "Crear cuenta",
            onSeleccionar = { if (it == "Ya tengo cuenta") onIrALogin() }
        )
        Spacer(Modifier.height(16.dp))
        Text("Crear cuenta de chofer", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Necesitas un codigo de activacion que te entrega el administrador de Tikaservi antes de registrarte.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(96.dp).align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            if (fotoUri != null) {
                AsyncImage(model = fotoUri, contentDescription = "Foto de perfil", modifier = Modifier.size(96.dp))
            } else {
                OutlinedButton(onClick = { selectorImagen.launch("image/*") }) { Text("Elegir foto") }
            }
        }
        if (fotoUri != null) {
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = { selectorImagen.launch("image/*") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Cambiar foto") }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(nombre, { nombre = it; error = null }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(dni, { dni = it; error = null }, label = { Text("DNI") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(telefono, { telefono = it; error = null }, label = { Text("Telefono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(email, { email = it; error = null }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(usuario, { usuario = it; error = null }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            password, { password = it; error = null },
            label = { Text("Contrasena") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            codigoActivacion, { codigoActivacion = it; error = null },
            label = { Text("Codigo de activacion") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        Spacer(Modifier.height(6.dp))
        if (solEnviada) {
            Text(
                "Listo, avisamos al administrador. Te va a contactar por WhatsApp con tu codigo.",
                style = MaterialTheme.typography.bodySmall
            )
        } else if (!mostrarSolicitudCodigo) {
            TextButton(onClick = { mostrarSolicitudCodigo = true }) {
                Text("No tenes codigo de activacion? Solicitalo")
            }
        } else {
            Spacer(Modifier.height(4.dp))
            Text("Solicitar codigo de activacion", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                solNombre, { solNombre = it; solError = null },
                label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                solTelefono, { solTelefono = it; solError = null },
                label = { Text("Telefono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                solDni, { solDni = it; solError = null },
                label = { Text("DNI (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                solUsuario, { solUsuario = it; solError = null },
                label = { Text("Usuario (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                solEmail, { solEmail = it; solError = null },
                label = { Text("Email (opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            solError?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (solNombre.isBlank() || solTelefono.isBlank()) {
                            solError = "Completa nombre y telefono"
                            return@Button
                        }
                        solEnviando = true
                        scope.launch {
                            try {
                                val resp = ApiClient.api.solicitarCodigoActivacion(
                                    SolicitarCodigoActivacionRequest(
                                        nombre = solNombre.trim(),
                                        telefono = solTelefono.trim(),
                                        email = solEmail.trim().ifBlank { null },
                                        usuario = solUsuario.trim().ifBlank { null },
                                        dni = solDni.trim().ifBlank { null }
                                    )
                                )
                                if (resp.isSuccessful) {
                                    solEnviada = true
                                    mostrarSolicitudCodigo = false
                                } else {
                                    solError = mensajeDeError(resp.errorBody()?.string(), "Error del servidor")
                                }
                            } catch (e: Exception) {
                                solError = "No se pudo conectar: ${e.message}"
                            } finally {
                                solEnviando = false
                            }
                        }
                    },
                    enabled = !solEnviando
                ) { Text("Enviar solicitud") }
                OutlinedButton(onClick = { mostrarSolicitudCodigo = false }, enabled = !solEnviando) { Text("Cancelar") }
            }
        }

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (nombre.isBlank() || dni.isBlank() || telefono.isBlank() || email.isBlank() ||
                    usuario.isBlank() || password.isBlank() || codigoActivacion.isBlank()
                ) {
                    error = "Completa todos los campos"
                    return@Button
                }
                if (fotoUri == null) {
                    error = "La foto de perfil es obligatoria"
                    return@Button
                }
                cargando = true
                scope.launch {
                    try {
                        val fotoPart = uriAFotoPart(context, fotoUri!!)
                        if (fotoPart == null) {
                            error = "No se pudo leer la foto elegida"
                            cargando = false
                            return@launch
                        }
                        val resp = ApiClient.api.registrarConductor(
                            nombre = textoAParteTexto(nombre.trim()),
                            dni = textoAParteTexto(dni.trim()),
                            telefono = textoAParteTexto(telefono.trim()),
                            email = textoAParteTexto(email.trim()),
                            usuario = textoAParteTexto(usuario.trim()),
                            password = textoAParteTexto(password),
                            codigoActivacion = textoAParteTexto(codigoActivacion.trim().uppercase()),
                            foto = fotoPart
                        )
                        if (resp.isSuccessful && resp.body() != null) {
                            val body = resp.body()!!
                            session.rolActivo = SessionManager.Rol.CHOFER
                            session.conductorId = body.conductor_id
                            session.nombreConductor = body.nombre
                            onRegistroExitoso()
                        } else {
                            error = mensajeDeError(resp.errorBody()?.string(), "Error del servidor (${resp.code()})")
                        }
                    } catch (e: Exception) {
                        error = "No se pudo conectar con el servidor: ${e.message}"
                    } finally {
                        cargando = false
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (cargando) CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            else Text("Registrarme")
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}
