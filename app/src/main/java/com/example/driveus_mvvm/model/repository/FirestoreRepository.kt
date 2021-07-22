package com.example.driveus_mvvm.model.repository


import androidx.annotation.WorkerThread
import com.example.driveus_mvvm.model.entities.Payout
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.entities.Vehicle
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

object FirestoreRepository {

    private const val CHANNELS_COLLECTION = "channel"
    private const val USERS_COLLECTION = "users"
    private const val RIDES_COLLECTION = "rides"
    private const val VEHICLES_COLLECTION = "vehicles"
    private const val PAYOUTS_COLLECTION = "payouts"

    private val db by lazy { FirebaseFirestore.getInstance() }

    //USER FUNCTIONS -----------------------------------------------------


    fun getUserById(userId: String) : DocumentReference {
        return db.collection(USERS_COLLECTION).document(userId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getUserByIdSync(userId: String) : DocumentSnapshot? {
        return db.collection(USERS_COLLECTION).document(userId).get().await()
    }

    fun getUserByUID(uid: String): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("uid", uid)
    }

    fun usernameInUse(username: String): Query {
        return db.collection(USERS_COLLECTION).whereEqualTo("username", username)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateIsDriver(userId: String, isDriver: Boolean) {
        db.collection("users").document(userId)
            .update(mapOf("isDriver" to isDriver))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addRideToUserAsDriver(userId: String, rideDocRef: DocumentReference?) {
        if (rideDocRef != null) {
            db.collection(USERS_COLLECTION).document(userId)
                .update("ridesAsDriver", FieldValue.arrayUnion(rideDocRef))
        }
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun createUser(user: User): Task<DocumentReference> {
        return db.collection(USERS_COLLECTION).add(user)
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

    fun getVehicleById(vehicleId: String, driverId: String): DocumentReference {
        return db.collection(USERS_COLLECTION).document(driverId)
            .collection(VEHICLES_COLLECTION).document(vehicleId)
    }

    fun getAllVehiclesByUserId(id: String): CollectionReference {
        return db.collection(USERS_COLLECTION).document(id)
            .collection(VEHICLES_COLLECTION)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateVehicleIsInRide(userId: String, vehicleId: String) {
        db.collection(USERS_COLLECTION).document(userId)
            .collection(VEHICLES_COLLECTION).document(vehicleId)
            .update(mapOf("isInRide" to true))
    }


    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteVehicleById(userID: String, vehicleId: String) {
        db.collection(USERS_COLLECTION).document(userID)
            .collection(VEHICLES_COLLECTION).document(vehicleId).delete()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addVehicle(vehicle: Vehicle, userId: String) {
        db.collection(USERS_COLLECTION).document(userId)
            .collection(VEHICLES_COLLECTION).add(vehicle)
    }

    //RIDES FUNCTIONS ----------------------------------------------------

    fun getRidesFromChannel(channelDocId: String) : Query {
        return db.collection(CHANNELS_COLLECTION).document(channelDocId)
                .collection(RIDES_COLLECTION).whereGreaterThan("date", Timestamp.now())
    }

    fun getRideById(channelDocId: String , rideDocId: String): DocumentReference {
        return db.collection(CHANNELS_COLLECTION).document(channelDocId)
            .collection(RIDES_COLLECTION).document(rideDocId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getRideByIdSync(channelDocId: String, rideDocId: String): DocumentSnapshot {
        return db.collection(CHANNELS_COLLECTION).document(channelDocId)
            .collection(RIDES_COLLECTION).document(rideDocId).get().await()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addNewRide(ride: Ride, channelDocId: String): Task<DocumentReference> {
        return db.collection(CHANNELS_COLLECTION).document(channelDocId)
            .collection(RIDES_COLLECTION).add(ride)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun addPassengerInARide(channelId: String, rideId: String, passengerReference: DocumentReference) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
            .collection(RIDES_COLLECTION).document(rideId)
            .update("passengers", FieldValue.arrayUnion(passengerReference))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addRideInAPassenger(userId: String, rideDocRef: DocumentReference?) {
        db.collection(USERS_COLLECTION).document(userId)
            .update("ridesAsPassenger", FieldValue.arrayUnion(rideDocRef))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun removeRideInAPassenger(userId: String, rideDocRef: DocumentReference?) {
        db.collection(USERS_COLLECTION).document(userId)
            .update("ridesAsPassenger", FieldValue.arrayRemove(rideDocRef))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun removePassengerInARide(channelId: String, rideId: String, passengerReference: DocumentReference) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
            .collection(RIDES_COLLECTION).document(rideId)
            .update("passengers", FieldValue.arrayRemove(passengerReference))
    }



    //PAYOUT FUNCTIONS ----------------------------------------------------

    fun getPayoutsFromRide(channelId: String, rideId: String): CollectionReference {
        return db.collection(CHANNELS_COLLECTION).document(channelId)
                .collection(RIDES_COLLECTION).document(rideId)
                .collection(PAYOUTS_COLLECTION)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun addSimplePayout(channelId: String, rideId: String, payout: Payout) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
            .collection(RIDES_COLLECTION).document(rideId)
            .collection(PAYOUTS_COLLECTION).add(payout)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteSimplePayout(channelId: String, rideId: String, payoutId: String) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
            .collection(RIDES_COLLECTION).document(rideId)
            .collection(PAYOUTS_COLLECTION).document(payoutId).delete()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getPayoutByUserAndRideSync(channelId: String, rideId: String, passengerReference: DocumentReference): QuerySnapshot? {
       return db.collection(CHANNELS_COLLECTION).document(channelId)
            .collection(RIDES_COLLECTION).document(rideId)
            .collection(PAYOUTS_COLLECTION).whereEqualTo("passenger", passengerReference).limit(1L).get().await()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun checkPayoutAsPaidUpdateBoolean(channelId: String, rideId: String, payoutId: String) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
                .collection(RIDES_COLLECTION).document(rideId)
                .collection(PAYOUTS_COLLECTION).document(payoutId)
                .update("isPaid", true).await()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun checkPayoutAsPaidUpdatePaidDate(channelId: String, rideId: String, payoutId: String, paidDate: Timestamp) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
                .collection(RIDES_COLLECTION).document(rideId)
                .collection(PAYOUTS_COLLECTION).document(payoutId)
                .update("paidDate", paidDate).await()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun checkPayoutAsUnpaidUpdateBoolean(channelId: String, rideId: String, payoutId: String) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
                .collection(RIDES_COLLECTION).document(rideId)
                .collection(PAYOUTS_COLLECTION).document(payoutId)
                .update("isPaid", false).await()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun checkPayoutAsUnpaidUpdatePaidDate(channelId: String, rideId: String, payoutId: String) {
        db.collection(CHANNELS_COLLECTION).document(channelId)
                .collection(RIDES_COLLECTION).document(rideId)
                .collection(PAYOUTS_COLLECTION).document(payoutId)
                .update("paidDate", null).await()
    }
}
