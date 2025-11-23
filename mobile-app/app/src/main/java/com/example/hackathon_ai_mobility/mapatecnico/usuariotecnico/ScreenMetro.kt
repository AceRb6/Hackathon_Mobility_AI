package com.example.hackathon_ai_mobility.mapa.usuario

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hackathon_ai_mobility.R
import com.example.hackathon_ai_mobility.dijkstra.GrafoMetroCompleto
import com.example.hackathon_ai_mobility.dijkstra.computeBestRoute
import com.example.hackathon_ai_mobility.mapa.EstacionMetro
import com.example.hackathon_ai_mobility.mapa.EstacionesMetro

private const val TAG = "ScreenMetroUsuario"
private val ColorRuta = Color(0xFFFF9800)

@Composable
fun ScreenDeMetro(
    viewModel: MetroUsuarioViewModel = viewModel(),
    onRutaCalculada: ((String, String, Int, List<String>) -> Unit)? = null,
    onRutaLimpiada: (() -> Unit)? = null,
    origenInicial: String? = null,
    destinoInicial: String? = null
) {
    val grafoMetro by viewModel.grafo.collectAsState()
    val estacionesActivas by viewModel.estacionesActivas.collectAsState()

    val estaciones = remember { EstacionesMetro.todasLasEstacionesMetro }

    var estacionOrigen by remember { mutableStateOf<EstacionMetro?>(null) }
    var estacionDestino by remember { mutableStateOf<EstacionMetro?>(null) }

    var pasosRuta by remember { mutableStateOf<List<String>>(emptyList()) }
    var estacionesEnRuta by remember { mutableStateOf<Set<String>>(emptySet()) }

    var mostrarDialogoSinRuta by remember { mutableStateOf(false) }
    var mensajeDialogoSinRuta by remember { mutableStateOf("") }

    var mostrarDialogoEstacionesCerradas by remember { mutableStateOf(false) }
    var estacionesCerradasEnRuta by remember { mutableStateOf<List<String>>(emptyList()) }

    // Calcula la ruta automáticamente cuando llegan origen/destino
    LaunchedEffect(origenInicial, destinoInicial, grafoMetro, estacionesActivas) {
        if (origenInicial.isNullOrBlank() || destinoInicial.isNullOrBlank()) {
            pasosRuta = emptyList()
            estacionesEnRuta = emptySet()
            estacionesCerradasEnRuta = emptyList()
            mostrarDialogoEstacionesCerradas = false
            mostrarDialogoSinRuta = false
            onRutaLimpiada?.invoke()
            return@LaunchedEffect
        }

        val grafo: GrafoMetroCompleto = grafoMetro ?: run {
            Log.d(TAG, "Grafo aún no cargado, no se puede calcular ruta")
            return@LaunchedEffect
        }

        val origenEst = estaciones.firstOrNull { it.nombre.equals(origenInicial, ignoreCase = true) }
        val destinoEst = estaciones.firstOrNull { it.nombre.equals(destinoInicial, ignoreCase = true) }

        if (origenEst == null || destinoEst == null) {
            Log.w(TAG, "No encontré origen=$origenInicial o destino=$destinoInicial en EstacionesMetro")
            return@LaunchedEffect
        }

        estacionOrigen = origenEst
        estacionDestino = destinoEst

        Log.d(TAG, "Calculando ruta de ${origenEst.nombre} a ${destinoEst.nombre}")

        val resultado = computeBestRoute(grafo, origenEst.nombre, destinoEst.nombre)

        if (resultado == null) {
            Log.d(TAG, "No hay ruta entre ${origenEst.nombre} y ${destinoEst.nombre}")

            estacionesEnRuta = emptySet()
            pasosRuta = emptyList()
            estacionesCerradasEnRuta = emptyList()
            mostrarDialogoEstacionesCerradas = false

            mensajeDialogoSinRuta =
                "No hay ruta disponible entre ${origenEst.nombre} y ${destinoEst.nombre}. " +
                        "Es posible que algunas estaciones estén cerradas."
            mostrarDialogoSinRuta = true

            onRutaLimpiada?.invoke()
            return@LaunchedEffect
        }

        val (info, pasos) = resultado

        Log.d(
            TAG,
            "Ruta encontrada: metros=${info.totalMetros}, transbordos=${info.totalTransbordos}"
        )

        val cerradasEnRuta = mutableSetOf<String>()
        val estacionesEnRutaTmp = mutableSetOf<String>()

        pasos.forEach { paso ->
            Log.d(TAG, "Paso: $paso")
            val nombreEstacion = when {
                paso.startsWith("Transbordo en") ->
                    paso.removePrefix("Transbordo en").substringBefore("→").trim()
                else ->
                    paso.substringBefore("(").trim()
            }

            val keyNombre = nombreEstacion.lowercase()
            val estaAbierta = estacionesActivas[keyNombre] ?: true
            if (!estaAbierta) cerradasEnRuta += nombreEstacion

            estacionesEnRutaTmp += nombreEstacion
        }

        estacionesEnRuta = estacionesEnRutaTmp
        pasosRuta = pasos

        if (cerradasEnRuta.isNotEmpty()) {
            estacionesCerradasEnRuta = cerradasEnRuta.toList()
            mostrarDialogoEstacionesCerradas = true
        } else {
            estacionesCerradasEnRuta = emptyList()
            mostrarDialogoEstacionesCerradas = false
        }

        onRutaCalculada?.invoke(
            origenEst.nombre,
            destinoEst.nombre,
            info.totalMetros,
            pasos
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // MAPA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {

                var escala by remember { mutableStateOf(1f) }
                var desplazamiento by remember { mutableStateOf(Offset.Zero) }

                val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
                    val nuevaEscala = (escala * zoomChange).coerceIn(1f, 4f)
                    escala = nuevaEscala
                    desplazamiento += panChange
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = escala
                            scaleY = escala
                            translationX = desplazamiento.x
                            translationY = desplazamiento.y
                        }
                        .transformable(transformableState)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mapa_metro),
                        contentDescription = "Mapa Metro",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        estaciones.forEach { estacion ->
                            val inRoute = estacionesEnRuta.any {
                                it.equals(estacion.nombre, ignoreCase = true)
                            }

                            val color = when {
                                estacionOrigen?.nombre == estacion.nombre -> Color.Green
                                estacionDestino?.nombre == estacion.nombre -> Color.Red
                                inRoute -> ColorRuta
                                else -> Color.Gray
                            }

                            drawCircle(
                                color = color,
                                radius = 6.dp.toPx(),
                                center = Offset(estacion.x, estacion.y)
                            )
                        }
                    }
                }
            }

            // LISTA DE PASOS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp)
                    .padding(16.dp)
            ) {
                if (estacionOrigen != null && estacionDestino != null) {
                    Text(
                        text = "Ruta de ${estacionOrigen!!.nombre} a ${estacionDestino!!.nombre}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(pasosRuta) { paso ->
                        Text(
                            text = "• $paso",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Diálogo estaciones cerradas
        if (mostrarDialogoEstacionesCerradas && estacionesCerradasEnRuta.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoEstacionesCerradas = false },
                title = { Text("Estaciones cerradas en la ruta") },
                text = {
                    Text(estacionesCerradasEnRuta.joinToString(separator = "\n"))
                },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoEstacionesCerradas = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }

        // Diálogo sin ruta
        if (mostrarDialogoSinRuta) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoSinRuta = false },
                title = { Text("Sin ruta disponible") },
                text = { Text(mensajeDialogoSinRuta) },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogoSinRuta = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}
