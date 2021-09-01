package com.example.driveus_mvvm.model.entities

import com.google.firebase.firestore.DocumentReference

data class Channel(var destinationZone : String? = "Sin zona" ,
                   var originZone : String? = "Sin zona",
                   var users: List<DocumentReference?>? = emptyList())
