package com.example.uberapp.providers

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.GeoQuery

class GeoProvider {

    val collection = FirebaseFirestore.getInstance().collection("Locations")
    // se crea la colección locations en firebase
    val geoFirestore = GeoFirestore(collection)

    fun saveLocation(idDriver: String, position: LatLng) {
        geoFirestore.setLocation(idDriver, GeoPoint(position.latitude, position.longitude))
    }

    // obtener los conductores a través de un radio de búsqueda
    fun getNearbyDrivers(position: LatLng, radius: Double): GeoQuery{
        val query = geoFirestore.queryAtLocation(GeoPoint(position.latitude, position.longitude), radius)
        // la posición de los conductores se va a estar actualizando constantemente por lo que tiene que
        // ir eliminando esos escuchadores
        query.removeAllListeners()
        return query
    }

    // elimina la licalización
    fun removeLocation(idDriver: String) {
        collection.document(idDriver).delete()
    }
    // verifica si ya había una localización en curso
    fun getLocation(idDriver: String): Task<DocumentSnapshot> {
        return collection.document(idDriver).get().addOnFailureListener { exception ->
            Log.d("FIREBASE", "ERROR: ${exception.toString()}")
        }
    }

}