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
 * Gestiona la lista de estaciones, el envío de reportes y funcionalidades de administrador.
 */
class ModeloDeVistaPantallaReportesUsuario : ViewModel() {

    // Lista de estaciones (Estado observable por la UI)
    private val _listaEstacionesBD = MutableStateFlow<List<EstacionBD>>(emptyList())
    val listaEstacionesBD: StateFlow<List<EstacionBD>> = _listaEstacionesBD.asStateFlow()

    // Lista de reportes (Historial)
    private val _listaReportesBD = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesBD: StateFlow<List<ModeloReportesBD>> = _listaReportesBD.asStateFlow()

    // Lista de reportes filtrados por estación (para pantalla admin)
    private val _reportesPorEstacion = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val reportesPorEstacion: StateFlow<List<ModeloReportesBD>> = _reportesPorEstacion.asStateFlow()

    init {
        cargarEstacionesMock()
        cargarReportesMock() // Cargamos reportes de ejemplo
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
     * Carga reportes de ejemplo para demostración.
     * En producción, esto vendría de Firestore.
     */
    private fun cargarReportesMock() {
        val reportesMock = listOf(
            ModeloReportesBD("Fallas por objeto en Santa Anita", "Pino Suárez", "9/05/2023"),
            ModeloReportesBD("Persona se cayó en las vías en Zócalo", "Pino Suárez", "8/05/2023"),
            ModeloReportesBD("Inundaciones en metro Normal", "Pino Suárez", "1/06/2022"),
            ModeloReportesBD("Torniquete atascado en la entrada principal", "Balderas", "15/11/2023"),
            ModeloReportesBD("Escalera eléctrica fuera de servicio", "Balderas", "14/11/2023"),
            ModeloReportesBD("Iluminación deficiente en el andén 2", "Candelaria", "20/11/2023")
        )
        _listaReportesBD.value = reportesMock
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

    /**
     * [FUNCIONALIDAD ADMIN] Obtiene todos los reportes de una estación específica.
     * 
     * @param nombreEstacion El nombre de la estación a filtrar.
     */
    fun obtenerReportesPorEstacion(nombreEstacion: String) {
        viewModelScope.launch {
            val reportesFiltrados = _listaReportesBD.value.filter {
                it.estacion?.equals(nombreEstacion, ignoreCase = true) == true
            }
            _reportesPorEstacion.value = reportesFiltrados
            println("Reportes encontrados para $nombreEstacion: ${reportesFiltrados.size}")
        }
    }

    /**
     * [FUNCIONALIDAD ADMIN] Cambia el estado de una estación (abierta/cerrada).
     * 
     * @param nombreEstacion El nombre de la estación a modificar.
     * @param nuevoEstado true = abierta, false = cerrada.
     */
    fun cambiarEstadoEstacion(nombreEstacion: String, nuevoEstado: Boolean) {
        viewModelScope.launch {
            val nuevaLista = _listaEstacionesBD.value.map { estacion ->
                if (estacion.nombre?.equals(nombreEstacion, ignoreCase = true) == true) {
                    estacion.copy(abierta = if (nuevoEstado) 1 else 0)
                } else {
                    estacion
                }
            }
            _listaEstacionesBD.value = nuevaLista
            println("Estación $nombreEstacion cambiada a: ${if (nuevoEstado) "ABIERTA" else "CERRADA"}")
        }
    }
}
