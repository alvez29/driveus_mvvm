package com.example.driveus_mvvm.model.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Payout(var creationDate: Timestamp? = null,
                  var paidDate: Timestamp? = null,
                  @field:JvmField
                  var isPaid: Boolean = false,
                  @field:JvmField
                  var isDebt: Boolean = false,
                  var passenger: DocumentReference? = null,
                  var price: Double = 0.0)