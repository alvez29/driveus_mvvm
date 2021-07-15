package com.example.driveus_mvvm.ui.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.storage.FirebaseStorage

object ImageUtils {
    fun convertUriToBitmap(imageUri: Uri?, activity: Activity?): Bitmap? {
        var res : Bitmap? = null

        activity?.contentResolver?.let {
            imageUri?.let { uri ->
                val inputStream = it.openInputStream(uri)
                res = BitmapFactory.decodeStream(inputStream)
            }
        }

        return res
    }

    fun convertBitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}