package com.example.hackathon_ai_mobility.dijkstra

/**
 * Costo lexicográfico: primero menos transbordos, luego menor distancia.
 */
data class CostoLexicografico(val transbordos: Int, val distancia: Int) : Comparable<CostoLexicografico> {
    override fun compareTo(other: CostoLexicografico): Int {
        if (transbordos != other.transbordos) return transbordos.compareTo(other.transbordos)
        return distancia.compareTo(other.distancia)
    }
}

/** Conexión dirigida del grafo (arista). */
private data class Conexion(
    val nodoDestino: Int,
    val distancia: Int,          // metros
    val esTransbordo: Boolean    // true si es un enlace de transbordo (peso en metros = 0)
)

/** Grafo dirigido representado como Lista de Adyacencia. */
private class Grafo {
    val listaDeAdyacencia = mutableListOf<MutableList<Conexion>>()

    fun agregarNodo(): Int {
        listaDeAdyacencia.add(mutableListOf())
        return listaDeAdyacencia.lastIndex
    }

    fun agregarConexion(origen: Int, destino: Int, distancia: Int, esTransbordo: Boolean) {
        listaDeAdyacencia[origen].add(Conexion(destino, distancia, esTransbordo))
    }

    val cantidadDeNodos: Int get() = listaDeAdyacencia.size
}

/**
 * Representa cada nodo como (estación, línea).
 * - Conexiones con peso = metros entre estaciones en la misma línea (esTransbordo=false)
 * - Conexiones de transbordo con peso 0 (esTransbordo=true), para contar transbordos sin sumar distancia
 */
