package com.example.hackathon_ai_mobility.regulador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

fun obtenerColorPorTipo(tipo: Int?): Color {
    return when (tipo) {
        1 -> Color(0xFFF44336)
        2 -> Color(0xFFFFC107)
        3 -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

fun obtenerDescripcionPorTipo(tipo: Int?): String {
    return when (tipo) {
        1 -> "Detener operaciones"
        2 -> "Afecta la operación"
        3 -> "No afecta la operación"
        else -> "Sin clasificar"
    }
}

fun obtenerEmojiPorTipo(tipo: Int?): String {
    return when (tipo) {
        1 -> "🚨"
        2 -> "⚠️"
        3 -> "✅"
        else -> "❓"
    }
}

fun obtenerColorPorEstado(estado: Int?): Color {
    return when (estado) {
        0 -> Color(0xFF2196F3)
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}

@Composable
fun PantallaDeReportesRegulador(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaRegulador = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
) {
    val listaReportes by viewModel.listaReportesSistema.collectAsState()
    var mostrarPendientes by remember { mutableStateOf(true) }

    val reportesFiltrados = if (mostrarPendientes) {
        listaReportes.filter { (it.reporteCompletado ?: 0) == 0 }
    } else {
        listaReportes.filter { (it.reporteCompletado ?: 0) == 1 || (it.reporteCompletado ?: 0) == 2 }
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Regulador 🚂",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { navegarPantallaInicial() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Salir")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { mostrarPendientes = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mostrarPendientes) Color(0xFF2196F3) else Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("📋 Pendientes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { mostrarPendientes = false },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!mostrarPendientes) Color(0xFF2196F3) else Color.DarkGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("📚 Historial", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (reportesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (mostrarPendientes) "✅" else "📋",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (mostrarPendientes) "No hay reportes pendientes" else "Sin historial",
                            color = Color.LightGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(reportesFiltrados) { reporte ->
                        val estadoActual = reporte.reporteCompletado ?: 0
                        if (estadoActual == 0) {
                            ItemReportePendiente(
                                reporte = reporte,
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState
                            )
                        } else {
                            ItemReporteHistorial(reporte)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemReportePendiente(
    reporte: ModeloReportesBD,
    viewModel: ModeloDeVistaRegulador,
    snackbarHostState: SnackbarHostState
) {
    var reporteTecnico by remember { mutableStateOf("") }
    var equipoLlevar by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableIntStateOf(0) }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val fechaFormateada = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val date = reporte.fechaHoraCreacionReporte?.toDate()
            if (date != null) {
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
            } else "Sin fecha"
        } catch (e: Exception) {
            "Sin fecha"
        }
    }

    val tipoInicial = reporte.tipoProblema ?: 0
    val colorTipoInicial = obtenerColorPorTipo(tipoInicial)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // HEADER CON ESTACION Y EMOJI DINAMICO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Emoji grande del tipo de problema
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colorTipoInicial.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = obtenerEmojiPorTipo(tipoInicial),
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = reporte.estacionQueTieneReporte ?: "Sin estación",
                            color = Color.Cyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            text = fechaFormateada,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // INFORMACION DEL REPORTE
            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📝", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Información del Reporte",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!reporte.tituloReporte.isNullOrBlank()) {
                        Row {
                            Text("Título: ", color = Color.LightGray, fontSize = 14.sp)
                            Text(reporte.tituloReporte!!, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row {
                        Text("⏰ ", fontSize = 14.sp)
                        Text("Hora: ", color = Color.LightGray, fontSize = 14.sp)
                        Text(reporte.horaProblema ?: "N/A", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row {
                        Text("👤 ", fontSize = 14.sp)
                        Text("Reportado por: ", color = Color.LightGray, fontSize = 14.sp)
                        Text(
                            reporte.nombreDeJefeDeEstacionCreadorReporte ?: "N/A",
                            color = Color.Cyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.4f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("📄 Descripción:", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reporte.descripcionReporteJefeDeEstacion ?: "Sin descripción",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // FORMULARIO DE ASIGNACION
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔧", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Asignación de Recursos",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reporteTecnico,
                onValueChange = { reporteTecnico = it },
                label = { Text("Instrucción Técnica", color = Color.LightGray) },
                placeholder = { Text("Escribe las instrucciones...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldActivado,
                    unfocusedContainerColor = FieldDesactivado,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color(0xFF2196F3),
                    unfocusedIndicatorColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = equipoLlevar,
                onValueChange = { equipoLlevar = it },
                label = { Text("Equipo Requerido", color = Color.LightGray) },
                placeholder = { Text("Herramientas, refacciones...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldActivado,
                    unfocusedContainerColor = FieldDesactivado,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedIndicatorColor = Color(0xFF2196F3),
                    unfocusedIndicatorColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚡", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Confirmar Nivel de Prioridad:",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (nivel in 1..3) {
                    val colorNivel = obtenerColorPorTipo(nivel)
                    val isSelected = tipoSeleccionado == nivel

                    Button(
                        onClick = { tipoSeleccionado = nivel },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) colorNivel else Color(0xFF3A3A3A),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).height(90.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isSelected) 8.dp else 2.dp
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(obtenerEmojiPorTipo(nivel), fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Nivel $nivel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                obtenerDescripcionPorTipo(nivel),
                                fontSize = 9.sp,
                                maxLines = 2,
                                lineHeight = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val esValido = !enviando && reporteTecnico.trim().isNotBlank() && equipoLlevar.trim().isNotBlank() && tipoSeleccionado > 0

            Button(
                onClick = {
                    enviando = true
                    viewModel.enviarReporteATecnico(
                        idDocumento = reporte.idDocumento ?: "",
                        descripcionTecnica = reporteTecnico,
                        equipoLlevar = equipoLlevar,
                        tipoProblemaConfirmado = tipoSeleccionado,
                        onSuccess = {
                            enviando = false
                            scope.launch {
                                snackbarHostState.showSnackbar("✓ Petición enviada exitosamente")
                            }
                        },
                        onError = { msg ->
                            enviando = false
                            scope.launch {
                                snackbarHostState.showSnackbar("✗ Error: $msg")
                            }
                        }
                    )
                },
                enabled = esValido,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esValido) Color(0xFF2196F3) else Color(0xFF3A3A3A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (esValido) 6.dp else 0.dp
                )
            ) {
                Text(
                    if (enviando) "⏳ Enviando..." else "🚀 Enviar Petición al Técnico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ItemReporteHistorial(reporte: ModeloReportesBD) {
    val estadoActual = reporte.reporteCompletado ?: 0
    val colorEstado = obtenerColorPorEstado(estadoActual)
    val colorPrioridad = obtenerColorPorTipo(reporte.tipoProblema)

    val textoCompleto = reporte.reporteTecnicoRegulador ?: ""
    val instruccion = textoCompleto.substringBefore("| Equipo:").removePrefix("Instruccion:").trim()
    val equipo = textoCompleto.substringAfter("| Equipo:", "").trim()

    val fecha = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val d = reporte.fechaHoraCreacionReporte?.toDate()
            if (d != null) SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(d) else "Sin fecha"
        } catch (e: Exception) {
            "Sin fecha"
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorPrioridad.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(obtenerEmojiPorTipo(reporte.tipoProblema), fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            reporte.estacionQueTieneReporte ?: "Sin estación",
                            color = Color.Cyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(fecha, color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                Surface(
                    color = colorEstado,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (estadoActual == 1) "⏳ EN PROCESO" else "✓ COMPLETADO",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                reporte.tituloReporte ?: "Sin título",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Reportado por: " + (reporte.nombreDeJefeDeEstacionCreadorReporte ?: "N/A"),
                color = Color.LightGray,
                fontSize = 12.sp
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color.Gray.copy(alpha = 0.3f))

            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔧 ", fontSize = 16.sp)
                        Text(
                            "Instrucción del Regulador:",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(instruccion, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📦 ", fontSize = 16.sp)
                        Text(
                            "Equipo Requerido:",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(equipo, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                color = colorPrioridad.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(obtenerEmojiPorTipo(reporte.tipoProblema), fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Prioridad Nivel ${reporte.tipoProblema} - ${obtenerDescripcionPorTipo(reporte.tipoProblema)}",
                        color = colorPrioridad,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}