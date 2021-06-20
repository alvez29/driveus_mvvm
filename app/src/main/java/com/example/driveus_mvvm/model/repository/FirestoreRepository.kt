package com.example.driveus_mvvm.model.repository


import androidx.annotation.WorkerThread
import com.example.driveus_mvvm.model.entities.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirestoreRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    //USER FUNCTIONS -----------------------------------------------------

    fun getUserById(userId: String) : DocumentReference {
        return db.collection("users").document(userId)
    }

    fun getUserFromUID(uid: String): Query {
        return db.collection("users").whereEqualTo("uid", uid)
    }

    suspend fun usernameInUse(username: String): Query {
        return db.collection("users").whereEqualTo("username", username)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateUserName(userId: String, name: String) {
            db.collection("users").document(userId)
                    .update(mapOf("name" to name))
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun createUser(user: User){
        db.collection("users").add(user)
    }
}