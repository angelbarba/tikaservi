package ar.com.tikaservi.app.ui.common

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Convierte una Uri elegida desde la galeria (ACTION_GET_CONTENT / PickVisualMedia)
 * en un MultipartBody.Part listo para mandar como "foto" en los registros.
 * Copia el contenido a un archivo temporal en cache porque el backend necesita
 * un tamano conocido (Content-Length) y un nombre de archivo.
 */
fun uriAFotoPart(context: Context, uri: Uri, nombreCampo: String = "foto"): MultipartBody.Part? {
    return try {
        val resolver = context.contentResolver
        val tipo = resolver.getType(uri) ?: "image/jpeg"
        val extension = when {
            tipo.contains("png") -> "png"
            tipo.contains("webp") -> "webp"
            else -> "jpg"
        }
        val archivoTemp = File(context.cacheDir, "foto_perfil_${System.currentTimeMillis()}.$extension")
        resolver.openInputStream(uri)?.use { input ->
            archivoTemp.outputStream().use { output -> input.copyTo(output) }
        } ?: return null

        val body = archivoTemp.asRequestBody(tipo.toMediaTypeOrNull())
        MultipartBody.Part.createFormData(nombreCampo, archivoTemp.name, body)
    } catch (e: Exception) {
        null
    }
}

fun textoAParteTexto(valor: String) = valor.toRequestBody("text/plain".toMediaTypeOrNull())

fun nombreArchivoDeUri(context: Context, uri: Uri): String {
    var nombre = "foto.jpg"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) {
            nombre = cursor.getString(idx) ?: nombre
        }
    }
    return nombre
}
