package ar.com.tikaservi.app.ui.chofer

import ar.com.tikaservi.app.ui.common.mensajeDeError

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.RecuperarPasswordRequest
import ar.com.tikaservi.app.data.model.RestablecerPasswordRequest
import kotlinx.coroutines.launch

@Composable
fun ChoferRecuperarPasswordScreen(onListo: () -> Unit, onVolver: () -> Unit) {
    val scope = rememberCoroutineScope()

    var identificador by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var pasoDos by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Recuperar contrasena", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            identificador, { identificador = it; error = null },
            label = { Text("Usuario, email o telefono") },
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !pasoDos
        )

        if (!pasoDos) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (identificador.isBlank()) { error = "Ingresa tu usuario, email o telefono"; return@Button }
                    cargando = true; error = null
                    scope.launch {
                        try {
                            val resp = ApiClient.api.recuperarPasswordConductor(
                                RecuperarPasswordRequest(identificador.trim())
                            )
                            if (resp.isSuccessful) {
                                mensaje = resp.body()?.mensaje ?: "Te enviamos un codigo"
                                pasoDos = true
                            } else {
                                error = mensajeDeError(resp.errorBody()?.string(), "No se pudo procesar el pedido")
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
                else Text("Enviar codigo")
            }
        } else {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                codigo, { codigo = it }, label = { Text("Codigo recibido") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                passwordNueva, { passwordNueva = it },
                label = { Text("Nueva contrasena") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (codigo.isBlank() || passwordNueva.isBlank()) {
                        error = "Completa el codigo y la nueva contrasena"; return@Button
                    }
                    cargando = true; error = null
                    scope.launch {
                        try {
                            val resp = ApiClient.api.restablecerPasswordConductor(
                                RestablecerPasswordRequest(identificador.trim(), codigo.trim(), passwordNueva)
                            )
                            if (resp.isSuccessful) {
                                mensaje = resp.body()?.mensaje ?: "Contrasena actualizada"
                                onListo()
                            } else {
                                error = mensajeDeError(resp.errorBody()?.string(), "Codigo incorrecto")
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
                else Text("Restablecer contrasena")
            }
        }

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        mensaje?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}
