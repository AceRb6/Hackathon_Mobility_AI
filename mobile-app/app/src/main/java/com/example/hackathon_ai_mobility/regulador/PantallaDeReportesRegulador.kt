package com.example.hackathon_ai_mobility.regulador

import android.util.Log
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
import androidx.compose.ui.graphics.Color.Companion.Black
import com.example.hackathon_ai_mobility.ui.theme.FieldActivado
import com.example.hackathon_ai_mobility.ui.theme.FieldDesactivado
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

// --- FUNCIONES AUXILIARES (Debe estar fuera de los @Composable) ---
fun obtenerColorPorTipo(tipo: Int?): Color {
    return when (tipo) {
        1 -> Color(0xFF4CAF50)      // Verde - Leve
        2 -> Color(0xFFFFC107)      // Amarillo - Medio
        3 -> Color(0xFFF44336)      // Rojo - Crítico
        else -> Color.Gray
    }
}
fun obtenerColorPorEstado(estado: Int?): Color {
    return when (estado) {
        0 -> Color(0xFF2196F3) // Azul: Pendiente
        1 -> Color(0xFFFFC107) // Amarillo: En Proceso / Asignado
        2 -> Color(0xFF4CAF50) // Verde: Completado
        else -> Color.Gray
    }
}
// ------------------------------------------------------------------

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
            // ... (Encabezado y Botones de Pestañas) ...
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
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Salir")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de Pestañas
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                    Text("Pendientes")
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

            // LISTA DE REPORTES
            if (reportesFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay reportes en esta sección", color = Color.Gray)
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
                                snackbarHostState = snackbarHostState,
                                onEnviado = { }
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

// --- ITEM: PENDIENTE (EDITABLE - ESTADO 0) ---
@Composable
fun ItemReportePendiente(
    reporte: ModeloReportesBD,
    viewModel: ModeloDeVistaRegulador,
    snackbarHostState: SnackbarHostState,
    onEnviado: () -> Unit // Mantengo el callback
) {
    val tiposProblema = mapOf(1 to Pair("✅", "Leve"), 2 to Pair("⚠️", "Medio"), 3 to Pair("🚨", "Crítico"))
    var reporteTecnico by remember { mutableStateOf("") }
    var equipoLlevar by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableIntStateOf(0) }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(colors = CardDefaults.cardColors(containerColor = Color.DarkGray), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Datos de solo lectura
            Text(reporte.estacionQueTieneReporte ?: "S/N", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (!reporte.tituloReporte.isNullOrBlank()) {
                Text(reporte.tituloReporte!!, color = Color.White, fontWeight = FontWeight.Bold)
            }
            // ... (otros campos de solo lectura) ...

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray)

            // FORMULARIO DE ASIGNACIÓN
            Text("Asignación de Recursos", color = Color.Green, fontWeight = FontWeight.Bold)

            OutlinedTextField(value = reporteTecnico, onValueChange = { reporteTecnico = it }, label = { Text("Instrucción Técnica") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = equipoLlevar, onValueChange = { equipoLlevar = it }, label = { Text("Equipo Requerido") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            // BOTONES DE PRIORIDAD
            Text("Nivel de Prioridad (Obligatorio):", color = Color.White, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tiposProblema.forEach { (tipo, info) ->
                    Button(
                        onClick = { tipoSeleccionado = tipo },
                        colors = ButtonDefaults.buttonColors(containerColor = if (tipoSeleccionado == tipo) obtenerColorPorTipo(tipo) else Color.Gray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(info.first, fontSize = 18.sp)
                            Text("$tipo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN ENVIAR
            val esValido = !enviando && reporteTecnico.trim().isNotBlank() && equipoLlevar.trim().isNotBlank() && tipoSeleccionado > 0

            Button(
                onClick = {
                    Log.d(
                        "ReguladorUI",
                        "Click Enviar Petición -> docId=${reporte.idDocumento}, estacion=${reporte.estacionQueTieneReporte}, tipoSeleccionado=$tipoSeleccionado"
                    )
                    enviando = true
                    viewModel.enviarReporteATecnico(
                        idDocumento = reporte.idDocumento ?: "",
                        descripcionTecnica = reporteTecnico,
                        equipoLlevar = equipoLlevar,
                        tipoProblemaConfirmado = tipoSeleccionado,
                        onSuccess = {
                            enviando = false
                            Log.d("ReguladorUI", "Petición enviada OK para docId=${reporte.idDocumento}")
                            scope.launch { snackbarHostState.showSnackbar("✓ Petición enviada") }
                        },
                        onError = { msg ->
                            enviando = false
                            Log.e("ReguladorUI", "Error al enviar petición docId=${reporte.idDocumento}: $msg")
                            scope.launch { snackbarHostState.showSnackbar("Error: $msg") }
                        }
                    )
                },
                enabled = esValido,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (esValido) Color.Blue else Color.DarkGray)
            ) {
                Text(if (enviando) "Enviando..." else "Enviar Petición")
            }
        }
    }
}

// --- ITEM: HISTORIAL (SOLO LECTURA - ESTADO 1 ó 2) ---
@Composable
fun ItemReporteHistorial(reporte: ModeloReportesBD) {
    val estadoActual = reporte.reporteCompletado ?: 0
    val colorEstado = obtenerColorPorEstado(estadoActual)
    val colorPrioridad = obtenerColorPorTipo(reporte.tipoProblema)

    // --- PARSEO CORRECTO DEL STRING CONCATENADO ---
    val textoCompleto = reporte.reporteTecnicoRegulador ?: ""
    // Busca "Instrucción: " y corta antes de " | Equipo:"
    val instruccion = textoCompleto.substringBefore("| Equipo:").removePrefix("Instrucción:").trim()
    // Busca " | Equipo:" y toma el resto
    val equipo = textoCompleto.substringAfter("| Equipo:", "").trim()

    val fecha = remember(reporte.fechaHoraCreacionReporte) {
        val d = reporte.fechaHoraCreacionReporte?.toDate()
        if(d!=null) SimpleDateFormat("dd/MM HH:mm").format(d) else "-"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera y Estado
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(reporte.estacionQueTieneReporte ?: "", color = Color.Cyan, fontWeight = FontWeight.Bold)
                Surface(color = colorEstado, shape = MaterialTheme.shapes.small) {
                    Text(
                        text = if (estadoActual == 1) "EN PROCESO" else "COMPLETADO",
                        color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(reporte.tituloReporte ?: "Sin Título", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Reporte de ${reporte.nombreDeJefeDeEstacionCreadorReporte}", color = Color.LightGray, fontSize = 12.sp)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            // --- DETALLES TÉCNICOS (RESTABLECIDOS) ---
            Text("Instrucción Regulador:", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(instruccion, color = Color.White, fontSize = 14.sp)

            Spacer(Modifier.height(4.dp))
            Text("Equipo Requerido:", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(equipo, color = Color.White, fontSize = 14.sp)

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Prioridad Nivel ${reporte.tipoProblema}", color = colorPrioridad, fontWeight = FontWeight.Bold)
            }
        }
    }
}