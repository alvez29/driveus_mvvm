package com.example.driveus_mvvm.view_model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.enums.RideFormEnum
import com.example.driveus_mvvm.ui.utils.DateTimeUtils
import com.example.driveus_mvvm.ui.utils.LocationUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

class RideViewModel : ViewModel() {

    private val tag = "FIRESTORE_RIDE_VIEW_MODEL"

    private val ridesAsPassenger: MutableLiveData<Map<String, Ride>> = MutableLiveData(mutableMapOf())
    private val ridesAsDriver: MutableLiveData<Map<String, Ride>> = MutableLiveData(mutableMapOf())
    private var meetingGeoPoint: GeoPoint? = null

    private val hasRidesAsDriver = MutableLiveData(false)
    private val hasRidesAsPassenger = MutableLiveData(false)

    private  val redirectRide = MutableLiveData(false)
    private val rideFormError = MutableLiveData<MutableMap<RideFormEnum, Int>>(mutableMapOf())

    private fun  validateRideForm(textInputs: Map<RideFormEnum, String>, context: Context): Boolean {
        val errorMap = mutableMapOf<RideFormEnum, Int>()
        var res = true

        //Capacity
        if (textInputs[RideFormEnum.CAPACITY].isNullOrBlank()){
            errorMap[RideFormEnum.CAPACITY] = R.string.sign_up_form_error_not_empty
            res = false
        }

        //Price
        if (textInputs[RideFormEnum.PRICE].isNullOrBlank()){
            errorMap[RideFormEnum.PRICE] = R.string.sign_up_form_error_not_empty
            res = false
        }else {
            val price = textInputs[RideFormEnum.PRICE]?.toDouble()
            val oneHundred = BigDecimal(100)
            val priceDecimals = price?.toBigDecimal()?.remainder(BigDecimal.ONE)?.times(oneHundred)
            if (priceDecimals?.rem(BigDecimal.ONE)?.toDouble()?.equals(0.0) == false) {
                errorMap[RideFormEnum.PRICE] = R.string.ride_form_error_price_decimals
                res = false
            }
        }
        //Date and Time
        if (textInputs[RideFormEnum.DATETIME].isNullOrBlank()){
            errorMap[RideFormEnum.DATETIME] = R.string.sign_up_form_error_not_empty
            res = false
        } else {
            val dateTimeStr = textInputs[RideFormEnum.DATETIME]
            val dateTimeTS: Timestamp? = dateTimeStr?.let { DateTimeUtils.dateStringToTimestamp(it) }

            if (dateTimeTS?.toDate()?.before(Timestamp.now().toDate()) == true){
                errorMap[RideFormEnum.DATETIME] = R.string.ride_form_error_date_time_past
                res = false
            }
        }

        //Meeting Point
        if (textInputs[RideFormEnum.MEETING_POINT].isNullOrBlank()) {
            errorMap[RideFormEnum.MEETING_POINT] = R.string.sign_up_form_error_not_empty
            res = false
        } else {
            meetingGeoPoint = LocationUtils.getLocation(textInputs[RideFormEnum.MEETING_POINT], context)
            if (meetingGeoPoint?.equals(GeoPoint(0.0, 0.0)) == true) {
                errorMap[RideFormEnum.MEETING_POINT] = R.string.ride_form_error_location_not_found
                res = false
            }
        }

        rideFormError.postValue(errorMap)
        return res
    }

    private fun getRideFromInputs(inputs: Map<RideFormEnum, String>, userId: DocumentReference, vehicleDocRef: DocumentReference): Ride? {

        return meetingGeoPoint?.let {
            Ride(
                capacity = inputs[RideFormEnum.CAPACITY]?.toInt(),
                price = inputs[RideFormEnum.PRICE]?.toDouble(),
                date =  inputs[RideFormEnum.DATETIME]?.let { DateTimeUtils.dateStringToTimestamp(it) },
                driver = userId,
                meetingPoint = it,
                vehicle = vehicleDocRef
            )
        }

    }

    fun hasRidesAsPassenger(): MutableLiveData<Boolean> {
        return hasRidesAsPassenger
    }

    fun hasRidesAsDriver(): MutableLiveData<Boolean> {
        return hasRidesAsDriver
    }


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
                    if (resMap.isNotEmpty()) {
                        hasRidesAsPassenger.postValue(true)
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
                    if (resMap.isNotEmpty()) {
                        hasRidesAsDriver.postValue(true)
                    }
                    ridesAsDriver.postValue(resMap)
                }
            }
        }
        return ridesAsDriver
    }

    fun getRedirectRide(): LiveData<Boolean> {
        return redirectRide
    }

    fun getRideFormErrors(): LiveData<MutableMap<RideFormEnum, Int>> {
        return rideFormError
    }


    fun addNewRide(inputs: Map<RideFormEnum, String>, userId: String, vehicleId: String, channelId: String, context: Context) {
        if (validateRideForm(inputs, context)) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val userDocumentId = FirestoreRepository.getUserById(userId)
                    val vehicleDocRef = FirestoreRepository.getVehicleById(userId, vehicleId)
                    getRideFromInputs(inputs, userDocumentId, vehicleDocRef)?.let {
                        FirestoreRepository.addNewRide(it, channelId)
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Listen failed.", e)
                }
            }
            redirectRide.postValue(true)
        }
    }
}