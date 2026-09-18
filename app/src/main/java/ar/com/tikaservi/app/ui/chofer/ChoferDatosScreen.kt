package ar.com.tikaservi.app.ui.chofer

import ar.com.tikaservi.app.ui.common.mensajeDeError

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.ConductorEditRequest
import ar.com.tikaservi.app.data.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ChoferDatosTab(session: SessionManager) {
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordActual by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    // P-11: antes la pestana arrancaba siempre vacia. GET /conductores/{id}
    // es publico y por eso (fix C-03) solo trae nombre y foto; telefono,
    // email y usuario no se pueden precargar porque el backend no los
    // expone sin una sesion de chofer con mas alcance que la actual.
    LaunchedEffect(Unit) {
        try {
            val perfil = ApiClient.api.getConductor(session.conductorId)
            nombre = perfil.nombre
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
                        val resp = ApiClient.api.editarConductor(
                            session.conductorId,
                            ConductorEditRequest(
                                password_actual = passwordActual,
                                nombre = nombre.ifBlank { null },
                                telefono = telefono.ifBlank { null },
                                email = email.ifBlank { null },
                                usuario = usuario.ifBlank { null },
                                password = passwordNueva.ifBlank { null }
                            )
                        )
                        if (resp.isSuccessful) {
                            mensaje = "Datos actualizados"
                            if (nombre.isNotBlank()) session.nombreConductor = nombre
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
