package com.example.driveus_mvvm.ui.utils

import com.google.firebase.Timestamp
import java.util.*


object DateTimeUtils {

    fun dateStringToTimestamp(dateTimeStr: String): Timestamp {
        //El string tendra el formato dd/MM/yyyy HH:mm

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

    fun getStartAndEndDateFromFilterString(filterString: String): Pair<Timestamp, Timestamp> {
        //El string tendrá la forma dd/MM/yyyy de HH:mm a HH:mm

        var startDateStr = ""
        var endDateStr = ""

        val filterStringSplit = filterString.split("de")
        val startHour = filterStringSplit[1].split("a").map { it.trim() }

        startDateStr = "${filterStringSplit[0].trim()} ${startHour[0]}"
        endDateStr = "${filterStringSplit[0].trim()} ${startHour[1]}"

        return Pair(dateStringToTimestamp(startDateStr), dateStringToTimestamp(endDateStr))

    }

    fun fixMinuteString(minute: Int): String {
        return if (minute < 10) {
            "0${minute}"
        } else {
            minute.toString()
        }
    }
}