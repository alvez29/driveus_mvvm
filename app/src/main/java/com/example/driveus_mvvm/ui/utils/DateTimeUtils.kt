package com.example.driveus_mvvm.ui.utils

import com.google.firebase.Timestamp
import java.util.*


object DateTimeUtils {

    fun dateStringToTimestamp(dateTimeStr: String): Timestamp {
        val dateArrayStr = dateTimeStr.split(" ")
        val dateStr: String = dateArrayStr[0].toString()
        val timeStr: String = dateArrayStr[1].toString()

        val dateSplit = dateStr.split("/")
        val day: Int = dateSplit[0].toInt()
        val month: Int = dateSplit[1].toInt() - 1
        val year: Int = dateSplit[2].toInt() - 1900

        val timeSplit = timeStr.split(":")
        val hour: Int = timeSplit[0].toInt()
        val min: Int = timeSplit[1].toInt()

        val date = Date(year, month, day, hour, min)

        return Timestamp(date)
    }
}