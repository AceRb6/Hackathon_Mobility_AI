package com.example.hackathon_ai_mobility.servicioMapas

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName
import org.osmdroid.util.GeoPoint

data class LugarOSM(
    @SerializedName("lat") val latitud: String,
    @SerializedName("lon") val longitud: String
)

data class RespuestaRutaOSM(
    @SerializedName("paths") val caminos: List<CaminoOSM>
)
data class CaminoOSM(
    @SerializedName("points") val puntos: CodificadoRutaOSM
)
data class CodificadoRutaOSM(
    @SerializedName("coordinates") val coordenadas: List<List<Double>>
)

interface NominatimAPI {
    @GET("search")
    suspend fun buscarLugar(
        @Query("q") nombre: String,
        @Query("format") formato: String = "json",
        @Query("limit") limite: Int = 1
    ): List<LugarOSM>
}

interface GraphHopperAPI {
    @GET("route")
    suspend fun obtenerRuta(
        @Query("point") origen: String,
        @Query("point") destino: String,
        @Query("vehicle") vehiculo: String = "foot",
        @Query("key") apiKey: String = "f928a48d-7bcc-4a54-822d-5a2186efd976"
    ): RespuestaRutaOSM
}

object ServicioMapas {
    private val retrofitNominatim = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val nominatimAPI = retrofitNominatim.create(NominatimAPI::class.java)

    private val retrofitGraphHopper = Retrofit.Builder()
        .baseUrl("https://graphhopper.com/api/1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val graphHopperAPI = retrofitGraphHopper.create(GraphHopperAPI::class.java)

    suspend fun obtenerCoordenadas(nombreLugar: String): GeoPoint? {
        return try {
            val resultados = nominatimAPI.buscarLugar("$nombreLugar, Austria")
            if (resultados.isNotEmpty()) {
                val lugar = resultados.first()
                GeoPoint(lugar.latitud.toDouble(), lugar.longitud.toDouble())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerPuntosRuta(
        origen: GeoPoint,
        destino: GeoPoint
    ): List<GeoPoint> {
        return try {
            val respuesta = graphHopperAPI.obtenerRuta(
                origen = "${origen.latitude},${origen.longitude}",
                destino = "${destino.latitude},${destino.longitude}",
                apiKey = "f928a48d-7bcc-4a54-822d-5a2186efd976"
            )
            respuesta.caminos.firstOrNull()?.puntos?.coordenadas?.map {
                GeoPoint(it[1], it[0])
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}