package com.example.hackathon_ai_mobility.reportes
/*
import androidx.compose.foundation.layout.heightIn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmetrocdmx.presentation.modelos.ModeloReportesBD
import com.example.appmetrocdmx.ui.theme.Black
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.appmetrocdmx.ui.theme.FieldActivado
import com.example.appmetrocdmx.ui.theme.FieldDesactivado
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.appmetrocdmx.presentation.modelos.EstacionBD
import androidx.compose.ui.focus.onFocusChanged
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun PantallaDeReportesJefeDeEstacion(

    /* auth: FirebaseAuth,
     viewmodel: ModeloDeVistaPantallaReportesUsuario = ModeloDeVistaPantallaReportesUsuario(),
     navegarPantallaPrincipal: () -> Unit = {},
     navegarPantallaMisReportesUsuario: () -> Unit = {} */
    auth: FirebaseAuth,
    viewmodel: ModeloDeVistaPantallaReportesUsuario = viewModel(),
    navegarPantallaPrincipal: () -> Unit = {},
    navegarPantallaMisReportesUsuario: () -> Unit = {}

){

    //AQUI EMPIEZA EL EJEMPLO DE LO QUE TENGO QUE MOSTRAR
    val variableParaParaConsumirViewmodel: State<List<ModeloReportesBD>> = viewmodel.listaReportesBD.collectAsState()

    //esto es para agregar nuevos artistas
    val nombre = remember { mutableStateOf("") }
    //val descripcion = remember { mutableStateOf("") }
    val imagenUrl = remember { mutableStateOf("") }
    //--->val estacion  tengo que crear esta BD

    //AQUI TERMINA EL EJEMPLO DE LO QUE TENGO QUE MOSTRAR

    //Variables de la seccion DESCRIPCION DEL REPORTE
    val descripcion = remember { mutableStateOf("") }


    //Esto es para Crear el reporte ya con la estacion seleccionada------------------

    // Estaciones desde Firestore
    val listaEstaciones by viewmodel.listaEstacionesBD.collectAsState()

    // Texto del buscador y selección
    var textoEstacion by remember { mutableStateOf("") }
    var estacionSeleccionada by remember { mutableStateOf<EstacionBD?>(null) }

    // Diálogos
    var mostrarDialogoEstacionCerrada by remember { mutableStateOf(false) }
    var mostrarDialogoGracias by remember { mutableStateOf(false) }

    //FIN Crear el reporte ya con la estacion seleccionada------------------




    //AQUI EMPIEZA EL MAQUETADO DEL BACKGROUND DE PANTALLAREPORTESUSUARIO
    Column(

        Modifier
            .fillMaxSize()
            .background(Black)

    ) {

        //TITULO DE LA PANTALLA
        Text(
            "Crear Reporte",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            modifier = Modifier.padding(16.dp)
        )
        //FIN TITULO DE LA PANTALLA



        //BOTON PARA IR A PANTALLA PRINCIPAL
        Button(onClick = {

            navegarPantallaPrincipal()

        }) {

            Text("Ir a Pantalla principal")

        }
        //FINAL BOTON PARA IR A PANTALLA PRINCIPAL

        //BOTON PARA IR A PANTALLA PRINCIPAL-----------
        Button(onClick = {

            navegarPantallaMisReportesUsuario()

        }) {

            Text("Ir a Pantalla Mis Reportes")

        }
        //FINAL BOTON PARA IR A PANTALLA PRINCIPAL------------


        //INICIO ESCOGER ESTACION
        Text(
            "Selecciona estacion",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier.padding(16.dp)
        )

        BuscadorEstacion(
            texto = textoEstacion,
            onTextoChange = { nuevoTexto ->
                textoEstacion = nuevoTexto
                estacionSeleccionada = null // se limpia si están escribiendo otra cosa
            },
            estaciones = listaEstaciones,
            onEstacionElegida = { estacion ->
                // aquí validamos si está abierta
                if (estacion.abierta == 1) {
                    estacionSeleccionada = estacion
                } else {
                    estacionSeleccionada = null
                    mostrarDialogoEstacionCerrada = true
                }
            }
        )
        //FINAL ESCOGER ESTACION



        //INICIO CUADRO DE TEXTO DESCRIPCION DEL PROBLEMA
        Text(
            "Descripcion del problema",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            modifier = Modifier.padding(16.dp)
        )

        DescripcionField(
            descripcion = descripcion.value,
            onDescripcionChange = { nuevaDescripcion ->
                descripcion.value = nuevaDescripcion
            }
        )


        //FINAL CUADRO DE TEXTO DESCRIPCION DEL PROBLEMA

        //BOTON PARA CREAR REPORTE
        Button(onClick = {

            val estacionNombre = estacionSeleccionada?.nombre

            // Si no se seleccionó estación, simplemente no dejamos continuar
            if (estacionNombre.isNullOrBlank()) {
                // aquí podrías poner otro diálogo/snackbar si quieres
                return@Button
            }

            // Crear el reporte con estación + descripción
            viewmodel.cargarDatosReportes(
                descripcionReporte = descripcion.value,
                estacionSeleccionada = estacionNombre
            )

            // Mostrar mensaje de agradecimiento
            mostrarDialogoGracias = true


            //viewmodel.cargarDatosReportes()
            //viewmodel.cargarDatosReportes(descripcion.value)

        }) {

            Text("Añadir reporte")

        }
        //FINAL BOTON PARA CREAR REPORTE

        //COLUMNA PARA VER LOS REPORTES
        /*LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp) // ya damos padding en cada card
        ){

            items(variableParaParaConsumirViewmodel.value) {

                ItemReporte(it)



            }

        }*/
        //FIN COLUMNA PARA VER LOS REPORTES





    }
    //AQUI TERMINA EL MAQUETADO DEL BACKGROUND DE PANTALLAREPORTESUSUARIO


    // Diálogos DE ESTACION CERRADADA
    if (mostrarDialogoEstacionCerrada) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEstacionCerrada = false },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoEstacionCerrada = false }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Estación cerrada") },
            text = { Text("Esta estación ya se encuentra cerrada, gracias por tu ayuda") }
        )
    }
    //FIN Diálogos DE ESTACION CERRADADA

    //DIALOGOS CREACION DE REPORTE CORRECTO
    if (mostrarDialogoGracias) {
        AlertDialog(
            onDismissRequest = { /* no hacer nada para obligar a elegir */ },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoGracias = false
                    navegarPantallaPrincipal()  // regreso a pantalla principal
                }) {
                    Text("Continuar")
                }
            },
            title = { Text("¡Gracias!") },
            text = { Text("Tu reporte se ha enviado correctamente. ¡Gracias por tu ayuda!") }
        )
    }

    //FIN DIALOGOS CREACION DE REPORTE CORRECTO


}


