package com.example.hackathon_ai_mobility.modelos

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

class ModeloReportesBD (

    //el formato tiene que ser el siguiente -> val nombreDeLaVariable: TipoDeVariable? = null
    val idDocumento: String? = null,
    val nombreDeJefeDeEstacionCreadorReporte: String? = null,
    @ServerTimestamp val fechaHoraCreacionReporte: Timestamp? = null,

    val tituloReporte: String? = null,
    val estacionQueTieneReporte: String? = null,
    val descripcionReporteJefeDeEstacion: String? = null,
    val tipoProblema: Int? = null,
    val horaProblema: String? = null,
    val reporteTecnicoRegulador: String? = null,
    val reporteCompletado: Int? = null

)