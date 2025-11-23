package com.example.hackathon_ai_mobility.regulador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import androidx.compose.ui.graphics.Color.Companion.Black
import com.example.hackathon_ai_mobility.ui.theme.FieldActivado
import com.example.hackathon_ai_mobility.ui.theme.FieldDesactivado
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun PantallaDeReportesRegulador(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaRegulador = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
) {
    // Consumo de datos del ViewModel
    val listaReportes by viewModel.listaReportesSistema.collectAsState()

    // Estado para controlar qué vista mostrar (true = Pendientes, false = Historial)
    var mostrarPendientes by remember { mutableStateOf(true) }

    // Filtros según la vista seleccionada
    val reportesFiltrados = if (mostrarPendientes) {
        listaReportes.filter { it.reporteCompletado == 0 } // Pendientes
    } else {
        listaReportes.filter { it.reporteCompletado == 1 } // Historial (completados)
    }

    // Estado para Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Encabezado
            Text(
                "Regulador - Reportes",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(onClick = { navegarPantallaInicial() }, modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Volver")
            }

            // Botones para cambiar entre Pendientes y Historial
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { mostrarPendientes = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mostrarPendientes) Color(0xFF2196F3) else Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Pendientes", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { mostrarPendientes = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!mostrarPendientes) Color(0xFF2196F3) else Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Historial", fontWeight = FontWeight.Bold)
                }
            }

            // Mostrar mensaje cuando no hay reportes
            if (reportesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (mostrarPendientes) "✓ No hay reportes pendientes" else "📋 No hay reportes en el historial",
                            color = Color.Green,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (mostrarPendientes)
                                "Todos los reportes han sido procesados"
                            else
                                "Aún no has completado ningún reporte",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Lista de tarjetas
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(reportesFiltrados) { reporte ->
                        if (mostrarPendientes) {
                            // Vista editable para pendientes
                            ItemReportePendiente(reporte, viewModel, snackbarHostState)
                        } else {
                            // Vista de solo lectura para historial
                            ItemReporteHistorial(reporte)
                        }
                    }
                }
            }
        }
    }
}

// Función para obtener color según tipo de problema
fun obtenerColorPorTipo(tipo: Int?): Color {
    return when (tipo) {
        1 -> Color(0xFFF44336)      // Rojo - Detener operaciones (CRÍTICO)
        2 -> Color(0xFFFFC107)      // Amarillo - Afecta la operación (MEDIO)
        3 -> Color(0xFF4CAF50)      // Verde - No afecta la operación (LEVE)
        else -> Color.Gray          // Default
    }
}

