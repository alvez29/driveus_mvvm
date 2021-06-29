package com.example.driveus_mvvm.ui.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

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
}