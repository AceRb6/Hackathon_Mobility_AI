package com.example.hackathon_ai_mobility.tecnico

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.app.NotificationCompat
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

    // Consumimos los datos del ViewModel
    val listaReportes by viewModel.listaReportesSistema.collectAsState()

    // Para el técnico: solo reportes con respuesta del regulador y todavía no completados
    val reportesAsignados = listaReportes.filter {
        (it.reporteTecnicoRegulador?.isNotBlank() == true) && (it.reporteCompletado == 0)
    }

    // Control para detectar NUEVOS reportes y lanzar alerta
    var inicializado by remember { mutableStateOf(false) }
    var ultimoTamano by remember { mutableStateOf(0) }

    LaunchedEffect(reportesAsignados.size) {
        if (!inicializado) {
            // Primera vez: solo guardamos el tamaño, no notificamos
            ultimoTamano = reportesAsignados.size
            inicializado = true
        } else {
            if (reportesAsignados.size > ultimoTamano) {
                // Llegó al menos 1 reporte nuevo al técnico
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
        // Encabezado
        Text(
            "Técnico - Reportes pendientes",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = { navegarPantallaInicial() }, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Volver")
        }

        // Lista de reportes asignados
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(reportesAsignados) { reporte ->
                ItemReporteTecnico(reporte = reporte, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ItemReporteTecnico(
    reporte: ModeloReportesBD,
    viewModel: ModeloDeVistaPantallaTecnico
) {
    var mostrarDetalles by remember { mutableStateOf(false) }
    var marcando by remember { mutableStateOf(false) }

    // Formateo seguro de fecha
    val fechaFormateada = remember(reporte.fechaHoraCreacionReporte) {
        try {
            val date = reporte.fechaHoraCreacionReporte?.toDate()
            if (date != null) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            } else "Sin fecha"
        } catch (e: Exception) {
            "Error fecha"
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado de la card
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Estación: ${reporte.estacionQueTieneReporte ?: "-"}",
                    color = Color.Cyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = fechaFormateada,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título del reporte
            Text(
                "Título: ${reporte.tituloReporte ?: "-"}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            // Tipo de problema
            Text(
                "Tipo de problema: ${reporte.tipoProblema ?: "-"}",
                color = Color.White
            )

            // Hora de inicio
            Text(
                "Hora de inicio: ${reporte.horaProblema ?: "-"}",
                color = Color.White
            )

            // Localización
            Text(
                "Localización: ${reporte.estacionQueTieneReporte ?: "-"}",
                color = Color.White
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray
            )

            // Opción para ver detalles
            TextButton(onClick = { mostrarDetalles = !mostrarDetalles }) {
                Text(
                    if (mostrarDetalles) "Ocultar detalles" else "Ver detalles",
                    color = Color.Cyan
                )
            }

            if (mostrarDetalles) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Descripción (reporte técnico del regulador):",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reporte.reporteTecnicoRegulador ?: "Sin descripción técnica",
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón: Se solucionó problema
            Button(
                onClick = {
                    val id = reporte.idDocumento ?: ""
                    if (id.isNotBlank()) {
                        marcando = true
                        viewModel.marcarReporteComoSolucionado(
                            idDocumento = id,
                            onSuccess = { marcando = false },
                            onError = { marcando = false }
                        )
                    }
                },
                enabled = !marcando && !reporte.idDocumento.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(if (marcando) "Guardando..." else "Se solucionó problema")
            }
        }
    }
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
        .setContentTitle("Nuevo reporte asignado")
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
