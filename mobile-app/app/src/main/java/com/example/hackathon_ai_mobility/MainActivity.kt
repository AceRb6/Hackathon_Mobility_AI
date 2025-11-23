package com.example.hackathon_ai_mobility

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.hackathon_ai_mobility.ui.theme.Hackathon_AI_MobilityTheme
import com.example.hackathon_ai_mobility.utils.obtenerRolDesdeCorreo
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private lateinit var navHostController: NavHostController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        enableEdgeToEdge()
        setContent {
            navHostController = rememberNavController()
            val usuarioActual = auth.currentUser

            Hackathon_AI_MobilityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    // Carga el mapa de navegación
                    ControladorDeNavegacion(navHostController, auth)

                    // AUTO-LOGIN SEGURO
                    LaunchedEffect(usuarioActual) {
                        if (usuarioActual != null) {
                            val correo = usuarioActual.email ?: ""
                            val rol = obtenerRolDesdeCorreo(correo)

                            val rutaDestino = when (rol) {
                                "jefeestacion" -> "navJefeEstacion"
                                "regulador" -> "navRegulador"
                                "tecnico" -> "navTecnico"
                                else -> null
                            }

                            if (rutaDestino != null) {
                                Log.i("AutoLogin", "Navegando a $rutaDestino")

                                // CORRECCIÓN DEL ERROR AQUÍ:
                                // Usamos try-catch y un método más seguro para limpiar el historial
                                try {
                                    navHostController.navigate(rutaDestino) {
                                        // En lugar de "navInicial", limpiamos todo el historial de forma segura
                                        popUpTo(0) { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    Log.e("AutoLogin", "Error navegando: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}