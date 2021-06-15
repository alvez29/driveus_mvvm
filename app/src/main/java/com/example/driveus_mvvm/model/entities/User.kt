package com.example.driveus_mvvm.model.entities

import com.google.firebase.firestore.DocumentReference

data class User(val email: String,
                val isDriver: Boolean,
                val name: String,
                val surname: String,
                val uid: String,
                val username: String,
                val channel: List<DocumentReference>,
                val ridesAsPassenger: List<DocumentReference>)
