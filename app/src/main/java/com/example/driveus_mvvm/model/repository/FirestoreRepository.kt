package com.example.driveus_mvvm.model.repository


import androidx.annotation.WorkerThread
import com.example.driveus_mvvm.model.entities.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    fun getUserById(userId: String) : DocumentReference {
        return db.collection("users").document(userId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateUserName(userId: String, name: String) {
            db.collection("users").document(userId)
                    .update(mapOf("name" to name))
    }


}