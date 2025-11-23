package com.example.hackathon_ai_mobility.tecnico

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
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

@Composable
fun PantallaTecnico(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaPantallaTecnico = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
) {
    val context = LocalContext.current

    // Datos simulados del ViewModel
    val listaReportes by viewModel.listaReportesSistema.collectAsState()
    val estacionTecnico by viewModel.tecnicoDependencia.collectAsState() // "Zaragoza" por defecto en el mock

    // Filtro (aunque el mock ya trae solo los de estado 1)
    val reportesAsignados = remember(listaReportes) {
        listaReportes.filter { it.reporteCompletado == 1 }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
    ) {
        Text(
            "Técnico - Tareas Asignadas",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Base: ${estacionTecnico ?: "Central"}",
            color = Color.Yellow,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
        )

        Button(
            onClick = { navegarPantallaInicial() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
        ) {
            Text("Cerrar Sesión")
        }

        if (reportesAsignados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay tareas asignadas.", color = Color.Gray)
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
                        estacionTecnico = estacionTecnico ?: "CDMX"
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
    estacionTecnico: String
) {
    val context = LocalContext.current
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var marcando by remember { mutableStateOf(false) }

    // Cálculo de ruta (Simulado)
    val destino = reporte.estacionQueTieneReporte ?: "Destino"
    val (tiempoRuta, modoRuta) = viewModel.getBestRouteTime(estacionTecnico, destino)

    // Parseo de texto
    val instruccion = reporte.reporteTecnicoRegulador?.substringAfter("Instrucción:")?.substringBefore("|")?.trim() ?: ""
    val equipo = reporte.reporteTecnicoRegulador?.substringAfter("| Equipo:")?.trim() ?: ""

    val colorPrioridad = when (reporte.tipoProblema) {
        3 -> Color(0xFFF44336) // Rojo
        2 -> Color(0xFFFFC107) // Amarillo
        else -> Color(0xFF4CAF50) // Verde
    }

    val tituloPrioridad = when (reporte.tipoProblema) {
        3 -> "ALTA / CRÍTICA"
        2 -> "MEDIA"
        else -> "BAJA"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(reporte.estacionQueTieneReporte ?: "-", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Surface(color = colorPrioridad, shape = MaterialTheme.shapes.small) {
                    Text(tituloPrioridad, color = if(reporte.tipoProblema == 2) Color.Black else Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Problema: ${reporte.tituloReporte}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Hora: ${reporte.horaProblema}", color = Color.Gray, fontSize = 12.sp)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            // Ruta
            Text("RUTA SUGERIDA (Desde $estacionTecnico)", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$tiempoRuta min", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text("via $modoRuta", color = Color.LightGray, fontSize = 12.sp)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)

            // Instrucciones
            Text("Instrucción:", color = Color.Green, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(instruccion, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Equipo:", color = Color.Green, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(equipo, color = Color.White, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(16.dp))

            // BOTONES
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón Mapa
                Button(
                    onClick = {
                        // Lanza Google Maps con la ruta
                        lanzarGoogleMapsIntent(context, estacionTecnico, destino)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF039BE5))
                ) {
                    Text("Ver Mapa 🗺️")
                }

                // Botón Solucionado
                Button(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Text("Completar")
                }
            }
        }
    }

    // Diálogo
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar Finalización") },
            text = { Text("¿El problema en $destino ha sido resuelto?") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }
}

/**
 * Lanza la aplicación de Google Maps.
 */
fun lanzarGoogleMapsIntent(context: Context, origen: String, destino: String) {
    try {
        // Intentamos abrir navegación en transporte público
        val uri = Uri.parse("google.navigation:q=${Uri.encode(destino + " Metro CDMX")}&mode=transit")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback al navegador
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${Uri.encode(origen)}&destination=${Uri.encode(destino)}&travelmode=transit")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Log.e("MAPS", "Error maps: ${e.message}")
    }
}

suspend fun repetirAlertasNuevoReporte(context: Context) {
    // ... (Misma implementación de notificación que tenías) ...
    // Para ahorrar espacio, solo asegúrate de copiar la función que te di antes aquí
}