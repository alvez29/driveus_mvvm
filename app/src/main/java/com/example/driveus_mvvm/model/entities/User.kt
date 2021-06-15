package com.example.driveus_mvvm.model.entities

import com.google.firebase.firestore.DocumentReference

data class User( val uid: String? = "",
                 val username: String? = "",
                 val surname: String? = "",
                 val name:String? = "",
                 val channels: List<DocumentReference?>? = emptyList(),
                 val rides: List<DocumentReference?>? = emptyList(),
                 @field:JvmField
                 val isDriver: Boolean? = false,
                 @field:JvmField
                 val email:String? = "" )
