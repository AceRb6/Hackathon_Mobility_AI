package com.example.hackathon_ai_mobility.presentation.modelos

/**
 * Modelo de datos que representa un Reporte de Incidente.
 *
 * @property descripcion Detalle del problema reportado.
 * @property estacion Nombre de la estación donde ocurre el incidente.
 * @property fecha Fecha y hora del reporte (simulado).
 */
data class ModeloReportesBD(
    val descripcion: String? = null,
    val estacion: String? = null,
    val fecha: String? = null
)
