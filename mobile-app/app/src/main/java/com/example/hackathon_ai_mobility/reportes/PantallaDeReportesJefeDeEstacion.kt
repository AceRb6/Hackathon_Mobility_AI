package com.example.hackathon_ai_mobility.reportes

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold // IMPORTANTE
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.modelos.EstacionBD
import com.example.hackathon_ai_mobility.ui.theme.FieldActivado
import com.example.hackathon_ai_mobility.ui.theme.FieldDesactivado
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@Composable
fun PantallaDeReportesJefeDeEstacion(
    auth: FirebaseAuth,
    viewmodel: ModeloDeVistaPantallaJefeDeEstacion = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
){
    // Variables de la pantalla
    val problema = remember { mutableStateOf("") }
    val descripcion = remember { mutableStateOf("") }
    val tipo = remember { mutableStateOf(0)}
    val hora = remember { mutableStateOf("") }
    val estacionMetro = remember { mutableStateOf("") }

    // Estaciones desde Firestore
    val listaEstaciones by viewmodel.listaEstacionesBD.collectAsState()

    // Texto del buscador y selección
    var textoEstacion by remember { mutableStateOf("") }
    var estacionSeleccionada by remember { mutableStateOf<EstacionBD?>(null) }

    // Diálogos
    var mostrarDialogoEstacionCerrada by remember { mutableStateOf(false) }
    var mostrarDialogoGracias by remember { mutableStateOf(false) }

    // --- SOLUCIÓN: SCAFFOLD PARA GESTIONAR LOS BORDES ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Black // El color de fondo va aquí ahora
    ) { innerPadding ->
        // 'innerPadding' contiene el margen calculado automáticamente (barra estado + barra navegación)

        Column(
            Modifier
                .padding(innerPadding) // APLICAMOS EL MARGEN AQUÍ
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // TITULO DE LA PANTALLA
            Text(
                "Jefe de estación",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                modifier = Modifier.padding(16.dp)
            )

            // BOTON PARA SALIR (Ir a Pantalla principal / Cerrar Sesión)
            Button(
                onClick = { navegarPantallaInicial() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Cerrar Sesión")
            }

            // BOTON PARA IR A Reportes Usuario (Placeholder)
            Button(
                onClick = { /* navegarPantallaMisReportesUsuario() */ },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Ir a Pantalla Mis Reportes")
            }

            // BUSCADOR DE ESTACIÓN
            Text(
                "Selecciona estación",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                modifier = Modifier.padding(16.dp)
            )

            BuscadorEstacion(
                texto = textoEstacion,
                onTextoChange = { nuevoTexto ->
                    textoEstacion = nuevoTexto
                    estacionSeleccionada = null
                },
                estaciones = listaEstaciones,
                onEstacionElegida = { estacion ->
                    if (estacion.abierta == 1) {
                        estacionSeleccionada = estacion
                        // Actualizamos también la variable visual si quieres
                        estacionMetro.value = estacion.nombre ?: ""
                    } else {
                        estacionSeleccionada = null
                        mostrarDialogoEstacionCerrada = true
                    }
                }
            )

            // CAMPO: TITULO PROBLEMA
            Text(
                "Titulo del problema",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                modifier = Modifier.padding(16.dp)
            )

            ProblemaField(
                problema = problema.value,
                onProblemaChange = { problema.value = it }
            )

            // CAMPO: DESCRIPCIÓN
            Text(
                "Descripción del problema",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                modifier = Modifier.padding(16.dp)
            )

            DescripcionField(
                descripcion = descripcion.value,
                onDescripcionChange = { descripcion.value = it }
            )

            // CAMPO: HORA
            Text(
                "Hora de inicio",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                modifier = Modifier.padding(16.dp)
            )

            HoraPickerField(hora)

            // Espacio final antes del botón
            //Spacer(modifier = Modifier.height(24.dp))

            // BOTON AÑADIR REPORTE
            Button(
                onClick = {
                    val estacionNombre = estacionSeleccionada?.nombre

                    if (estacionNombre.isNullOrBlank()) {
                        return@Button
                    }

                    viewmodel.cargarDatosReportes(
                        descripcionReporte = descripcion.value,
                        estacionSeleccionada = estacionNombre,
                        horaCuandoEsmpezoProblema = hora.value,
                        tipodelproblema = tipo.value,
                        reporteTecnico = "",
                        reporteStatus = 0 // Pendiente
                    )
                    mostrarDialogoGracias = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 50.dp) // Altura mínima para buen touch
            ) {
                Text("Añadir reporte", fontSize = 18.sp)
            }

            // Espacio extra al final para que el scroll no quede justo
            //Spacer(modifier = Modifier.height(50.dp))
        }
    }

    // --- DIÁLOGOS ---

    if (mostrarDialogoEstacionCerrada) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEstacionCerrada = false },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoEstacionCerrada = false }) {
                    Text("Aceptar")
                }
            },
            title = { Text("Estación cerrada") },
            text = { Text("Esta estación ya se encuentra cerrada.") }
        )
    }

    if (mostrarDialogoGracias) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoGracias = false
                    // Opcional: Limpiar campos
                    problema.value = ""
                    descripcion.value = ""
                    hora.value = ""
                    textoEstacion = ""
                    estacionSeleccionada = null
                }) {
                    Text("Continuar")
                }
            },
            title = { Text("¡Gracias!") },
            text = { Text("Tu reporte se ha enviado correctamente al Regulador.") }
        )
    }
}

