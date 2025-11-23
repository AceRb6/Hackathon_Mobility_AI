package com.example.hackathon_ai_mobility.dijkstra


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class MetroAdminViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    fun importarTramosDesdeAssets(context: Context, nombreAsset: String = "tramos_metro.txt") {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val texto = context.assets.open(nombreAsset).bufferedReader().use { it.readText() }
                    val lineas = texto.lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && !it.startsWith("#") }
                        .toList()

                    // Primera línea es encabezado -> saltamos si detectamos "Línea|..."
                    val contenido = if (lineas.firstOrNull()?.contains("|") == true &&
                        lineas.first().lowercase(Locale.ROOT).contains("línea")
                    ) lineas.drop(1) else lineas

                    val batch: WriteBatch = db.batch()
                    val estacionesPorNombre = mutableMapOf<String, MutableSet<String>>() // para poblar 'estaciones'

                    contenido.forEach { fila ->
                        val partes = fila.split("|")
                        if (partes.size < 4) return@forEach

                        val linea = partes[0].trim()
                        val inter = partes[1].trim() // "A - B"
                        val metros = partes[2].trim().toIntOrNull() ?: 0
                        val estado = partes[3].trim().toIntOrNull() ?: 1

                        val ests = inter.split("-")
                        if (ests.size != 2) return@forEach
                        val origen = ests[0].trim()
                        val destino = ests[1].trim()

                        // DocID estable (útil para idempotencia)
                        /*val docId = "${linea}_${origen}_${destino}"
                            .replace(" ", "_")
                            .replace("/", "_")

                        val ref = db.collection("tramos").document(docId)*/
                        val docId = sanitizeId("${linea}_${origen}_${destino}")
                        val ref = db.collection("tramosBD").document(docId)

                        val data = mapOf(
                            "linea" to linea,
                            "origen" to origen,
                            "destino" to destino,
                            "metros" to metros,
                            "estado" to estado
                        )
                        batch.set(ref, data)

                        // Para la colección estaciones
                        estacionesPorNombre.getOrPut(origen.lowercase()) { mutableSetOf() }.add(linea)
                        estacionesPorNombre.getOrPut(destino.lowercase()) { mutableSetOf() }.add(linea)
                    }

                    // Opcional: materializamos 'estaciones'
                    estacionesPorNombre.forEach { (clave, setLineas) ->
                        val nombre = clave.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                        /*val docId = nombre.replace(" ", "_")
                        val ref = db.collection("estaciones").document(docId)*/
                        val docId = sanitizeId(nombre)
                        val ref = db.collection("estacionesBD").document(docId)
                        val data = mapOf(
                            "nombre" to nombre,
                            "lineas" to setLineas.sorted(),
                            // Heurística: consideramos abierta si aparece en algún tramo estado=1.
                            // (Podrías subir estados por estación si los tienes explícitos)
                            "abierta" to 1
                        )
                        batch.set(ref, data)
                    }

                    batch.commit().await()
                }
            }.onSuccess { Log.i("Metro", "Migración de tramos/estaciones completada") }
                .onFailure { Log.e("Metro", "Error migrando: ${it.message}", it) }
        }
    }

    private fun sanitizeId(raw: String): String =
        raw.replace(" ", "_")
            .replace("/", "_")
            .replace(".", "_")
            .replace("#", "_")
            .replace("[", "_")
            .replace("]", "_")

}