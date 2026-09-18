package ar.com.tikaservi.app.data.model

// ============================================================
// Modelos verificados linea por linea contra el codigo fuente
// real del backend (/opt/viajes/backend/main.py). La API real
// devuelve respuestas PLANAS (sin envolver en {"pasajero": {}})
// y tiene reglas estrictas: origen/destino deben ser exactamente
// uno de los valores de GET /localidades (lista simple de
// strings), hora_salida debe ser "HH:00" en punto, etc.
// ============================================================

// ---------- Chofer / Conductor ----------

data class LoginConductorRequest(val usuario: String, val password: String)

data class LoginConductorResponse(
    val conductor_id: Int,
    val nombre: String,
    val foto_url: String? = null
)

data class ConductorRegistroResponse(
    val conductor_id: Int,
    val nombre: String,
    val foto_url: String? = null
)

data class Vehiculo(
    val id: Int = 0,
    val conductor_id: Int = 0,
    val marca_modelo: String = "",
    val patente: String = "",
    val asientos_totales: Int = 0,
    val activo: Int = 1
)

data class VehiculoRequest(
    val conductor_id: Int,
    val marca_modelo: String,
    val patente: String,
    val asientos_totales: Int
)

data class VehiculoRespuesta(val vehiculo_id: Int)

// ---------- Pasajero ----------

data class LoginPasajeroRequest(val usuario: String, val password: String)

data class LoginPasajeroResponse(
    val pasajero_id: Int,
    val nombre: String,
    val foto_url: String? = null,
    val token: String
)

data class PasajeroRegistroResponse(
    val pasajero_id: Int,
    val nombre: String,
    val email_verificado: Boolean,
    val correo_enviado: Boolean,
    val mensaje: String
)

data class VerificarEmailRequest(val codigo: String)

data class VerificarEmailResponse(
    val pasajero_id: Int,
    val nombre: String,
    val foto_url: String? = null,
    val token: String
)

data class ReenviarCodigoResponse(val ok: Boolean, val mensaje: String)

// ---------- Viajes ----------

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
    val encomienda_ocupa_asiento: Int = 0,
    val conductor_nombre: String? = null,
    val conductor_telefono: String? = null,
    val conductor_foto_url: String? = null,
    val conductor_viajes_finalizados: Int? = null,
    val marca_modelo: String? = null,
    val patente: String? = null,
    val asientos_reservados_total: Int? = null,
    val encomiendas_total: Int? = null,
    val reservas_confirmadas_total: Int? = null
)

data class ViajeRequest(
    val conductor_id: Int,
    val vehiculo_id: Int,
    val origen: String,
    val destino: String,
    val fecha: String,
    val hora_salida: String,
    val precio: Double? = null,
    val paradas: String? = "",
    val acepta_encomiendas: Boolean = false,
    val encomienda_ocupa_asiento: Boolean = false,
    val asientos_disponibles: Int? = null
)

data class ViajeRespuesta(val viaje_id: Int)

// P-03: edicion de viaje ya publicado (paridad con conductor-viajes.html).
// Todos los campos opcionales salvo conductor_id: solo se manda lo que
// cambio, igual que hace la web (el backend conserva el resto tal cual esta).
data class ViajeEditRequest(
    val conductor_id: Int,
    val origen: String? = null,
    val destino: String? = null,
    val fecha: String? = null,
    val hora_salida: String? = null,
    val precio: Double? = null,
    val paradas: String? = null,
    val asientos_disponibles: Int? = null
)

// ---------- Reservas ----------

data class ReservaRequest(
    val viaje_id: Int,
    val pasajero_id: Int,
    val asientos: Int = 1,
    val tipo: String = "pasajero",
    val tamano_encomienda: String? = null,
    val punto_retiro_lat: Double? = null,
    val punto_retiro_lng: Double? = null,
    val origen_deseado: String? = null,
    val destino_deseado: String? = null
)

data class ReservaRespuesta(val reserva_id: Int)

data class ReservaCancelarRequest(val pasajero_id: Int)

data class ReservaPasajero(
    val id: Int,
    val asientos_reservados: Int,
    val estado: String,
    val creado_en: String? = null,
    val tipo: String,
    val tamano_encomienda: String? = null,
    val punto_retiro_lat: Double? = null,
    val punto_retiro_lng: Double? = null,
    val origen_deseado: String? = null,
    val destino_deseado: String? = null,
    val viaje_id: Int,
    val origen: String,
    val destino: String,
    val fecha: String,
    val hora_salida: String,
    val precio: Double? = null,
    val viaje_estado: String,
    val conductor_nombre: String? = null,
    val conductor_telefono: String? = null
)

data class ReservaChofer(
    val id: Int,
    val asientos_reservados: Int,
    val estado: String,
    val creado_en: String? = null,
    val tipo: String,
    val tamano_encomienda: String? = null,
    val punto_retiro_lat: Double? = null,
    val punto_retiro_lng: Double? = null,
    val origen_deseado: String? = null,
    val destino_deseado: String? = null,
    val nombre: String,
    val dni: String? = null,
    val telefono: String? = null,
    val pasajero_viajes_realizados: Int? = null
)

data class OkRespuesta(val ok: Boolean)

data class ApiError(val detail: String? = null)

// ---------- Recuperar / restablecer contrasena ----------

data class RecuperarPasswordRequest(val identificador: String)

data class RecuperarPasswordResponse(val ok: Boolean, val mensaje: String)

data class RestablecerPasswordRequest(
    val identificador: String,
    val codigo: String,
    val password: String
)

data class RestablecerPasswordResponse(val ok: Boolean, val mensaje: String)

// ---------- Editar datos (requieren password_actual) ----------

data class PasajeroEditRequest(
    val password_actual: String,
    val nombre: String? = null,
    val dni: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val usuario: String? = null,
    val password: String? = null
)

data class ConductorEditRequest(
    val password_actual: String,
    val nombre: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val usuario: String? = null,
    val password: String? = null
)

// ---------- Solicitudes de pasajero ("pedir viaje") ----------

data class SolicitudRequest(
    val pasajero_id: Int,
    val origen: String,
    val destino: String,
    val fecha: String,
    val franjas_horarias: List<String>,
    val comentario: String? = null,
    val tipo: String = "viaje",
    val asientos: Int? = null,
    val tamano_encomienda: String? = null
)

// Tal como la ve el propio pasajero (sus solicitudes)
data class Solicitud(
    val id: Int,
    val pasajero_id: Int,
    val origen: String,
    val destino: String,
    val fecha: String,
    val franjas_horarias: List<String>,
    val comentario: String? = null,
    val estado: String,
    val tipo: String,
    val asientos: Int? = null,
    val tamano_encomienda: String? = null,
    val creado_en: String? = null
)

// Tal como la ve el chofer al buscar candidatos (incluye datos del pasajero)
data class SolicitudPublica(
    val id: Int,
    val origen: String,
    val destino: String,
    val fecha: String,
    val franjas_horarias: List<String>,
    val comentario: String? = null,
    val tipo: String,
    val asientos: Int? = null,
    val tamano_encomienda: String? = null,
    val creado_en: String? = null,
    val pasajero_id: Int,
    val pasajero_nombre: String,
    val pasajero_telefono: String? = null,
    val pasajero_foto_url: String? = null,
    val pasajero_viajes_realizados: Int? = null,
    val whatsapp_link: String? = null
)

data class CancelarSolicitudRequest(val pasajero_id: Int)