class GrafoMetroCompleto private constructor(
    private val grafo: Grafo,
    private val nodoAEstacion: List<String>,
    private val nodoALinea: List<String>,
    private val estacionLineaANodo: Map<Pair<String, String>, Int>,
    private val estacionALineas: Map<String, MutableList<String>>
) {

    data class Resultado(val costo: CostoLexicografico, val nodosEnRuta: List<Int>) {
        val totalTransbordos get() = costo.transbordos
        val totalMetros get() = costo.distancia
    }

    fun obtenerNodoDe(estacion: String, linea: String): Int? =
        estacionLineaANodo[normalizar(estacion) to linea.uppercase()]

    fun nombreEstacionDe(nodo: Int) = nodoAEstacion[nodo]
    fun nombreLineaDe(nodo: Int) = nodoALinea[nodo]

    /**
     * Dijkstra que minimiza (transbordos, distancia).
     */
    fun rutaMasCorta(origen: Int, destino: Int): Resultado? {
        val INF = CostoLexicografico(Int.MAX_VALUE / 4, Int.MAX_VALUE / 4)
        val costos = Array(grafo.cantidadDeNodos) { INF }
        val predecesor = IntArray(grafo.cantidadDeNodos) { -1 }
        costos[origen] = CostoLexicografico(0, 0)

        val colaPrioridad = java.util.PriorityQueue(
            compareBy<Pair<Int, CostoLexicografico>> { it.second.transbordos }
                .thenBy { it.second.distancia }
        )
        colaPrioridad.add(origen to costos[origen])

        while (colaPrioridad.isNotEmpty()) {
            val (nodoActual, costoActual) = colaPrioridad.poll()
            if (costoActual != costos[nodoActual]) continue
            if (nodoActual == destino) break

            for (conexion in grafo.listaDeAdyacencia[nodoActual]) {
                val incrementoTransbordos = if (conexion.esTransbordo) 1 else 0
                val costoTentativo = CostoLexicografico(
                    costoActual.transbordos + incrementoTransbordos,
                    costoActual.distancia + conexion.distancia
                )
                if (costoTentativo < costos[conexion.nodoDestino]) {
                    costos[conexion.nodoDestino] = costoTentativo
                    predecesor[conexion.nodoDestino] = nodoActual
                    colaPrioridad.add(conexion.nodoDestino to costoTentativo)
                }
            }
        }

        if (costos[destino] == INF) return null

        // reconstrucción del camino
        val ruta = mutableListOf<Int>()
        var actual = destino
        while (actual != -1) {
            ruta.add(actual)
            actual = predecesor[actual]
        }
        ruta.reverse()
        return Resultado(costos[destino], ruta)
    }

    /**
     * Devuelve la ruta formateada como: "Estación (Línea)", colapsando secuencias en la misma línea
     * y marcando transbordos.
     */
    fun rutaFormateada(nodos: List<Int>): List<String> {
        if (nodos.isEmpty()) return emptyList()
        val salida = mutableListOf<String>()
        var lineaAnterior = nombreLineaDe(nodos.first())
        salida.add("${nombreEstacionDe(nodos.first())} ($lineaAnterior)")
        for (i in 1 until nodos.size) {
            val linea = nombreLineaDe(nodos[i])
            if (linea != lineaAnterior) {
                salida.add("Transbordo en ${nombreEstacionDe(nodos[i - 1])} → $linea")
            }
            salida.add("${nombreEstacionDe(nodos[i])} ($linea)")
            lineaAnterior = linea
        }
        // eliminar duplicados consecutivos (cuando la estación se repite al crear transbordo)
        return salida.fold(mutableListOf()) { acc, s ->
            if (acc.isEmpty() || acc.last() != s) acc.add(s)
            acc
        }
    }

    companion object {
        /**
         * Construye el grafo del Metro a partir del contenido de un archivo de texto.
         * Formato esperado por línea: "Línea|Interestación|Longitud|Estado"
         *  - Interestación: "A - B"
         *  - Longitud: metros (entero)
         */
        fun desdeTexto(texto: String): GrafoMetroCompleto {
            val grafo = Grafo()
            val nodoAEstacion = mutableListOf<String>()
            val nodoALinea = mutableListOf<String>()
            val estacionLineaANodo = mutableMapOf<Pair<String, String>, Int>()
            val estacionALineas = mutableMapOf<String, MutableList<String>>()

            // Crea (si no existe) y devuelve el id de nodo para (estación, línea)
            fun obtenerONuevoNodo(estacion: String, linea: String): Int {
                val clave = normalizar(estacion) to linea.uppercase()
                return estacionLineaANodo.getOrPut(clave) {
                    val id = grafo.agregarNodo()
                    nodoAEstacion.add(estacion.trim())
                    nodoALinea.add(linea.uppercase())
                    estacionALineas.getOrPut(normalizar(estacion)) { mutableListOf() }
                        .apply { if (!contains(linea.uppercase())) add(linea.uppercase()) }
                    id
                }
            }

            val segmentosMismaLinea = mutableListOf<Triple<Int, Int, Int>>() // (u, v, metros)

            texto.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .forEach { linea ->
                    val partes = linea.split("|")
                    if (partes.size < 3) return@forEach
                    val nombreLinea = partes[0].trim()
                    val interesctacion = partes[1].trim() // "A - B"
                    val metros = partes[2].trim().toIntOrNull() ?: 0

                    val estaciones = interesctacion.split("-")
                    if (estaciones.size != 2) return@forEach
                    val estacionA = estaciones[0].trim()
                    val estacionB = estaciones[1].trim()

                    val nodoA = obtenerONuevoNodo(estacionA, nombreLinea)
                    val nodoB = obtenerONuevoNodo(estacionB, nombreLinea)

                    // Tramo ida y vuelta
                    segmentosMismaLinea += Triple(nodoA, nodoB, metros)
                    segmentosMismaLinea += Triple(nodoB, nodoA, metros)
                }

            // Conexiones dentro de la misma línea (peso = metros, no cuentan transbordo)
            for ((u, v, w) in segmentosMismaLinea) {
                grafo.agregarConexion(u, v, w, esTransbordo = false)
            }

            // Conexiones de transbordo entre TODAS las líneas que comparten estación
            estacionALineas.forEach { (estacionClave, lineas) ->
                if (lineas.size > 1) {
                    for (i in lineas.indices) {
                        for (j in i + 1 until lineas.size) {
                            val li = lineas[i]
                            val lj = lineas[j]
                            val a = estacionLineaANodo[estacionClave to li]
                            val b = estacionLineaANodo[estacionClave to lj]
                            if (a != null && b != null) {
                                grafo.agregarConexion(a, b, 0, esTransbordo = true)
                                grafo.agregarConexion(b, a, 0, esTransbordo = true)
                            }
                        }
                    }
                }
            }

            return GrafoMetroCompleto(grafo, nodoAEstacion, nodoALinea, estacionLineaANodo, estacionALineas)
        }

        // Alias para mantener compatibilidad con posibles llamadas existentes.
        fun fromText(text: String): GrafoMetroCompleto = desdeTexto(text)

        private fun normalizar(s: String) = s.trim().lowercase()
    }
}

/**
 * Calcula la mejor ruta (mínimos transbordos y luego menor distancia) entre dos estaciones.
 * Devuelve el resultado y la ruta formateada.
 */
fun computeBestRoute(metro: GrafoMetroCompleto, origin: String, destination: String): Pair<GrafoMetroCompleto.Resultado, List<String>>? {
    val lineasSistema = sequenceOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "12")

    val nodosOrigen = lineasSistema.mapNotNull { linea ->
        metro.obtenerNodoDe(origin, linea)?.let { it to linea }
    }.toList()
    val nodosDestino = lineasSistema.mapNotNull { linea ->
        metro.obtenerNodoDe(destination, linea)?.let { it to linea }
    }.toList()

    if (nodosOrigen.isEmpty() || nodosDestino.isEmpty()) return null

    var mejor: GrafoMetroCompleto.Resultado? = null
    var rutaBonita: List<String> = emptyList()

    for ((origenNodo, _) in nodosOrigen) {
        for ((destinoNodo, _) in nodosDestino) {
            val r = metro.rutaMasCorta(origenNodo, destinoNodo) ?: continue
            if (mejor == null || r.costo < mejor!!.costo) {
                mejor = r
                rutaBonita = metro.rutaFormateada(r.nodosEnRuta)
            }
        }
    }

    return mejor?.let { it to rutaBonita }
}