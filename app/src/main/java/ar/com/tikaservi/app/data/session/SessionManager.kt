package ar.com.tikaservi.app.data.session

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val appContext = context.applicationContext

    // B-02: la sesion (token de pasajero, ids) va cifrada con Jetpack Security en
    // vez de SharedPreferences en texto plano. Si el keystore falla (dispositivo
    // raro, o quedo corrupto tras un cambio de clave), volvemos a un archivo
    // plano de emergencia en vez de romper el login por completo.
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "tikaservi_session_v2",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("SessionManager", "No se pudo abrir la sesion cifrada, uso fallback sin cifrar", e)
        appContext.getSharedPreferences("tikaservi_session", Context.MODE_PRIVATE)
    }

    enum class Rol { NINGUNO, PASAJERO, CHOFER }

    var rolActivo: Rol
        get() = Rol.valueOf(prefs.getString(KEY_ROL, Rol.NINGUNO.name)!!)
        set(value) = prefs.edit().putString(KEY_ROL, value.name).apply()

    var conductorId: Int
        get() = prefs.getInt(KEY_CONDUCTOR_ID, -1)
        set(value) = prefs.edit().putInt(KEY_CONDUCTOR_ID, value).apply()

    var vehiculoId: Int
        get() = prefs.getInt(KEY_VEHICULO_ID, -1)
        set(value) = prefs.edit().putInt(KEY_VEHICULO_ID, value).apply()

    var nombreConductor: String?
        get() = prefs.getString(KEY_NOMBRE_CONDUCTOR, null)
        set(value) = prefs.edit().putString(KEY_NOMBRE_CONDUCTOR, value).apply()

    var pasajeroId: Int
        get() = prefs.getInt(KEY_PASAJERO_ID, -1)
        set(value) = prefs.edit().putInt(KEY_PASAJERO_ID, value).apply()

    var pasajeroToken: String?
        get() = prefs.getString(KEY_PASAJERO_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_PASAJERO_TOKEN, value).apply()

    var nombrePasajero: String?
        get() = prefs.getString(KEY_NOMBRE_PASAJERO, null)
        set(value) = prefs.edit().putString(KEY_NOMBRE_PASAJERO, value).apply()

    val haySesionChofer: Boolean get() = conductorId > 0
    val haySesionPasajero: Boolean get() = pasajeroId > 0 && !pasajeroToken.isNullOrBlank()

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ROL = "rol_activo"
        private const val KEY_CONDUCTOR_ID = "conductor_id"
        private const val KEY_VEHICULO_ID = "vehiculo_id"
        private const val KEY_NOMBRE_CONDUCTOR = "nombre_conductor"
        private const val KEY_PASAJERO_ID = "pasajero_id"
        private const val KEY_PASAJERO_TOKEN = "pasajero_token"
        private const val KEY_NOMBRE_PASAJERO = "nombre_pasajero"
    }
}
