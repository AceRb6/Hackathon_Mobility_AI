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
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

@Composable
fun PantallaTecnico(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaPantallaTecnico = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
) {
    val context = LocalContext.current
    val listaReportes by viewModel.listaReportesSistema.collectAsState()
    val estacionTecnico by viewModel.tecnicoDependencia.collectAsState()

    // Filtra por ESTADO 1 (En Proceso). El filtro de dependencia ya está en el ViewModel.
    val reportesAsignados = listaReportes.filter { it.reporteCompletado == 1 }

    // NOTIFICACIÓN (Lógica simplificada)
    var inicializado by remember { mutableStateOf(false) }
    var ultimoTamano by remember { mutableStateOf(0) }
    LaunchedEffect(reportesAsignados.size) {
        if (!inicializado) {
            ultimoTamano = reportesAsignados.size
            inicializado = true
        } else {
            if (reportesAsignados.size > ultimoTamano) {
                // Notificación de nuevo reporte
                repetirAlertasNuevoReporte(context)
            }
            ultimoTamano = reportesAsignados.size
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Black).padding(16.dp)
    ) {
        // Encabezado
        Text("Técnico - Tareas Asignadas", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Dependencia: ${estacionTecnico ?: "Cargando..."}", color = Color.Yellow, modifier = Modifier.padding(bottom = 16.dp))

        Button(onClick = { navegarPantallaInicial() }, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Cerrar Sesión")
        }

        if (reportesAsignados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay tareas asignadas en este momento.", color = Color.Gray)
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
                        estacionTecnico = estacionTecnico ?: "Origen", // Pasamos la estación
                        destinoReporte = reporte.estacionQueTieneReporte ?: "Destino"
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
    estacionTecnico: String,
    destinoReporte: String
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var marcando by remember { mutableStateOf(false) }

    // ESTADO PARA LA RUTA SIMULADA
    val resultadoRuta = viewModel.getBestRouteTime(estacionTecnico, destinoReporte)
    val tiempoRuta = resultadoRuta.first
    val modoRuta = resultadoRuta.second

    // Parseo del texto concatenado del Regulador
    val instruccion = reporte.reporteTecnicoRegulador?.substringBefore("| Equipo:")?.removePrefix("Instrucción:")?.trim() ?: "Sin instrucción"
    val equipo = reporte.reporteTecnicoRegulador?.substringAfter("| Equipo:", "")?.trim() ?: "Sin equipo"

    // Obtener color de prioridad
    val colorPrioridad = when (reporte.tipoProblema) {
        3 -> Color.Red
        2 -> Color(0xFFffc107)
        else -> Color(0xFF4CAF50)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado
            Text("Estación Destino: ${destinoReporte}", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Problema: ${reporte.tituloReporte ?: "-"}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Prioridad: Nivel ${reporte.tipoProblema}", color = colorPrioridad)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            // SECCIÓN RUTA SIMULADA
            Text("RUTA MÁS RÁPIDA (Simulada)", color = Color.Yellow, fontWeight = FontWeight.Bold)
            Text("Origen: $estacionTecnico", color = Color.LightGray)
            Row {
                Text("Tiempo Estimado: ", color = Color.White)
                Text("${tiempoRuta} min", color = Color.Red, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Text("(${modoRuta})", color = Color.LightGray)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            // Instrucción del Regulador
            Text("Instrucción Regulador:", color = Color.Green, fontWeight = FontWeight.SemiBold)
            Text(instruccion, color = Color.White)
            Text("Equipo Requerido: $equipo", color = Color.White)

            Spacer(modifier = Modifier.height(12.dp))

            // --- BOTONES DE ACCIÓN ---
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {

                // Botón para Mapa y Ruta (Llamaría a Intent o Navegación)
                Button(
                    onClick = {
                        // Lógica real: Abrir Google Maps Intent con origen/destino
                        Log.i("MAPS", "Navegando a $destinoReporte desde $estacionTecnico...")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF03A9F4))
                ) {
                    Text("Abrir Mapa 🗺️", fontSize = 12.sp)
                }

                // Botón: Se solucionó problema (Abre Diálogo)
                Button(
                    onClick = { mostrarConfirmacion = true },
                    enabled = !marcando && !reporte.idDocumento.isNullOrBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Solucionado", fontSize = 12.sp)
                }
            }
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN ---
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Finalizar Tarea") },
            text = { Text("¿Confirma que el problema en ${reporte.estacionQueTieneReporte} ha sido completamente resuelto?") },
            confirmButton = {
                Button(
                    onClick = {
                        val id = reporte.idDocumento ?: ""
                        if (id.isNotBlank()) {
                            marcando = true
                            viewModel.marcarReporteComoSolucionado(
                                idDocumento = id,
                                onSuccess = {
                                    marcando = false
                                    mostrarConfirmacion = false
                                },
                                onError = {
                                    marcando = false
                                    mostrarConfirmacion = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") } }
        )
    }
}
/**
 * Función simulada para lanzar un intento de mapa o diálogo de ruta.
 * Nota: La lógica de la API de Maps debe ser implementada por el desarrollador.
 */
fun mostrarRutaMaps(origen: String, destino: String) {
    // Aquí podrías usar un Intent para abrir la aplicación de Google Maps
    // o navegar a un Composable que muestre la información de la ruta más rápida.
    Log.i("MAPS", "Consultando ruta: $origen -> $destino")
    // Ejemplo de Intent (Necesitaría importaciones en el archivo)
    // val gmmIntentUri = Uri.parse("google.navigation:q=$destino&mode=d")
    // val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    // mapIntent.setPackage("com.google.android.apps.maps")
    // context.startActivity(mapIntent)
}

/**
 * Lanza una notificación al sistema cada 30 segundos durante 2 minutos
 * para avisar al técnico que tiene un nuevo reporte.
 */
suspend fun repetirAlertasNuevoReporte(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelId = "canal_alertas_tecnico"
    val channelName = "Alertas de reportes para técnico"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("🚨 Nuevo reporte asignado")
        .setContentText("Tienes un nuevo reporte técnico por atender.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    // 2 minutos / cada 30s ≈ 4 repeticiones
    repeat(4) { index ->
        notificationManager.notify(1001, builder.build())
        if (index < 3) {
            delay(30_000L) // 30 segundos
        }
    }
}