package com.example.hackathon_ai_mobility.tecnico

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.hackathon_ai_mobility.servicioMapas.ServicioMapas
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@Composable
fun MapaRutaOSMScreen(
    origenNombre: String,
    destinoNombre: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mapView: MapView? by remember { mutableStateOf(null) }

    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    LaunchedEffect(origenNombre, destinoNombre) {
        coroutineScope.launch {
            val origenCoords = ServicioMapas.obtenerCoordenadas(origenNombre)
            val destinoCoords = ServicioMapas.obtenerCoordenadas(destinoNombre)

            if (origenCoords != null && destinoCoords != null) {
                val puntosRuta = ServicioMapas.obtenerPuntosRuta(origenCoords, destinoCoords)

                mapView?.let { map ->
                    map.overlays.clear()

                    val startMarker = org.osmdroid.views.overlay.Marker(map)
                    startMarker.position = origenCoords
                    startMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    startMarker.title = "Origen: $origenNombre"
                    map.overlays.add(startMarker)

                    val endMarker = org.osmdroid.views.overlay.Marker(map)
                    endMarker.position = destinoCoords
                    endMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                    endMarker.title = "Destino: $destinoNombre"
                    map.overlays.add(endMarker)

                    if (puntosRuta.isNotEmpty()) {
                        val polyline = Polyline()
                        polyline.setPoints(puntosRuta)
                        polyline.outlinePaint.color = ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                        polyline.outlinePaint.strokeWidth = 10f
                        map.overlays.add(polyline)
                    }

                    val mapController = map.controller
                    mapController.setZoom(14.0)
                    mapController.setCenter(origenCoords)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Button(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(androidx.compose.ui.Alignment.TopStart)
        ) {
            Text("Volver")
        }
    }
}