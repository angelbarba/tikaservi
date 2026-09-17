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
}
