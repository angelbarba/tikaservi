package ar.com.tikaservi.app.data.api

import ar.com.tikaservi.app.BuildConfig
import ar.com.tikaservi.app.data.session.SessionEvents
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://tikaservi.com.ar/viajes-api/"

    // B-04: el logging de requests/responses solo va en debug. En release
    // quedaria escribiendo tokens y datos personales al log de Android.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    // B-03: si un pedido mandaba X-Pasajero-Token y el servidor contesta 401,
    // la sesion esta vencida o fue cerrada desde otro lado. Avisamos por
    // SessionEvents para que la UI limpie la sesion y vuelva al login, en vez
    // de dejar que la pantalla se quede mostrando datos viejos o reintentando.
    private val sesionInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401 && request.header("X-Pasajero-Token") != null) {
            SessionEvents.sesionExpirada.tryEmit(Unit)
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(sesionInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val api: TikaserviApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TikaserviApi::class.java)
    }
}
