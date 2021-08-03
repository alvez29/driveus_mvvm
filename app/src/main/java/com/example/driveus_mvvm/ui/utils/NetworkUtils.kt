package com.example.driveus_mvvm.ui.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat

object NetworkUtils {

    fun hasConnection(context: Context?) : Boolean {
        val connectivityManager = context?.let { ContextCompat.getSystemService(it, ConnectivityManager::class.java) }
        val currentNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(currentNetwork)
        val capability = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        return currentNetwork != null && capability == true
    }

}