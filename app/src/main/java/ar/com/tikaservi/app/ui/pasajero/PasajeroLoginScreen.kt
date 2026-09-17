package ar.com.tikaservi.app.ui.pasajero

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.LoginPasajeroRequest
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.common.TabPillRow
import kotlinx.coroutines.launch

@Composable
fun PasajeroLoginScreen(
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit,
    onIrARecuperar: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TabPillRow(
            opciones = listOf("Crear cuenta", "Ya tengo cuenta"),
            seleccionado = "Ya tengo cuenta",
            onSeleccionar = { if (it == "Crear cuenta") onIrARegistro() }
        )
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it; error = null },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Contrasena") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (usuario.isBlank() || password.isBlank()) {
                    error = "Completa usuario y contrasena"
                    return@Button
                }
                cargando = true
                scope.launch {
                    try {
                        val resp = ApiClient.api.loginPasajero(
                            LoginPasajeroRequest(usuario.trim(), password)
                        )
                        if (resp.isSuccessful && resp.body() != null) {
                            val body = resp.body()!!
                            session.rolActivo = SessionManager.Rol.PASAJERO
                            session.pasajeroId = body.pasajero_id
                            session.pasajeroToken = body.token
                            session.nombrePasajero = body.nombre
                            onLoginExitoso()
                        } else {
                            val cuerpoError = resp.errorBody()?.string().orEmpty()
                            error = when {
                                cuerpoError.contains("EMAIL_NO_VERIFICADO") ->
                                    "Todavia no verificaste tu correo. Revisa el codigo que te enviamos."
                                resp.code() == 401 -> "Usuario o contrasena incorrectos"
                                else -> "Error del servidor (${resp.code()})"
                            }
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
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Iniciar sesion")
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onIrARecuperar, modifier = Modifier.fillMaxWidth()) {
            Text("Olvidaste tu contrasena?")
        }
        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}