// ... (Tus funciones ProblemaField, DescripcionField, HoraPickerField, BuscadorEstacion se mantienen igual abajo) ...
// COPIA Y PEGA TUS FUNCIONES AUXILIARES AQUÍ ABAJO SI NO LAS TIENES EN OTRO ARCHIVO
// (ProblemaField, DescripcionField, HoraPickerField, BuscadorEstacion)
@Composable
fun ProblemaField(
    problema: String,
    onProblemaChange: (String) -> Unit
){
    val maxLength = 350
    OutlinedTextField(
        value = problema,
        onValueChange = { if (it.length <= maxLength) onProblemaChange(it) },
        label = { Text("Problema") },
        placeholder = { Text("Escribe qué problema hay...") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp, max = 150.dp), // Ajusté altura para estética
        maxLines = 5,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = FieldDesactivado,
            focusedContainerColor = FieldActivado
        ),
        supportingText = { Text("${problema.length} / $maxLength") }
    )
}

@Composable
fun DescripcionField(
    descripcion: String,
    onDescripcionChange: (String) -> Unit
) {
    val maxLength = 350
    OutlinedTextField(
        value = descripcion,
        onValueChange = { if (it.length <= maxLength) onDescripcionChange(it) },
        label = { Text("Descripción") },
        placeholder = { Text("Detalles técnicos...") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp, max = 200.dp),
        maxLines = 10,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = FieldDesactivado,
            focusedContainerColor = FieldActivado
        ),
        supportingText = { Text("${descripcion.length} / $maxLength") }
    )
}

@Composable
fun HoraPickerField(hora: MutableState<String>) {
    val context = LocalContext.current
    OutlinedTextField(
        value = hora.value,
        onValueChange = { },
        label = { Text("Hora") },
        placeholder = { Text("Selecciona una hora") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                TimePickerDialog(
                    context,
                    { _, h, m -> hora.value = "%02d:%02d".format(h, m) },
                    hour, minute, true
                ).show()
            },
        readOnly = true,
        enabled = false, // Truco: deshabilitar input directo, pero el clickable del modifier sí funciona
        colors = TextFieldDefaults.colors(
            disabledContainerColor = FieldDesactivado,
            disabledTextColor = Color.White,
            disabledLabelColor = Color.Gray
        )
    )
}

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
            onValueChange = {
                onTextoChange(it)
                mostrarSugerencias = true
            },
            label = { Text("Nombre de la estación") },
            placeholder = { Text("Escribe para buscar...") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if(it.isFocused) mostrarSugerencias = true },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = FieldDesactivado,
                focusedContainerColor = FieldActivado
            )
        )

        val sugerencias = remember(texto, estaciones) {
            if (texto.isBlank()) emptyList()
            else estaciones.filter {
                it.nombre?.startsWith(texto, ignoreCase = true) == true
            }.take(5)
        }

        if (mostrarSugerencias && sugerencias.isNotEmpty()) {
            Column(Modifier.background(Color.DarkGray)) { // Fondo para sugerencias
                sugerencias.forEach { estacion ->
                    Text(
                        text = estacion.nombre.orEmpty(),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTextoChange(estacion.nombre.orEmpty())
                                onEstacionElegida(estacion)
                                mostrarSugerencias = false
                            }
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}