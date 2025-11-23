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

    // Flujo de datos para mostrar la lista de reportes en la pantalla del Regulador
    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        escucharReportesEnTiempoReal()
    }

    /**
     * Escucha en tiempo real la colección "reportes_sistema".
     * Muestra el historial completo ordenado por fecha descendente.
     */
    private fun escucharReportesEnTiempoReal() {
        db.collection("reportes_sistema")
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

    /**
     * Actualiza el reporte existente.
     * Concatena la descripción técnica con el equipo necesario.
     * Actualiza el tipo de problema confirmado.
     * Cierra el reporte (estado 1).
     */
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
                    onError("El ID del documento o la descripción técnica están vacíos.")
                    return@launch
                }

                // Concatenación de datos para el campo de texto único en BD
                val reporteFinalString = "Reporte: $descripcionTecnica | Equipo: $equipoLlevar"

                val actualizaciones = mapOf(
                    "reporteTecnicoRegulador" to reporteFinalString,
                    "tipoProblema" to tipoProblemaConfirmado,
                    "reporteCompletado" to 1 // 1 = Completado/Cerrado
                )

                db.collection("reportes_sistema")
                    .document(idDocumento)
                    .update(actualizaciones)
                    .await()

                onSuccess()

            } catch (e: Exception) {
                Log.e("ReguladorVM", "Error actualizando reporte técnico", e)
                onError(e.message ?: "Error desconocido al actualizar")
            }
        }
    }
}