package com.example.hackathon_ai_mobility.mapa.usuario

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.dijkstra.GrafoMetroCompleto
import com.example.hackathon_ai_mobility.dijkstra.cargarGrafoDesdeFirestore
import com.example.hackathon_ai_mobility.modelos.EstacionBD
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetroUsuarioViewModel(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _grafo = MutableStateFlow<GrafoMetroCompleto?>(null)
    val grafo: StateFlow<GrafoMetroCompleto?> = _grafo.asStateFlow()

    // nombreEstacionLowercase -> estáAbierta (true/false)
    private val _estacionesActivas = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val estacionesActivas: StateFlow<Map<String, Boolean>> = _estacionesActivas.asStateFlow()

    private var estacionesListener: ListenerRegistration? = null

    init {
        cargarGrafo()
        observarEstaciones()
    }

    private fun cargarGrafo() {
        viewModelScope.launch {
            try {
                _grafo.value = cargarGrafoDesdeFirestore(db)
            } catch (e: Exception) {
                Log.e("MetroUsuarioVM", "Error al cargar grafo desde Firestore", e)
                _grafo.value = null
            }
        }
    }

    private fun observarEstaciones() {
        estacionesListener?.remove()

        estacionesListener = db.collection("estacionesBD")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MetroUsuarioVM", "Error al escuchar estacionesBD", error)
                    return@addSnapshotListener
                }

                val mapa = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val estacion = doc.toObject(EstacionBD::class.java)
                        val nombre = estacion?.nombre?.trim()
                        if (nombre.isNullOrEmpty()) return@mapNotNull null
                        val abierta = estacion.abierta ?: 1
                        nombre.lowercase() to (abierta == 1)
                    }
                    ?.toMap()
                    ?: emptyMap()

                _estacionesActivas.value = mapa
            }
    }

    override fun onCleared() {
        super.onCleared()
        estacionesListener?.remove()
        estacionesListener = null
    }
}
