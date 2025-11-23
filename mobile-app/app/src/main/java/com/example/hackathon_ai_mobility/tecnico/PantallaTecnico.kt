package com.example.hackathon_ai_mobility.tecnico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD

@Composable
fun PantallaTecnico(
    navController: NavHostController,
    viewModel: ModeloDeVistaPantallaTecnico = viewModel()
) {
    val reportes by viewModel.listaReportesSistema.collectAsState()
    val dependencia by viewModel.tecnicoDependencia.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Panel del técnico",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Dependencia: ${dependencia ?: "Sin estación asignada"}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(reportes) { reporte ->
                ReporteCardTecnico(
                    reporte = reporte,
                    dependenciaTecnico = dependencia,
                    onVerRuta = { origen, destino ->
                        val origenParam = origen.replace(" ", "%20")
                        val destinoParam = destino.replace(" ", "%20")
                        navController.navigate("screenMetro?origen=$origenParam&destino=$destinoParam")
                    },
                    onMarcarSolucionado = { id ->
                        viewModel.marcarReporteComoSolucionado(
                            idDocumento = id,
                            onSuccess = { viewModel.cargarReportesSistema() },
                            onError = { /* podrías mostrar un snackbar, log, etc. */ }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReporteCardTecnico(
    reporte: ModeloReportesBD,
    dependenciaTecnico: String?,
    onVerRuta: (String, String) -> Unit,
    onMarcarSolucionado: (String) -> Unit
) {
    val estacionTecnico = dependenciaTecnico
        ?.trim()
        ?.replaceFirstChar { it.uppercaseChar() } // "zaragoza" -> "Zaragoza"

    val estacionIncidente = reporte.estacionQueTieneReporte?.trim()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = reporte.tituloReporte ?: "(Sin título)",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Estación con incidente: ${reporte.estacionQueTieneReporte ?: "—"}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Descripción: ${reporte.descripcionReporteJefeDeEstacion ?: "—"}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (!estacionTecnico.isNullOrBlank() && !estacionIncidente.isNullOrBlank()) {
                    Button(onClick = {
                        onVerRuta(estacionTecnico, estacionIncidente)
                    }) {
                        Text("Ver ruta en mapa")
                    }
                } else {
                    Text(
                        text = "Sin estación asignada al técnico o al reporte",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                val idDoc = reporte.idDocumento
                if (!idDoc.isNullOrBlank()) {
                    TextButton(onClick = { onMarcarSolucionado(idDoc) }) {
                        Text("Marcar solucionado")
                    }
                }
            }
        }
    }
}
