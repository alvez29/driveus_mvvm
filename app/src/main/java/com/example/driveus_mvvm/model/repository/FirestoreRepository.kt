package com.example.driveus_mvvm.model.repository


import androidx.annotation.WorkerThread
import com.example.driveus_mvvm.model.entities.User
import com.google.firebase.firestore.*

object FirestoreRepository {

    private const val CHANNELS_COLLECTION = "channel"
    private const val USERS_COLLECTION = "users"
    private const val RIDES_COLLECTION = "rides"
    
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
    suspend fun updateUserName(userId: String, name: String) {
            db.collection(USERS_COLLECTION).document(userId)
                    .update(mapOf("name" to name))
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

    //RIDES FUNCTIONS ----------------------------------------------------

    fun getRidesFromChannel(channelDocId: String) : CollectionReference {
        return db.collection(CHANNELS_COLLECTION).document(channelDocId).collection(RIDES_COLLECTION)
    }



}
