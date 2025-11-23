package com.example.hackathon_ai_mobility.modelos



//TOMAR EN CUENTA PARA MAS ADELANTE QUE SI ESPECIFICAS DATA CLASS
//SE PUEDE AUTOCOMPLETAR CODIGO MAS ADELANTE
data class TramoBD(
    val linea: String? = null,
    val origen: String? = null,
    val destino: String? = null,
    val metros: Int? = null,
    val estado: Int? = null, // 1 abierto, 0 cerrado
)

data class EstacionBD(
    val nombre: String? = null,
    val lineas: List<String>? = null,
    val abierta: Int? = null   // 1 abierta, 0 cerrada
)