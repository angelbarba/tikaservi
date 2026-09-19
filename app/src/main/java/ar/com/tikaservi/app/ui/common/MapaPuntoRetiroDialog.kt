package ar.com.tikaservi.app.ui.common

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import org.json.JSONObject

/**
 * Pedido de Angel: la geoposicion del punto de retiro tiene que dar las dos
 * opciones, geolocalizar automaticamente O ajustar a mano en un mapa - igual
 * que el picker que ya existe en los portales web (Leaflet + Nominatim,
 * pasajero-viajes.html / conductor-viajes.html). En vez de reimplementar un
 * mapa nativo (que hubiera necesitado una API key de Google Maps que este
 * proyecto no usa), se reutiliza la misma pagina Leaflet dentro de un
 * WebView: es el mismo picker probado de la web, corriendo embebido.
 *
 * Al abrirse intenta ubicar automaticamente por GPS/red (usa el mismo
 * obtenerUbicacionActual con timeout) y centra el mapa ahi si lo logra; el
 * usuario siempre puede arrastrar el pin, tocar el mapa o buscar una
 * direccion para ajustarlo a mano, y hay un boton de "mi ubicacion" dentro
 * del mapa para repetir el intento automatico cuando quiera.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapaPuntoRetiroDialog(
    latInicial: Double?,
    lngInicial: Double?,
    onConfirmar: (Double, Double) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onCancelar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // Fix: el Dialog de Compose crea su ventana con alto "wrap content"
        // por defecto. Sin forzarla a MATCH_PARENT, el fillMaxSize() de aca
        // abajo no tiene contra que expandirse y el WebView (con el mapa)
        // queda colapsado a practicamente 0 de alto: por eso solo se veian
        // el buscador y los botones, y el mapa aparecia en blanco/vacio.
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        Surface(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val webView = WebView(ctx)
                    webView.settings.javaScriptEnabled = true
                    webView.settings.domStorageEnabled = true

                    webView.addJavascriptInterface(object {
                        @JavascriptInterface
                        fun confirmar(lat: Double, lng: Double) {
                            webView.post { onConfirmar(lat, lng) }
                        }

                        @JavascriptInterface
                        fun cancelar() {
                            webView.post { onCancelar() }
                        }

                        @JavascriptInterface
                        fun pedirUbicacionActual() {
                            webView.post {
                                obtenerUbicacionActual(
                                    ctx,
                                    onResultado = { lat, lng ->
                                        webView.post {
                                            webView.evaluateJavascript("centrarEnUbicacion($lat, $lng)", null)
                                        }
                                    },
                                    onError = { err ->
                                        webView.post {
                                            webView.evaluateJavascript(
                                                "avisarErrorUbicacion(${JSONObject.quote(err)})", null
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }, "AndroidBridge")

                    val url = buildString {
                        append("file:///android_asset/mapa_punto_retiro.html")
                        if (latInicial != null && lngInicial != null) {
                            append("?lat=").append(latInicial).append("&lng=").append(lngInicial)
                        }
                    }
                    webView.loadUrl(url)

                    // Intento automatico: si no venimos con una ubicacion ya
                    // marcada (p. ej. editando un punto de retiro existente),
                    // probamos geolocalizar apenas se abre el mapa. Si falla o
                    // tarda de mas, el usuario sigue pudiendo ajustar a mano.
                    if (latInicial == null || lngInicial == null) {
                        obtenerUbicacionActual(
                            ctx,
                            onResultado = { lat, lng ->
                                webView.post { webView.evaluateJavascript("centrarEnUbicacion($lat, $lng)", null) }
                            },
                            onError = { /* el usuario ajusta a mano, no hace falta avisar */ }
                        )
                    }

                    webView
                }
            )
        }
    }
}
