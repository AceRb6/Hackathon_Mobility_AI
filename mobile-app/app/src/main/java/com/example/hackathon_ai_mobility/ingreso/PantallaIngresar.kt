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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PantallaIngresar(
    auth: FirebaseAuth,
    navegarPantallaInicial: () -> Unit = {}
    //navegarPantallaPrincipal: () -> Unit = {},
    //navegarPantallaPrincipalAdmin: () -> Unit = {}
) {
    var correoIngresar by remember { mutableStateOf("") }
    var contraseñaIngresar by remember { mutableStateOf("") }

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
                    .clickable { navegarPantallaInicial()}
                    //ver donde esta la flecha
                    .background(color = Color.Gray)
            )
            Spacer(modifier = Modifier.weight(1f))
        }


        Spacer(Modifier.height(48.dp))

        /*Titulo de ingresar*/
        Text("Ingresar", color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Spacer(Modifier.height(48.dp))


        Text("Correo electronico", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = correoIngresar,
            onValueChange = { correoIngresar = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(

                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado


            )


        )

        Spacer(Modifier.height(48.dp))

        Text("Contraseña", color = White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
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

        /*ingresar con Usuario y contraseña*/
        Button(onClick = {

            auth.signInWithEmailAndPassword(correoIngresar, contraseñaIngresar)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("Ariel", "Ingreso correcto")

                        // Determinar a dónde navegar según el correo
                        val correoLower = correoIngresar.lowercase()
                        val esAdmin = correoLower.endsWith("@metro-cdmx.gob")

                        if (esAdmin) {
                            //navegarPantallaPrincipalAdmin()
                        } else {
                            //navegarPantallaPrincipal()
                        }

                    } else {
                        Log.i("Ariel", "Ingreso incorrecto")
                    }
                }

        }) {
            Text(text = "Iniciar sesion")
        }


    }
}

