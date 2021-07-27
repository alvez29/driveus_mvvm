package com.example.driveus_mvvm.view_model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.Payout
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.enums.RideFormEnum
import com.example.driveus_mvvm.ui.utils.DateTimeUtils
import com.example.driveus_mvvm.ui.utils.LocationUtils
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.util.*

class RideViewModel : ViewModel() {

    private val tag = "FIRESTORE_RIDE_VIEW_MODEL"
    private val msgListenFailed = "Listen failed."
    private val opFailed = "Operation Failed"

    private val ridesAsPassenger: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    private val ridesAsDriver: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    private val rideById: MutableLiveData<Ride> = MutableLiveData(null)
    private val passengersList: MutableLiveData<List<DocumentSnapshot>> = MutableLiveData(emptyList())
    private val meetingPoint: MutableLiveData<GeoPoint> = MutableLiveData(GeoPoint(0.0, 0.0))
    private var meetingGeoPoint: GeoPoint? = null

    private val redirectRide = MutableLiveData(false)
    private val rideFormError = MutableLiveData<MutableMap<RideFormEnum, Int>>(mutableMapOf())


    private fun  validateRideForm(textInputs: Map<RideFormEnum, String>, context: Context, seats: Int): Boolean {
        val errorMap = mutableMapOf<RideFormEnum, Int>()
        var res = true

        //Capacity
        if (textInputs[RideFormEnum.CAPACITY].isNullOrBlank()){
            errorMap[RideFormEnum.CAPACITY] = R.string.sign_up_form_error_not_empty
            res = false
        } else if (compareValues(textInputs[RideFormEnum.CAPACITY]?.toInt(), seats) >= 0) {
            errorMap[RideFormEnum.CAPACITY] = R.string.ride_form_error_max_capacity
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

        if (textInputs[RideFormEnum.REPEAT].equals("true")){
            //Day of week repeat
            if (textInputs[RideFormEnum.DAYS_OF_THE_WEEK] == "[]") {
                errorMap[RideFormEnum.DAYS_OF_THE_WEEK] = R.string.ride_form_error_no_days_of_week
                res = false
            }

            //Time of repeat rides
            if (textInputs[RideFormEnum.TIME_REPEAT].isNullOrBlank()){
                errorMap[RideFormEnum.TIME_REPEAT] = R.string.sign_up_form_error_not_empty
                res = false
            }

            //Date limit
            if (textInputs[RideFormEnum.DAY_LIMIT].isNullOrBlank()) {
                errorMap[RideFormEnum.DAY_LIMIT] = R.string.sign_up_form_error_not_empty
                res = false
            } else {
                val dateStr = textInputs[RideFormEnum.DAY_LIMIT]
                val dateTS: Date? = dateStr?.let { DateTimeUtils.dateStringToDate(it) }
                val limit = Calendar.getInstance().apply {
                    this.add(Calendar.DAY_OF_MONTH, 120)
                }

                //Limit no puede estar en el pasado
                if (dateTS?.before(Timestamp.now().toDate()) == true){
                    errorMap[RideFormEnum.DAY_LIMIT] = R.string.ride_form_error_date_time_past
                    res = false

                    //Maximo 4 meses de límite
                } else if (compareValues(limit.time, dateTS) < 0) {
                    errorMap[RideFormEnum.DAY_LIMIT] = R.string.ride_form_error_limit_4_months
                    res = false
                }
            }
        } else {
            //Date and Time
            if (textInputs[RideFormEnum.DATETIME].isNullOrBlank()){
                errorMap[RideFormEnum.DATETIME] = R.string.sign_up_form_error_not_empty
                res = false
            } else {
                val dateTimeStr = textInputs[RideFormEnum.DATETIME]
                val dateTimeTS: Timestamp? = dateTimeStr?.let { DateTimeUtils.dateTimeStringToTimestamp(it) }

                if (dateTimeTS?.toDate()?.before(Timestamp.now().toDate()) == true){
                    errorMap[RideFormEnum.DATETIME] = R.string.ride_form_error_date_time_past
                    res = false
                }
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
                date =  inputs[RideFormEnum.DATETIME]?.let { DateTimeUtils.dateTimeStringToTimestamp(it) },
                driver = userId,
                meetingPoint = it,
                vehicle = vehicleDocRef
            )
        }

    }

    private fun createSimplePayout(passengerDocRef: DocumentReference, price: Double): Payout {
        return Payout(
                    creationDate = Timestamp.now(),
                    paidDate = null,
                    passenger = passengerDocRef,
                    price = price,
                    isPaid = false
        )
    }

    //TODO:Actualizar meeting point
    fun getMeetingPoint(): LiveData<GeoPoint> {
        return meetingPoint
    }

    fun getComingRidesAsPassenger(userId: String): LiveData<List<DocumentSnapshot>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { user, error ->
            if ( error != null) {
                Log.w(tag, msgListenFailed, error)
                ridesAsPassenger.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsPassenger?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, msgListenFailed, error)
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
                Log.w(tag, msgListenFailed, error)
                ridesAsDriver.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsDriver?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, msgListenFailed, error)
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
                Log.w(tag, msgListenFailed, error)
                ridesAsPassenger.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsPassenger?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, msgListenFailed, error)
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
                Log.w(tag, msgListenFailed, error)
                ridesAsDriver.value = emptyList()
            }

            val userObject = user?.toObject(User::class.java)
            val resList = mutableListOf<DocumentSnapshot>()

            userObject?.ridesAsDriver?.forEach { docReference ->
                docReference?.addSnapshotListener { rideDoc, error ->
                    if ( error != null) {
                        Log.w(tag, msgListenFailed, error)
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
                        Log.w(tag, msgListenFailed, error)
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

    fun getRedirectRide(): LiveData<Boolean> {
        return redirectRide
    }

    fun getRideFormErrors(): LiveData<MutableMap<RideFormEnum, Int>> {
        return rideFormError
    }

    private suspend fun addSingleRide(userId: String, vehicleDocRef: DocumentReference, inputs: Map<RideFormEnum, String>, channelId: String) {
        val userDocumentId = FirestoreRepository.getUserById(userId)
        var rideDocRef: DocumentReference? = null

        vehicleDocRef.id.let { FirestoreRepository.updateVehicleIsInRide(userId, it) }

        getRideFromInputs(inputs, userDocumentId, vehicleDocRef)?.let {
            rideDocRef = FirestoreRepository.addNewRide(it, channelId).await()
        }
        FirestoreRepository.addRideToUserAsDriver(userId, rideDocRef)
    }

    private suspend fun addRepeatableRides(userId: String, vehicleDocRef: DocumentReference, inputs: Map<RideFormEnum, String>, channelId: String) {
        val eachRideInput = inputs as MutableMap<RideFormEnum, String>
        val limit = inputs[RideFormEnum.DAY_LIMIT]?.let { DateTimeUtils.dateStringToDate(it) }
        val aDay = Calendar.getInstance()
        val listDayOfWeek = inputs[RideFormEnum.DAYS_OF_THE_WEEK]?.let { DateTimeUtils.strToListIntDaysOfTheWeek(it) }
        while (compareValues(limit, aDay.time) >= 0) {
            if (listDayOfWeek?.contains(aDay.get(Calendar.DAY_OF_WEEK)) == true){
                eachRideInput[RideFormEnum.DATETIME] = "${aDay.get(Calendar.DAY_OF_MONTH)}/${aDay.get(Calendar.MONTH)+1}/${aDay.get(Calendar.YEAR)} ${inputs[RideFormEnum.TIME_REPEAT]}"
                addSingleRide(userId, vehicleDocRef, eachRideInput, channelId)
            }
           aDay.add(Calendar.DAY_OF_MONTH, 1)
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun addNewRide(inputs: Map<RideFormEnum, String>, userId: String, vehicleId: String, channelId: String, context: Context) {
        //OJO: Scope de toda la aplicación. No usar a la ligera
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val vehicleDocRef = FirestoreRepository.getVehicleById(vehicleId,userId)
                val seats: Int? = vehicleDocRef.get().await().getDouble("seats")?.toInt()

                if (seats?.let { validateRideForm(inputs, context, it) } == true) {
                    redirectRide.postValue(true)
                    if (inputs[RideFormEnum.REPEAT] == "true"){
                        addRepeatableRides(userId, vehicleDocRef,inputs,channelId)
                    } else {
                        addSingleRide(userId, vehicleDocRef, inputs, channelId)
                    }
                 }
            } catch (e: Exception) {
                Log.w(tag, msgListenFailed, e)
            }
        }
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


    fun addPassengerInARide(channelId: String, rideId: String, passengerId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val passengerDocRef: DocumentReference? = FirestoreRepository.getUserByIdSync(passengerId)?.reference
                if (passengerDocRef != null) {
                    FirestoreRepository.addPassengerInARide(channelId, rideId, passengerDocRef)
                }
                val rideReference = FirestoreRepository.getRideByIdSync(channelId, rideId)
                val ride = rideReference.toObject(Ride::class.java)
                FirestoreRepository.addRideInAPassenger(passengerId, rideReference.reference)
                val payout = passengerDocRef?.let { passengerDocumentReference ->
                    ride?.price?.let { price ->
                        createSimplePayout(passengerDocumentReference, price)
                    }
                }
                if (payout != null) {
                    val payoutDocRef: DocumentReference = FirestoreRepository.addSimplePayout(channelId, rideId, payout).await()
                    FirestoreRepository.addPayoutInAPassenger(passengerId, payoutDocRef)
                    ride?.driver?.id?.let { FirestoreRepository.addPayoutInADriver(it, payoutDocRef) }
                }
            } catch (e: Exception) {
                Log.w(tag, opFailed, e)
            }
        }
    }

    fun removePassengerInARide(channelId: String, rideId: String, passengerId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val passengerDocRef: DocumentReference? = FirestoreRepository.getUserByIdSync(passengerId)?.reference
                if (passengerDocRef != null) {
                    FirestoreRepository.removePassengerInARide(channelId, rideId, passengerDocRef)
                }
                val rideReference = FirestoreRepository.getRideByIdSync(channelId, rideId)
                FirestoreRepository.removeRideInAPassenger(passengerId, rideReference.reference)

                val payoutDocRef: DocumentReference? = passengerDocRef?.let {
                    FirestoreRepository.getPayoutByUserAndRideSync(channelId, rideId, it)?.documents?.get(0)?.reference
                }
                if (payoutDocRef != null) {
                    val ride: Ride? = FirestoreRepository.getRideById(channelId, rideId).get().await().toObject(Ride::class.java)
                    val hasRidePaid: Boolean = FirestoreRepository.getPayoutById(channelId, rideId, payoutDocRef.id)?.get("isPaid") as Boolean
                    FirestoreRepository.deletePayoutFromPassenger(passengerId, payoutDocRef)
                    ride?.driver?.id?.let { FirestoreRepository.deletePayoutFromDriver(it, payoutDocRef) }
                    if (hasRidePaid) {
                        ride?.driver?.id?.let { FirestoreRepository.addDebtInADriver(it, payoutDocRef) }
                        FirestoreRepository.addDebtInAPassenger(passengerId, payoutDocRef)
                        FirestoreRepository.checkPayoutAsUnpaidUpdateBoolean(channelId,rideId, payoutDocRef.id)
                        FirestoreRepository.payoutToDebt(channelId,rideId, payoutDocRef.id)
                    } else {
                        FirestoreRepository.deleteSimplePayout(channelId, rideId, payoutDocRef.id)
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, opFailed, e)
            }
        }
    }
}

