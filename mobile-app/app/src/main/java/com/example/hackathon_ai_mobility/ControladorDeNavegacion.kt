package com.example.hackathon_ai_mobility

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hackathon_ai_mobility.ingreso.PantallaIngresar
import com.example.hackathon_ai_mobility.inicial.PantallaInicial
import com.example.hackathon_ai_mobility.registro.PantallaRegistro
import com.example.hackathon_ai_mobility.reportes.PantallaDeReportesJefeDeEstacion
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ControladorDeNavegacion(navHostController: NavHostController, auth: FirebaseAuth) {

    /*Que pantalla inicia*/
    NavHost(navController = navHostController, startDestination = "navReportesJefeEstacion"){

        composable("navInicial"){
            PantallaInicial(
                navegarPantallaIngresar = {navHostController.navigate("navIngresar")},
                navegarPantallaRegistro = {navHostController.navigate("navRegistro")}
            )
        }
        composable("navIngresar"){
            PantallaIngresar(
                auth,
                navegarPantallaInicial = {navHostController.navigate("navInicial")}
                //navegarPantallaPrincipal = {navHostController.navigate("navPrincipalUsuario")},
                //navegarPantallaPrincipalAdmin = { navHostController.navigate("navPrincipalAdmin")}
            )

        }

        composable("navRegistro"){
            PantallaRegistro(
                auth,
                navegarPantallaInicial = {navHostController.navigate("navInicial")}
                //navegarPantallaPrincipal = {navHostController.navigate("navPrincipalUsuario")},
                //navegarPantallaPrincipalAdmin = { navHostController.navigate("navPrincipalAdmin") }
            )
        }

        composable("navReportesJefeEstacion"){
            PantallaDeReportesJefeDeEstacion(
                auth,
                navegarPantallaInicial = {navHostController.navigate("navInicial")}

            )

        }



    }

}