@Composable
fun ItemReportePendiente(
    reporte: ModeloReportesBD,
    viewModel: ModeloDeVistaRegulador,
    snackbarHostState: SnackbarHostState
) {
    // Mapa de tipos de problema con descripciones actualizadas
    val tiposProblema = mapOf(
        1 to Pair("🚨", "Detener operaciones"),
        2 to Pair("⚠️", "Afecta la operación"),
        3 to Pair("✅", "No afecta la operación")
    )

    // Variables locales para el formulario de respuesta
    var reporteTecnico by remember { mutableStateOf("") }
    var equipoLlevar by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableIntStateOf(reporte.tipoProblema ?: 1) }
    var enviando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Formateo seguro de fecha
    val fechaFormateada = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val date = reporte.fechaHoraCreacionReporte?.toDate()
            if (date != null) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
            } else "Sin fecha"
        } catch (e: Exception) {
            "Error fecha"
        }
    }

    // Color del borde según tipo de problema
    val colorTipo = obtenerColorPorTipo(tipoSeleccionado)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        modifier = Modifier
            .fillMaxWidth()
            .background(colorTipo.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Indicador de tipo con color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorTipo.copy(alpha = 0.3f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tiposProblema[tipoSeleccionado]?.first} ${tiposProblema[tipoSeleccionado]?.second ?: "Desconocido"}",
                    color = colorTipo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = fechaFormateada,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SECCIÓN 1: DATOS RECIBIDOS (SOLO LECTURA) ---
            Text(
                text = "Estación: ${reporte.estacionQueTieneReporte}",
                color = Color.Cyan,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Problema: ${reporte.tituloReporte}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Hora Inicio: ${reporte.horaProblema}", color = Color.White)

            Spacer(modifier = Modifier.height(4.dp))
            Text("Descripción:", color = Color.LightGray, fontSize = 14.sp)
            Text(
                text = reporte.descripcionReporteJefeDeEstacion ?: "Sin descripción",
                color = Color.White,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f)).padding(4.dp).fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray)

            // --- SECCIÓN 2: DATOS A LLENAR (REGULADOR) ---
            Text("Respuesta del Regulador", color = Color.Green, fontWeight = FontWeight.Bold)

            // Input: Reporte Técnico
            OutlinedTextField(
                value = reporteTecnico,
                onValueChange = { reporteTecnico = it },
                label = { Text("Reporte Técnico") },
                placeholder = { Text("Instrucciones o diagnóstico") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldActivado,
                    unfocusedContainerColor = FieldDesactivado,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input: Equipo a llevar
            OutlinedTextField(
                value = equipoLlevar,
                onValueChange = { equipoLlevar = it },
                label = { Text("Equipo a llevar") },
                placeholder = { Text("Herramientas, refacciones...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldActivado,
                    unfocusedContainerColor = FieldDesactivado,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input: Selector de Tipo con colores
            Text("Confirmar Nivel de Severidad:", color = Color.White, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tiposProblema.forEach { (tipo, emojiYDescripcion) ->
                    val colorBoton = obtenerColorPorTipo(tipo)
                    Button(
                        onClick = { tipoSeleccionado = tipo },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tipoSeleccionado == tipo) colorBoton else Color.Gray,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emojiYDescripcion.first, fontSize = 20.sp)
                            Text("Nivel $tipo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de envío - CORRECCIÓN: validación con trim()
            Button(
                onClick = {
                    enviando = true
                    viewModel.completarReporteTecnico(
                        idDocumento = reporte.idDocumento ?: "",
                        descripcionTecnica = reporteTecnico,
                        equipoLlevar = equipoLlevar,
                        tipoProblemaConfirmado = tipoSeleccionado,
                        onSuccess = {
                            enviando = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "✓ Petición enviada exitosamente",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onError = { errorMessage ->
                            enviando = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "✗ Error: $errorMessage",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                    )
                },
                // CORRECCIÓN: agregado .trim() para evitar espacios en blanco
                enabled = !enviando && reporteTecnico.trim().isNotBlank() && equipoLlevar.trim().isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text(if (enviando) "Enviando..." else "Enviar Petición")
            }
        }
    }
}

@Composable
fun ItemReporteHistorial(
    reporte: ModeloReportesBD
) {
    val tiposProblema = mapOf(
        1 to Pair("", "Operacion detenida"),
        2 to Pair("️", "Afecta la operación"),
        3 to Pair("", "No afecta la operación")
    )

    // Formateo seguro de fecha
    val fechaFormateada = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val date = reporte.fechaHoraCreacionReporte?.toDate()
            if (date != null) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
            } else "Sin fecha"
        } catch (e: Exception) {
            "Error fecha"
        }
    }

    val colorTipo = obtenerColorPorTipo(reporte.tipoProblema)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Indicador de tipo con color
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorTipo.copy(alpha = 0.3f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${tiposProblema[reporte.tipoProblema]?.first ?: "❓"} ${tiposProblema[reporte.tipoProblema]?.second ?: "Desconocido"}",
                        color = colorTipo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✓ COMPLETADO",
                        color = Color.Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = fechaFormateada,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información del reporte original
            Text(
                text = "Estación: ${reporte.estacionQueTieneReporte}",
                color = Color.Cyan,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Problema: ${reporte.tituloReporte}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Hora Inicio: ${reporte.horaProblema}", color = Color.White)

            Spacer(modifier = Modifier.height(4.dp))
            Text("Descripción Original:", color = Color.LightGray, fontSize = 14.sp)
            Text(
                text = reporte.descripcionReporteJefeDeEstacion ?: "Sin descripción",
                color = Color.White,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f)).padding(4.dp).fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray)

            // Respuesta del regulador (solo lectura)
            Text("Respuesta Enviada:", color = Color.Green, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reporte.reporteTecnicoRegulador ?: "Sin respuesta registrada",
                color = Color.White,
                modifier = Modifier
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
                    .fillMaxWidth()
            )
        }
    }
}