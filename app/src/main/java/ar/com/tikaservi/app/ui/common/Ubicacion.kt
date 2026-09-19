package ar.com.tikaservi.app.ui.common

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper

/**
 * Fix geoposicion: la version anterior (P-08) solo pedia una actualizacion a
 * GPS_PROVIDER (o NETWORK_PROVIDER si GPS no estaba habilitado) con
 * requestSingleUpdate y SIN timeout. En interiores o con GPS "frio" (sin
 * vista clara al cielo) el fix puede tardar mas de un minuto o no llegar
 * nunca, y sin timeout el boton se queda girando para siempre sin avisar
 * nada: eso es lo que se veia como "la geoposicion no funciona" al reservar
 * un asiento o pedir una encomienda. Esta version pide a GPS y NETWORK en
 * paralelo (lo que responda primero gana), cancela el otro listener, y si
 * ninguno responde en TIMEOUT_MS avisa con un error en vez de colgarse.
 */
private const val TIMEOUT_MS = 15_000L

fun obtenerUbicacionActual(
    context: Context,
    onResultado: (Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val proveedores = listOfNotNull(
        LocationManager.GPS_PROVIDER.takeIf { lm.isProviderEnabled(it) },
        LocationManager.NETWORK_PROVIDER.takeIf { lm.isProviderEnabled(it) }
    )
    if (proveedores.isEmpty()) {
        onError("Activa la ubicacion del dispositivo para continuar")
        return
    }

    var resuelto = false
    val handler = Handler(Looper.getMainLooper())
    val listeners = mutableListOf<LocationListener>()

    fun limpiar() {
        listeners.forEach { l ->
            try { lm.removeUpdates(l) } catch (_: SecurityException) {}
        }
    }

    val timeoutRunnable = Runnable {
        if (!resuelto) {
            resuelto = true
            limpiar()
            onError("No se pudo obtener tu ubicacion. Probá salir a espacio abierto o revisar el GPS.")
        }
    }

    try {
        proveedores.forEach { proveedor ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!resuelto) {
                        resuelto = true
                        handler.removeCallbacks(timeoutRunnable)
                        limpiar()
                        onResultado(location.latitude, location.longitude)
                    }
                }
            }
            listeners.add(listener)
            lm.requestSingleUpdate(proveedor, listener, Looper.getMainLooper())
        }
        handler.postDelayed(timeoutRunnable, TIMEOUT_MS)
    } catch (e: SecurityException) {
        onError("Sin permiso de ubicacion")
    }
}
