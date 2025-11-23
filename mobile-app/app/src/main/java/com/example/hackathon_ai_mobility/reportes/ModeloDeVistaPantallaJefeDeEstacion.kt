package com.example.hackathon_ai_mobility.reportes

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hackathon_ai_mobility.modelos.EstacionBD
import com.example.hackathon_ai_mobility.modelos.ModeloReportesBD
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

//@Composable
class ModeloDeVistaPantallaJefeDeEstacion: ViewModel(){

    /*aqui se inicializa la base de datos*/
    private var db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    private val _datosReportes = MutableStateFlow<List<ModeloReportesBD>>(emptyList())
    val listaReportesBD: StateFlow<List<ModeloReportesBD>> = _datosReportes

    private val _datosEstaciones = MutableStateFlow<List<EstacionBD>>(emptyList())
    val listaEstacionesBD: StateFlow<List<EstacionBD>> = _datosEstaciones

    init {

        // db = Firebase.firestore
        //getResportesUsuarioActual()

        getEstacionesBD()//<--------solo se ocupa este cuando lo tenga

    }

    private fun getEstacionesBD() {
        viewModelScope.launch {
            val result: List<EstacionBD> = withContext(Dispatchers.IO) {
                getTodasLasEstaciones()
            }
            _datosEstaciones.value = result
        }
    }

    private fun getResportesUsuarioActual(){
        viewModelScope.launch {

            var result: List<ModeloReportesBD> = withContext(Dispatchers.IO){
                getTodosReportesUsuarioActual()
            }
            _datosReportes.value = result


        }
    }
    /*Funcion para ver a todos los reportes o elementos de la coleccion*/

    suspend fun getTodosReportesUsuarioActual():List<ModeloReportesBD>{

        return try {

            //COSAS NUEVAS QUE SE AGREGARON PARA LOS REPORTES POR USUARIO(<-) SI QUITAS LAS DE ESE SIMBOLO MANDARA TODOS LOS REPORTES NO SOLO EL DE EL USUARIO ACTUAL
            val usuarioActual = auth.currentUser//(<-)
            val correo = usuarioActual?.email ?: return emptyList()//(<-)


            db.collection("reportesBD")
                .whereEqualTo("nombreDeUsuarioCreadorReporte", correo)//(<-)
                .get()
                .await()
                .documents
                .mapNotNull { snapshot ->

                    //snapshot.toObject(ModeloReportesBD::class.java)//me parece que esta parte se refiere a la clase que tenfgo en la carpeta model

                    // mapear datos y adjuntar el id del documento
                    val reporte = snapshot.toObject(ModeloReportesBD::class.java)
                    if (reporte != null) {
                        ModeloReportesBD(
                            idDocumento = snapshot.id,
                            nombreDeJefeDeEstacionCreadorReporte = reporte.nombreDeJefeDeEstacionCreadorReporte,
                            fechaHoraCreacionReporte = reporte.fechaHoraCreacionReporte,
                            tituloReporte = reporte.tituloReporte,
                            estacionQueTieneReporte = reporte.estacionQueTieneReporte,
                            descripcionReporteJefeDeEstacion = reporte.descripcionReporteJefeDeEstacion,
                            tipoProblema = reporte.tipoProblema,
                            horaProblema = reporte.horaProblema,
                            reporteCompletado = reporte.reporteCompletado

                        )
                    } else {
                        null
                    }
                }


        }catch (e:Exception){

            Log.i("Ariel", e.toString())
            emptyList()

        }

    }


    suspend fun getTodasLasEstaciones(): List<EstacionBD> {
        return try {
            db.collection("estacionesBD")
                .get()
                .await()
                .documents
                .mapNotNull { snapshot ->
                    snapshot.toObject(EstacionBD::class.java)
                }
        } catch (e: Exception) {
            Log.i("Ariel", e.toString())
            emptyList()
        }
    }

    fun eliminarReporte(idDocumento: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.collection("reportesBD")
                        .document(idDocumento)
                        .delete()
                        .await()
                }

                // recargar lista solo del usuario actual
                val result = withContext(Dispatchers.IO) {
                    getTodosReportesUsuarioActual()
                }
                _datosReportes.value = result

            } catch (e: Exception) {
                Log.e("Ariel", "Error al eliminar reporte: ${e.message}")
            }
        }
    }


    fun cargarDatosReportes(
        descripcionReporte: String,
        estacionSeleccionada: String,
        horaCuandoEsmpezoProblema: String,
        descripcionReporte: String,
        tipodelproblema: int,
        reporteTecnico: String,
        reporteStatus: int
    ){

        val usuarioActual = auth.currentUser
        //val random = (1..100).random()

        val objetoReporteDeViewmodel = ModeloReportesBD(
            /*
             nombreArtista = "Random $random",
             descriptionArtista = "Descripcion Random numero $random",
             imagen = "https://img.freepik.com/vector-premium/plantillas-diseno-vectorial-iconos-prueba_1172029-3113.jpg",
             createdByUid = usuarioActual?.uid,
             createdByName = usuarioActual?.email
             */
            //nombreDeUsuarioCreadorReporte = "Random $random",
            nombreDeJefeDeEstacionCreadorReporte = usuarioActual?.email ?: "Usuario desconocido",
            estacionQueTieneReporte = estacionSeleccionada,
            horaProblema = horaCuandoEsmpezoProblema,
            descripcionReporteJefeDeEstacion = descripcionReporte,
            tipoProblema = tipodelproblema,
            reporteTecnicoRegulador = reporteTecnico,
            reporteCompletado = reporteStatus


            )
        db.collection("reportesBD")
            .add(objetoReporteDeViewmodel)
            .addOnSuccessListener {
                Log.i("Ariel", "Reporte creado con éxito por ${usuarioActual?.email}")
            }
            .addOnFailureListener {
                Log.e("Ariel", "Error al crear artista: ${it.message}")
            }
        getResportesUsuarioActual()

    }


}