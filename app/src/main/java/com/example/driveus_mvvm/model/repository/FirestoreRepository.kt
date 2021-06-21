package com.example.driveus_mvvm.model.repository


import androidx.annotation.WorkerThread
import com.example.driveus_mvvm.model.entities.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirestoreRepository {

    private const val CHANNELS_COLLECTION = "channel"
    private const val USERS_COLLECTION = "users"
    
    private val db by lazy { FirebaseFirestore.getInstance() }

    //USER FUNCTIONS -----------------------------------------------------

    fun getUserById(userId: String) : DocumentReference {
        return db.collection(USERS_COLLECTION).document(userId)
    }

    fun getUserFromUID(uid: String): Query {
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

}
