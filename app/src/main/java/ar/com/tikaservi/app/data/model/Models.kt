package ar.com.tikaservi.app.data.model

/**
 * Modelos de datos verificados contra la API real (curl directo al
 * servidor cloud, 17 sep 2026). OJO: las respuestas de login/registro
 * son planas (los campos van sueltos en el JSON raiz), NO anidadas
 * bajo "conductor"/"pasajero" como se habia asumido en la primera
 * version -- ese era el bug que rompia el login con NullPointerException.
 */

data class Conductor(
    val id: Int = 0,
    val nombre: String = "",
    val telefono: String = "",
    val usuario: String = "",
    val foto_url: String? = null,
    val email: String? = null,
    val dni: String? = null,
    val activo: Int = 1,
    val conductor_viajes_finalizados: Int? = null
)

data class LoginConductorRequest(val usuario: String, val password: String)

// Respuesta real: {"conductor_id":61,"nombre":"...","foto_url":"..."}
data class LoginConductorResponse(
    val conductor_id: Int,
    val nombre: String,
    val foto_url: String? = null
)

data class Vehiculo(
    val id: Int = 0,
    val conductor_id: Int = 0,
    val marca_modelo: String = "",
    val patente: String = "",
    val asientos_totales: Int = 4,
    val activo: Int = 1
)

data class Pasajero(
    val id: Int = 0,
    val nombre: String = "",
    val dni: String = "",
    val telefono: String = "",
    val usuario: String? = null,
    val foto_url: String? = null,
    val email: String? = null,
    val pasajero_viajes_realizados: Int? = null
)

data class LoginPasajeroRequest(val usuario: String, val password: String)

// Respuesta real: {"pasajero_id":43,"nombre":"...","foto_url":"...","token":"..."}
data class LoginPasajeroResponse(
    val pasajero_id: Int,
    val nombre: String,
    val foto_url: String? = null,
    val token: String
)

data class Viaje(
    val id: Int = 0,
    val conductor_id: Int = 0,
    val vehiculo_id: Int = 0,
    val origen: String = "",
    val destino: String = "",
    val fecha: String = "",
    val hora_salida: String = "",
    val precio: Double? = null,
    val paradas: String? = null,
    val asientos_disponibles: Int = 0,
    val estado: String = "activo",
    val acepta_encomiendas: Int = 0,
    val conductor_viajes_finalizados: Int? = null,
    val conductor_nombre: String? = null,
    val marca_modelo: String? = null,
    val patente: String? = null
)

data class NuevoViajeRequest(
    val conductor_id: Int,
    val vehiculo_id: Int,
    val origen: String,
    val destino: String,
    val fecha: String,
    val hora_salida: String,
    val precio: Double?,
    val paradas: String? = null,
    val asientos_disponibles: Int? = null,
    val acepta_encomiendas: Boolean = false
)

data class Reserva(
    val id: Int = 0,
    val viaje_id: Int = 0,
    val pasajero_id: Int = 0,
    val asientos_reservados: Int = 1,
    val estado: String = "confirmada",
    val tipo: String = "pasajero",
    val tamano_encomienda: String? = null,
    val punto_retiro_lat: Double? = null,
    val punto_retiro_lng: Double? = null,
    val pasajero_viajes_realizados: Int? = null,
    val nombre_pasajero: String? = null
)

data class NuevaReservaRequest(
    val viaje_id: Int,
    val pasajero_id: Int,
    val asientos_reservados: Int = 1,
    val tipo: String = "pasajero",
    val tamano_encomienda: String? = null,
    val punto_retiro_lat: Double? = null,
    val punto_retiro_lng: Double? = null
)

data class Localidad(
    val id: Int = 0,
    val nombre: String = ""
)

data class ApiError(
    val detail: String? = null
)
