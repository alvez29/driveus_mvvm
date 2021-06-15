package com.example.driveus_mvvm.model.repository


import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    fun getUserById(userId: String) : DocumentReference {
        return db.collection("users").document(userId)
    }

}