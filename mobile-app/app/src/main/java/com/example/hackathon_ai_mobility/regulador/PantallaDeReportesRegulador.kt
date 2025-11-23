package com.example.hackathon_ai_mobility.regulador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
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
        else -> ""
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
        containerColor = Color(0xFF0D0D0D)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Regulador",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = { navegarPantallaInicial() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF252525),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(42.dp),
                    shape = RoundedCornerShape(21.dp),
                    elevation = ButtonDefaults.buttonElevation(2.dp)
                ) {
                    Text("Salir", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TABS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { mostrarPendientes = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mostrarPendientes) Color(0xFF1976D2) else Color(0xFF252525),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (mostrarPendientes) 6.dp else 0.dp
                    )
                ) {
                    Text(
                        "Pendientes",
                        fontSize = 17.sp,
                        fontWeight = if (mostrarPendientes) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { mostrarPendientes = false },
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!mostrarPendientes) Color(0xFF1976D2) else Color(0xFF252525),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (!mostrarPendientes) 6.dp else 0.dp
                    )
                ) {
                    Text(
                        "Historial",
                        fontSize = 17.sp,
                        fontWeight = if (!mostrarPendientes) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (reportesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (mostrarPendientes) "Sin reportes pendientes" else "Sin historial",
                            color = Color(0xFF666666),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 30.dp)
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
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            } else "Sin fecha"
        } catch (e: Exception) {
            "Sin fecha"
        }
    }

    val horaFormateada = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val date = reporte.fechaHoraCreacionReporte?.toDate()
            if (date != null) {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            } else "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }

    val tipoInicial = reporte.tipoProblema ?: 0
    val colorTipoInicial = obtenerColorPorTipo(tipoInicial)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // HEADER CON EMOJI DE NIVEL
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(colorTipoInicial.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = obtenerEmojiPorTipo(tipoInicial),
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = reporte.estacionQueTieneReporte ?: "Sin estación",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$fechaFormateada  •  $horaFormateada",
                            color = Color(0xFF999999),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECCION: INFORMACION DEL REPORTE
            Surface(
                color = Color(0xFF252525),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Hora del problema
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Hora del Problema",
                                color = Color(0xFF888888),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                reporte.horaProblema ?: "No especificada",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Título si existe
                        if (!reporte.tituloReporte.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = Color(0xFF1A1A1A),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    reporte.tituloReporte!!,
                                    color = Color(0xFF00BCD4),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reportado por
                    Column {
                        Text(
                            "Reportado por",
                            color = Color(0xFF888888),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            reporte.nombreDeJefeDeEstacionCreadorReporte ?: "Desconocido",
                            color = Color(0xFF00BCD4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Descripción
                    Text(
                        "Descripción del Problema",
                        color = Color(0xFF888888),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reporte.descripcionReporteJefeDeEstacion ?: "Sin descripción",
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(10.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECCION: ASIGNACION DE RECURSOS
            Text(
                "ASIGNACIÓN DE RECURSOS",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo: Instrucción Técnica
            Column {
                Text(
                    "Instrucción Técnica",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = reporteTecnico,
                    onValueChange = { reporteTecnico = it },
                    placeholder = { Text("Escribe las instrucciones...", color = Color(0xFF666666), fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF252525),
                        unfocusedContainerColor = Color(0xFF252525),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1976D2),
                        focusedIndicatorColor = Color(0xFF1976D2),
                        unfocusedIndicatorColor = Color(0xFF3A3A3A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 20.sp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campo: Equipo Requerido
            Column {
                Text(
                    "Equipo Requerido",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = equipoLlevar,
                    onValueChange = { equipoLlevar = it },
                    placeholder = { Text("Herramientas, refacciones...", color = Color(0xFF666666), fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF252525),
                        unfocusedContainerColor = Color(0xFF252525),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF1976D2),
                        focusedIndicatorColor = Color(0xFF1976D2),
                        unfocusedIndicatorColor = Color(0xFF3A3A3A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, lineHeight = 20.sp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SELECTOR DE NIVEL DE PRIORIDAD
            Text(
                "NIVEL DE PRIORIDAD",
                color = Color(0xFFCCCCCC),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

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
                            containerColor = if (isSelected) colorNivel else Color(0xFF252525),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).height(95.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isSelected) 8.dp else 2.dp
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(obtenerEmojiPorTipo(nivel), fontSize = 38.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Nivel $nivel",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                obtenerDescripcionPorTipo(nivel),
                                fontSize = 9.sp,
                                maxLines = 2,
                                lineHeight = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BOTON ENVIAR
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
                                snackbarHostState.showSnackbar("Petición enviada exitosamente")
                            }
                        },
                        onError = { msg ->
                            enviando = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: $msg")
                            }
                        }
                    )
                },
                enabled = esValido,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esValido) Color(0xFF1976D2) else Color(0xFF3A3A3A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (esValido) 8.dp else 0.dp
                )
            ) {
                Text(
                    if (enviando) "Enviando..." else "Enviar al Técnico",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorPrioridad.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(obtenerEmojiPorTipo(reporte.tipoProblema), fontSize = 30.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            reporte.estacionQueTieneReporte ?: "Sin estación",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            fecha,
                            color = Color(0xFF999999),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = colorEstado,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (estadoActual == 1) "PROCESO" else "HECHO",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                reporte.tituloReporte ?: "Sin título",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Reportado por: " + (reporte.nombreDeJefeDeEstacionCreadorReporte ?: "N/A"),
                color = Color(0xFF999999),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF3A3A3A), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = Color(0xFF252525),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Instrucción del Regulador:",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        instruccion.ifEmpty { "Sin instrucción" },
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Equipo Requerido:",
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        equipo.ifEmpty { "Sin equipo" },
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                color = colorPrioridad.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(obtenerEmojiPorTipo(reporte.tipoProblema), fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Nivel ${reporte.tipoProblema} - ${obtenerDescripcionPorTipo(reporte.tipoProblema)}",
                        color = colorPrioridad,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}