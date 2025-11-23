package com.example.hackathon_ai_mobility.tecnico

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    // Dependencia por defecto para pruebas
    private val _tecnicoDependencia = MutableStateFlow<String?>("Zaragoza")
    val tecnicoDependencia: StateFlow<String?> = _tecnicoDependencia.asStateFlow()

    private val _listaReportesSistema = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesSistema: StateFlow<List<ModeloReportesBD>> = _listaReportesSistema.asStateFlow()

    init {
        // EN LUGAR DE ESCUCHAR FIREBASE, CARGAMOS DATOS DE PRUEBA
        cargarDatosDePrueba()

        // fetchTechnicianDataAndListenForReports() // <-- Descomentar esto cuando quieras volver a usar Firebase real
    }

    private fun cargarDatosDePrueba() {
        val listaMock = listOf(
            ModeloReportesBD(
                idDocumento = "mock_1",
                tituloReporte = "Falla en Sistema de Frenado",
                estacionQueTieneReporte = "Pantitlán",
                descripcionReporteJefeDeEstacion = "El tren no responde al frenado regenerativo en la vía 2.",
                tipoProblema = 3, // Crítico (Rojo)
                horaProblema = "08:30",
                reporteTecnicoRegulador = "Instrucción: Revisar zapatas y sistema neumático de urgencia | Equipo: Kit de frenos, llaves de presión y gato hidráulico",
                reporteCompletado = 1 // Estado 1: Asignado a técnico
            ),
            ModeloReportesBD(
                idDocumento = "mock_2",
                tituloReporte = "Cortocircuito en Vías",
                estacionQueTieneReporte = "Tacubaya",
                descripcionReporteJefeDeEstacion = "Se observa humo en la zona de vías dirección Observatorio.",
                tipoProblema = 3, // Crítico (Rojo)
                horaProblema = "09:15",
                reporteTecnicoRegulador = "Instrucción: Cortar corriente y verificar aislantes | Equipo: Equipo de protección eléctrica, medidor de voltaje",
                reporteCompletado = 1
            ),
            ModeloReportesBD(
                idDocumento = "mock_3",
                tituloReporte = "Escalera eléctrica detenida",
                estacionQueTieneReporte = "Mixcoac",
                descripcionReporteJefeDeEstacion = "La escalera 4 se detuvo bruscamente con usuarios.",
                tipoProblema = 2, // Medio (Amarillo)
                horaProblema = "10:00",
                reporteTecnicoRegulador = "Instrucción: Reiniciar sistema y verificar sensores de peso | Equipo: Llaves maestras y laptop de diagnóstico",
                reporteCompletado = 1
            ),
            ModeloReportesBD(
                idDocumento = "mock_4",
                tituloReporte = "Luminaria fundida en andén",
                estacionQueTieneReporte = "Zaragoza",
                descripcionReporteJefeDeEstacion = "Poca visibilidad en la zona de mujeres.",
                tipoProblema = 1, // Leve (Verde)
                horaProblema = "11:45",
                reporteTecnicoRegulador = "Instrucción: Reemplazo de tubos LED | Equipo: Escalera de tijera y repuestos LED",
                reporteCompletado = 1
            )
        )
        _listaReportesSistema.value = listaMock
    }

    // --- LÓGICA DE RUTA Y MAPA (Se mantiene funcional) ---
    fun getBestRouteTime(origen: String, destino: String): Pair<Int, String> {
        val random = Random(System.currentTimeMillis().toInt())
        val tiempoBaseMin = when {
            origen.contains(destino, ignoreCase = true) -> 5
            else -> random.nextInt(15, 45)
        }
        val tiempoOptimo = tiempoBaseMin + random.nextInt(0, 10)
        return Pair(tiempoOptimo, "Metro/Bus, Vía rápida")
    }

    // --- LÓGICA DE COMPLETADO (Solo simulación si no hay conexión) ---
    fun marcarReporteComoSolucionado(
        idDocumento: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Si es un mock, simulamos el éxito localmente quitándolo de la lista
                if (idDocumento.startsWith("mock")) {
                    val actual = _listaReportesSistema.value.toMutableList()
                    actual.removeAll { it.idDocumento == idDocumento }
                    _listaReportesSistema.value = actual
                    onSuccess()
                } else {
                    // Si fuera real, usaríamos Firebase
                    db.collection("reportesBD")
                        .document(idDocumento)
                        .update("reporteCompletado", 2)
                        .await()
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("TecnicoVM", "Error simulado", e)
                onError("Error al actualizar")
            }
        }
    }
}