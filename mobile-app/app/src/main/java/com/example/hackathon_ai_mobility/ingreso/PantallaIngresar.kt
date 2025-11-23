package com.example.hackathon_ai_mobility.ingreso

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hackathon_ai_mobility.R
import com.example.hackathon_ai_mobility.ui.theme.FieldActivado
import com.example.hackathon_ai_mobility.ui.theme.FieldDesactivado
import com.example.hackathon_ai_mobility.utils.obtenerRolDesdeCorreo // Asegúrate de que este import exista
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PantallaIngresar(
    auth: FirebaseAuth,
    navegarPantallaInicial: () -> Unit = {},
    // Navegación específica por rol
    navegarAJefeEstacion: () -> Unit = {},
    navegarARegulador: () -> Unit = {},
    navegarATecnico: () -> Unit = {}
) {
    // VARIABLES DE ESTADO (Estas faltaban y causaban el error)
    var correoIngresar by remember { mutableStateOf("") }
    var contraseñaIngresar by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* Icono Flecha Atrás */
        Row {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "Volver",
                tint = White,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(30.dp)
                    .clickable { navegarPantallaInicial() }
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(48.dp))

        /* Título */
        Text("Ingresar", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Spacer(Modifier.height(48.dp))

        /* Input Correo */
        Text("Correo electrónico", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        TextField(
            value = correoIngresar,
            onValueChange = { correoIngresar = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado
            )
        )

        Spacer(Modifier.height(24.dp))

        /* Input Contraseña */
        Text("Contraseña", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        TextField(
            value = contraseñaIngresar,
            onValueChange = { contraseñaIngresar = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado
            )
        )

        Spacer(Modifier.height(48.dp))

        /* BOTÓN DE INICIO DE SESIÓN CON LÓGICA DE ROL */
        Button(onClick = {
            // Validar que los campos no estén vacíos
            if (correoIngresar.isNotBlank() && contraseñaIngresar.isNotBlank()) {
                auth.signInWithEmailAndPassword(correoIngresar, contraseñaIngresar)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("Login", "Ingreso correcto")

                            // 1. Validar Rol desde el correo
                            val rol = obtenerRolDesdeCorreo(correoIngresar)

                            // 2. Navegar según el rol detectado
                            when (rol) {
                                "jefeestacion" -> navegarAJefeEstacion()
                                "regulador" -> navegarARegulador()
                                "tecnico" -> navegarATecnico()
                                else -> {
                                    Log.w("Login", "Correo válido pero rol desconocido")
                                    // Opcional: Mostrar mensaje de error al usuario
                                }
                            }
                        } else {
                            Log.e("Login", "Error: ${task.exception?.message}")
                        }
                    }
            }
        }) {
            Text(text = "Iniciar sesión")
        }
    }
}