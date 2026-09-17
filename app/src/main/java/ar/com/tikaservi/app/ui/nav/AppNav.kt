package ar.com.tikaservi.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.chofer.ChoferHomeScreen
import ar.com.tikaservi.app.ui.chofer.ChoferLoginScreen
import ar.com.tikaservi.app.ui.chofer.ChoferPublicarViajeScreen
import ar.com.tikaservi.app.ui.chofer.ChoferRegistroScreen
import ar.com.tikaservi.app.ui.chofer.ChoferReservasViajeScreen
import ar.com.tikaservi.app.ui.chofer.ChoferVehiculosScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroHomeScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroLoginScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroMisReservasScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroRegistroScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroReservaScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroVerificarEmailScreen
import ar.com.tikaservi.app.ui.role.RoleSelectionScreen

private object Rutas {
    const val SELECCION_ROL = "seleccion_rol"

    const val LOGIN_PASAJERO = "login_pasajero"
    const val REGISTRO_PASAJERO = "registro_pasajero"
    const val VERIFICAR_EMAIL_PASAJERO = "verificar_email_pasajero/{pasajeroId}"
    const val HOME_PASAJERO = "home_pasajero"
    const val VIAJE_DETALLE = "viaje_detalle/{viajeId}"
    const val MIS_RESERVAS_PASAJERO = "mis_reservas_pasajero"

    const val LOGIN_CHOFER = "login_chofer"
    const val REGISTRO_CHOFER = "registro_chofer"
    const val HOME_CHOFER = "home_chofer"
    const val VEHICULOS_CHOFER = "vehiculos_chofer"
    const val PUBLICAR_VIAJE = "publicar_viaje"
    const val RESERVAS_VIAJE = "reservas_viaje/{viajeId}"

    fun verificarEmail(pasajeroId: Int) = "verificar_email_pasajero/$pasajeroId"
    fun viajeDetalle(viajeId: Int) = "viaje_detalle/$viajeId"
    fun reservasViaje(viajeId: Int) = "reservas_viaje/$viajeId"
}

@Composable
fun TikaserviNavHost() {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val session = remember { SessionManager(context) }

    val inicio = when {
        session.haySesionPasajero -> Rutas.HOME_PASAJERO
        session.haySesionChofer -> Rutas.HOME_CHOFER
        else -> Rutas.SELECCION_ROL
    }

    NavHost(navController = navController, startDestination = inicio) {
        composable(Rutas.SELECCION_ROL) {
            RoleSelectionScreen(
                onElegirPasajero = { navController.navigate(Rutas.LOGIN_PASAJERO) },
                onElegirChofer = { navController.navigate(Rutas.LOGIN_CHOFER) }
            )
        }

        // ---------------- Pasajero ----------------

        composable(Rutas.LOGIN_PASAJERO) {
            PasajeroLoginScreen(
                onLoginExitoso = {
                    navController.navigate(Rutas.HOME_PASAJERO) {
                        popUpTo(Rutas.SELECCION_ROL) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO_PASAJERO) },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.REGISTRO_PASAJERO) {
            PasajeroRegistroScreen(
                onRegistroExitoso = { pasajeroId ->
                    navController.navigate(Rutas.verificarEmail(pasajeroId)) {
                        popUpTo(Rutas.LOGIN_PASAJERO) { inclusive = false }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            Rutas.VERIFICAR_EMAIL_PASAJERO,
            arguments = listOf(navArgument("pasajeroId") { type = NavType.IntType })
        ) { entrada ->
            val pasajeroId = entrada.arguments?.getInt("pasajeroId") ?: -1
            PasajeroVerificarEmailScreen(
                pasajeroId = pasajeroId,
                onVerificado = {
                    navController.navigate(Rutas.HOME_PASAJERO) {
                        popUpTo(Rutas.SELECCION_ROL) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.HOME_PASAJERO) {
            PasajeroHomeScreen(
                onCerrarSesion = {
                    navController.navigate(Rutas.SELECCION_ROL) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerViaje = { viajeId -> navController.navigate(Rutas.viajeDetalle(viajeId)) },
                onVerMisReservas = { navController.navigate(Rutas.MIS_RESERVAS_PASAJERO) }
            )
        }
        composable(
            Rutas.VIAJE_DETALLE,
            arguments = listOf(navArgument("viajeId") { type = NavType.IntType })
        ) { entrada ->
            val viajeId = entrada.arguments?.getInt("viajeId") ?: -1
            PasajeroReservaScreen(
                viajeId = viajeId,
                onReservaExitosa = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.MIS_RESERVAS_PASAJERO) {
            PasajeroMisReservasScreen(onVolver = { navController.popBackStack() })
        }

        // ---------------- Chofer ----------------

        composable(Rutas.LOGIN_CHOFER) {
            ChoferLoginScreen(
                onLoginExitoso = {
                    navController.navigate(Rutas.HOME_CHOFER) {
                        popUpTo(Rutas.SELECCION_ROL) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO_CHOFER) },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.REGISTRO_CHOFER) {
            ChoferRegistroScreen(
                onRegistroExitoso = {
                    navController.navigate(Rutas.HOME_CHOFER) {
                        popUpTo(Rutas.SELECCION_ROL) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.HOME_CHOFER) {
            ChoferHomeScreen(
                onCerrarSesion = {
                    navController.navigate(Rutas.SELECCION_ROL) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onVerVehiculos = { navController.navigate(Rutas.VEHICULOS_CHOFER) },
                onPublicarViaje = { navController.navigate(Rutas.PUBLICAR_VIAJE) },
                onVerReservas = { viajeId -> navController.navigate(Rutas.reservasViaje(viajeId)) }
            )
        }
        composable(Rutas.VEHICULOS_CHOFER) {
            ChoferVehiculosScreen(onVolver = { navController.popBackStack() })
        }
        composable(Rutas.PUBLICAR_VIAJE) {
            ChoferPublicarViajeScreen(
                onPublicadoExitoso = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            Rutas.RESERVAS_VIAJE,
            arguments = listOf(navArgument("viajeId") { type = NavType.IntType })
        ) { entrada ->
            val viajeId = entrada.arguments?.getInt("viajeId") ?: -1
            ChoferReservasViajeScreen(
                viajeId = viajeId,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
