package ar.com.tikaservi.app.data.session

import kotlinx.coroutines.flow.MutableSharedFlow

// B-03: cuando el backend rechaza un X-Pasajero-Token (401, sesion vencida o
// cerrada desde otro dispositivo), ApiClient emite aca y AppNav limpia la
// sesion y navega al login. No cubre al chofer porque hoy el cliente todavia
// no manda X-Conductor-Token en ningun pedido (rollout gradual pendiente).
object SessionEvents {
    val sesionExpirada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
}
