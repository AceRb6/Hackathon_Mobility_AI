package com.example.hackathon_ai_mobility.prueba_mapa.usuario

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.hackathon_ai_mobility.R
import com.example.hackathon_ai_mobility.dijkstra.GrafoMetroCompleto
import com.example.hackathon_ai_mobility.dijkstra.computeBestRoute

import com.example.appmetrocdmx.presentation.prueba_mapa.EstacionesMetro
import com.example.hackathon_ai_mobility.ui.theme.NaranjaAppMetroCDMX
import androidx.compose.runtime.produceState
import com.google.firebase.firestore.FirebaseFirestore

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.*
import com.example.appmetrocdmx.presentation.prueba_mapa.EstacionMetro
import com.example.hackathon_ai_mobility.mapatecnico.usuariotecnico.MetroUsuarioViewModel


private const val TAG = "ScreenDeMetroUsuario"

//@Preview
@Composable
fun ScreenDeMetroUsuario(
    /* onRutaCalculada: ((origen: String, destino: String, totalMetros: Int, pasos: List<String>) -> Unit)? = null,
     onRutaLimpiada: (() -> Unit)? = null*/
    viewModel: MetroUsuarioViewModel = viewModel(),
    origenInicial: String? = null,
    destinoInicial: String? = null,
    onRutaCalculada: ((String, String, Int, List<String>) -> Unit)? = null,
    onRutaLimpiada: (() -> Unit)? = null
) {

    val grafoMetro by viewModel.grafo.collectAsState()
    Log.d(TAG, "Grafo cargado desde Firebase")


    val contexto = LocalContext.current

    // Mapa nombre estación -> abierta/cerrada que viene de Firestore
    val estacionesActivas by viewModel.estacionesActivas.collectAsState()

    // Estado para el AlertDialog de estaciones cerradas
    var mostrarDialogoEstacionesCerradas by remember { mutableStateOf(false) }
    var estacionesCerradasEnRuta by remember { mutableStateOf<List<String>>(emptyList()) }

    // NUEVO: diálogo cuando no existe ruta entre origen y destino
    var mostrarDialogoSinRuta by remember { mutableStateOf(false) }
    var mensajeDialogoSinRuta by remember { mutableStateOf("") }


    // 1. cargar el grafo desde el txt offline
    /*val grafoMetro by remember {
        mutableStateOf(
            GrafoMetroCompleto.fromText(
                contexto.assets.open("tramos_metro.txt")
                    .bufferedReader()
                    .use { it.readText() }
            )
        )
    }
    Log.d(TAG, "Grafo cargado desde assets")*/
    // Grafo cargado desde Firestore (puede ser null mientras se carga)
    /* val grafoMetro by produceState<GrafoMetroCompleto?>(initialValue = null) {
         value = cargarGrafoDesdeFirestore(
             db = FirebaseFirestore.getInstance()
         )
     }*/


    // 2. lista de estaciones
    val estaciones = EstacionesMetro.todasLasEstacionesMetro
    Log.d(TAG, "Total de estaciones: ${estaciones.size}")

    // 3. visibilidad (todas apagadas al inicio)
    val visibles = remember(estaciones) {
        mutableStateListOf<Boolean>().apply {
            repeat(estaciones.size) { add(false) }
        }
    }

    // 4. nombre -> indices (para pantitlán repetida, etc.)
    val indicesPorNombre = remember(estaciones) {
        estaciones.withIndex()
            .groupBy(
                keySelector = { it.value.nombre.trim().lowercase() },
                valueTransform = { it.index }
            )
    }
    Log.d(TAG, "Mapa de nombre->indices creado con ${indicesPorNombre.size} claves")

    //Inicio Dialogo para lo de si cerre alguna estacion-----------------------------------
    LaunchedEffect(estacionesActivas) {
        // Si no hay ruta dibujada (ninguna estación "visible"), no hay nada que comprobar
        val hayRutaActual = visibles.any { it }
        if (!hayRutaActual) return@LaunchedEffect

        val cerradasEnRuta = mutableSetOf<String>()

        // Recorremos las estaciones que forman parte de la ruta (visibles == true)
        estaciones.forEachIndexed { index, est ->
            if (visibles[index]) {
                val key = est.nombre.trim().lowercase()
                val estaAbierta = estacionesActivas[key] ?: true
                if (!estaAbierta) {
                    cerradasEnRuta += est.nombre
                }
            }
        }

        // Si ahora hay estaciones cerradas en la ruta, mostramos el diálogo
        if (cerradasEnRuta.isNotEmpty()) {
            estacionesCerradasEnRuta = cerradasEnRuta.toList()
            mostrarDialogoEstacionesCerradas = true
        } else {
            estacionesCerradasEnRuta = emptyList()
            mostrarDialogoEstacionesCerradas = false
        }
    }

    //FIN Dialogo para lo de si cerre alguna estacion-----------------------------------

    // estados de origen/destino
    var estacionOrigen by remember { mutableStateOf<EstacionMetro?>(null) }
    var estacionDestino by remember { mutableStateOf<EstacionMetro?>(null) }

// 👇 SOLO LÓGICA, NINGÚN @Composable AQUÍ
    LaunchedEffect(origenInicial, destinoInicial, grafoMetro) {
        if (origenInicial.isNullOrBlank() || destinoInicial.isNullOrBlank()) return@LaunchedEffect

        val grafo = grafoMetro ?: return@LaunchedEffect

        // Limpiamos cualquier ruta previa
        for (i in visibles.indices) visibles[i] = false

        val resultado = computeBestRoute(grafo, origenInicial, destinoInicial) ?: return@LaunchedEffect

        val (_, pasos) = resultado

        // Marcamos como visibles las estaciones de la ruta
        pasos.forEach { paso ->
            val nombreEstacion = when {
                paso.startsWith("Transbordo en") ->
                    paso.removePrefix("Transbordo en").substringBefore("→").trim()
                else ->
                    paso.substringBefore("(").trim()
            }

            val keyNombre = nombreEstacion.lowercase()
            indicesPorNombre[keyNombre]?.forEach { idx ->
                visibles[idx] = true
            }
        }

        // Guardamos origen/destino para que la UI los conozca
        estacionOrigen = estaciones.firstOrNull {
            it.nombre.trim().equals(origenInicial.trim(), ignoreCase = true)
        }
        estacionDestino = estaciones.firstOrNull {
            it.nombre.trim().equals(destinoInicial.trim(), ignoreCase = true)
        }
    }

// 👇 A PARTIR DE AQUÍ YA VIENE EL MAPA (COMPOSABLES)
    BoxWithConstraints(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(2 / 3f)
    ) {
        val density = LocalDensity.current
        val anchoPx = with(density) { maxWidth.toPx() }
        val altoPx = with(density) { maxHeight.toPx() }
        val radioToquePx = with(density) { 18.dp.toPx() }

        // ===== ZOOM Y PAN =====
        var escala by remember { mutableStateOf(1f) }
        var desplazamiento by remember { mutableStateOf(Offset.Zero) }
        val minEscala = 1f
        val maxEscala = 5f

        val transformState = rememberTransformableState { zoomChange, panChange, _ ->
            val nuevaEscala = (escala * zoomChange).coerceIn(minEscala, maxEscala)

            val boost = 1.2f * nuevaEscala.coerceAtLeast(1f)
            val desplazamientoTentativo = desplazamiento + panChange * boost

            val maxOffsetX = (anchoPx * (nuevaEscala - 1f)) / 2f
            val maxOffsetY = (altoPx * (nuevaEscala - 1f)) / 2f

            desplazamiento = Offset(
                x = desplazamientoTentativo.x.coerceIn(-maxOffsetX, maxOffsetX),
                y = desplazamientoTentativo.y.coerceIn(-maxOffsetY, maxOffsetY)
            )

            escala = nuevaEscala
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = desplazamiento.x
                    translationY = desplazamiento.y
                    scaleX = escala
                    scaleY = escala
                }
                .transformable(transformState)
        ) {
            // fondo
            Image(
                painter = painterResource(id = R.drawable.metro_cdmx),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.matchParentSize()
            )

            // canvas encima
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(estaciones, escala, desplazamiento, estacionesActivas) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (escala > 1.01f) {
                                    Log.d(TAG, "Doble tap: reset zoom -> escala 1, sin desplazamiento")
                                    escala = 1f
                                    desplazamiento = Offset.Zero
                                } else {
                                    Log.d(TAG, "Doble tap: zoom x2")
                                    escala = 2f
                                    desplazamiento = Offset.Zero
                                }
                            }
                        ) { tapPantalla ->
                            Log.d(TAG, "Tap en pantalla: $tapPantalla")

                            // buscar estación más cercana
                            val tocada = estaciones.minByOrNull { est ->
                                val estPx = Offset(anchoPx * est.x, altoPx * est.y)
                                estPx.minus(tapPantalla).getDistance()
                            }

                            if (tocada == null) {
                                Log.d(TAG, "No se encontró estación cercana")
                                return@detectTapGestures
                            }

                            val estPx = Offset(anchoPx * tocada.x, altoPx * tocada.y)
                            val distancia = estPx.minus(tapPantalla).getDistance()
                            Log.d(TAG, "Estación más cercana: ${tocada.nombre} (distancia=$distancia)")

                            if (distancia > radioToquePx) {
                                Log.d(TAG, "Tap fuera del radio de toque ($radioToquePx), no se toma")
                                return@detectTapGestures
                            }

                            // ===== LÓGICA ORIGEN/DESTINO =====
                            val esOrigenActual = estacionOrigen?.nombre?.trim()
                                ?.equals(tocada.nombre.trim(), ignoreCase = true) == true
                            val esDestinoActual = estacionDestino?.nombre?.trim()
                                ?.equals(tocada.nombre.trim(), ignoreCase = true) == true

                            // 1) tocar una ya seleccionada -> limpiar
                            if (esOrigenActual || esDestinoActual) {
                                Log.d(TAG, "Tocaste estación ya seleccionada (${tocada.nombre}), limpiando selección")
                                estacionOrigen = null
                                estacionDestino = null
                                for (i in visibles.indices) visibles[i] = false
                                onRutaLimpiada?.invoke()

                                // 🔴 también reseteamos el diálogo de estaciones cerradas
                                mostrarDialogoEstacionesCerradas = false
                                estacionesCerradasEnRuta = emptyList()

                                return@detectTapGestures
                            }

                            // 2) no había nada -> esto es el origen
                            if (estacionOrigen == null && estacionDestino == null) {
                                estacionOrigen = tocada
                                Log.d(TAG, "Origen establecido en: ${tocada.nombre}")

                                indicesPorNombre[tocada.nombre.trim().lowercase()]?.forEach { idx ->
                                    visibles[idx] = true
                                    Log.d(TAG, "Marcando visible idx=$idx para ${tocada.nombre}")
                                }
                                return@detectTapGestures
                            }

                            // 3) había sólo origen -> esto es el destino
                            if (estacionOrigen != null && estacionDestino == null) {
                                estacionDestino = tocada
                                Log.d(TAG, "Destino establecido en: ${tocada.nombre}")

                                val origen = estacionOrigen!!
                                val destino = estacionDestino!!

                                // apagar todo
                                for (i in visibles.indices) visibles[i] = false
                                Log.d(TAG, "Visibles limpiados antes de pintar ruta")

                                /* Log.d(TAG, "Calculando ruta de ${origen.nombre} a ${destino.nombre}")
                                 val resultado = computeBestRoute(grafoMetro, origen.nombre, destino.nombre)*/

                                Log.d(TAG, "Calculando ruta de ${origen.nombre} a ${destino.nombre}")
                                val grafo = grafoMetro ?: run {
                                    Log.d(TAG, "Grafo aún no cargado, no se puede calcular ruta")
                                    return@detectTapGestures
                                }

                                val resultado = computeBestRoute(grafo, origen.nombre, destino.nombre)

                                if (resultado == null) {
                                    Log.d(TAG, "No hay ruta entre ${origen.nombre} y ${destino.nombre}")
                                    // limpiamos cualquier diálogo previo de estaciones cerradas
                                    mostrarDialogoEstacionesCerradas = false
                                    estacionesCerradasEnRuta = emptyList()

                                    // NUEVO: mostramos diálogo avisando que no existe ruta
                                    mensajeDialogoSinRuta =
                                        "No hay ruta disponible entre ${origen.nombre} y ${destino.nombre}. " +
                                                "Es posible que algunas estaciones estén cerradas."
                                    mostrarDialogoSinRuta = true

                                } else {
                                    val (info, pasos) = resultado
                                    Log.d(
                                        TAG,
                                        "Ruta encontrada: metros=${info.totalMetros}, transbordos=${info.totalTransbordos}"
                                    )

                                    val cerradasEnRuta = mutableSetOf<String>()

                                    pasos.forEach { paso ->
                                        Log.d(TAG, "Paso: $paso")
                                        val nombreEstacion = when {
                                            paso.startsWith("Transbordo en") ->
                                                paso.removePrefix("Transbordo en").substringBefore("→").trim()
                                            else ->
                                                paso.substringBefore("(").trim()
                                        }

                                        val keyNombre = nombreEstacion.lowercase()

                                        // ¿Está cerrada esta estación según Firestore?
                                        val estaAbierta = estacionesActivas[keyNombre] ?: true
                                        if (!estaAbierta) {
                                            cerradasEnRuta += nombreEstacion
                                        }

                                        /*
                                        indicesPorNombre[nombreEstacion.lowercase()]?.forEach { idx ->
                                            visibles[idx] = true
                                            Log.d(TAG, "Marcando visible en ruta: $nombreEstacion (idx=$idx)")
                                        }*/
                                        indicesPorNombre[keyNombre]?.forEach { idx ->
                                            visibles[idx] = true
                                            Log.d(TAG, "Marcando visible en ruta: $nombreEstacion (idx=$idx)")
                                        }

                                    }

                                    // Si hay estaciones cerradas, guardamos la lista y encendemos el diálogo
                                    if (cerradasEnRuta.isNotEmpty()) {
                                        estacionesCerradasEnRuta = cerradasEnRuta.toList()
                                        mostrarDialogoEstacionesCerradas = true
                                    } else {
                                        estacionesCerradasEnRuta = emptyList()
                                        mostrarDialogoEstacionesCerradas = false
                                    }


                                    // ESTO SIRVE PARA LA PESTAÑA QUE SE GENERA EN LA PANTALLA PRINCIPAL
                                    onRutaCalculada?.invoke(
                                        origen.nombre,
                                        destino.nombre,
                                        info.totalMetros,
                                        pasos
                                    )
                                    //------------------------

                                }
                                return@detectTapGestures
                            }

                            // 4) ya había origen y destino -> shift
                            if (estacionOrigen != null && estacionDestino != null) {
                                val nuevoOrigen = estacionDestino!!
                                val nuevoDestino = tocada
                                Log.d(
                                    TAG,
                                    "Shift de ruta: origen pasa a ${nuevoOrigen.nombre}, nuevo destino ${nuevoDestino.nombre}"
                                )

                                estacionOrigen = nuevoOrigen
                                estacionDestino = nuevoDestino

                                for (i in visibles.indices) visibles[i] = false
                                Log.d(TAG, "Visibles limpiados antes de pintar NUEVA ruta")

                                Log.d(TAG, "Calculando ruta de ${nuevoOrigen.nombre} a ${nuevoDestino.nombre}")

                                val grafoShift = grafoMetro ?: run {
                                    Log.d(TAG, "Grafo aún no cargado, no se puede calcular ruta (shift)")
                                    return@detectTapGestures
                                }

                                val resultado = computeBestRoute(
                                    grafoShift,
                                    nuevoOrigen.nombre,
                                    nuevoDestino.nombre
                                )

                                if (resultado == null) {
                                    Log.d(TAG, "No hay ruta entre ${nuevoOrigen.nombre} y ${nuevoDestino.nombre}")
                                    mostrarDialogoEstacionesCerradas = false
                                    estacionesCerradasEnRuta = emptyList()

                                    // NUEVO: diálogo "no hay ruta"
                                    mensajeDialogoSinRuta =
                                        "No hay ruta disponible entre ${nuevoOrigen.nombre} y ${nuevoDestino.nombre}. " +
                                                "Es posible que algunas estaciones estén cerradas."
                                    mostrarDialogoSinRuta = true

                                } else {
                                    val (info, pasos) = resultado
                                    Log.d(
                                        TAG,
                                        "Ruta encontrada (shift): metros=${info.totalMetros}, transbordos=${info.totalTransbordos}"
                                    )

                                    val cerradasEnRuta = mutableSetOf<String>()

                                    pasos.forEach { paso ->
                                        Log.d(TAG, "Paso: $paso")
                                        val nombreEstacion = when {
                                            paso.startsWith("Transbordo en") ->
                                                paso.removePrefix("Transbordo en").substringBefore("→").trim()
                                            else ->
                                                paso.substringBefore("(").trim()
                                        }

                                        val keyNombre = nombreEstacion.lowercase()

                                        // ¿Está cerrada esta estación según Firestore?
                                        val estaAbierta = estacionesActivas[keyNombre] ?: true
                                        if (!estaAbierta) {
                                            cerradasEnRuta += nombreEstacion
                                        }

                                        indicesPorNombre[keyNombre]?.forEach { idx ->
                                            visibles[idx] = true
                                            Log.d(TAG, "Marcando visible en ruta (shift): $nombreEstacion (idx=$idx)")
                                        }
                                    }

                                    // Si hay estaciones cerradas, guardamos la lista y encendemos el diálogo
                                    if (cerradasEnRuta.isNotEmpty()) {
                                        estacionesCerradasEnRuta = cerradasEnRuta.toList()
                                        mostrarDialogoEstacionesCerradas = true
                                    } else {
                                        estacionesCerradasEnRuta = emptyList()
                                        mostrarDialogoEstacionesCerradas = false
                                    }

                                    onRutaCalculada?.invoke(
                                        nuevoOrigen.nombre,
                                        nuevoDestino.nombre,
                                        info.totalMetros,
                                        pasos
                                    )
                                }
                            }

                        }
                    }
            ) {
                // colores
                val coloresLineas = mapOf(
                    "1" to Color(0xFFE91E63),
                    "2" to Color(0xFF0055A4),
                    "3" to Color(0xFF7B9F57),
                    "4" to Color(0xFF00BCD4),
                    "5" to Color(0xFFFFEB3B),
                    "6" to Color(0xFFF44336),
                    "7" to Color(0xFFFF9800),
                    "8" to Color(0xFF4CAF50),
                    "9" to Color(0xFF795548),
                    "A" to Color(0xFF9C27B0),
                    "B" to Color(0xFF607D8B),
                    "12" to Color(0xFFFFC107)
                )

                val estacionesPorLinea = estaciones.groupBy { it.linea }
                estacionesPorLinea.forEach { (linea, estDeLinea) ->
                    val colorLinea = coloresLineas[linea] ?: Color.Black
                    for (i in 0 until estDeLinea.lastIndex) {
                        val estI = estDeLinea[i]
                        val estJ = estDeLinea[i + 1]
                        val idxI = estaciones.indexOf(estI)
                        val idxJ = estaciones.indexOf(estJ)

                        if (idxI != -1 && idxJ != -1 && visibles[idxI] && visibles[idxJ]) {
                            val pI = Offset(size.width * estI.x, size.height * estI.y)
                            val pJ = Offset(size.width * estJ.x, size.height * estJ.y)
                            drawLine(
                                color = colorLinea,
                                start = pI,
                                end = pJ,
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }

                /*estaciones.forEachIndexed { index, est ->
                    val centro = Offset(size.width * est.x, size.height * est.y)
                    drawCircle(
                        color = if (visibles[index]) NaranjaAppMetroCDMX else Color.LightGray,
                        radius = 2.5.dp.toPx(),
                        center = centro
                    )
                }*/
                estaciones.forEachIndexed { index, est ->
                    val centro = Offset(size.width * est.x, size.height * est.y)

                    val key = est.nombre.trim().lowercase()
                    val estaAbierta = estacionesActivas[key] ?: true  // si no viene de Firestore, asumimos abierta

                    val colorEstacion = when {
                        !estaAbierta -> Color.Red                        // cerrada -> ROJO
                        visibles[index] -> NaranjaAppMetroCDMX           // parte de la ruta -> naranja
                        else -> Color.LightGray                          // resto
                    }

                    drawCircle(
                        color = colorEstacion,
                        radius = 2.5.dp.toPx(),
                        center = centro
                    )
                }


            }
        }
    }

    //INICIO Diálogo que avisa al usuario de estaciones cerradas en la ruta
    if (mostrarDialogoEstacionesCerradas && estacionesCerradasEnRuta.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEstacionesCerradas = false },
            title = { Text("Estaciones cerradas en la ruta") },
            text = {
                Column {
                    Text("La ruta calculada pasa por estaciones que actualmente están cerradas:")
                    Spacer(Modifier.height(8.dp))
                    estacionesCerradasEnRuta.forEach { nombre ->
                        Text("• $nombre", color = Color.Red)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoEstacionesCerradas = false }) {
                    Text("Entendido")
                }
            }
        )
    }
    //FIN Diálogo que avisa al usuario de estaciones cerradas en la ruta

    //INICIO NUEVO: Diálogo cuando no existe ninguna ruta disponible
    if (mostrarDialogoSinRuta) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSinRuta = false },
            title = { Text("Ruta no disponible") },
            text = {
                Text(mensajeDialogoSinRuta)
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogoSinRuta = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
    //FIN NUEVO: Diálogo cuando no existe ninguna ruta disponible

}


