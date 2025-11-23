package com.example.hackathon_ai_mobility

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hackathon_ai_mobility.ingreso.PantallaIngresar
import com.example.hackathon_ai_mobility.inicial.PantallaInicial
import com.example.hackathon_ai_mobility.registro.PantallaRegistro
import com.example.hackathon_ai_mobility.reportes.PantallaDeReportesJefeDeEstacion
import com.example.hackathon_ai_mobility.regulador.PantallaDeReportesRegulador
import com.example.hackathon_ai_mobility.tecnico.PantallaTecnico
import com.google.firebase.auth.FirebaseAuth
import android.net.Uri
import com.example.hackathon_ai_mobility.prueba_mapa.usuario.ScreenDeMetroUsuario


@Composable
fun ControladorDeNavegacion(navHostController: NavHostController, auth: FirebaseAuth) {

    // Start destination DEBE ser "navInicial"
    NavHost(navController = navHostController, startDestination = "navInicial"){

        // PANTALLA INICIAL (Obligatoria para que no falle el arranque)
        composable("navInicial"){
            PantallaInicial(
                navegarPantallaIngresar = {navHostController.navigate("navIngresar")},
                navegarPantallaRegistro = {navHostController.navigate("navRegistro")}
            )
        }

        composable("navIngresar"){
            PantallaIngresar(
                auth,
                navegarPantallaInicial = {navHostController.navigate("navInicial")},
                navegarAJefeEstacion = {
                    navHostController.navigate("navJefeEstacion") { popUpTo("navInicial") { inclusive = true } }
                },
                navegarARegulador = {
                    navHostController.navigate("navRegulador") { popUpTo("navInicial") { inclusive = true } }
                },
                navegarATecnico = {
                    navHostController.navigate("navTecnico") { popUpTo("navInicial") { inclusive = true } }
                }
            )
        }

        composable("navRegistro"){
            PantallaRegistro(
                auth,
                navegarPantallaInicial = {navHostController.navigate("navInicial")},
                navegarAlHome = { ruta ->
                    navHostController.navigate(ruta) { popUpTo("navInicial") { inclusive = true } }
                }
            )
        }

        // Roles
        composable("navJefeEstacion"){
            PantallaDeReportesJefeDeEstacion(
                auth = auth,
                navegarPantallaInicial = {
                    auth.signOut()
                    navHostController.navigate("navInicial") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("navRegulador"){
            PantallaDeReportesRegulador(
                auth = auth,
                navegarPantallaInicial = {
                    auth.signOut()
                    navHostController.navigate("navInicial") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("navTecnico") {
            PantallaTecnico(
                auth = auth,
                navegarPantallaInicial = {
                    auth.signOut()
                    navHostController.navigate("navInicial") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navegarAMapaRutaOSM = { origen, destino ->
                    val origenEncoded = Uri.encode(origen)
                    val destinoEncoded = Uri.encode(destino)
                    navHostController.navigate("navMapaMetro/$origenEncoded/$destinoEncoded")
                }
            )
        }


        composable(
            route = "navMapaMetro/{origen}/{destino}",
            arguments = listOf(
                navArgument("origen") { type = NavType.StringType },
                navArgument("destino") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val origen = backStackEntry.arguments?.getString("origen") ?: ""
            val destino = backStackEntry.arguments?.getString("destino") ?: ""

            // Aquí mostramos la pantalla del mapa
            ScreenDeMetroUsuario(
                origenInicial = origen,
                destinoInicial = destino
            )
        }




    }
}