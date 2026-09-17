package ar.com.tikaservi.app.data.api

import ar.com.tikaservi.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface TikaserviApi {

    @GET("localidades")
    suspend fun getLocalidades(): List<Localidad>

    @POST("conductores/login")
    suspend fun loginConductor(@Body body: LoginConductorRequest): Response<LoginConductorResponse>

    @GET("conductores/{id}/vehiculos")
    suspend fun getVehiculosDeConductor(@Path("id") conductorId: Int): List<Vehiculo>

    @GET("conductores/{id}/viajes")
    suspend fun getViajesDeConductor(@Path("id") conductorId: Int): List<Viaje>

    @Multipart
    @POST("conductores")
    suspend fun registrarConductor(
        @Part("nombre") nombre: okhttp3.RequestBody,
        @Part("telefono") telefono: okhttp3.RequestBody,
        @Part("usuario") usuario: okhttp3.RequestBody,
        @Part("password") password: okhttp3.RequestBody,
        @Part foto: okhttp3.MultipartBody.Part
    ): Response<LoginConductorResponse>

    @POST("vehiculos")
    suspend fun crearVehiculo(@Body body: Map<String, @JvmSuppressWildcards Any?>): Response<Vehiculo>

    @GET("viajes")
    suspend fun buscarViajes(
        @Query("origen") origen: String? = null,
        @Query("destino") destino: String? = null,
        @Query("fecha") fecha: String? = null
    ): List<Viaje>

    @GET("viajes/{id}")
    suspend fun getViaje(@Path("id") id: Int): Viaje

    @POST("viajes")
    suspend fun publicarViaje(@Body body: NuevoViajeRequest): Response<Viaje>

    @POST("viajes/{id}/cancelar")
    suspend fun cancelarViaje(@Path("id") id: Int): Response<Unit>

    @POST("viajes/{id}/finalizar")
    suspend fun finalizarViaje(@Path("id") id: Int): Response<Unit>

    @GET("viajes/{id}/reservas")
    suspend fun getReservasDeViaje(@Path("id") viajeId: Int): List<Reserva>

    @POST("pasajeros/login")
    suspend fun loginPasajero(@Body body: LoginPasajeroRequest): Response<LoginPasajeroResponse>

    @Multipart
    @POST("pasajeros")
    suspend fun registrarPasajero(
        @Part("nombre") nombre: okhttp3.RequestBody,
        @Part("dni") dni: okhttp3.RequestBody,
        @Part("telefono") telefono: okhttp3.RequestBody,
        @Part("usuario") usuario: okhttp3.RequestBody,
        @Part("password") password: okhttp3.RequestBody,
        @Part foto: okhttp3.MultipartBody.Part
    ): Response<LoginPasajeroResponse>

    @GET("pasajeros/{id}/reservas")
    suspend fun getReservasDePasajero(
        @Path("id") pasajeroId: Int,
        @Header("X-Pasajero-Token") token: String
    ): List<Reserva>

    @POST("reservas")
    suspend fun crearReserva(@Body body: NuevaReservaRequest): Response<Reserva>

    @POST("reservas/{id}/cancelar")
    suspend fun cancelarReserva(@Path("id") id: Int): Response<Unit>
}
