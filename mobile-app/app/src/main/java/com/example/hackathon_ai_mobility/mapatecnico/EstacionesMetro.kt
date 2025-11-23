package com.example.appmetrocdmx.presentation.prueba_mapa

import androidx.compose.runtime.Immutable

@Immutable
data class EstacionMetro(
    val nombre: String,
    val x: Float,   // 0..1 respecto al ancho del mapa
    val y: Float,    // 0..1 respecto al alto del mapa
    val linea: String
)

object EstacionesMetro {

    val todasLasEstacionesMetro: List<EstacionMetro> = listOf(
        // Línea 1
        EstacionMetro("Pantitlán",              0.860f, 0.493f,"1"),
        EstacionMetro("Zaragoza",               0.808f, 0.486f,"1"),
        EstacionMetro("Gómez Farías",           0.784f, 0.470f,"1"),
        EstacionMetro("Boulevard Puerto Aéreo", 0.775f, 0.444f,"1"),
        EstacionMetro("Balbuena",               0.765f, 0.418f,"1"),
        EstacionMetro("Moctezuma",              0.736f, 0.399f,"1"),
        EstacionMetro("San Lázaro",             0.697f, 0.371f,"1"),
        EstacionMetro("Candelaria",             0.659f, 0.372f,"1"),
        EstacionMetro("Merced",                 0.586f, 0.421f,"1"),
        EstacionMetro("Pino Suárez",            0.547f, 0.430f,"1"),
        EstacionMetro("Isabel la Católica",     0.498f, 0.431f,"1"),
        EstacionMetro("Salto del Agua",         0.449f, 0.431f,"1"),
        EstacionMetro("Balderas",               0.371f, 0.431f,"1"),
        EstacionMetro("Cuauhtémoc",             0.328f, 0.431f,"1"),
        EstacionMetro("Insurgentes",            0.289f, 0.431f,"1"),
        EstacionMetro("Sevilla",                0.252f, 0.450f,"1"),
        EstacionMetro("Chapultepec",            0.217f, 0.474f,"1"),
        EstacionMetro("Juanacatlán",            0.186f, 0.495f,"1"),
        EstacionMetro("Tacubaya",               0.150f, 0.519f,"1"),
        EstacionMetro("Observatorio",           0.112f, 0.545f,"1"),


// Línea 2
        EstacionMetro("Tasqueña",         0.549f, 0.708f,"2"),
        EstacionMetro("General Anaya",    0.549f, 0.684f,"2"),
        EstacionMetro("Ermita",           0.549f, 0.663f,"2"),
        EstacionMetro("Portales",         0.549f, 0.640f,"2"),
        EstacionMetro("Nativitas",        0.549f, 0.617f,"2"),
        EstacionMetro("Villa de Cortés",  0.549f, 0.594f,"2"),
        EstacionMetro("Xola",             0.549f, 0.571f,"2"),
        EstacionMetro("Viaducto",         0.549f, 0.549f,"2"),
        EstacionMetro("Chabacano",        0.549f, 0.519f,"2"),
        EstacionMetro("San Antonio Abad", 0.549f, 0.486f,"2"),
        EstacionMetro("Pino Suárez",      0.549f, 0.430f,"2"),
        EstacionMetro("Zócalo",           0.549f, 0.390f,"2"),
        EstacionMetro("Allende",          0.520f, 0.360f,"2"),
        EstacionMetro("Bellas Artes",     0.448f, 0.350f,"2"),
        EstacionMetro("Hidalgo",          0.371f, 0.350f,"2"),
        EstacionMetro("Revolución",       0.337f, 0.350f,"2"),
        EstacionMetro("San Cosme",        0.290f, 0.350f,"2"),
        EstacionMetro("Normal",           0.254f, 0.350f,"2"),
        EstacionMetro("Colegio Militar",  0.220f, 0.350f,"2"),
        EstacionMetro("Popotla",          0.193f, 0.335f,"2"),
        EstacionMetro("Cuitláhuac",       0.170f, 0.320f,"2"),
        EstacionMetro("Tacuba",           0.150f, 0.305f,"2"),
        EstacionMetro("Panteones",        0.1f, 0.305f,"2"),
        EstacionMetro("Cuatro Caminos",   0.043f, 0.305f,"2"),

        // Línea 3
        EstacionMetro("Indios Verdes",                      0.472f, 0.166f,"3"),
        EstacionMetro("Deportivo 18 de Marzo",              0.472f, 0.218f,"3"),
        EstacionMetro("Potrero",                            0.460f, 0.238f,"3"),
        EstacionMetro("La Raza",                            0.430f, 0.257f,"3"),
        EstacionMetro("Tlatelolco",                         0.405f, 0.275f,"3"),
        EstacionMetro("Guerrero",                           0.371f, 0.327f,"3"),
        EstacionMetro("Hidalgo",                            0.371f, 0.350f,"3"),
        EstacionMetro("Juárez",                             0.371f, 0.390f,"3"),
        EstacionMetro("Balderas",                           0.371f, 0.431f,"3"),
        EstacionMetro("Niños Héroes",                       0.371f, 0.452f,"3"),
        EstacionMetro("Hospital General",                   0.371f, 0.475f,"3"),
        EstacionMetro("Centro Médico",                      0.371f, 0.519f,"3"),
        EstacionMetro("Etiopía/Plaza de la Transparencia",  0.371f, 0.542f,"3"),
        EstacionMetro("Eugenia",                            0.371f, 0.565f,"3"),
        EstacionMetro("División del Norte",                 0.371f, 0.587f,"3"),
        EstacionMetro("Zapata",                             0.371f, 0.610f,"3"),
        EstacionMetro("Coyoacán",                           0.371f, 0.632f,"3"),
        EstacionMetro("Viveros/Derechos Humanos",           0.371f, 0.656f,"3"),
        EstacionMetro("Miguel Ángel de Quevedo",            0.371f, 0.678f,"3"),
        EstacionMetro("Copilco",                            0.371f, 0.700f,"3"),
        EstacionMetro("Universidad",                        0.371f, 0.722f,"3"),

        // Línea 4
        EstacionMetro("Martín Carrera",  0.659f, 0.218f,"4"),
        EstacionMetro("Talismán",        0.659f, 0.237f,"4"),
        EstacionMetro("Bondojito",       0.659f, 0.254f,"4"),
        EstacionMetro("Consulado",       0.659f, 0.288f,"4"),
        EstacionMetro("Canal del Norte", 0.659f, 0.320f,"4"),
        EstacionMetro("Morelos",         0.659f, 0.347f,"4"),
        EstacionMetro("Candelaria",      0.659f, 0.372f,"4"),
        EstacionMetro("Fray Servando",   0.659f, 0.460f,"4"),
        EstacionMetro("Jamaica",         0.659f, 0.519f,"4"),
        EstacionMetro("Santa Anita",     0.659f, 0.555f,"4"),

        // Línea 5
        EstacionMetro("Pantitlán",              0.860f, 0.493f,"5"),
        EstacionMetro("Hangares",               0.860f, 0.443f,"5"),
        EstacionMetro("Terminal Aérea",         0.860f, 0.402f,"5"),
        EstacionMetro("Oceanía",                0.8f, 0.341f,"5"),
        EstacionMetro("Aragón",                 0.758f, 0.313f,"5"),
        EstacionMetro("Eduardo Molina",         0.729f, 0.294f,"5"),
        EstacionMetro("Consulado",              0.659f, 0.288f,"5"),
        EstacionMetro("Valle Gómez",            0.564f, 0.288f,"5"),
        EstacionMetro("Misterios",              0.455f, 0.275f,"5"),
        EstacionMetro("La Raza",                0.430f, 0.257f,"5"),
        EstacionMetro("Autobuses del Norte",    0.400f, 0.237f,"5"),
        EstacionMetro("Instituto del Petróleo", 0.371f, 0.218f,"5"),
        EstacionMetro("Politécnico",            0.329f, 0.176f,"5"),


        // Línea 6
        EstacionMetro("El Rosario",             0.150f, 0.191f,"6"),
        EstacionMetro("Tezozómoc",              0.170f, 0.205f,"6"),
        EstacionMetro("Azcapotzalco",           0.207f, 0.218f,"6"),
        EstacionMetro("Ferrería",               0.243f, 0.218f,"6"),
        EstacionMetro("Norte 45",               0.292f, 0.218f,"6"),
        EstacionMetro("Vallejo",                0.325f, 0.218f,"6"),
        EstacionMetro("Instituto del Petróleo", 0.371f, 0.218f,"6"),
        EstacionMetro("Lindavista",             0.438f, 0.218f,"6"),
        EstacionMetro("Deportivo 18 de Marzo",  0.472f, 0.218f,"6"),
        EstacionMetro("La Villa/Basílica",    0.560f, 0.218f,"6"),
        EstacionMetro("Martín Carrera",         0.659f, 0.218f,"6"),


        // Línea 7
        EstacionMetro("El Rosario",             0.150f, 0.191f,"7"),
        EstacionMetro("Aquiles Serdán",         0.150f, 0.221f,"7"),
        EstacionMetro("Camarones",              0.150f, 0.242f,"7"),
        EstacionMetro("Refinería",              0.150f, 0.278f,"7"),
        EstacionMetro("Tacuba",                 0.148f, 0.305f,"7"),
        EstacionMetro("San Joaquín",            0.150f, 0.355f,"7"),
        EstacionMetro("Polanco",                0.150f, 0.396f,"7"),
        EstacionMetro("Auditorio",              0.150f, 0.439f,"7"),
        EstacionMetro("Constituyentes",         0.150f, 0.480f,"7"),
        EstacionMetro("Tacubaya",               0.150f, 0.519f,"7"),
        EstacionMetro("San Pedro de los Pinos", 0.150f, 0.558f,"7"),
        EstacionMetro("San Antonio",            0.150f, 0.580f,"7"),
        EstacionMetro("Mixcoac",                0.150f, 0.610f,"7"),
        EstacionMetro("Barranca del Muerto",    0.150f, 0.648f,"7"),


        // Línea 8
        EstacionMetro("Garibaldi",                 0.449f, 0.327f,"8"),
        EstacionMetro("Bellas Artes",              0.449f, 0.350f,"8"),
        EstacionMetro("San Juan de Letrán",        0.449f, 0.388f,"8"),
        EstacionMetro("Salto del Agua",            0.449f, 0.431f,"8"),
        EstacionMetro("Doctores",                  0.462f, 0.461f,"8"),
        EstacionMetro("Obrera",                    0.492f, 0.482f,"8"),
        EstacionMetro("Chabacano",                 0.549f, 0.519f,"8"),
        EstacionMetro("La Viga",                   0.587f, 0.545f,"8"),
        EstacionMetro("Santa Anita",               0.659f, 0.555f,"8"),
        EstacionMetro("Coyuya",                    0.712f, 0.579f,"8"),
        EstacionMetro("Iztacalco",                 0.712f, 0.600f,"8"),
        EstacionMetro("Apatlaco",                  0.712f, 0.624f,"8"),
        EstacionMetro("Aculco",                    0.712f, 0.646f,"8"),
        EstacionMetro("Escuadrón 201",             0.712f, 0.670f,"8"),
        EstacionMetro("Atlalilco",                 0.725f, 0.689f,"8"),
        EstacionMetro("Iztapalapa",                0.751f, 0.699f,"8"),
        EstacionMetro("Cerro de la Estrella",      0.786f, 0.699f,"8"),
        EstacionMetro("UAM I",                     0.817f, 0.699f,"8"),
        EstacionMetro("Constitución de 1917",      0.852f, 0.699f,"8"),


        // Línea 9
        EstacionMetro("Pantitlán",       0.860f, 0.493f,"9"),
        EstacionMetro("Puebla",          0.839f, 0.509f,"9"),
        EstacionMetro("Ciudad Deportiva",0.809f, 0.519f,"9"),
        EstacionMetro("Velódromo",       0.763f, 0.519f,"9"),
        EstacionMetro("Mixiuhca",        0.720f, 0.519f,"9"),
        EstacionMetro("Jamaica",         0.659f, 0.519f,"9"),
        EstacionMetro("Chabacano",       0.549f, 0.519f,"9"),
        EstacionMetro("Lázaro Cardenas", 0.462f, 0.519f,"9"),
        EstacionMetro("Centro Médico",   0.371f, 0.519f,"9"),
        EstacionMetro("Chilpancingo",    0.316f, 0.519f,"9"),
        EstacionMetro("Patriotismo",     0.227f, 0.519f,"9"),
        EstacionMetro("Tacubaya",        0.150f, 0.519f,"9"),


        // Línea A
        EstacionMetro("Pantitlán",        0.860f, 0.493f,"A"),
        EstacionMetro("Agrícola Oriental",0.893f, 0.520f,"A"),
        EstacionMetro("Canal de San Juan",0.893f, 0.543f,"A"),
        EstacionMetro("Tepalcates",       0.893f, 0.566f,"A"),
        EstacionMetro("Guelatao",         0.893f, 0.588f,"A"),
        EstacionMetro("Peñón Viejo",      0.893f, 0.610f,"A"),
        EstacionMetro("Acatitla",         0.893f, 0.633f,"A"),
        EstacionMetro("Santa Marta",      0.893f, 0.656f,"A"),
        EstacionMetro("Los Reyes",        0.909f, 0.679f,"A"),
        EstacionMetro("La Paz",           0.935f, 0.696f,"A"),



        // Línea B
        EstacionMetro("Ciudad Azteca",        0.842f, 0.121f,"B"),
        EstacionMetro("Plaza Aragón",         0.842f, 0.141f,"B"),
        EstacionMetro("Olímpica",             0.842f, 0.161f,"B"),
        EstacionMetro("Ecatepec",             0.842f, 0.182f,"B"),
        EstacionMetro("Múzquiz",              0.842f, 0.202f,"B"),
        EstacionMetro("Río de los Remedios",  0.842f, 0.223f,"B"),
        EstacionMetro("Impulsora",            0.842f, 0.243f,"B"),
        EstacionMetro("Nezahualcóyotl",       0.842f, 0.263f,"B"),
        EstacionMetro("Villa de Aragón",      0.842f, 0.284f,"B"),
        EstacionMetro("Bosques de Aragón",    0.842f, 0.304f,"B"),
        EstacionMetro("Deportivo Oceanía",    0.830f, 0.322f,"B"),
        EstacionMetro("Oceanía",              0.800f, 0.341f,"B"),
        EstacionMetro("Romero Rubio",         0.775f, 0.358f,"B"),
        EstacionMetro("Ricardo Flores Magón", 0.734f, 0.371f,"B"),
        EstacionMetro("San Lázaro",           0.697f, 0.371f,"B"),
        EstacionMetro("Morelos",              0.659f, 0.347f,"B"),
        EstacionMetro("Tepito",               0.600f, 0.327f,"B"),
        EstacionMetro("Lagunilla",            0.543f, 0.327f,"B"),
        EstacionMetro("Garibaldi",            0.449f, 0.327f,"B"),
        EstacionMetro("Guerrero",             0.371f, 0.327f,"B"),
        EstacionMetro("Buenavista",           0.323f, 0.327f,"B"),

        // Línea 12
        EstacionMetro("Tláhuac",                    0.880f, 0.899f,"12"),
        EstacionMetro("Tlaltenco",                  0.858f, 0.883f,"12"),
        EstacionMetro("Zapotitlán",                 0.835f, 0.868f,"12"),
        EstacionMetro("Nopalera",                   0.811f, 0.852f,"12"),
        EstacionMetro("Olivos",                     0.789f, 0.837f,"12"),
        EstacionMetro("Tezonco",                    0.767f, 0.822f,"12"),
        EstacionMetro("Periférico Oriente",         0.743f, 0.806f,"12"),
        EstacionMetro("Calle 11",                   0.725f, 0.783f,"12"),
        EstacionMetro("Lomas Estrella",             0.725f, 0.760f,"12"),
        EstacionMetro("San Andrés Tomatlán",        0.725f, 0.737f,"12"),
        EstacionMetro("Culhuacán",                  0.725f, 0.714f,"12"),
        EstacionMetro("Atlalilco",                  0.725f, 0.689f,"12"),
        EstacionMetro("Mexicaltzingo",              0.652f, 0.689f,"12"),
        EstacionMetro("Ermita",                     0.549f, 0.663f,"12"),
        EstacionMetro("Eje Central",                0.509f, 0.663f,"12"),
        EstacionMetro("Parque de los Venados",      0.457f, 0.610f,"12"),
        EstacionMetro("Zapata",                     0.371f, 0.610f,"12"),
        EstacionMetro("Hospital 20 de Noviembre",   0.286f, 0.610f,"12"),
        EstacionMetro("Insurgentes Sur",            0.211f, 0.610f,"12"),
        EstacionMetro("Mixcoac",                    0.150f, 0.610f,"12"),

        )

    // atajos útiles
    val porNombre: Map<String, EstacionMetro> = todasLasEstacionesMetro.associateBy { it.nombre }

    fun buscar(query: String): List<EstacionMetro> =
        todasLasEstacionesMetro.filter { it.nombre.contains(query, ignoreCase = true) }
}
