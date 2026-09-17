package ar.com.tikaservi.app.ui.pasajero

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.VerificarEmailRequest
import ar.com.tikaservi.app.data.session.SessionManager
import kotlinx.coroutines.launch

@Composable
fun PasajeroVerificarEmailScreen(
    pasajeroId: Int,
    onVerificado: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session = remember { SessionManager(context) }

    var codigo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var reenviando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verifica tu correo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Te enviamos un codigo de activacion por email. Ingresalo para poder iniciar sesion.")
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it; error = null },
            label = { Text("Codigo de verificacion") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        mensaje?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (codigo.isBlank()) { error = "Ingresa el codigo"; return@Button }
                cargando = true
                error = null
                scope.launch {
                    try {
                        val resp = ApiClient.api.verificarEmailPasajero(
                            pasajeroId, VerificarEmailRequest(codigo.trim())
                        )
                        if (resp.isSuccessful && resp.body() != null) {
                            val body = resp.body()!!
                            session.rolActivo = SessionManager.Rol.PASAJERO
                            session.pasajeroId = body.pasajero_id
                            session.pasajeroToken = body.token
                            session.nombrePasajero = body.nombre
                            onVerificado()
                        } else {
                            error = resp.errorBody()?.string()?.take(200) ?: "Codigo invalido"
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
            else Text("Verificar")
        }

        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = {
                reenviando = true
                mensaje = null
                scope.launch {
                    try {
                        val resp = ApiClient.api.reenviarCodigoPasajero(pasajeroId)
                        mensaje = if (resp.isSuccessful) resp.body()?.mensaje ?: "Codigo reenviado"
                        else "No se pudo reenviar el codigo"
                    } catch (e: Exception) {
                        mensaje = "Error: ${e.message}"
                    } finally {
                        reenviando = false
                    }
                }
            },
            enabled = !reenviando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Reenviar codigo") }

        TextButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}
