package com.example.driveus_mvvm.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

fun GeoPoint.toLatLng() : LatLng {
    return LatLng(this.latitude, this.longitude)
}