@Composable
fun DescripcionField(
    descripcion: String,
    onDescripcionChange: (String) -> Unit
) {
    // var descripcion by remember { mutableStateOf("") }
    val maxLength = 350



    OutlinedTextField(
        value = descripcion,
        onValueChange = { textoReporte: String ->
            if (textoReporte.length <= maxLength) {
                onDescripcionChange(textoReporte)
            }
        },
        label = { Text("Descripción") },
        placeholder = { Text("Escribe una descripción...") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 300.dp),
        maxLines = 10,
        singleLine = false,
        colors = TextFieldDefaults.colors(

            unfocusedContainerColor = FieldDesactivado,
            focusedContainerColor = FieldActivado


        ),
        supportingText = {
            Text("${descripcion.length} / $maxLength")
        }
    )
}

/*
@Composable
fun BuscadorEstacion(
    texto: String,
    onTextoChange: (String) -> Unit,
    estaciones: List<EstacionBD>,
    onEstacionElegida: (EstacionBD) -> Unit
) {
    Column {
        OutlinedTextField(
            value = texto,
            onValueChange = { onTextoChange(it) },
            label = { Text("Nombre de la estación") },
            placeholder = { Text("Escribe el nombre de la estación") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado
            )
        )

        val sugerencias = remember(texto, estaciones) {
            if (texto.isBlank()) {
                emptyList()
            } else {
                estaciones
                    .filter { estacion ->
                        estacion.nombre?.startsWith(texto, ignoreCase = true) == true
                    }
                    .sortedBy { it.nombre }
                    .take(5)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 180.dp)
        ) {
            items(sugerencias) { estacion ->
                Text(
                    text = estacion.nombre.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTextoChange(estacion.nombre.orEmpty())
                            onEstacionElegida(estacion)
                        }
                        .padding(8.dp),
                    color = Color.White
                )
            }
        }
    }
}
*/
@Composable
fun BuscadorEstacion(
    texto: String,
    onTextoChange: (String) -> Unit,
    estaciones: List<EstacionBD>,
    onEstacionElegida: (EstacionBD) -> Unit
) {
    var mostrarSugerencias by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = texto,
            onValueChange = { nuevoTexto ->
                onTextoChange(nuevoTexto)
                // Cada vez que escribe, volvemos a mostrar las sugerencias
                mostrarSugerencias = true
            },
            label = { Text("Nombre de la estación") },
            placeholder = { Text("Escribe el nombre de la estación") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .onFocusChanged { focusState ->
                    // Si tiene foco y hay texto, mostramos sugerencias
                    mostrarSugerencias = focusState.isFocused && texto.isNotBlank()
                },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado
            )
        )

        val sugerencias = remember(texto, estaciones) {
            if (texto.isBlank()) {
                emptyList()
            } else {
                estaciones
                    .filter { estacion ->
                        estacion.nombre?.startsWith(texto, ignoreCase = true) == true
                    }
                    .sortedBy { it.nombre }
                    .take(5)
            }
        }

        if (mostrarSugerencias && sugerencias.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
            ) {
                items(sugerencias) { estacion ->
                    Text(
                        text = estacion.nombre.orEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTextoChange(estacion.nombre.orEmpty())
                                onEstacionElegida(estacion)
                                // Al elegir, escondemos las opciones
                                mostrarSugerencias = false
                            }
                            .padding(8.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}*/