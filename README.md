# Tikaservi -- App Android (Pasajero / Chofer)

App nativa (Kotlin + Jetpack Compose) que consume la misma API real de
tikaservi.com.ar (https://tikaservi.com.ar/viajes-api/..., FastAPI +
PostgreSQL en el servidor cloud). No hay backend nuevo: es un cliente mas
de la API que ya usan conductor-viajes.html / pasajero-viajes.html.

## Estado actual (v0.1.0 -- scaffold inicial)

Hecho:
- Seleccion de rol (Pasajero / Chofer) al abrir la app.
- Login de pasajero y de chofer contra la API real.
- Home de pasajero: busqueda de viajes por origen/destino.
- Home de chofer: listado de "mis viajes publicados".
- Sesion persistida localmente (SharedPreferences), igual que el
  localStorage de la version web.

Pendiente (proximas iteraciones):
- Registro de pasajero/chofer (multipart, foto obligatoria).
- Publicar viaje (chofer) y reservar asiento/encomienda (pasajero),
  incluyendo el mapa de punto de retiro.
- Gestion de vehiculos.
- Notificaciones push nativas (Firebase Cloud Messaging) -- hoy el
  backend usa Web Push, hay que sumar un endpoint/logica para FCM.
- Icono definitivo (hoy hay uno provisorio, vectorial).

## Por que se compila con GitHub Actions y no en el Raspberry Pi

El Pi (192.168.1.100) es ARM64 (aarch64). Las herramientas oficiales de
compilacion de Android (aapt2, d8/r8, apksigner, dentro del Android SDK
build-tools) solo se distribuyen para Linux x86_64 -- no corren
nativamente aca. Por eso el codigo se escribe y versiona en el Pi, pero
la compilacion del APK la hace el workflow de GitHub Actions
(.github/workflows/build.yml), que corre en un runner x86_64 con el SDK
ya instalado.

Cada push a main (o ejecucion manual del workflow) genera un
tikaservi-debug-apk descargable desde la pestana Actions del repo.

## Como probar

1. Descargar el APK generado por el workflow.
2. Instalarlo en un Android real (o emulador) con adb install app-debug.apk.
3. Necesita conexion a internet -- pega directo contra tikaservi.com.ar,
   no hay modo offline.
4. Para probar el login hace falta un usuario/conductor ya existente en
   la base (se puede crear uno con el registro en la web mientras el
   registro nativo no esta listo).
