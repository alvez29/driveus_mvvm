package com.example.driveus_mvvm.model.repository


import androidx.annotation.WorkerThread
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.entities.Vehicle
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

object FirestoreRepository {

    private const val CHANNELS_COLLECTION = "channel"
    private const val USERS_COLLECTION = "users"
    private const val RIDES_COLLECTION = "rides"
    private const val VEHICLES_COLLECTION = "vehicles"
    
    private val db by lazy { FirebaseFirestore.getInstance() }

    //USER FUNCTIONS -----------------------------------------------------

    fun getUserById(userId: String) : DocumentReference {
        return db.collection(USERS_COLLECTION).document(userId)
    }

    fun getUserByUID(uid: String): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("uid", uid)
    }

    fun usernameInUse(username: String): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("username", username)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun updateIsDriver(userId: String, isDriver: Boolean) {
        db.collection("users").document(userId)
            .update(mapOf("isDriver" to isDriver))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addRideToUserAsDriver(userId: String, channelDocId: DocumentReference) {
        db.collection(USERS_COLLECTION).document(userId)
            .collection(RIDES_COLLECTION).add(channelDocId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun createUser(user: User){
        db.collection(USERS_COLLECTION).add(user)
    }
    
    //CHANNEL FUNCTIONS --------------------------------------------------

    fun getChannelById(docId: String): DocumentReference {
        return db.collection(CHANNELS_COLLECTION).document(docId)
    }

    fun getAllChannels(): CollectionReference {
        return db.collection(CHANNELS_COLLECTION)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun subscribeToChannelUserSide(userDocId: String, channelReference: DocumentReference){
        db.collection(USERS_COLLECTION).document(userDocId)
            .update("channels", FieldValue.arrayUnion(channelReference))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun unsubscribeToChannelUserSide(userDocId: String, channelReference: DocumentReference){
        db.collection(USERS_COLLECTION).document(userDocId)
            .update("channels", FieldValue.arrayRemove(channelReference))
    }
    
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
     suspend fun subscribeToChannelChannelSide(channelDocId: String, userReference: DocumentReference){
        db.collection(CHANNELS_COLLECTION).document(channelDocId)
                .update("users", FieldValue.arrayUnion(userReference))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun unsubscribeToChannelChannelSide(channelDocId: String, userReference: DocumentReference){
        db.collection(CHANNELS_COLLECTION).document(channelDocId)
                .update("users", FieldValue.arrayRemove(userReference))
    }

    
    //VEHICLE FUNCTIONS -----------------------------------------------------

    fun getVehicleById(userId: String, vehicleId: String): DocumentReference {
        return db.collection(USERS_COLLECTION).document(userId).collection(VEHICLES_COLLECTION).document(vehicleId)
    }

    fun getAllVehiclesByUserId(id: String): CollectionReference {
        return db.collection("users").document(id).collection("vehicles")
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun deleteVehicleById(userID: String, vehicleId: String) {
        db.collection("users").document(userID).collection("vehicles").document(vehicleId).delete()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addVehicle(vehicle: Vehicle, userId: String) {
        db.collection("users").document(userId).collection("vehicles").add(vehicle)
    }
   
    //RIDES FUNCTIONS ----------------------------------------------------

    fun getRidesFromChannel(channelDocId: String) : Query {
        return db.collection(CHANNELS_COLLECTION).document(channelDocId)
                .collection(RIDES_COLLECTION).whereGreaterThan("date", Timestamp.now())
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addNewRide(ride: Ride, channelDocId: String): DocumentReference? {
        return db.collection(CHANNELS_COLLECTION).document(channelDocId)
            .collection(RIDES_COLLECTION).add(ride).await()
    }

}
