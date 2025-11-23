package com.example.hackathon_ai_mobility.dijkstra


import com.example.hackathon_ai_mobility.dijkstra.GrafoMetroCompleto
import com.example.hackathon_ai_mobility.modelos.TramoBD
import com.example.hackathon_ai_mobility.modelos.EstacionBD
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun cargarGrafoDesdeFirestore(
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
): GrafoMetroCompleto {
    // 1) Bajamos todos los tramos
    val tramos = db.collection("tramosBD").get().await()
        .documents.mapNotNull { it.toObject(TramoBD::class.java) }

    // 2) (Opcional) Bajamos estaciones (para estados/transbordos)
    val estacionesByNombre = db.collection("estacionesBD").get().await()
        .documents.mapNotNull { it.toObject(EstacionBD::class.java) }
        .associateBy { it.nombre?.trim()?.lowercase().orEmpty() }

    // 3) Construimos el grafo como hace 'desdeTexto()'
    //    pero a partir de tramos
    /*val grafo = GrafoMetroCompleto.fromText(buildString {
        // Simulamos el formato "Línea|A - B|metros|estado"
        tramos.forEach { t ->
            val linea = t.linea?.trim().orEmpty()
            val origen = t.origen?.trim().orEmpty()
            val destino = t.destino?.trim().orEmpty()
            val metros = t.metros ?: 0
            val estado = t.estado ?: 1
            if (linea.isNotEmpty() && origen.isNotEmpty() && destino.isNotEmpty()) {
                appendLine("$linea|$origen - $destino|$metros|$estado")
            }
        }
    })*/

    // 3) Construimos el grafo solo con tramos cuyas estaciones estén abiertas
    // (si el admin pone abierta = 0 en una estación, ningún tramo que la use entra al grafo, y Dijkstra nunca podrá pasar por ahí.)
    val grafo = GrafoMetroCompleto.fromText(buildString {
        tramos.forEach { t ->
            val linea   = t.linea?.trim().orEmpty()
            val origen  = t.origen?.trim().orEmpty()
            val destino = t.destino?.trim().orEmpty()
            val metros  = t.metros ?: 0
            val estadoTramo = t.estado ?: 1   // por si algún día quieres cerrar solo un tramo

            if (linea.isEmpty() || origen.isEmpty() || destino.isEmpty()) return@forEach

            val origenKey  = origen.lowercase()
            val destinoKey = destino.lowercase()

            val origenAbierta  = (estacionesByNombre[origenKey]?.abierta ?: 1) == 1
            val destinoAbierta = (estacionesByNombre[destinoKey]?.abierta ?: 1) == 1

            // Solo añadimos el tramo si:
            // - el tramo está activo (estadoTramo == 1)
            // - y ambas estaciones están abiertas
            if (estadoTramo == 1 && origenAbierta && destinoAbierta) {
                appendLine("$linea|$origen - $destino|$metros|$estadoTramo")
            }
        }
    })

    // Si quieres usar 'abierta' de estaciones para filtrar nodos/edges,
    // aquí podrías quitar conexiones de estaciones cerradas (abierta==0).
    // Por simplicidad, dejamos todo y dejamos 'estado' para UI.

    return grafo
}
