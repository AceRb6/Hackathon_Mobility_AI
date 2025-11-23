package com.example.hackathon_ai_mobility.reportes

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.presentation.modelos.EstacionBD
import com.example.hackathon_ai_mobility.presentation.modelos.ModeloReportesBD
import com.example.hackathon_ai_mobility.presentation.viewmodel.ModeloDeVistaPantallaReportesUsuario
import com.example.hackathon_ai_mobility.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla de administrador para gestionar reportes por estación.
 * Permite filtrar reportes, ver detalles y clausurar/abrir estaciones.
 */
@Composable
fun PantallaReportesAdmin(
    auth: FirebaseAuth? = null,
    viewmodel: ModeloDeVistaPantallaReportesUsuario = viewModel(),
    navegarPantallaPrincipal: () -> Unit = {}
) {
    // Estados desde el ViewModel
    val listaEstaciones by viewmodel.listaEstacionesBD.collectAsState()
    val reportesPorEstacion by viewmodel.reportesPorEstacion.collectAsState()

    // Estados locales
    var textoEstacion by remember { mutableStateOf("") }
    var estacionSeleccionada by remember { mutableStateOf<EstacionBD?>(null) }
    var reporteSeleccionado by remember { mutableStateOf<ModeloReportesBD?>(null) }

    // Estructura Principal
    Scaffold(
        containerColor = MetroWhite,
        topBar = {
            // Encabezado Rojo
            SmallTopAppBar(
                title = {
                    Text(
                        "Gestión de Reportes",
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

            // SECCIÓN 1: BUSCADOR DE ESTACIÓN
            Card(
                colors = CardDefaults.cardColors(containerColor = MetroLightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Filtrar por Estación",
                        style = MaterialTheme.typography.titleMedium,
                        color = MetroRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BuscadorEstacion(
                        texto = textoEstacion,
                        onTextoChange = { nuevoTexto ->
                            textoEstacion = nuevoTexto
                            estacionSeleccionada = null
                        },
                        estaciones = listaEstaciones,
                        onEstacionElegida = { estacion ->
                            estacionSeleccionada = estacion
                            textoEstacion = estacion.nombre ?: ""
                        }
                    )
                </Column>
            }

            // BOTÓN BUSCAR
            Button(
                onClick = {
                    estacionSeleccionada?.nombre?.let { nombre ->
                        viewmodel.obtenerReportesPorEstacion(nombre)
                    }
                },
                enabled = estacionSeleccionada != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MetroRed,
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("BUSCAR REPORTES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // SECCIÓN 2: LISTA DE REPORTES
            Card(
                colors = CardDefaults.cardColors(containerColor = MetroLightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Toma el espacio restante
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Text(
                        "Reportes Encontrados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MetroRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (reportesPorEstacion.isEmpty()) {
                        // Estado vacío
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (estacionSeleccionada == null)
                                    "Selecciona una estación para ver los reportes"
                                else
                                    "No hay reportes para esta estación",
                                color = MetroDarkGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        // Lista de reportes
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(reportesPorEstacion) { reporte ->
                                TarjetaReporte(
                                    reporte = reporte,
                                    onClick = { reporteSeleccionado = reporte }
                                )
                            }
                        }
                    }
                }
            }

            // Botón secundario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = navegarPantallaPrincipal) {
                    Text("Volver", color = MetroDarkGray)
                }
            }
        }
    }

    // --- MODAL: DETALLE DEL REPORTE ---
    if (reporteSeleccionado != null) {
        val reporte = reporteSeleccionado!!
        val estacionBD = listaEstaciones.firstOrNull {
            it.nombre?.equals(reporte.estacion, ignoreCase = true) == true
        }
        val estaAbierta = (estacionBD?.abierta ?: 0) == 1
        val textoBotonEstado = if (estaAbierta) "Clausurar" else "Abrir"

        AlertDialog(
            onDismissRequest = { reporteSeleccionado = null },
            confirmButton = {},
            dismissButton = {},
            title = {
                Text(
                    "Detalle del Reporte",
                    color = MetroRed,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    // Información del reporte
                    Text(
                        text = "Fecha: ${reporte.fecha ?: "Sin fecha"}",
                        fontSize = 14.sp,
                        color = MetroDarkGray
                    )
                    Text(
                        text = "Estación: ${reporte.estacion ?: "-"}",
                        fontSize = 14.sp,
                        color = MetroDarkGray
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Descripción del reporte:",
                        fontWeight = FontWeight.Bold,
                        color = MetroRed
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = reporte.descripcion ?: "Sin descripción",
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(20.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Clausurar/Abrir
                        Button(
                            onClick = {
                                estacionBD?.nombre?.let { nombre ->
                                    viewmodel.cambiarEstadoEstacion(nombre, !estaAbierta)
                                }
                                reporteSeleccionado = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (estaAbierta) MetroRed else Color(0xFF28a745)
                            )
                        ) {
                            Text(textoBotonEstado)
                        }

                        // Botón Ignorar
                        OutlinedButton(
                            onClick = { reporteSeleccionado = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ignorar", color = MetroDarkGray)
                        }
                    }
                }
            },
            containerColor = MetroWhite
        )
    }
}

/**
 * Tarjeta individual para mostrar un reporte en la lista.
 * Basada en el componente ItemReporteAdmin del código de referencia.
 */
@Composable
fun TarjetaReporte(
    reporte: ModeloReportesBD,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MetroWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE3E3E3))
    ) {
        Column {
            // Header con fecha (gris)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F1F3))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Fecha: ${reporte.fecha ?: "Sin fecha"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E2E2E)
                )
            }

            // Descripción
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = reporte.descripcion ?: "Sin descripción",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF414141)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "📍 ${reporte.estacion ?: "-"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MetroRed
                )
            }
        }
    }
}
