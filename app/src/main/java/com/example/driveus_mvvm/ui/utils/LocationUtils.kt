package com.example.driveus_mvvm.ui.utils

import android.content.Context
import android.location.Geocoder
import com.google.firebase.firestore.GeoPoint
import java.util.*

object LocationUtils {

    fun getAddress(location: GeoPoint?, context: Context) : String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = location?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) }

        return "${address?.firstOrNull()?.thoroughfare?:""} ${address?.firstOrNull()?.subThoroughfare?:""}"
    }

    fun getLocation(location: String?, context: Context) : GeoPoint? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = location?.let { geocoder.getFromLocationName(it,  10) }
        address?.filter { it.countryName == "Spain" }
        return if (address?.isEmpty() == true){
            GeoPoint(0.0, 0.0)
        } else {
            address?.first()?.let { GeoPoint(it.latitude, it.longitude) }
        }
    }
}