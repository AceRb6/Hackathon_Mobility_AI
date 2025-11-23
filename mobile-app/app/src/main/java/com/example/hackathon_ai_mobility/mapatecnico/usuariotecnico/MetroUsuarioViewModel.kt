package com.example.hackathon_ai_mobility.mapatecnico.usuariotecnico

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.dijkstra.GrafoMetroCompleto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.hackathon_ai_mobility.modelos.EstacionBD
import com.example.hackaton_ai_mobility.dijkstra.cargarGrafoDesdeFirestore


class MetroUsuarioViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _grafo = MutableStateFlow<GrafoMetroCompleto?>(null)
    val grafo: StateFlow<GrafoMetroCompleto?> = _grafo

    private var listener: ListenerRegistration? = null

    // nombre estación (lowercase) -> true = abierta, false = cerrada
    private val _estacionesActivas =
        MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val estacionesActivas: StateFlow<Map<String, Boolean>> = _estacionesActivas

    init {
        escucharCambios()
    }

    private fun escucharCambios() {
        // Por si acaso había otro listener
        listener?.remove()

        // Escuchar estacionesBD (que es donde actualiza el admin) :contentReference[oaicite:8]{index=8}
        /*listener = db.collection("estacionesBD")
            .addSnapshotListener { _, error ->
                if (error != null) {
                    Log.e("MetroUsuarioViewModel", "Error escuchando estaciones", error)
                    return@addSnapshotListener
                }

                // Reconstuir el grafo con el estado más reciente
                viewModelScope.launch {
                    _grafo.value = cargarGrafoDesdeFirestore(db)
                }
            }*/

        listener = db.collection("estacionesBD")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MetroUsuarioViewModel", "Error escuchando estaciones", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val nuevoMapa = mutableMapOf<String, Boolean>()

                    snapshot.documents.forEach { doc ->
                        val estacion = doc.toObject(EstacionBD::class.java)
                        val nombreKey = estacion?.nombre?.trim()?.lowercase()
                        val abiertaInt = estacion?.abierta ?: 1   // 1 = abierta, 0 = cerrada
                        val estaAbierta = abiertaInt == 1

                        if (nombreKey != null) {
                            nuevoMapa[nombreKey] = estaAbierta
                        }
                    }

                    _estacionesActivas.value = nuevoMapa
                }

                // Reconstruir el grafo con el estado más reciente
                viewModelScope.launch {
                    _grafo.value = cargarGrafoDesdeFirestore(db)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        listener = null
    }
}
