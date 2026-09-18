package ar.com.tikaservi.app.push

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ar.com.tikaservi.app.MainActivity
import ar.com.tikaservi.app.R
import ar.com.tikaservi.app.data.session.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** P-12: recibe los mensajes FCM (equivalente nativo al Web Push de la PWA). */
class TikaserviFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Firebase puede rotar el token en cualquier momento (reinstalacion,
        // restauracion de backup, etc). Si hay sesion activa, re-registrarlo.
        scope.launch { registrarTokenFcm(SessionManager(applicationContext), token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val titulo = message.notification?.title ?: message.data["titulo"] ?: "Tikaservi"
        val cuerpo = message.notification?.body ?: message.data["cuerpo"] ?: return
        mostrarNotificacion(titulo, cuerpo)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(this, CANAL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Android 13+: sin el permiso no se puede mostrar. Se pidio al
            // entrar al Home; si el usuario lo nego, simplemente no se ve.
            return
        }
        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notificacion)
    }

    companion object {
        const val CANAL_ID = "tikaservi_notificaciones"
    }
}
