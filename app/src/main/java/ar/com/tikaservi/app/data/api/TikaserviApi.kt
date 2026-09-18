package ar.com.tikaservi.app.data.api

import ar.com.tikaservi.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TikaserviApi {

    // GET /viajes-api/localidades -> lista simple de strings, ej:
    // ["Santa Victoria Este", "Santa Maria", "Curvita", ...]
    // Usar SIEMPRE estos valores exactos como origen/destino (no texto libre).
    @GET("localidades")
    suspend fun getLocalidades(): List<String>

    // ---------------- Chofer / Conductor ----------------

    @POST("conductores/login")
    suspend fun loginConductor(@Body body: LoginConductorRequest): Response<LoginConductorResponse>

    @Multipart
    @POST("conductores")
    suspend fun registrarConductor(
        @Part("nombre") nombre: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("email") email: RequestBody,
        @Part("usuario") usuario: RequestBody,
        @Part("password") password: RequestBody,
        @Part("codigo_activacion") codigoActivacion: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<ConductorRegistroResponse>

    @GET("conductores/{id}/vehiculos")
    suspend fun getVehiculosDeConductor(@Path("id") conductorId: Int): List<Vehiculo>

    @POST("vehiculos")
    suspend fun crearVehiculo(@Body body: VehiculoRequest): Response<VehiculoRespuesta>

    @GET("conductores/{id}/viajes")
    suspend fun getViajesDeConductor(@Path("id") conductorId: Int): List<Viaje>

    // ---------------- Viajes ----------------

    // origen/destino deben ser exactamente un valor de getLocalidades().
    // hora_salida en formato "HH:00" (en punto).
    // tipo: "viaje" (default) o "encomienda".
    @GET("viajes")
    suspend fun buscarViajes(
        @Query("origen") origen: String? = null,
        @Query("destino") destino: String? = null,
        @Query("fecha") fecha: String? = null,
        @Query("conductor_id") conductorId: Int? = null,
        @Query("incluir_completos") incluirCompletos: Boolean? = null,
        @Query("tipo") tipo: String? = null
    ): List<Viaje>

    @GET("viajes/{id}")
    suspend fun getViaje(@Path("id") id: Int): Viaje

    @POST("viajes")
    suspend fun publicarViaje(@Body body: ViajeRequest): Response<ViajeRespuesta>

    @POST("viajes/{id}/cancelar")
    suspend fun cancelarViaje(
        @Path("id") id: Int,
        @Query("conductor_id") conductorId: Int
    ): Response<OkRespuesta>

    // Fase 4 (P-01/P-02/P-03): finalizar, reactivar y editar un viaje ya
    // publicado - existian en el backend y en la web desde siempre, pero la
    // app nunca los llamaba.
    @POST("viajes/{id}/finalizar")
    suspend fun finalizarViaje(
        @Path("id") id: Int,
        @Query("conductor_id") conductorId: Int
    ): Response<OkRespuesta>

    @POST("viajes/{id}/reactivar")
    suspend fun reactivarViaje(
        @Path("id") id: Int,
        @Query("conductor_id") conductorId: Int
    ): Response<OkRespuesta>

    @PUT("viajes/{id}")
    suspend fun editarViaje(
        @Path("id") id: Int,
        @Body body: ViajeEditRequest
    ): Response<OkRespuesta>

    @GET("viajes/{id}/reservas")
    suspend fun getReservasDeViaje(
        @Path("id") viajeId: Int,
        @Query("conductor_id") conductorId: Int
    ): List<ReservaChofer>

    // ---------------- Pasajero ----------------

    @POST("pasajeros/login")
    suspend fun loginPasajero(@Body body: LoginPasajeroRequest): Response<LoginPasajeroResponse>

    @Multipart
    @POST("pasajeros")
    suspend fun registrarPasajero(
        @Part("nombre") nombre: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("email") email: RequestBody,
        @Part("usuario") usuario: RequestBody,
        @Part("password") password: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<PasajeroRegistroResponse>

    @POST("pasajeros/{id}/verificar-email")
    suspend fun verificarEmailPasajero(
        @Path("id") pasajeroId: Int,
        @Body body: VerificarEmailRequest
    ): Response<VerificarEmailResponse>

    @POST("pasajeros/{id}/reenviar-codigo")
    suspend fun reenviarCodigoPasajero(@Path("id") pasajeroId: Int): Response<ReenviarCodigoResponse>

    @GET("pasajeros/{id}/reservas")
    suspend fun getReservasDePasajero(
        @Path("id") pasajeroId: Int,
        @Header("X-Pasajero-Token") token: String
    ): List<ReservaPasajero>

    // ---------------- Reservas ----------------

    @POST("reservas")
    suspend fun crearReserva(@Body body: ReservaRequest): Response<ReservaRespuesta>

    @POST("reservas/{id}/cancelar")
    suspend fun cancelarReserva(
        @Path("id") id: Int,
        @Body body: ReservaCancelarRequest
    ): Response<OkRespuesta>

    // P-07
    @PUT("reservas/{id}")
    suspend fun editarReserva(
        @Path("id") id: Int,
        @Body body: ReservaEditRequest
    ): Response<OkRespuesta>

    // P-08
    @PUT("reservas/{id}/punto-retiro")
    suspend fun actualizarPuntoRetiro(
        @Path("id") id: Int,
        @Body body: PuntoRetiroRequest
    ): Response<OkRespuesta>

    // P-12: tipo = "pasajero" -> mandar X-Pasajero-Token si hay sesion; tipo = "chofer" no lo necesita.
    @POST("push/suscribir-fcm")
    suspend fun suscribirPushFcm(
        @Body body: PushSuscripcionFcmRequest,
        @Header("X-Pasajero-Token") pasajeroToken: String? = null
    ): Response<OkRespuesta>
    // ---------------- Recuperar / restablecer contrasena ----------------

    @POST("pasajeros/recuperar-password")
    suspend fun recuperarPasswordPasajero(@Body body: RecuperarPasswordRequest): Response<RecuperarPasswordResponse>

    @POST("pasajeros/restablecer-password")
    suspend fun restablecerPasswordPasajero(@Body body: RestablecerPasswordRequest): Response<RestablecerPasswordResponse>

    @POST("conductores/recuperar-password")
    suspend fun recuperarPasswordConductor(@Body body: RecuperarPasswordRequest): Response<RecuperarPasswordResponse>

    @POST("conductores/restablecer-password")
    suspend fun restablecerPasswordConductor(@Body body: RestablecerPasswordRequest): Response<RestablecerPasswordResponse>

    // ---------------- Editar datos propios ----------------

    @PUT("pasajeros/{id}")
    suspend fun editarPasajero(@Path("id") id: Int, @Body body: PasajeroEditRequest): Response<OkRespuesta>

    @PUT("conductores/{id}")
    suspend fun editarConductor(@Path("id") id: Int, @Body body: ConductorEditRequest): Response<OkRespuesta>

    // ---------------- Solicitudes de pasajero ("pedir viaje") ----------------

    @POST("solicitudes")
    suspend fun crearSolicitud(@Body body: SolicitudRequest): Response<Solicitud>

    @GET("solicitudes")
    suspend fun buscarSolicitudes(
        @Query("origen") origen: String? = null,
        @Query("destino") destino: String? = null,
        @Query("fecha") fecha: String? = null
    ): List<SolicitudPublica>

    @GET("pasajeros/{id}/solicitudes")
    suspend fun getSolicitudesDePasajero(@Path("id") pasajeroId: Int): List<Solicitud>

    @POST("solicitudes/{id}/cancelar")
    suspend fun cancelarSolicitud(
        @Path("id") id: Int,
        @Body body: CancelarSolicitudRequest
    ): Response<OkRespuesta>
}
