package com.example.hackathon_ai_mobility.utils

fun obtenerRolDesdeCorreo(correo: String): String? {
    val regex = Regex("^[a-zA-Z0-9._-]+@metro\\.(jefeestacion|regulador|tecnico)\\.gob\\.mx$")
    val match = regex.find(correo.lowercase())
    return match?.groupValues?.get(1)
}