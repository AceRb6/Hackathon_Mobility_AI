package com.example.hackathon_ai_mobility.reportes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.presentation.modelos.EstacionBD
import com.example.hackathon_ai_mobility.presentation.viewmodel.ModeloDeVistaPantallaReportesUsuario
import com.example.hackathon_ai_mobility.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla principal para que el Jefe de Estación cree reportes de incidentes.
 * Utiliza un diseño limpio en Rojo y Blanco para alinearse con la identidad del Metro.
 */
@Composable
fun PantallaDeReportesJefeDeEstacion(
    auth: FirebaseAuth? = null, // Opcional por ahora para pruebas
    viewmodel: ModeloDeVistaPantallaReportesUsuario = viewModel(),
    navegarPantallaPrincipal: () -> Unit = {},
    navegarPantallaMisReportesUsuario: () -> Unit = {}
) {
    // Estados de la UI
    val listaEstaciones by viewmodel.listaEstacionesBD.collectAsState()
    
    // Variables del formulario
    var textoEstacion by remember { mutableStateOf("") }
    var estacionSeleccionada by remember { mutableStateOf<EstacionBD?>(null) }
    var descripcion by remember { mutableStateOf("") }

    // Control de Diálogos
    var mostrarDialogoEstacionCerrada by remember { mutableStateOf(false) }
    var mostrarDialogoGracias by remember { mutableStateOf(false) }

    // Estructura Principal (Fondo Blanco)
    Scaffold(
        containerColor = MetroWhite,
        topBar = {
            // Encabezado Rojo Institucional
            SmallTopAppBar(
                title = {
                    Text(
                        "Crear Reporte",
                        color = MetroWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MetroRed
                )
            )
        }
    ) { paddingValues ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECCIÓN 1: SELECCIÓN DE ESTACIÓN
            Card(
                colors = CardDefaults.cardColors(containerColor = MetroLightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "¿Dónde ocurrió el incidente?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MetroRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    BuscadorEstacion(
                        texto = textoEstacion,
                        onTextoChange = { nuevoTexto ->
                            textoEstacion = nuevoTexto
                            estacionSeleccionada = null // Reiniciar selección al escribir
                        },
                        estaciones = listaEstaciones,
                        onEstacionElegida = { estacion ->
                            if (estacion.abierta == 1) {
                                estacionSeleccionada = estacion
                                textoEstacion = estacion.nombre ?: ""
                            } else {
                                estacionSeleccionada = null
                                mostrarDialogoEstacionCerrada = true
                            }
                        }
                    )
                }
            }

            // SECCIÓN 2: DESCRIPCIÓN DEL PROBLEMA
            Card(
                colors = CardDefaults.cardColors(containerColor = MetroLightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().weight(1f) // Ocupa el espacio restante
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Detalles del Reporte",
                        style = MaterialTheme.typography.titleMedium,
                        color = MetroRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    DescripcionField(
                        descripcion = descripcion,
                        onDescripcionChange = { descripcion = it }
                    )
                }
            }

            // SECCIÓN 3: BOTONES DE ACCIÓN
            Button(
                onClick = {
                    val nombreEstacion = estacionSeleccionada?.nombre
                    if (!nombreEstacion.isNullOrBlank() && descripcion.isNotBlank()) {
                        viewmodel.cargarDatosReportes(descripcion, nombreEstacion)
                        mostrarDialogoGracias = true
                    }
                },
                enabled = estacionSeleccionada != null && descripcion.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MetroRed,
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ENVIAR REPORTE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            // Botones secundarios de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = navegarPantallaPrincipal) {
                    Text("Cancelar", color = MetroDarkGray)
                }
                TextButton(onClick = navegarPantallaMisReportesUsuario) {
                    Text("Ver Mis Reportes", color = MetroRed)
                }
            }
        }
    }

    // --- DIÁLOGOS ---

    if (mostrarDialogoEstacionCerrada) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEstacionCerrada = false },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoEstacionCerrada = false }) {
                    Text("Entendido", color = MetroRed)
                }
            },
            title = { Text("Estación Cerrada") },
            text = { Text("Esta estación no está operativa actualmente. Por favor selecciona otra.") },
            containerColor = MetroWhite,
            titleContentColor = MetroRed
        )
    }

    if (mostrarDialogoGracias) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoGracias = false
                        navegarPantallaPrincipal()
                        // Limpiar campos
                        textoEstacion = ""
                        descripcion = ""
                        estacionSeleccionada = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MetroRed)
                ) {
                    Text("Continuar")
                }
            },
            title = { Text("¡Reporte Enviado!") },
            text = { Text("Gracias por tu colaboración. Tu reporte ha sido registrado exitosamente.") },
            containerColor = MetroWhite,
            titleContentColor = MetroRed
        )
    }
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
        label = { Text("Describe el problema aquí...") },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), // Llena el espacio de la tarjeta
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MetroRed,
            unfocusedBorderColor = MetroDarkGray,
            cursorColor = MetroRed,
            containerColor = MetroWhite
        ),
        supportingText = {
            Text(
                text = "${descripcion.length} / $maxLength",
                modifier = Modifier.fillMaxWidth(),
                color = MetroDarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { mostrarSugerencias = it.isFocused && texto.isNotBlank() },
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MetroRed,
                unfocusedBorderColor = MetroDarkGray,
                cursorColor = MetroRed,
                containerColor = MetroWhite
            )
        )

        // Lista de sugerencias flotante
        val sugerencias = remember(texto, estaciones) {
            if (texto.isBlank()) emptyList()
            else estaciones.filter { 
                it.nombre?.contains(texto, ignoreCase = true) == true 
            }.take(5)
        }

        if (mostrarSugerencias && sugerencias.isNotEmpty()) {
            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.heightIn(max = 200.dp),
                colors = CardDefaults.cardColors(containerColor = MetroWhite)
            ) {
                LazyColumn {
                    items(sugerencias) { estacion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEstacionElegida(estacion)
                                    mostrarSugerencias = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Indicador de Línea (Círculo de color simulado)
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MetroRed, shape = RoundedCornerShape(50))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = estacion.nombre ?: "",
                                color = MetroBlack,
                                fontSize = 16.sp
                            )
                        }
                        Divider(color = MetroLightGray)
                    }
                }
            }
        }
    }
}