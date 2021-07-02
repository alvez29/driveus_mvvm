package com.example.driveus_mvvm.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.Timestamp
import java.sql.Time

class RideViewModel : ViewModel() {

    private val tag = "FIRESTORE_RIDE_VIEW_MODEL"

    private val ridesAsPassenger: MutableLiveData<Map<String, Ride>> = MutableLiveData(mutableMapOf())
    private val ridesAsDriver: MutableLiveData<Map<String, Ride>> = MutableLiveData(mutableMapOf())

    fun getComingRidesAsPassenger(userId: String): MutableLiveData<Map<String, Ride>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsPassenger.value = mapOf()
            }

            val userObject = user?.toObject(User::class.java)
            val resMap = mutableMapOf<String, Ride>()

            userObject?.ridesAsPassenger?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsPassenger.value = mapOf()
                    }

                    val ride: Ride? = rideDoc?.toObject(Ride::class.java)

                    ride?.let {
                        //TODO: Contemplar que el parametro de filtrar se encuentre en el usuario y no realizar el filtro en cliente
                        // El filtro controla el estado del mapa actual
                        if (ride.date?.toDate()?.after(Timestamp.now().toDate()) == true) {
                            resMap[rideDoc.id] = it
                        } else if (ride.date?.toDate()?.after(Timestamp.now().toDate()) == false && resMap.containsKey(rideDoc.id) ) {
                            resMap.remove(rideDoc.id)
                        }

                    }

                    ridesAsPassenger.postValue(resMap)
                }
            }
        }

        return ridesAsPassenger
    }

    fun getComingRidesAsDriver(userId: String): MutableLiveData<Map<String, Ride>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsDriver.value = mapOf()
            }

            val userObject = user?.toObject(User::class.java)
            val resMap = mutableMapOf<String, Ride>()

            userObject?.ridesAsDriver?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsDriver.value = mapOf()
                    }

                    val ride: Ride? = rideDoc?.toObject(Ride::class.java)

                    ride?.let {
                        //TODO: Contemplar que el parametro de filtrar se encuentre en el usuario y no realizar el filtro en cliente
                        // El filtro controla el estado del mapa actual
                        if (ride.date?.toDate()?.after(Timestamp.now().toDate()) == true) {
                            resMap[rideDoc.id] = it
                        } else if (ride.date?.toDate()?.after(Timestamp.now().toDate()) == false && resMap.containsKey(rideDoc.id) ) {
                            resMap.remove(rideDoc.id)
                        }

                    }

                    ridesAsDriver.postValue(resMap)
                }
            }


        }

        return ridesAsDriver
    }

    fun getRidesAsPassenger(userId: String): MutableLiveData<Map<String, Ride>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsPassenger.value = mapOf()
            }

            val userObject = user?.toObject(User::class.java)
            val resMap = mutableMapOf<String, Ride>()

            userObject?.ridesAsPassenger?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsPassenger.value = mapOf()
                    }

                    val ride: Ride? = rideDoc?.toObject(Ride::class.java)

                    ride?.let {
                        resMap[rideDoc.id] = it
                    }
                    ridesAsPassenger.postValue(resMap)
                }
            }
        }
        return ridesAsPassenger
    }

    fun getRidesAsDriver(userId: String): MutableLiveData<Map<String, Ride>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, "Listen failed.", error)
                ridesAsDriver.value = mapOf()
            }

            val userObject = user?.toObject(User::class.java)
            val resMap = mutableMapOf<String, Ride>()

            userObject?.ridesAsDriver?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, "Listen failed.", error)
                        ridesAsDriver.value = mapOf()
                    }

                    val ride: Ride? = rideDoc?.toObject(Ride::class.java)

                    ride?.let {
                            resMap[rideDoc.id] = it
                    }
                    ridesAsDriver.postValue(resMap)
                }
            }
        }
        return ridesAsDriver
    }

}