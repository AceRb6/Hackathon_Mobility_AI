package com.example.hackathon_ai_mobility.reportes

import androidx.compose.runtime.Composable
import com.example.hackathon_ai_mobility.modelos.EstacionBD
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ModeloDeVistaPantallaJefeDeEstacion(){

    /*aqui se inicializa la base de datos*/
    private var db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    private val _datosReportes = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesBD: StateFlow<List<ModeloReportesBD>> = _datosReportes

    private val _datosEstaciones = MutableStateFlow<List<EstacionBD>>(emptyList())
    val listaEstacionesBD: StateFlow<List<EstacionBD>> = _datosEstaciones

}