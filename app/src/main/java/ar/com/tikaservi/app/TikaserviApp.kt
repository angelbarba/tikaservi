package ar.com.tikaservi.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import ar.com.tikaservi.app.push.TikaserviFirebaseMessagingService

class TikaserviApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // P-12: el canal se debe crear antes de que llegue la primera
        // notificacion (minSdk 26, asi que siempre existe NotificationChannel).
        val canal = NotificationChannel(
            TikaserviFirebaseMessagingService.CANAL_ID,
            "Notificaciones de Tikaservi",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos de reservas, viajes y solicitudes"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }
}
