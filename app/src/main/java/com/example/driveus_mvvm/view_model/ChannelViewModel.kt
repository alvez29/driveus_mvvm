package com.example.driveus_mvvm.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ChannelViewModel : ViewModel() {

    private val tag = "FIRESTORE_CHANNEL_VIEW_MODEL"

    //Mapa con clave id de los documentos y valor el propio Channel
    private val allChannels: MutableLiveData<Map<String, Channel>> = MutableLiveData(mutableMapOf())
    private val userChannels: MutableLiveData<Map<String, Channel>> = MutableLiveData(mutableMapOf())
    private val channelRides: MutableLiveData<Map<String, Ride>> = MutableLiveData(mutableMapOf())
    private val channelById: MutableLiveData<Channel> = MutableLiveData()
    private val hasAnyRide = MutableLiveData(true)

    fun hasAnyRide(): LiveData<Boolean> {
        return hasAnyRide
    }

    fun getChannelById(channelDocId: String) : LiveData<Channel> {
        FirestoreRepository.getChannelById(channelDocId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(tag, "Listen failed.", error)
                channelById.postValue(Channel())
            }

            value?.let {
                channelById.postValue(it.toObject(Channel::class.java))
            }
        }

        return channelById
    }

    fun getAllChannels() : LiveData<Map<String, Channel>> {
        FirestoreRepository.getAllChannels()
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w(tag, "Listen failed.", error)
                    allChannels.postValue(mutableMapOf())
                }

                val resMap = mutableMapOf<String, Channel>()

                value?.forEach {
                    resMap[it.id] = it.toObject(Channel::class.java)
                }

                allChannels.postValue(resMap)
            }

        return allChannels
    }

    fun getUserChannels(userId: String): LiveData<Map<String, Channel>> {
        FirestoreRepository.getUserById(userId).addSnapshotListener { value, _ ->
            val subscribedChannels = value?.get("channels") as? List<DocumentReference>

            val resMap = mutableMapOf<String, Channel>()

            if (subscribedChannels?.isEmpty() == true) {
                userChannels.postValue(resMap)
            } else {
                subscribedChannels?.forEach { channelReference ->
                    FirestoreRepository.getChannelById(channelReference.id).addSnapshotListener { value, error ->

                        if (error != null) {
                            Log.w(tag, "Listen failed.", error)
                            userChannels.postValue(mutableMapOf())
                        }

                        value?.let {
                            val channelAux: Channel? = value.toObject(Channel::class.java)
                            channelAux?.let {
                                resMap[value.id] = it
                            }

                            userChannels.postValue(resMap)
                        }
                    }
                }
            }

        }

        return userChannels
    }

    fun getRidesFromChannel(channelDocId: String): LiveData<Map<String, Ride>> {
        FirestoreRepository.getRidesFromChannel(channelDocId).addSnapshotListener { value, error ->
            if (error != null) {
                Log.w(tag, "Listen failed.", error)
                channelRides.postValue(emptyMap())
            }

            val resMap = mutableMapOf<String, Ride>()

            if (value?.documents?.isEmpty() == false) {
                value.documents.forEach { document ->
                    val ride: Ride? = document.toObject(Ride::class.java)
                    ride?.let {
                        resMap[document.id] = it
                    }
                }
            }
            channelRides.postValue(resMap)
        }

        return channelRides
    }

    fun subscribeToChannel(userId: String, channelDocId: String) {
        FirestoreRepository.getUserById(userId).get().addOnSuccessListener { document ->
            val userReference = document.reference

            FirestoreRepository.getChannelById(channelDocId).get().addOnSuccessListener { channelDoc ->
                viewModelScope.launch {
                    FirestoreRepository.subscribeToChannelUserSide(userId, channelDoc.reference)
                    FirestoreRepository.subscribeToChannelChannelSide(channelDoc.id, userReference)
                }
            }
        }
    }

    fun unsubscribeToChannel(userId: String, channelDocId: String) {
        FirestoreRepository.getUserById(userId).get().addOnSuccessListener { document ->
            val userReference = document.reference

            FirestoreRepository.getChannelById(channelDocId).get().addOnSuccessListener { channelDoc ->
                viewModelScope.launch {
                    FirestoreRepository.unsubscribeToChannelChannelSide(channelDoc.id, userReference)
                    FirestoreRepository.unsubscribeToChannelUserSide(userId, channelDoc.reference)
                }
            }
        }
    }
}
