package com.example.hackathon_ai_mobility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.hackathon_ai_mobility.reportes.PantallaDeReportesJefeDeEstacion
import com.example.hackathon_ai_mobility.ui.theme.Hackathon_AI_MobilityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hackathon_AI_MobilityTheme {
                // Mostramos directamente la pantalla de reportes
                PantallaDeReportesJefeDeEstacion(
                    navegarPantallaPrincipal = { /* Acción al cancelar */ },
                    navegarPantallaMisReportesUsuario = { /* Acción al ver reportes */ }
                )
            }
        }
    }
}