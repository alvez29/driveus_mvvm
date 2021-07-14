package com.example.driveus_mvvm.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RideViewModel : ViewModel() {

    private val tag = "FIRESTORE_RIDE_VIEW_MODEL"

    private val ridesAsPassenger: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    private val ridesAsDriver: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    private val rideById: MutableLiveData<Ride> = MutableLiveData(null)
    private val passengersList: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    private val meetingPoint: MutableLiveData<GeoPoint> = MutableLiveData(GeoPoint(0.0, 0.0))

    //TODO:Actualizar meeting point
    fun getMeetingPoint(): LiveData<GeoPoint> {
        return meetingPoint
    }

    fun getComingRidesAsPassenger(userId: String): LiveData<List<DocumentSnapshot>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsPassenger.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsPassenger?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsPassenger.value = emptyList()
                    }

                    //TODO: Contemplar que el parametro de filtrar se encuentre en el usuario y no realizar el filtro en cliente
                    // El filtro controla el estado del mapa actual
                    if ((rideDoc?.get("date") as? Timestamp)?.toDate()?.after(Timestamp.now().toDate()) == true) {
                        resList.add(rideDoc)
                    } else if (rideDoc?.getTimestamp("date")?.toDate()?.after(Timestamp.now().toDate()) == false
                            && resList.map { it.id }.contains(rideDoc.id) ) {
                        resList.remove(rideDoc)
                    }

                    ridesAsPassenger.postValue(resList)
                }
            }
        }

        return ridesAsPassenger
    }

    fun getComingRidesAsDriver(userId: String): LiveData<List<DocumentSnapshot>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsDriver.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsDriver?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsDriver.value = mutableListOf()
                    }

                    //TODO: Contemplar que el parametro de filtrar se encuentre en el usuario y no realizar el filtro en cliente
                    // El filtro controla el estado del mapa actual
                    if ((rideDoc?.get("date") as? Timestamp)?.toDate()?.after(Timestamp.now().toDate()) == true) {
                        resList.add(rideDoc)
                    } else if (rideDoc?.getTimestamp("date")?.toDate()?.after(Timestamp.now().toDate()) == false
                            && resList.map { it.id }.contains(rideDoc.id) ) {
                        resList.remove(rideDoc)
                    }

                    ridesAsDriver.postValue(resList)
                }
            }


        }

        return ridesAsDriver
    }

    fun getRidesAsPassenger(userId: String): LiveData<List<DocumentSnapshot>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsPassenger.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsPassenger?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsPassenger.value = emptyList()
                    }

                    rideDoc?.let {
                        resList.add(rideDoc)
                    }

                    ridesAsPassenger.postValue(resList)
                }
            }
        }
        return ridesAsPassenger
    }

    fun getRidesAsDriver(userId: String): LiveData<List<DocumentSnapshot>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsDriver.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsDriver?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsDriver.value = emptyList()
                    }

                    rideDoc?.let {
                        resList.add(rideDoc)
                    }

                    ridesAsDriver.postValue(resList)
                }
            }
        }
        return ridesAsDriver
    }

    fun getRideById(channelId: String?, rideId: String?): LiveData<Ride> {
        rideId?.let {
            channelId?.let {
                FirestoreRepository.getRideById(channelId, rideId).addSnapshotListener { value, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        rideById.value = null
                        meetingPoint.value = GeoPoint(0.0,0.0)
                    }

                    val ride: Ride? = value?.toObject(Ride::class.java)

                    ride?.let {
                        rideById.postValue(ride)
                        meetingPoint.postValue(it.meetingPoint)
                    }
                }
            }
        }

        return rideById
    }

    fun getPassengersList(channelId: String, rideId: String) : MutableLiveData<List<DocumentSnapshot>> {
        FirestoreRepository.getRideById(channelId, rideId).addSnapshotListener { value, _ ->
            val passengersAux = value?.get("passengers") as? List<DocumentReference>

            viewModelScope.launch(Dispatchers.IO) {
                val resList = mutableListOf<DocumentSnapshot>()

                passengersAux?.forEach {
                    val passenger = FirestoreRepository.getUserByIdSync(it.id)

                    if (passenger != null) {
                        resList.add(passenger)
                    }
                }
                
                passengersList.postValue(resList)
            }
        }

        return passengersList
    }

}