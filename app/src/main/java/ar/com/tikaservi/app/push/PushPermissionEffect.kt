package ar.com.tikaservi.app.push

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

/**
 * P-12: al entrar a un Home con sesion activa, pide el permiso de
 * notificaciones (solo hace falta desde Android 13/API 33) y registra el
 * token FCM del dispositivo contra el backend. Se usa igual en el Home de
 * chofer y en el de pasajero.
 */
@Composable
fun RegistrarPushAlEntrar() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Se pidio el permiso lo haya concedido o no; el registro del token
        // sirve igual (si lo nego, simplemente no vera notificaciones).
        scope.launch { registrarTokenFcmSiHaySesion(context) }
    }

    LaunchedEffect(Unit) {
        val haceFaltaPedir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (haceFaltaPedir) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            registrarTokenFcmSiHaySesion(context)
        }
    }
}
