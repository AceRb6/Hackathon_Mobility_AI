package com.example.hackathon_ai_mobility.regulador

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.EstacionBD
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ModeloDeVistaRegulador : ViewModel() {

    private val db = Firebase.firestore

    // Estado para la lista de estaciones (para el Dropdown)
    private val _listaEstaciones = MutableStateFlow<List<EstacionBD>>(emptyList())
    val listaEstaciones: StateFlow<List<EstacionBD>> = _listaEstaciones.asStateFlow()

    init {
        obtenerEstaciones()
    }

    // Descarga las estaciones de la colección existente "estacionesBD"
    private fun obtenerEstaciones() {
        viewModelScope.launch {
            try {
                val result = db.collection("estacionesBD").get().await()
                val estaciones = result.toObjects(EstacionBD::class.java)
                _listaEstaciones.value = estaciones
            } catch (e: Exception) {
                Log.e("ReguladorVM", "Error al obtener estaciones", e)
            }
        }
    }

    // Función para enviar el reporte con las variables solicitadas
    fun enviarReporteRegulador(
        titulo: String,
        horaIni: String,
        descripcion: String,
        estacion: String,
        equipo: String,
        tipo: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (titulo.isBlank() || horaIni.isBlank() || descripcion.isBlank() || estacion.isBlank()) {
                onError("Faltan campos obligatorios")
                return@launch
            }

            val reporteData = hashMapOf(
                "Titulo" to titulo,
                "Hora_ini" to horaIni,
                "Descripcion" to descripcion,
                "Estacion" to estacion,
                "Equipo" to equipo,
                "Tipo" to tipo,
                "fecha_creacion" to FieldValue.serverTimestamp()
            )

            try {
                // Guarda en la colección "reportes_regulador" (ajusta el nombre si es necesario)
                db.collection("reportes_regulador")
                    .add(reporteData)
                    .await()
                onSuccess()
            } catch (e: Exception) {
                Log.e("ReguladorVM", "Error enviando reporte", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}