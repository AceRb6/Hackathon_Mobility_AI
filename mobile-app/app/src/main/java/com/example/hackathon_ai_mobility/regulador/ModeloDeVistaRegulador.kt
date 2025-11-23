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
        Log.d("ReguladorVM", "Iniciando listener de reportesBD…")

        db.collection("reportesBD")
            .orderBy("fechaHoraCreacionReporte", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ReguladorVM", "Error al escuchar: ${e.message}", e)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.e("ReguladorVM", "Snapshot de reportesBD es NULL")
                    return@addSnapshotListener
                }

                Log.d("ReguladorVM", "Snapshot reportesBD size=${snapshot.size()}")

                val listaConIDs = snapshot.documents.mapNotNull { doc ->
                    val rawData = doc.data
                    Log.d("ReguladorVM", "docId=${doc.id} -> $rawData")

                    val reporte = doc.toObject(ModeloReportesBD::class.java)
                    reporte?.copy(idDocumento = doc.id)
                }

                Log.d("ReguladorVM", "Lista mapeada para Regulador size=${listaConIDs.size}")
                _listaReportesSistema.value = listaConIDs
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
                Log.d(
                    "ReguladorVM",
                    "enviarReporteATecnico() id=$idDocumento desc='$descripcionTecnica' equipo='$equipoLlevar' tipo=$tipoProblemaConfirmado"
                )

                if (idDocumento.isBlank()) {
                    val msg = "Error interno: ID de reporte no encontrado."
                    Log.e("ReguladorVM", msg)
                    onError(msg)
                    return@launch
                }

                val reporteFinalString = "Instrucción: $descripcionTecnica | Equipo: $equipoLlevar"

                val actualizaciones = mapOf(
                    "reporteTecnicoRegulador" to reporteFinalString,
                    "tipoProblema" to tipoProblemaConfirmado,
                    "reporteCompletado" to 1
                )

                Log.d("ReguladorVM", "Actualizando docId=$idDocumento con $actualizaciones")

                db.collection("reportesBD")
                    .document(idDocumento)
                    .update(actualizaciones)
                    .await()

                Log.d("ReguladorVM", "enviarReporteATecnico() OK para docId=$idDocumento")
                onSuccess()

            } catch (e: Exception) {
                Log.e("ReguladorVM", "Error al actualizar", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }

}