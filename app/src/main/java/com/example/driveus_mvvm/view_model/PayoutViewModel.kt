package com.example.driveus_mvvm.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.model.entities.Payout
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PayoutViewModel : ViewModel() {

    private val tag = "FIRESTORE_PAYOUTS_VIEW_MODEL"

    private val payoutsFromRide: MutableLiveData<Map<String, QueryDocumentSnapshot>> = MutableLiveData(emptyMap())
    private val pendingPayoutsAsDriverFromUser: MutableLiveData<List<Pair<String, DocumentSnapshot>>> = MutableLiveData(emptyList())
    private val pendingPayoutsAsPassengerFromUser: MutableLiveData<List<Pair<String, DocumentSnapshot>>> = MutableLiveData(emptyList())


    fun getPayoutsListFromRide(channelId: String, rideId: String): LiveData<Map<String, QueryDocumentSnapshot>> {
            FirestoreRepository.getPayoutsFromRide(channelId, rideId).addSnapshotListener { collectionSnap, error ->
                viewModelScope.launch(Dispatchers.IO) {
                if (error != null) {
                    payoutsFromRide.value = emptyMap()
                    Log.d(tag, error.message.toString())
                }

                val auxMap = mutableMapOf<String, QueryDocumentSnapshot>()

                collectionSnap?.forEach {
                    val user = it.getDocumentReference("passenger")?.id?.let { it1 -> FirestoreRepository.getUserByIdSync(it1) }
                    val username = user?.getString("username").toString()

                    auxMap[username] = it
                }

                payoutsFromRide.postValue(auxMap)
            }
        }

        return payoutsFromRide
    }

    fun getPendingPayoutsAsPassengerFromUser(userId: String): LiveData<List<Pair<String, DocumentSnapshot>>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { value, error ->
            viewModelScope.launch(Dispatchers.IO) {
                if (error != null) {
                    pendingPayoutsAsPassengerFromUser.value = emptyList()
                    Log.d(tag, error.message.toString())
                }

                val auxList = mutableListOf<Pair<String, DocumentSnapshot>>()
                (value?.get("payoutsAsPassenger") as? List<DocumentReference>)?.forEach {
                    val payout = it.get().await()
                    val passengerUsername = (payout.get("passenger") as DocumentReference).get().await().get("username").toString()
                    val pairAux = Pair<String, DocumentSnapshot>(passengerUsername, payout)

                    auxList.add(pairAux)
                }

                pendingPayoutsAsPassengerFromUser.postValue(auxList)
            }
        }

        return pendingPayoutsAsPassengerFromUser
    }

    fun getPendingPayoutsAsDriverFromUser(userId: String): LiveData<List<Pair<String, DocumentSnapshot>>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { value, error ->
            viewModelScope.launch(Dispatchers.IO) {
                if (error != null) {
                    pendingPayoutsAsDriverFromUser.value = emptyList()
                    Log.d(tag, error.message.toString())
                }

                val auxlist = mutableListOf<Pair<String, DocumentSnapshot>>()
                (value?.get("payoutsAsDriver") as? List<DocumentReference>)?.forEach {
                    val payout = it.get().await()
                    val passengerUsername = (payout.get("passenger") as DocumentReference).get().await().get("username").toString()
                    val pairAux = Pair<String, DocumentSnapshot>(passengerUsername, payout)

                    auxlist.add(pairAux)
                }

                pendingPayoutsAsDriverFromUser.postValue(auxlist)
            }
        }

        return pendingPayoutsAsDriverFromUser
    }

    fun checkPayoutAsPaid(channelId: String, rideId: String, payoutDocRef: DocumentReference, passenger: DocumentReference) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val driver: DocumentReference = FirestoreRepository.getRideByIdSync(channelId, rideId).get("driver") as DocumentReference
                FirestoreRepository.checkPayoutAsPaidUpdateBoolean(channelId, rideId, payoutDocRef.id)
                FirestoreRepository.checkPayoutAsPaidUpdatePaidDate(channelId, rideId, payoutDocRef.id, Timestamp.now())
                FirestoreRepository.deletePayoutFromPassenger(passenger.id, payoutDocRef)
                FirestoreRepository.deletePayoutFromDriver(driver.id, payoutDocRef)
            }catch (e: Exception) {
                Log.d(tag, e.message.toString())
            }
        }
    }

    fun checkPayoutAsUnpaid(channelId: String, rideId: String, payoutDocRef: DocumentReference, driverId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val passenger: DocumentReference =
                    FirestoreRepository.getPayoutById(channelId, rideId, payoutDocRef.id)?.get("passenger") as DocumentReference
                FirestoreRepository.checkPayoutAsUnpaidUpdateBoolean(channelId, rideId, payoutDocRef.id)
                FirestoreRepository.checkPayoutAsUnpaidUpdatePaidDate(channelId, rideId, payoutDocRef.id)
                FirestoreRepository.addPayoutFromPassenger(passenger.id, payoutDocRef)
                if (driverId != null) {
                    FirestoreRepository.addPayoutFromDriver(driverId, payoutDocRef)
                }
            }catch (e: Exception) {
                Log.d(tag, e.message.toString())
            }
        }
    }
}

