package com.example.hackathon_ai_mobility

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.hackathon_ai_mobility.ui.theme.Hackathon_AI_MobilityTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    /*aqui se inicializa el navHostController*/
    private lateinit var navHostController: NavHostController
    /*aqui se inicializa el auth*/
    private lateinit var auth: FirebaseAuth
    /*aqui se inicializa la base de datos*//*esta parte se recorrre a homeviewmodel*/
    //private lateinit var db: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        //db = Firebase.firestore <---esto se traslado a homeviewmodel


        enableEdgeToEdge()
        setContent {

            navHostController = rememberNavController()
            val UsuarioActual = auth.currentUser


            Hackathon_AI_MobilityTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    /*estto tiene que coincidir con el NavHost de ControladorDeNavegacion*/
                    ControladorDeNavegacion(navHostController, auth /*, db*/)


                    // Navegar automáticamente si hay sesión
                    /* LaunchedEffect(UsuarioActual) {
                         if (UsuarioActual != null) {
                             //navegar a home
                             Log.i("<---Ariel--->", "Usuario logueado (mantiene sesión)")
                            // navHostController.navigate("navPrincipalUsuario") {
                                 navHostController.navigate("navPrincipalAdmin") {
                                 //popUpTo("navInicial") { inclusive = true }//recordar cambiar esta linea cuando ya acabe la pantalla
                                 popUpTo("navPrincipalAdmin") { inclusive = true }
                                 }
                             //Desloguear cuenta
                             /*auth.signOut()
                             Log.i("<---Ariel--->", "Usuario deslogueado (cerro sesión)")*/
                         } else {
                             Log.i("<---Ariel--->", "Usuario no está logueado")
                         }
                     }*/
                    // Navegar automáticamente si hay sesión
                    LaunchedEffect(UsuarioActual) {
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            val email = currentUser.email?.lowercase()
                            Log.i("<---Ariel--->", "Usuario logueado (mantiene sesión)")

                            if (email != null && email.endsWith("@metro-cdmx.gob")) {
                                // Auto-login administrador
                                navHostController.navigate("navReportesJefeEstacion") {
                                    popUpTo("navInicial") { inclusive = true }
                                }
                            } else {
                                // Auto-login usuario normal
                                navHostController.navigate("navReportesJefeEstacion") {
                                    popUpTo("navInicial") { inclusive = true }
                                }
                            }

                        } else {
                            Log.i("<---Ariel--->", "Usuario no está logueado")
                        }
                    }



                }

            }
        }
    }

    /*override fun onStart() {
        super.onStart()
        val UsuarioActual = auth.currentUser
        if (UsuarioActual != null) {
            //navegar a home
            //navHostController.navigate("navIngresar")
            Log.i("<---Ariel--->", "Usuario logueado(mantiene Sesion)")

            //Desloguear cuenta
            //auth.signOut()
        }else{
            Log.i("<---Ariel--->", "Usuario No esta logueado")
        }
    }*/
}

