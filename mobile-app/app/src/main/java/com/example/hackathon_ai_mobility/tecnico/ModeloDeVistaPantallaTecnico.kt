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
import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class ModeloDeVistaPantallaTecnico : ViewModel() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 1. Almacena la estación asignada del técnico logueado
    private val _tecnicoDependencia = MutableStateFlow<String?>(null)
    val tecnicoDependencia: StateFlow<String?> = _tecnicoDependencia.asStateFlow()

    // 2. Lista de reportes completa filtrada para la estación del técnico
    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    // 3. NUEVO: Variable para manejar la alerta de nuevos reportes (Corrección del error)
    private val _nuevosReportes = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val nuevosReportes: StateFlow<List<ModeloReportesBD>> = _nuevosReportes.asStateFlow()

    init {
        fetchTechnicianDataAndListenForReports()
    }

    /**
     * 1. Obtiene el UID del usuario logueado.
     * 2. Obtiene el campo 'dependencia' de 'usuariosBD'.
     * 3. Inicia el listener de reportes general y el de nuevos reportes.
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
                // CORRECCIÓN: También escuchar alertas si tenemos dependencia
                escucharNuevosReportes(dependencia)
            } else {
                Log.w("TecnicoVM", "Dependencia no encontrada para UID $uid. Mostrando reportes sin filtrar.")
                listenForAssignedReports(null) // Fallback
                escucharNuevosReportes(null)
            }
        }
    }

    private fun listenForAssignedReports(dependencia: String?) {
        var query = db.collection("reportesBD")
            .whereEqualTo("reporteCompletado", 1) // Solo reportes Asignados/En proceso (Estado 1)
            .orderBy("fechaHoraCreacionReporte", Query.Direction.DESCENDING)

        // FILTRADO POR ESTACIÓN ASIGNADA
        if (dependencia != null) {
            // Nota: capitalize() está obsoleto en versiones nuevas de Kotlin,
            // pero lo dejo como lo tenías. Si te da warning usa: .replaceFirstChar { it.uppercase() }
            query = query.whereEqualTo("estacionQueTieneReporte", dependencia.capitalize())
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

    // Listener para nuevo reporte de parte de regulador (Alertas)
    private fun escucharNuevosReportes(dependencia: String?) {
        var query = db.collection("reportesBD")
            .whereEqualTo("reporteCompletado", 1) // Estado 1 = asignado por regulador
            .orderBy("fechaHoraCreacionReporte", Query.Direction.DESCENDING)

        if (dependencia != null) {
            query = query.whereEqualTo("estacionQueTieneReporte", dependencia.capitalize())
        }

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("TecnicoVM", "Error escuchando nuevos reportes: ${e.message}", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Filtramos solo los cambios de tipo ADDED (Agregados)
                val nuevos = snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .mapNotNull { doc ->
                        val data = doc.document.toObject(ModeloReportesBD::class.java)
                        data?.copy(idDocumento = doc.document.id)
                    }

                if (nuevos.isNotEmpty()) {
                    Log.d("TecnicoVM", "Nuevos reportes asignados: ${nuevos.size}")
                    // Aquí ya no dará error porque la variable _nuevosReportes existe
                    _nuevosReportes.value = nuevos
                }
            }
        }
    }

    /**
     * SIMULACIÓN: Calcula la mejor ruta y tiempo entre dos estaciones.
     */
    fun getBestRouteTime(origen: String, destino: String): Pair<Int, String> {
        // Simulación básica de distancias y tiempo:
        val tiempoBaseMin = when {
            origen.contains(destino, ignoreCase = true) -> 5
            origen == "Zaragoza" && destino == "Chapultepec" -> 35
            else -> Random.nextInt(20, 50)
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