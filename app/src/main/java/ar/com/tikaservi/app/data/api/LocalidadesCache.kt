package ar.com.tikaservi.app.data.api

// Cache simple en memoria de GET /localidades (lista fija de nombres exactos
// que acepta el backend para origen/destino). Se pide una sola vez por
// proceso de la app.
object LocalidadesCache {
    @Volatile private var cache: List<String>? = null

    suspend fun obtener(forzar: Boolean = false): List<String> {
        if (!forzar) {
            cache?.let { return it }
        }
        val lista = ApiClient.api.getLocalidades()
        cache = lista
        return lista
    }
}
