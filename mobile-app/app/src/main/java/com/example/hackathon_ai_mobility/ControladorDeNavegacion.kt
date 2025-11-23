package com.example.hackathon_ai_mobility

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hackathon_ai_mobility.ingreso.PantallaIngresar
import com.example.hackathon_ai_mobility.inicial.PantallaInicial
import com.example.hackathon_ai_mobility.registro.PantallaRegistro
import com.example.hackathon_ai_mobility.reportes.PantallaDeReportesJefeDeEstacion
import com.example.hackathon_ai_mobility.regulador.PantallaDeReportesRegulador
import com.google.firebase.auth.FirebaseAuth

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

        composable("navTecnico"){
            // Placeholder
            androidx.compose.material3.Text("Bienvenido Técnico")
        }
    }
}