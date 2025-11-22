package com.example.hackathon_ai_mobility.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.presentation.modelos.EstacionBD
import com.example.hackathon_ai_mobility.presentation.modelos.ModeloReportesBD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la lógica de la pantalla de reportes.
 * Gestiona la lista de estaciones y el envío de reportes.
 */
class ModeloDeVistaPantallaReportesUsuario : ViewModel() {

    // Lista de estaciones (Estado observable por la UI)
    private val _listaEstacionesBD = MutableStateFlow<List<EstacionBD>>(emptyList())
    val listaEstacionesBD: StateFlow<List<EstacionBD>> = _listaEstacionesBD.asStateFlow()

    // Lista de reportes (Historial)
    private val _listaReportesBD = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesBD: StateFlow<List<ModeloReportesBD>> = _listaReportesBD.asStateFlow()

    init {
        cargarEstacionesMock()
    }

    /**
     * Carga una lista simulada de estaciones del Metro CDMX.
     * En una app real, esto vendría de una base de datos o API.
     */
    private fun cargarEstacionesMock() {
        val estacionesMock = listOf(
            EstacionBD("Pantitlán", "Línea 1", 1),
            EstacionBD("Zaragoza", "Línea 1", 1),
            EstacionBD("Gómez Farías", "Línea 1", 1),
            EstacionBD("Boulevard Puerto Aéreo", "Línea 1", 1),
            EstacionBD("Balbuena", "Línea 1", 1),
            EstacionBD("Moctezuma", "Línea 1", 1),
            EstacionBD("Candelaria", "Línea 1", 1),
            EstacionBD("San Lázaro", "Línea 1", 1),
            EstacionBD("Merced", "Línea 1", 1),
            EstacionBD("Pino Suárez", "Línea 1", 1),
            EstacionBD("Isabel la Católica", "Línea 1", 1),
            EstacionBD("Salto del Agua", "Línea 1", 0), // Ejemplo de estación cerrada
            EstacionBD("Balderas", "Línea 1", 1),
            EstacionBD("Cuauhtémoc", "Línea 1", 1),
            EstacionBD("Insurgentes", "Línea 1", 1),
            EstacionBD("Sevilla", "Línea 1", 1),
            EstacionBD("Chapultepec", "Línea 1", 1),
            EstacionBD("Juanacatlán", "Línea 1", 1),
            EstacionBD("Tacubaya", "Línea 1", 1),
            EstacionBD("Observatorio", "Línea 1", 1)
        )
        _listaEstacionesBD.value = estacionesMock
    }

    /**
     * Simula el envío de un reporte.
     *
     * @param descripcionReporte El texto del problema.
     * @param estacionSeleccionada El nombre de la estación.
     */
    fun cargarDatosReportes(descripcionReporte: String, estacionSeleccionada: String) {
        viewModelScope.launch {
            // Simulamos un pequeño retraso o proceso de red
            println("Enviando reporte: $descripcionReporte en $estacionSeleccionada")
            
            // Agregamos el reporte a la lista local para que se vea reflejado (opcional)
            val nuevoReporte = ModeloReportesBD(
                descripcion = descripcionReporte,
                estacion = estacionSeleccionada,
                fecha = "Ahora"
            )
            _listaReportesBD.value = _listaReportesBD.value + nuevoReporte
        }
    }
}
