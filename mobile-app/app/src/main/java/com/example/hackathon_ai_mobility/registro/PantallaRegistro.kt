package com.example.hackathon_ai_mobility.registro

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
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hackathon_ai_mobility.R
import com.example.hackathon_ai_mobility.modelos.ModeloUsuarioBD
import com.example.hackathon_ai_mobility.ui.theme.FieldActivado
import com.example.hackathon_ai_mobility.ui.theme.FieldDesactivado
import com.example.hackathon_ai_mobility.utils.obtenerRolDesdeCorreo
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun PantallaRegistro(
    auth: FirebaseAuth,
    navegarPantallaInicial: () -> Unit = {},
    // Callback para navegar a la pantalla correspondiente tras registro exitoso
    navegarAlHome: (String) -> Unit = {}
) {
    // --- DECLARACIÓN DE VARIABLES (CRÍTICO: Deben estar aquí al inicio) ---
    var correoRegistro by remember { mutableStateOf("") }
    var contraseñaRegistro by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    // Inicializar Firestore
    val db = Firebase.firestore

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
                    .background(color = Color.Gray)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(48.dp))

        /* Título Registro */
        Text("Registro", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Spacer(Modifier.height(48.dp))

        /* Campo Correo */
        Text("Correo electrónico", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        TextField(
            value = correoRegistro,
            onValueChange = { correoRegistro = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado
            )
        )

        Spacer(Modifier.height(24.dp))

        /* Campo Contraseña */
        Text("Contraseña", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        TextField(
            value = contraseñaRegistro,
            onValueChange = { contraseñaRegistro = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado
            )
        )

        Spacer(Modifier.height(24.dp))

        // Mensaje de error en rojo si existe
        if (mensajeError.isNotEmpty()) {
            Text(mensajeError, color = Red, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
        }

        /* BOTÓN DE REGISTRO */
        Button(onClick = {
            mensajeError = ""

            // 1. Validar formato del correo antes de enviar a Firebase
            val rolDetectado = obtenerRolDesdeCorreo(correoRegistro)

            if (rolDetectado == null) {
                mensajeError = "El correo debe ser @metro.[cargo].gob.mx"
                return@Button
            }

            if (contraseñaRegistro.length < 6) {
                mensajeError = "La contraseña debe tener al menos 6 caracteres"
                return@Button
            }

            // 2. Crear usuario en Authentication
            auth.createUserWithEmailAndPassword(correoRegistro, contraseñaRegistro)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid
                        if (uid != null) {

                            // 3. Guardar datos extra en Firestore (usuariosBD)
                            val objetoUsuario = ModeloUsuarioBD(
                                correoUsuarioBD = correoRegistro,
                                rolBD = rolDetectado, // Usamos el rol extraído
                                nombreUsuarioBD = correoRegistro.substringBefore("@")
                            )

                            db.collection("usuariosBD").document(uid).set(objetoUsuario)
                                .addOnSuccessListener {
                                    Log.i("Registro", "Usuario guardado en BD con rol: $rolDetectado")

                                    // 4. Navegar a la pantalla correcta
                                    when(rolDetectado) {
                                        "jefeestacion" -> navegarAlHome("navJefeEstacion")
                                        "regulador" -> navegarAlHome("navRegulador")
                                        "tecnico" -> navegarAlHome("navTecnico")
                                        else -> navegarPantallaInicial()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    mensajeError = "Error al guardar datos: ${e.message}"
                                }
                        }
                    } else {
                        mensajeError = "Error al registrar: ${task.exception?.message}"
                    }
                }
        }) {
            Text(text = "Registrar usuario")
        }
    }
}