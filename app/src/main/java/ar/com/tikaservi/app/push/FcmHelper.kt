package ar.com.tikaservi.app.push

import android.content.Context
import ar.com.tikaservi.app.data.api.ApiClient
import ar.com.tikaservi.app.data.model.PushSuscripcionFcmRequest
import ar.com.tikaservi.app.data.session.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * P-12: registro del token FCM de este dispositivo contra el backend.
 *
 * Se llama desde las pantallas Home (chofer/pasajero) al entrar con sesion
 * activa, y desde onNewToken() cuando Firebase rota el token en background.
 * Nunca lanza excepcion hacia arriba: que falle el registro de push no debe
 * frenar ninguna otra pantalla (mismo criterio que enviar_push() del lado
 * del servidor, que tampoco frena la operacion principal si falla).
 */
suspend fun obtenerTokenFcm(): String? = suspendCancellableCoroutine { cont ->
    try {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { tarea ->
            if (tarea.isSuccessful) cont.resume(tarea.result) else cont.resume(null)
        }
    } catch (e: Exception) {
        cont.resume(null)
    }
}

suspend fun registrarTokenFcmSiHaySesion(context: Context) {
    val session = SessionManager(context)
    val token = obtenerTokenFcm() ?: return
    registrarTokenFcm(session, token)
}

suspend fun registrarTokenFcm(session: SessionManager, token: String) {
    try {
        when {
            session.haySesionChofer -> {
                ApiClient.api.suscribirPushFcm(
                    PushSuscripcionFcmRequest(tipo = "chofer", usuario_id = session.conductorId, token = token)
                )
            }
            session.haySesionPasajero -> {
                ApiClient.api.suscribirPushFcm(
                    PushSuscripcionFcmRequest(tipo = "pasajero", usuario_id = session.pasajeroId, token = token),
                    pasajeroToken = session.pasajeroToken
                )
            }
        }
    } catch (_: Exception) {
        // Sin conexion o el backend no respondio: no rompe la pantalla que
        // llamo a esto. Se reintenta la proxima vez que se abra el Home.
    }
}
