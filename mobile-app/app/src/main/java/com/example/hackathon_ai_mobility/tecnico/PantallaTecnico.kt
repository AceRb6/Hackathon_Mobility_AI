package com.example.hackathon_ai_mobility.tecnico

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun PantallaTecnico(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaPantallaTecnico = viewModel(),
    navegarPantallaInicial: () -> Unit = {},
    // 🔹 Callback para navegar al mapa con origen y destino
    navegarAMapaRutaOSM: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    // Flujos del ViewModel
    val listaReportes by viewModel.listaReportesSistema.collectAsState()
    val estacionTecnico by viewModel.tecnicoDependencia.collectAsState()

    // Filtramos sólo reportes asignados (reporteCompletado == 1)
    val reportesAsignados = remember(listaReportes) {
        listaReportes.filter { it.reporteCompletado == 1 }
    }

    // Lógica para detectar nuevos reportes y disparar notificaciones
    var inicializado by remember { mutableStateOf(false) }
    var ultimoTamano by remember { mutableStateOf(0) }

    LaunchedEffect(reportesAsignados.size) {
        if (!inicializado) {
            // Primera vez: solo inicializamos el tamaño
            ultimoTamano = reportesAsignados.size
            inicializado = true
        } else {
            // Si hay más reportes que antes, disparar alertas
            if (reportesAsignados.size > ultimoTamano) {
                repetirAlertasNuevoReporte(context)
            }
            ultimoTamano = reportesAsignados.size
        }
    }

    // UI principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Técnico - Tareas Asignadas",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Dependencia: ${estacionTecnico ?: "Cargando..."}",
            color = Color.Yellow,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
        )

        Button(
            onClick = {
                // Si quieres, aquí también puedes hacer: auth.signOut()
                navegarPantallaInicial()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Cerrar Sesión")
        }

        if (reportesAsignados.isEmpty()) {
            // Sin tareas asignadas
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay tareas asignadas en este momento.",
                    color = Color.Gray
                )
            }
        } else {
            // Lista de reportes asignados
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(reportesAsignados) { reporte ->
                    ItemReporteTecnico(
                        reporte = reporte,
                        viewModel = viewModel,
                        estacionTecnico = estacionTecnico ?: "Origen",
                        destinoReporte = reporte.estacionQueTieneReporte ?: "Destino",
                        navegarAMapaRutaOSM = navegarAMapaRutaOSM
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
    destinoReporte: String,
    // 🔹 Aquí recibimos el callback para abrir el mapa
    navegarAMapaRutaOSM: (String, String) -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var marcando by remember { mutableStateOf(false) }

    // Cálculo de ruta simulada (usa viewModel.getBestRouteTime)
    val resultadoRuta = viewModel.getBestRouteTime(estacionTecnico, destinoReporte)
    val tiempoRuta = resultadoRuta.first
    val modoRuta = resultadoRuta.second

    // Parseo de la cadena reporteTecnicoRegulador, por ejemplo:
    // "Instrucción: Revisar subestación X | Equipo: Herramientas de alta tensión"
    val instruccion = reporte.reporteTecnicoRegulador
        ?.substringAfter("Instrucción:", "")
        ?.substringBefore("|")
        ?.trim()
        .takeIf { !it.isNullOrBlank() }
        ?: "Sin instrucción"

    val equipo = reporte.reporteTecnicoRegulador
        ?.substringAfter("| Equipo:", "")
        ?.trim()
        .takeIf { !it.isNullOrBlank() }
        ?: "Sin equipo"

    // Color según nivel de prioridad
    val colorPrioridad = when (reporte.tipoProblema) {
        3 -> Color.Red          // Alta
        2 -> Color(0xFFFFC107)  // Media
        else -> Color(0xFF4CAF50) // Baja / normal
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado del reporte
            Text(
                text = "Estación Destino: $destinoReporte",
                color = Color.Cyan,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Problema: ${reporte.tituloReporte ?: "-"}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Prioridad: Nivel ${reporte.tipoProblema}",
                color = colorPrioridad,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray
            )

            // Bloque de ruta recomendada (simulada)
            Text(
                text = "RUTA MÁS RÁPIDA (Simulada)",
                color = Color.Yellow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Origen: $estacionTecnico",
                color = Color.LightGray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Tiempo Estimado: ", color = Color.White)
                Text(
                    text = "$tiempoRuta min",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(12.dp))
                Text(text = "($modoRuta)", color = Color.LightGray)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray
            )

            // Instrucciones del regulador
            Text(
                text = "Instrucción del Regulador:",
                color = Color.Green,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = instruccion, color = Color.White)
            Text(text = "Equipo Requerido: $equipo", color = Color.White)

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                // 🔹 Botón para ir al mapa con la ruta sugerida
                Button(
                    onClick = {
                        // Lista de estaciones candidatas para origen
                        val candidatos = listOf("Chapultepec", "Moctezuma", "Zaragoza")

                        // Elegimos la de MENOR tiempo estimado hacia la estación del reporte
                        val origenMasCercano = candidatos.minByOrNull { origenCandidato ->
                            viewModel.getBestRouteTime(origenCandidato, destinoReporte).first
                        } ?: estacionTecnico   // fallback por si acaso

                        // Navegamos al mapa con origen = la más cercana y destino = estación del reporte
                        navegarAMapaRutaOSM(origenMasCercano, destinoReporte)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF03A9F4)
                    )
                ) {
                    Text("Abrir Mapa 🗺️", fontSize = 12.sp)
                }

                // Botón para marcar como solucionado
                Button(
                    onClick = { mostrarConfirmacion = true },
                    enabled = !marcando && !reporte.idDocumento.isNullOrBlank(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Solucionado", fontSize = 12.sp)
                }
            }
        }
    }

    // Diálogo de confirmación al marcar como solucionado
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Finalizar Tarea") },
            text = {
                Text(
                    "¿Confirma que el problema en ${reporte.estacionQueTieneReporte} ha sido completamente resuelto?"
                )
            },
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
                                    // Aquí podrías mostrar un snackbar / diálogo de error si quieres
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Envía varias notificaciones para avisar al técnico que tiene un nuevo reporte.
 * Se llama desde un LaunchedEffect cuando aumenta la cantidad de reportes asignados.
 */
suspend fun repetirAlertasNuevoReporte(context: Context) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelId = "canal_alertas_tecnico"
    val channelName = "Alertas de Reportes Técnicos"

    // Crear canal de notificación en Android 8.0+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones cuando hay nuevos reportes asignados al técnico"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("🚨 Nuevo reporte asignado")
        .setContentText("Tienes un nuevo reporte técnico por atender.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    // Repetimos la notificación varias veces con intervalo
    repeat(4) { index ->
        notificationManager.notify(1001 + index, builder.build())
        if (index < 3) {
            // Espera 30 segundos entre notificaciones
            delay(30_000L)
        }
    }
}
