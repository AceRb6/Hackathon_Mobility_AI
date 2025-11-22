package com.example.hackathon_ai_mobility

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hackathon_ai_mobility.inicial.PantallaInicial
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ControladorDeNavegacion(navHostController: NavHostController, auth: FirebaseAuth) {

    /*Que pantalla inicia*/
    NavHost(navController = navHostController, startDestination = "navInicial"){

        composable("navInicial"){
            PantallaInicial(
                navegarPantallaIngresar = {navHostController.navigate("navIngresar")},
                navegarPantallaRegistro = {navHostController.navigate("navRegistro")}
            )
        }



    }

}