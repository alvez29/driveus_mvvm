package com.example.driveus_mvvm.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PayoutViewModel : ViewModel() {

    private val tag = "FIRESTORE_PAYOUTS_VIEW_MODEL"

    private val payoutsFromRide: MutableLiveData<Map<String, QueryDocumentSnapshot>> = MutableLiveData(emptyMap())

    fun getPayoutsListFromRide(channelId: String, rideId: String): MutableLiveData<Map<String, QueryDocumentSnapshot>> {
            FirestoreRepository.getPayoutsFromRide(channelId, rideId).addSnapshotListener { collectionSnap, error ->
                viewModelScope.launch(Dispatchers.IO) {
                if (error != null) {
                    payoutsFromRide.value = emptyMap()
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
}

