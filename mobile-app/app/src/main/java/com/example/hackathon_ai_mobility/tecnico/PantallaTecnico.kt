package com.example.hackathon_ai_mobility.tecnico

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

// --- IMPORTACIONES DE TU MAPA ---
// Asegúrate de que estos paquetes coincidan con tu estructura de carpetas
import com.example.hackathon_ai_mobility.prueba_mapa.usuario.ScreenDeMetroUsuario
import com.example.hackathon_ai_mobility.mapatecnico.usuariotecnico.MetroUsuarioViewModel

@Composable
fun PantallaTecnico(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaPantallaTecnico = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
) {
    val context = LocalContext.current

    // Datos (Mocks o Reales según tu VM)
    val listaReportes by viewModel.listaReportesSistema.collectAsState()
    val estacionTecnico by viewModel.tecnicoDependencia.collectAsState()

    // Filtro de seguridad (Estado 1 = Asignado)
    val reportesAsignados = remember(listaReportes) {
        listaReportes.filter { it.reporteCompletado == 1 }
    }

    // Lógica de Notificaciones
    var inicializado by remember { mutableStateOf(false) }
    var ultimoTamano by remember { mutableStateOf(0) }

    LaunchedEffect(reportesAsignados.size) {
        if (!inicializado) {
            ultimoTamano = reportesAsignados.size
            inicializado = true
        } else {
            if (reportesAsignados.size > ultimoTamano) {
                repetirAlertasNuevoReporte(context)
            }
            ultimoTamano = reportesAsignados.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
    ) {
        Text("Técnico - Tareas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        val base = estacionTecnico ?: "Zaragoza"
        Text("Base Operativa: $base", color = Color.Yellow, fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp, top = 4.dp))

        Button(
            onClick = { navegarPantallaInicial() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Cerrar Sesión")
        }

        if (reportesAsignados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin tareas pendientes", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(reportesAsignados) { reporte ->
                    ItemReporteTecnico(
                        reporte = reporte,
                        viewModel = viewModel,
                        estacionBase = base
                    )
                }
            }
        }
    }
}

@Composable
fun ItemReporteTecnico(
    reporte: ModeloReportesBD,
    viewModel: ModeloDeVistaPantallaTecnico,
    estacionBase: String
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var mostrarMapaInterno by remember { mutableStateOf(false) } // Controla el diálogo del mapa
    var marcando by remember { mutableStateOf(false) }
    var mostrarDetalles by remember { mutableStateOf(false) }

    val destino = reporte.estacionQueTieneReporte ?: "Destino"

    // Cálculo de tiempo (Simulado para la tarjeta)
    val (tiempo, modo) = viewModel.getBestRouteTime(estacionBase, destino)

    // Parseo de texto del regulador
    val instruccion = reporte.reporteTecnicoRegulador?.substringAfter("Instrucción:")?.substringBefore("|")?.trim() ?: "N/A"
    val equipo = reporte.reporteTecnicoRegulador?.substringAfter("| Equipo:")?.trim() ?: "N/A"

    val colorPrioridad = when (reporte.tipoProblema) { 3 -> Color.Red; 2 -> Color(0xFFFFC107); else -> Color(0xFF4CAF50) }

    val fecha = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val d = reporte.fechaHoraCreacionReporte?.toDate()
            if(d!=null) SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(d) else "-"
        } catch (e: Exception) { "-" }
    }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Ir a: $destino", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(fecha, color = Color.LightGray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))
            Text("Problema: ${reporte.tituloReporte}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Nivel Prioridad: ${reporte.tipoProblema}", color = colorPrioridad, fontWeight = FontWeight.SemiBold)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            Text("RUTA SUGERIDA", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$estacionBase ➝ ", color = Color.LightGray, fontSize = 13.sp)
                Text("~$tiempo min", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Text("($modo)", color = Color.LightGray, fontSize = 12.sp)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            TextButton(onClick = { mostrarDetalles = !mostrarDetalles }) {
                Text(if (mostrarDetalles) "Ocultar Instrucciones" else "Ver Instrucciones", color = Color.Green)
            }

            if (mostrarDetalles) {
                Text("Instrucción: $instruccion", color = Color.White, fontSize = 14.sp)
                Text("Equipo: $equipo", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
            }

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // BOTÓN VER MAPA (Abre tu ScreenDeMetroUsuario)
                Button(
                    onClick = { mostrarMapaInterno = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF039BE5))
                ) {
                    Text("Ver Ruta 🗺️", fontSize = 12.sp)
                }

                // BOTÓN COMPLETAR
                Button(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Completar", fontSize = 12.sp)
                }
            }
        }
    }

    // --- DIÁLOGO A PANTALLA COMPLETA CON EL MAPA ---
    if (mostrarMapaInterno) {
        Dialog(
            onDismissRequest = { mostrarMapaInterno = false },
            properties = DialogProperties(usePlatformDefaultWidth = false) // Ocupa toda la pantalla
        ) {
            Scaffold(
                containerColor = Black,
                topBar = {
                    // Barra superior flotante para cerrar el mapa
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF222222))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ruta Asignada", color = Color.Gray, fontSize = 12.sp)
                            Text("$estacionBase ➝ $destino", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { mostrarMapaInterno = false }) {
                            Text("❌", fontSize = 24.sp, color = Color.White)
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    // Instanciamos el ViewModel específico del mapa
                    val mapaViewModel: MetroUsuarioViewModel = viewModel()

                    // LLAMADA A TU SCREEN EXISTENTE
                    ScreenDeMetroUsuario(
                        viewModel = mapaViewModel,
                        origenInicial = estacionBase,
                        destinoInicial = destino,
                        // Callbacks opcionales
                        onRutaCalculada = { _, _, _, _ -> },
                        onRutaLimpiada = { }
                    )
                }
            }
        }
    }

    // Diálogo Confirmación de Solución
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar Solución") },
            text = { Text("¿El problema ha sido resuelto?") },
            confirmButton = {
                Button(
                    onClick = {
                        marcando = true
                        viewModel.marcarReporteComoSolucionado(
                            idDocumento = reporte.idDocumento ?: "",
                            onSuccess = { marcando = false; mostrarConfirmacion = false },
                            onError = { marcando = false }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }
}

suspend fun repetirAlertasNuevoReporte(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "canal_alertas_tecnico"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Alertas Técnico", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("🚨 Tarea Pendiente")
        .setContentText("Revisa tu lista de asignaciones.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    repeat(3) {
        notificationManager.notify(2000 + it, builder.build())
        delay(15_000L)
    }
}