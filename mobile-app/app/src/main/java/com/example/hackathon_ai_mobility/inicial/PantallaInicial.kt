package com.example.hackathon_ai_mobility.inicial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
        /*Funciones Lamda para la navegacion*/
fun PantallaInicial(navegarPantallaIngresar: () -> Unit = {}, navegarPantallaRegistro: () -> Unit = {}) {

    /*ESTOS VALORES ESTAN EN LOS COMPOSABLE  QUE TENEMOS AQUI
        Y ABAJO TENER EN CUENTA A LA HORA DE MODIFICAR*/

    /*valores de la pantalla (tener en cuenta si algo pasa quitarlos)*/
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalMargin = (configuration.screenWidthDp * 0.1f).dp // 10% de margen

    //AQUI EMPIEZA EL MAQUETADO DEL BACKGROUND DE INITIAL SCREEN
    /*fondo de la pantalla*/
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Gray, Black/*, Naranja_AppMetroCDMX*/),
                    startY = 100f, /*donde empieza lo gris*/
                    endY = 800f/*Float.POSITIVE_INFINITY*//*Donde empieza lo negro*/
                )
            ), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /*Icono App (checar que sea responsive)*/
        Spacer(modifier = Modifier.weight(.5f))
        Image(
            painter = painterResource(id = R.drawable.logo_appmetrocdmx),
            contentDescription = "",
            modifier = Modifier

                .fillMaxWidth(0.2f) // ancho de pantalla
                .aspectRatio(1f)    // Mantener forma cuadrada
                .clip(CircleShape) // Forma circular

        )/*TEXTO (en trabajo a futuro hacer el texto de manera responsiva)*/
        Spacer(modifier = Modifier.weight(.5f))
        Text(
            "Bienvenid@ \n" + "\n" + "Esta App se diseño para que " + "puedas generar rutas a través" + "del STC Metro, donde podras " + "informarte de cierre de " + "estaciones y opciones de " + "movilidad que puedas utilizar " + "en tu viaje.",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = horizontalMargin)
        )

        /*Botones (en trabajo a futuro hacer los botones de manera responsiva)*/
        Spacer(modifier = Modifier.weight(.5f))
        Button(
            onClick = {navegarPantallaRegistro()},/*ir a la pantalla de registro*/
            modifier = Modifier
                .fillMaxWidth()
                // .height(48.dp) /*investigar porque no lo necesita*/
                .padding(horizontal = horizontalMargin),
            colors = ButtonDefaults.buttonColors(containerColor = NaranjaAppMetroCDMX)
        ) {
            Text(
                text = "Registrarse",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,

                )/*texto boton registro*/
        }

        /*hay 2 formas de hacerlo (informarse de cual es la diferencia entre ellas)*/
        /*No importa el tamaño de la pantalla ni cuánto espacio libre haya en el Column/Row, siempre medirá 16 dp.*/
        Spacer(modifier = Modifier.height(16.dp))
        //BotonesPantallaRegistro()
        /*0.1f significa que este Spacer tomará 10% del espacio libre (no del total, sino del que sobra después de colocar los demás elementos).*/
        /* Spacer(modifier = Modifier.weight(.1f))
         BotonesPantallaRegistro()*/

        /*BOTON INGRESAR*/
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ingresar",
            color = Color.White,
            modifier = Modifier
                .padding(20.dp)
                .clickable { navegarPantallaIngresar()},/*Accion de ir a la pantalla de ingresar*/
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.weight(1f))

    }

    //AQUI TERMINA EL MAQUETADO DEL BACKGROUND DE INITIAL SCREEN


}

/*ESTE ES EL BOTON DE INGRESAR CON GOOGLE*/
@Composable
fun BotonesPantallaRegistro() {

    /*valores de la pantalla (tener en cuenta si algo pasa quitarlos)*/
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalMargin = (configuration.screenWidthDp * 0.1f).dp // 10% de margen


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)/*invesigar porque este si lo necesita*/
            .padding(horizontal = horizontalMargin)
            .background(BackgroundButton, CircleShape)
            .border(2.dp, ShapeButton, CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_google),
            contentDescription = "",
            modifier = Modifier /*aqui si importa el orden, si pones el pading antes o despues se modifica como se percibe la imagen*/
                .padding(start = 16.dp) //espacio entre el logo y el texto*/
                .size(20.dp)   /*Tamaño del logo de google (invesitgar como hacer este numero responsivo)*/

        )
        Text(
            text = "Continuar con Google",
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

    }
}