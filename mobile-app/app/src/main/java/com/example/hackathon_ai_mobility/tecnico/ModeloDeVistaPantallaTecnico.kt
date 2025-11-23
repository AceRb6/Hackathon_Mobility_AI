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
import com.example.hackathon_ai_mobility.modelos.ModeloUsuarioBD // Asumimos esta clase existe

class ModeloDeVistaPantallaTecnico : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Almacena la estación asignada del técnico logueado (Ej: "zaragoza")
    private val _tecnicoDependencia = MutableStateFlow<String?>(null)
    val tecnicoDependencia: StateFlow<String?> = _tecnicoDependencia.asStateFlow()

    // Lista de reportes filtrada para la estación del técnico
    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        fetchTechnicianDataAndListenForReports()
    }

    /**
     * 1. Obtiene el UID del usuario logueado.
     * 2. Obtiene el campo 'dependencia' de 'usuariosBD'.
     * 3. Inicia el listener de reportes filtrado por esa dependencia y Estado 1.
     */
    private fun fetchTechnicianDataAndListenForReports() {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            val uid = user.uid

            // Intenta obtener la dependencia del técnico desde usuariosBD
            val usuarioDoc = db.collection("usuariosBD").document(uid).get().await()
            val dependencia = usuarioDoc.getString("dependencia")?.lowercase()

            _tecnicoDependencia.value = dependencia // Almacenamos la dependencia

            if (dependencia != null) {
                Log.d("TecnicoVM", "Filtro activo para dependencia: $dependencia")
                listenForAssignedReports(dependencia)
            } else {
                Log.w("TecnicoVM", "Dependencia no encontrada para UID $uid. Mostrando reportes sin filtrar.")
                listenForAssignedReports(null) // Fallback
            }
        }
    }

    private fun listenForAssignedReports(dependencia: String?) {
        /*var query = db.collection("reportesBD")
            .whereEqualTo("reporteCompletado", 1) // Solo reportes Asignados/En proceso (Estado 1)
            .orderBy("fechaHoraCreacionReporte", Query.Direction.DESCENDING)*/
        var query = db.collection("reportesBD")
            .whereEqualTo("reporteCompletado", 1)
            .orderBy("fechaHoraCreacionReporte", Query.Direction.DESCENDING)

        // FILTRADO POR ESTACIÓN ASIGNADA
        if (dependencia != null) {
            query = query.whereEqualTo("estacionQueTieneReporte", dependencia.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }) // Asume que la estación está en mayúsculas
        }

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("TecnicoVM", "Error escuchando reportes: ${e.message}", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val lista = snapshot.documents.mapNotNull { document ->
                    val data = document.toObject(ModeloReportesBD::class.java)
                    // Inyectamos el ID del documento
                    data?.copy(idDocumento = document.id)
                }
                _listaReportesSistema.value = lista
            }
        }
    }

    /**
     * SIMULACIÓN: Calcula la mejor ruta y tiempo entre dos estaciones.
     * En producción, esta lógica consultaría Google Maps API.
     */
    fun getBestRouteTime(origen: String, destino: String): Pair<Int, String> {
        // Simulación básica de distancias y tiempo:
        val tiempoBaseMin = when {
            origen.contains(destino, ignoreCase = true) -> 5 // Ya está en la estación
            origen == "Zaragoza" && destino == "Chapultepec" -> 35
            else -> Random.nextInt(20, 50) // Tiempo base de 20-50 minutos
        }

        // Simulación del cálculo API
        val tiempoOptimo = tiempoBaseMin + Random.nextInt(0, 10)

        return Pair(tiempoOptimo, "Bus/Metro, Vía rápida")
    }

    /**
     * El técnico marca que el problema ya se solucionó. Actualiza el campo reporteCompletado a 2.
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
                    .update("reporteCompletado", 2) // Estado 2 = Completado
                    .await()

                onSuccess()
            } catch (e: Exception) {
                Log.e("TecnicoVM", "Error al marcar como solucionado", e)
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}