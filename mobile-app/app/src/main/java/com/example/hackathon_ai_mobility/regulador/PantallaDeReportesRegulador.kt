package com.example.hackathon_ai_mobility.regulador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import com.example.hackathon_ai_mobility.ui.theme.FieldActivado
import com.example.hackathon_ai_mobility.ui.theme.FieldDesactivado
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PantallaDeReportesRegulador(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaRegulador = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
) {
    val listaCompletaReportes by viewModel.listaReportesSistema.collectAsState()

    // 0 = Pendientes, 1 = Historial
    var pestanaSeleccionada by remember { mutableIntStateOf(0) }

    val reportesFiltrados = remember(listaCompletaReportes, pestanaSeleccionada) {
        listaCompletaReportes.filter {
            (it.reporteCompletado ?: 0) == pestanaSeleccionada
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- ENCABEZADO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Regulador", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { navegarPantallaInicial() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Salir", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PESTAÑAS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { pestanaSeleccionada = 0 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pestanaSeleccionada == 0) Color.Blue else Color.Gray
                    )
                ) {
                    Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pendientes")
                }

                Button(
                    onClick = { pestanaSeleccionada = 1 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pestanaSeleccionada == 1) Color(0xFF2E7D32) else Color.Gray
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Historial")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LISTA ---
            if (reportesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (pestanaSeleccionada == 0) "No hay reportes pendientes" else "No hay historial",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(reportesFiltrados) { reporte ->
                        if (pestanaSeleccionada == 0) {
                            // Al enviar con éxito, cambiamos pestanaSeleccionada a 1 (Historial)
                            ItemReporteRegulador(
                                reporte = reporte,
                                viewModel = viewModel,
                                onReporteEnviado = { pestanaSeleccionada = 1 }
                            )
                        } else {
                            ItemReporteSolucionado(reporte)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemReporteRegulador(
    reporte: ModeloReportesBD,
    viewModel: ModeloDeVistaRegulador,
    onReporteEnviado: () -> Unit // Callback para cambiar de pestaña
) {
    var reporteTecnico by remember { mutableStateOf("") }
    var equipoLlevar by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableIntStateOf(0) }
    var enviando by remember { mutableStateOf(false) }

    val fechaFormateada = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val date = reporte.fechaHoraCreacionReporte?.toDate()
            if (date != null) SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date) else "-"
        } catch (e: Exception) { "-" }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Datos de Solo Lectura (Jefe de Estación)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(reporte.estacionQueTieneReporte ?: "S/N", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(fechaFormateada, color = Color.LightGray, fontSize = 12.sp)
            }
            Text("Problema: ${reporte.tituloReporte}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Hora Inicio: ${reporte.horaProblema}", color = Color.White)

            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(0.3f)).padding(8.dp)) {
                Text(reporte.descripcionReporteJefeDeEstacion ?: "Sin descripción", color = Color.White, fontSize = 14.sp)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray)

            // Formulario Regulador
            Text("Respuesta Técnica", color = Color.Green, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = reporteTecnico,
                onValueChange = { reporteTecnico = it },
                label = { Text("Instrucción Técnica") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = FieldActivado, unfocusedContainerColor = FieldDesactivado)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = equipoLlevar,
                onValueChange = { equipoLlevar = it },
                label = { Text("Equipo Requerido") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = FieldActivado, unfocusedContainerColor = FieldDesactivado)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // SELECTOR DE PRIORIDAD CON COLORES
            Text("Asignar Prioridad (Obligatorio):", color = Color.White, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { tipoSeleccionado = 1 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipoSeleccionado == 1) Color(0xFF66BB6A) else Color.Gray
                    )
                ) { Text("1", color = Color.Black, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { tipoSeleccionado = 2 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipoSeleccionado == 2) Color(0xFFFFCA28) else Color.Gray
                    )
                ) { Text("2", color = Color.Black, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { tipoSeleccionado = 3 },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipoSeleccionado == 3) Color.Red else Color.Gray
                    )
                ) { Text("3", color = Color.White, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val esValido = !enviando && reporteTecnico.isNotBlank() && equipoLlevar.isNotBlank() && tipoSeleccionado > 0

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
                            onReporteEnviado() // <--- AQUÍ CAMBIAMOS DE PESTAÑA
                        },
                        onError = { enviando = false }
                    )
                },
                enabled = esValido,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esValido) Color.Blue else Color.DarkGray
                )
            ) {
                Text(if (enviando) "Enviando..." else "Enviar Petición")
            }
        }
    }
}

@Composable
fun ItemReporteSolucionado(reporte: ModeloReportesBD) {
    // Lógica de colores para el historial
    val colorPrioridad = when(reporte.tipoProblema) {
        1 -> Color(0xFF66BB6A) // Verde Claro
        2 -> Color(0xFFFFCA28) // Amarillo
        3 -> Color.Red       // Rojo
        else -> Color.Gray
    }

    // Separar el texto "Reporte: ... | Equipo: ..." para mostrarlo bonito
    val textoCompleto = reporte.reporteTecnicoRegulador ?: ""
    val partes = textoCompleto.split("| Equipo:")
    val instruccion = partes.getOrNull(0)?.replace("Reporte:", "")?.trim() ?: textoCompleto
    val equipo = partes.getOrNull(1)?.trim() ?: "No especificado"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(reporte.estacionQueTieneReporte ?: "", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                // Badge visual de prioridad
                Surface(shape = MaterialTheme.shapes.small, color = colorPrioridad) {
                    Text(
                        "NIVEL ${reporte.tipoProblema}",
                        color = if (reporte.tipoProblema == 2) Color.Black else Color.White, // Texto negro si es amarillo
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Problema Original: ${reporte.tituloReporte}", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Sección Resumen Regulador
            Text("RESUMEN DE ACCIÓN", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(4.dp))
            Text("Instrucción:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(instruccion, color = Color.LightGray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Equipo Asignado:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(equipo, color = Color.LightGray, fontSize = 14.sp)
        }
    }
}