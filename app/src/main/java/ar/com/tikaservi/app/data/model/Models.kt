package ar.com.tikaservi.app.data.model

/**
 * Modelos de datos que reflejan el esquema real de viajes.db / Postgres
 * (ver mapa técnico de tikaservi.com.ar). Los campos son nullable/con
 * default donde el backend los devuelve opcionalmente, para que Gson no
 * rompa el parseo si falta alguno.
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
data class LoginConductorResponse(val conductor: Conductor)

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
data class LoginPasajeroResponse(val pasajero: Pasajero, val token: String)

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
    val nombre_conductor: String? = null
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
