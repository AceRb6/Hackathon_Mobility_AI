package com.example.hackathon_ai_mobility.presentation.modelos

/**
 * Modelo de datos que representa una Estación del Metro.
 *
 * @property nombre El nombre de la estación (ej. "Pino Suárez").
 * @property linea La línea a la que pertenece (ej. "Línea 1").
 * @property abierta Indica si la estación está operativa (1 = Abierta, 0 = Cerrada).
 */
data class EstacionBD(
    val nombre: String? = null,
    val linea: String? = null,
    val abierta: Int = 1 // Por defecto asumimos que está abierta
)
