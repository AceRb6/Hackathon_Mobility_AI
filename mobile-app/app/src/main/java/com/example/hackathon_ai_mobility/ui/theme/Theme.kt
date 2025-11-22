package com.example.hackathon_ai_mobility.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Esquema de colores Oscuro (Adaptado al Metro)
private val DarkColorScheme = darkColorScheme(
    primary = MetroRed,
    onPrimary = MetroWhite,
    secondary = MetroDarkGray,
    onSecondary = MetroWhite,
    tertiary = MetroRed,
    background = MetroBlack,
    surface = MetroDarkGray,
    onBackground = MetroWhite,
    onSurface = MetroWhite
)

// Esquema de colores Claro (Principal)
private val LightColorScheme = lightColorScheme(
    primary = MetroRed,
    onPrimary = MetroWhite,
    secondary = MetroDarkGray,
    onSecondary = MetroWhite,
    tertiary = MetroRed,
    background = MetroWhite,
    surface = MetroLightGray,
    onBackground = MetroBlack,
    onSurface = MetroBlack
)

@Composable
fun Hackathon_AI_MobilityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos color dinámico para forzar nuestra identidad visual
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}