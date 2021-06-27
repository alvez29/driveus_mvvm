package com.example.driveus_mvvm.model.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint

data class Ride(var date: Timestamp? = null,
                var driver: DocumentReference? = null,
                var meetingPoint : GeoPoint = GeoPoint(0.0 , 0.0 ),
                var passengers : List<DocumentReference> = emptyList(),
                var price : Double? = 0.0,
                var vehicle : DocumentReference? = null)