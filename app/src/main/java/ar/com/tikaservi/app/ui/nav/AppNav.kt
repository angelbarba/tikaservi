package ar.com.tikaservi.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ar.com.tikaservi.app.data.session.SessionManager
import ar.com.tikaservi.app.ui.chofer.ChoferHomeScreen
import ar.com.tikaservi.app.ui.chofer.ChoferLoginScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroHomeScreen
import ar.com.tikaservi.app.ui.pasajero.PasajeroLoginScreen
import ar.com.tikaservi.app.ui.role.RoleSelectionScreen

private object Rutas {
    const val SELECCION_ROL = "seleccion_rol"
    const val LOGIN_PASAJERO = "login_pasajero"
    const val HOME_PASAJERO = "home_pasajero"
    const val LOGIN_CHOFER = "login_chofer"
    const val HOME_CHOFER = "home_chofer"
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
        composable(Rutas.LOGIN_PASAJERO) {
            PasajeroLoginScreen(
                onLoginExitoso = {
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
                }
            )
        }
        composable(Rutas.LOGIN_CHOFER) {
            ChoferLoginScreen(
                onLoginExitoso = {
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
                }
            )
        }
    }
}
