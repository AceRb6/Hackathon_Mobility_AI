package com.example.hackathon_ai_mobility.tecnico

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ModeloDeVistaPantallaTecnico : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore

    // Lista de TODOS los reportes del sistema (se filtra en la UI)
    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        escucharReportesEnTiempoReal()
    }

    /**
     * Escucha en tiempo real la colección "reportesBD" y adjunta el idDocumento
     * a cada ModeloReportesBD.
     */
    private fun escucharReportesEnTiempoReal() {
        db.collection("reportesBD")
            .orderBy("fechaHoraCreacionReporte", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("TecnicoVM", "Error escuchando reportes", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.documents.mapNotNull { document ->
                        val data = document.toObject(ModeloReportesBD::class.java)
                        if (data != null) {
                            ModeloReportesBD(
                                idDocumento = document.id,
                                nombreDeJefeDeEstacionCreadorReporte = data.nombreDeJefeDeEstacionCreadorReporte,
                                fechaHoraCreacionReporte = data.fechaHoraCreacionReporte,
                                tituloReporte = data.tituloReporte,
                                estacionQueTieneReporte = data.estacionQueTieneReporte,
                                descripcionReporteJefeDeEstacion = data.descripcionReporteJefeDeEstacion,
                                tipoProblema = data.tipoProblema,
                                horaProblema = data.horaProblema,
                                reporteTecnicoRegulador = data.reporteTecnicoRegulador,
                                reporteCompletado = data.reporteCompletado
                            )
                        } else {
                            null
                        }
                    }

                    _listaReportesSistema.value = lista
                }
            }
    }

    /**
     * El técnico marca que el problema ya se solucionó.
     * Actualiza el campo reporteCompletado a 1 en Firestore.
     */
    fun marcarReporteComoSolucionado(
        idDocumento: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (idDocumento.isBlank()) {
                    onError("ID de documento vacío")
                    return@launch
                }

                db.collection("reportesBD")
                    .document(idDocumento)
                    .update("reporteCompletado", 1)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                Log.e("TecnicoVM", "Error al marcar como solucionado", e)
                onError(e.message ?: "Error desconocido al marcar como solucionado")
            }
        }
    }


}
