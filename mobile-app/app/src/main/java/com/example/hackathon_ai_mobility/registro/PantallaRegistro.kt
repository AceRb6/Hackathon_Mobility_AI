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
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import androidx.compose.ui.graphics.Color.Companion.Black
import com.example.hackathon_ai_mobility.R
import com.example.hackathon_ai_mobility.modelos.ModeloUsuarioBD
import com.example.hackathon_ai_mobility.ui.theme.FieldActivado
import com.example.hackathon_ai_mobility.ui.theme.FieldDesactivado

@Composable
fun PantallaRegistro(
    auth: FirebaseAuth,
    navegarPantallaInicial: () -> Unit = {}
    //navegarPantallaPrincipal: () -> Unit = {},
    //navegarPantallaPrincipalAdmin: () -> Unit = {}
) {

    var correoRegistro by remember { mutableStateOf("") }
    var contraseñaRegistro by remember { mutableStateOf("") }

    /*Conexion con la base de datos para guardar a los usuarios*/
    var db: FirebaseFirestore = Firebase.firestore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(horizontal = 32.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* Icono Flechita*/
        /*falta agregar el navigate de iniciar sesion y
        el navigate to back de la flechita
         */
        Row(){
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "",
                tint = White,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(30.dp)
                    .clickable { navegarPantallaInicial() }
                    //ver donde esta la flecha
                    .background(color = Color.Gray)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(48.dp))

        /*Titulo de REGISTRO*/
        Text("Registro", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Spacer(Modifier.height(48.dp))

        Text("Correo electronico", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = correoRegistro,
            onValueChange = { correoRegistro = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(

                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado


            )
        )

        Spacer(Modifier.height(48.dp))

        Text("Contraseña", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = contraseñaRegistro,
            onValueChange = { contraseñaRegistro = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(

                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado


            )
        )

        Spacer(Modifier.height(48.dp))

        /*registrar con Usuario y contraseña*/
        Button(onClick = {

            // LOG ANTES DE INTENTAR REGISTRAR
            Log.d(
                "FirebaseAuthRegistro",
                "Intentando registrar correo=$correoRegistro, longitudPassword=${contraseñaRegistro.length}"
            )


            auth.createUserWithEmailAndPassword(correoRegistro, contraseñaRegistro)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        Log.i("FirebaseAuthRegistro", "Registro en Auth exitoso")

                        Log.i("Ariel", "Registrado")

                        val uid = task.result?.user?.uid
                        if (uid != null) {

                            // 1. Determinar si es admin por el dominio
                            val correoLower = correoRegistro.lowercase()
                            val esAdmin = correoLower.endsWith("@metro-cdmx.gob")

                            // 2. Definir rol según el dominio
                            val rol = if (esAdmin) "Administrador" else "Usuario"

                            // 3. Crear objeto para la BD con el rol correcto
                            val objetoUsuario = ModeloUsuarioBD(
                                correoUsuarioBD = correoRegistro,
                                rolBD = rol,
                                nombreUsuarioBD = correoRegistro.substringBefore("@")
                            )

                            val docRef = db.collection("usuariosBD").document(uid)

                            docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        Log.i("Firestore", "El usuario ya existe, no se volverá a crear.")
                                    } else {
                                        docRef.set(objetoUsuario)
                                            .addOnSuccessListener {
                                                Log.i("Firestore", "Usuario guardado correctamente")

                                                // 4. Navegar según el rol
                                                if (esAdmin) {
                                                   // navegarPantallaPrincipalAdmin()
                                                } else {
                                                    //navegarPantallaPrincipal()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "Error al guardar usuario", e)
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error al consultar si el usuario existe", e)
                                }
                        } else {
                            Log.e("Firestore", "El UID del usuario es nulo")
                        }

                    } else {
                        Log.i("Ariel", "No registrado")
                        // LOG DETALLADO DEL ERROR DE AUTH
                        val e = task.exception
                        Log.e(
                            "FirebaseAuthRegistro",
                            "Error al registrar usuario en Auth: ${e?.message}",
                            e
                        )

                    }

                }

        }) {
            Text(text = "Registrar usuario")
        }


    }
}