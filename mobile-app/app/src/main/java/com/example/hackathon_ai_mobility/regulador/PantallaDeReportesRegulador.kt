package com.example.hackathon_ai_mobility.regulador

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun PantallaDeReportesRegulador(
    auth: FirebaseAuth,
    viewModel: ModeloDeVistaRegulador = viewModel(),
    navegarPantallaInicial: () -> Unit = {}
) {
    // Consumo de datos del ViewModel
    val listaReportes by viewModel.listaReportesSistema.collectAsState()

    // Filtro: Solo mostrar los reportes pendientes (reporteCompletado == 0) para que el regulador trabaje
    val reportesPendientes = listaReportes.filter { it.reporteCompletado == 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
    ) {
        // Encabezado
        Text(
            "Regulador - Peticiones",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = { navegarPantallaInicial() }, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Volver")
        }

        // Lista de tarjetas
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(reportesPendientes) { reporte ->
                ItemReporteRegulador(reporte, viewModel)
            }
        }
    }
}

@Composable
fun ItemReporteRegulador(
    reporte: ModeloReportesBD,
    viewModel: ModeloDeVistaRegulador
) {
    // Variables locales para el formulario de respuesta
    var reporteTecnico by remember { mutableStateOf("") }
    var equipoLlevar by remember { mutableStateOf("") }
    // Inicializa el tipo con el que envió el jefe de estación, por defecto 1 si es nulo
    var tipoSeleccionado by remember { mutableIntStateOf(reporte.tipoProblema ?: 1) }
    var enviando by remember { mutableStateOf(false) }

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

            // --- SECCIÓN 1: DATOS RECIBIDOS (SOLO LECTURA) ---
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Estación: ${reporte.estacionQueTieneReporte}",
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

            // Input: Selector de Tipo (1, 2, 3)
            Text("Confirmar Tipo de Problema:", color = Color.White, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 3).forEach { tipo ->
                    Button(
                        onClick = { tipoSeleccionado = tipo },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tipoSeleccionado == tipo) Color.Green else Color.Gray,
                            contentColor = if (tipoSeleccionado == tipo) Color.Black else Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(tipo.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de envío
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
                            // El reporte desaparecerá de la lista automáticamente al cambiar reporteCompletado a 1
                        },
                        onError = {
                            enviando = false
                            // Manejo de error básico
                        }
                    )
                },
                enabled = !enviando && reporteTecnico.isNotBlank() && equipoLlevar.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text(if (enviando) "Enviando..." else "Enviar Petición")
            }
        }
    }
}