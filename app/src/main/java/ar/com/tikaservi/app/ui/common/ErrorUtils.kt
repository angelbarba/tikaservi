package ar.com.tikaservi.app.ui.common

import org.json.JSONArray
import org.json.JSONObject

/**
 * El backend de Tikaservi devuelve errores en dos formas:
 *  - HTTPException(codigo, "mensaje claro"): {"detail": "mensaje claro"}
 *  - Validacion de Pydantic (422): {"detail": [{"type":..., "loc":..., "msg": "...", ...}]}
 *
 * Antes se mostraba el JSON crudo tal cual llegaba (algo como
 * {"detail":[{"type":"value_error", ...}]}), que es ilegible para el
 * usuario. Esta funcion extrae el mensaje real, igual que lo muestra
 * el portal web, y si no puede parsearlo cae en un mensaje generico.
 */
fun mensajeDeError(cuerpo: String?, fallback: String): String {
    if (cuerpo.isNullOrBlank()) return fallback
    return try {
        val json = JSONObject(cuerpo)
        when (val detail = json.opt("detail")) {
            is String -> detail
            is JSONArray -> {
                if (detail.length() > 0) {
                    val primero = detail.optJSONObject(0)
                    val msg = primero?.optString("msg")
                    if (!msg.isNullOrBlank()) msg.removePrefix("Value error, ") else fallback
                } else fallback
            }
            else -> fallback
        }
    } catch (e: Exception) {
        fallback
    }
}
