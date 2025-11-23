package com.example.hackathon_ai_mobility.tecnico

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.example.hackathon_ai_mobility.modelos.ModeloUsuarioBD
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

    // Estación (dependencia) asignada al técnico logueado (ej. "zaragoza")
    private val _tecnicoDependencia = MutableStateFlow<String?>(null)
    val tecnicoDependencia: StateFlow<String?> = _tecnicoDependencia.asStateFlow()

    // Lista de reportes del sistema
    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        cargarDependenciaTecnico()
        cargarReportesSistema()
    }

    /**
     * Obtiene la dependencia del técnico (campo "dependencia" de usuariosBD/uid)
     */
    private fun cargarDependenciaTecnico() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user == null) {
                    Log.w(TAG, "No hay usuario autenticado")
                    _tecnicoDependencia.value = null
                    return@launch
                }

                val uid = user.uid

                val usuarioDoc = db.collection("usuariosBD")
                    .document(uid)
                    .get()
                    .await()

                val dependencia = usuarioDoc.getString("dependencia")
                    ?.trim()

                _tecnicoDependencia.value = dependencia
                Log.d(TAG, "Dependencia del técnico: $dependencia")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar dependencia del técnico", e)
                _tecnicoDependencia.value = null
            }
        }
    }

    /**
     * Carga los reportes desde la colección reportesBD.
     * Inyecta el idDocumento real de Firebase en cada reporte.
     */
    fun cargarReportesSistema() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("reportesBD")
                    .orderBy("fechaHoraCreacionReporte", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val lista = snapshot.documents.mapNotNull { document ->
                    val data = document.toObject(ModeloReportesBD::class.java)
                    data?.copy(idDocumento = document.id)
                }

                _listaReportesSistema.value = lista
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar reportes del sistema", e)
            }
        }
    }

    /**
     * SIMULACIÓN: Calcula tiempo de ruta.
     * (Si luego conectas con Google Maps o tu grafo, remplazas esto).
     */
    fun getBestRouteTime(origen: String, destino: String): Pair<Int, String> {
        // Simulación muy básica: tiempo base + número aleatorio
        val tiempoBaseMin = 20
        val tiempoOptimo = tiempoBaseMin + Random.nextInt(0, 15)
        return tiempoOptimo to "Metro / Ruta simulada"
    }

    /**
     * Marca un reporte como solucionado (reporteCompletado = 2)
     */
    fun marcarReporteComoSolucionado(
        idDocumento: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (idDocumento.isBlank()) {
                    onError("Id del documento vacío")
                    return@launch
                }

                db.collection("reportesBD")
                    .document(idDocumento)
                    .update("reporteCompletado", 2)
                    .await()

                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error al marcar como solucionado", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }

    companion object {
        private const val TAG = "TecnicoVM"
    }
}
