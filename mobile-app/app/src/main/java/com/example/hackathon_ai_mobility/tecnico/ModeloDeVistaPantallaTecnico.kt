package com.example.hackathon_ai_mobility.tecnico

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class ModeloDeVistaPantallaTecnico : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Estación base del técnico (Ej: "Zaragoza")
    private val _tecnicoDependencia = MutableStateFlow<String?>("Zaragoza") // Valor default por seguridad
    val tecnicoDependencia: StateFlow<String?> = _tecnicoDependencia.asStateFlow()

    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        fetchTechnicianDataAndListenForReports()
    }

    private fun fetchTechnicianDataAndListenForReports() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                try {
                    // Consultamos la dependencia del técnico en Firestore
                    val doc = db.collection("usuariosBD").document(user.uid).get().await()
                    val dependencia = doc.getString("dependencia")?.replaceFirstChar { it.uppercase() }

                    if (dependencia != null) {
                        _tecnicoDependencia.value = dependencia
                    }
                } catch (e: Exception) {
                    Log.e("TecnicoVM", "Error obteniendo usuario: ${e.message}")
                }
            }
            // Iniciamos la escucha de reportes reales
            listenForAssignedReports()
        }
    }

    private fun listenForAssignedReports() {
        // Escucha SOLO reportes en Estado 1 (Enviados por el Regulador)
        db.collection("reportesBD")
            .whereEqualTo("reporteCompletado", 1)
            .orderBy("fechaHoraCreacionReporte", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("TecnicoVM", "Error escuchando reportes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        val r = doc.toObject(ModeloReportesBD::class.java)
                        // Inyectamos el ID del documento para poder actualizarlo luego
                        r?.copy(idDocumento = doc.id)
                    }
                    _listaReportesSistema.value = lista
                }
            }
    }

    // Simulación de cálculo de tiempo para la UI
    fun getBestRouteTime(origen: String, destino: String): Pair<Int, String> {
        val random = Random(System.currentTimeMillis().toInt())
        val tiempoBaseMin = when {
            origen.contains(destino, ignoreCase = true) -> 5
            else -> random.nextInt(15, 45)
        }
        val tiempoOptimo = tiempoBaseMin + random.nextInt(0, 10)
        return Pair(tiempoOptimo, "Metro/Bus, Vía rápida")
    }

    // Acción de Completar: Pasa el reporte a Estado 2
    fun marcarReporteComoSolucionado(
        idDocumento: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                db.collection("reportesBD")
                    .document(idDocumento)
                    .update("reporteCompletado", 2)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al actualizar")
            }
        }
    }
}

