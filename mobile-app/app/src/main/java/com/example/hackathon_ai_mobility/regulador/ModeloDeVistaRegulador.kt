package com.example.hackathon_ai_mobility.regulador

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ModeloDeVistaRegulador : ViewModel() {

    private val db = Firebase.firestore

    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        escucharReportesEnTiempoReal()
    }

    private fun escucharReportesEnTiempoReal() {
        // Escuchamos la colección 'reportesBD'
        db.collection("reportesBD")
            .orderBy("fechaHoraCreacionReporte", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ReguladorVM", "Error al escuchar: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Mapeo manual para inyectar el ID del documento
                    val listaConIDs = snapshot.documents.mapNotNull { doc ->
                        val reporte = doc.toObject(ModeloReportesBD::class.java)
                        // Inyectamos doc.id, CRÍTICO para la función de actualización
                        reporte?.copy(idDocumento = doc.id)
                    }
                    _listaReportesSistema.value = listaConIDs
                }
            }
    }

    // --- FUNCIÓN REQUERIDA POR LA PANTALLA ---
    fun enviarReporteATecnico(
        idDocumento: String,
        descripcionTecnica: String,
        equipoLlevar: String,
        tipoProblemaConfirmado: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (idDocumento.isBlank()) {
                    onError("Error interno: ID de reporte no encontrado.")
                    return@launch
                }

                val reporteFinalString = "Instrucción: $descripcionTecnica | Equipo: $equipoLlevar"

                val actualizaciones = mapOf(
                    "reporteTecnicoRegulador" to reporteFinalString,
                    "tipoProblema" to tipoProblemaConfirmado,
                    "reporteCompletado" to 1
                )

                db.collection("reportesBD")
                    .document(idDocumento)
                    .update(actualizaciones)
                    .await()

                onSuccess()

            } catch (e: Exception) {
                Log.e("ReguladorVM", "Error al actualizar", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}