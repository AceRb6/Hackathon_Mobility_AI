package com.example.hackathon_ai_mobility.regulador

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ModeloDeVistaRegulador : ViewModel() {

    private val db = Firebase.firestore

    // Flujo de datos para mostrar la lista de reportes
    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        escucharReportesEnTiempoReal()
    }

    /**
     * Escucha reportes de la colección CORRECTA "reportesBD"
     */
    private fun escucharReportesEnTiempoReal() {
        // ESTO ESTÁ CORRECTO EN TU CÓDIGO (BD)
        db.collection("reportesBD")
            .orderBy("fechaHoraCreacionReporte", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ReguladorVM", "Error escuchando reportes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val reportes = snapshot.toObjects<ModeloReportesBD>()
                    _listaReportesSistema.value = reportes
                }
            }
    }

    fun completarReporteTecnico(
        idDocumento: String,
        descripcionTecnica: String,
        equipoLlevar: String,
        tipoProblemaConfirmado: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (idDocumento.isBlank() || descripcionTecnica.isBlank()) {
                    onError("Faltan datos obligatorios (ID o Descripción).")
                    return@launch
                }

                val reporteFinalString = "Reporte: $descripcionTecnica | Equipo: $equipoLlevar"

                val actualizaciones = mapOf(
                    "reporteTecnicoRegulador" to reporteFinalString,
                    "tipoProblema" to tipoProblemaConfirmado,
                    "reporteCompletado" to 1 // Pasa a estado completado/asignado a técnico
                )

                // --- CORRECCIÓN CRÍTICA AQUÍ ---
                // Antes tenías "reportesDB", debe ser "reportesBD" para coincidir con tu Firebase
                db.collection("reportesBD")
                    .document(idDocumento)
                    .update(actualizaciones)
                    .await()

                onSuccess()

            } catch (e: Exception) {
                Log.e("ReguladorVM", "Error actualizando reporte", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}