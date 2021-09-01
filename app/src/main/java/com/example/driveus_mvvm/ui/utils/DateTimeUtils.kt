package com.example.driveus_mvvm.ui.utils

import com.google.firebase.Timestamp
import java.util.*


object DateTimeUtils {


    fun dateTimeStringToTimestamp(dateTimeStr: String): Timestamp {
        //El string tendra el formato dd/MM/yyyy HH:mm

        val dateArrayStr = dateTimeStr.split(" ")
        val dateStr: String = dateArrayStr[0]
        val timeStr: String = dateArrayStr[1]
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

    fun dateStringToDate(dateStr: String): Date {
        //El string de la fecha tendrá el formato dd/MM/yyyy
        val dateSplit = dateStr.split("/")
        val day: Int = dateSplit[0].toInt()
        val month: Int = dateSplit[1].toInt() - 1
        val year: Int = dateSplit[2].toInt() - 1900

        return Date(year, month, day)
    }

    fun strToListIntDaysOfTheWeek(daysOfTheWeekInputStr: String): List<Int> {
        var daysOfTheWeekStr = daysOfTheWeekInputStr.replace("[", "")
        daysOfTheWeekStr = daysOfTheWeekStr.replace("]", "")
        val listDayOfWeekStr = daysOfTheWeekStr.split(",")
        val listDayOfWeekInt = mutableListOf<Int>()

        listDayOfWeekStr.forEach {
            when (it.trim()) {
                "SUNDAY" -> listDayOfWeekInt.add(1)
                "MONDAY" -> listDayOfWeekInt.add(2)
                "TUESDAY" -> listDayOfWeekInt.add(3)
                "WEDNESDAY" -> listDayOfWeekInt.add(4)
                "THURSDAY" -> listDayOfWeekInt.add(5)
                "FRIDAY" -> listDayOfWeekInt.add(6)
                "SATURDAY" -> listDayOfWeekInt.add(7)
            }
        }

        return listDayOfWeekInt
    }

    fun getStartAndEndDateFromFilterString(filterString: String): Pair<Timestamp, Timestamp> {
        //El string tendrá la forma dd/MM/yyyy de HH:mm a HH:mm

        val startDateStr: String
        val endDateStr: String

        val filterStringSplit = filterString.split("de")
        val startHour = filterStringSplit[1].split("a").map { it.trim() }

        startDateStr = "${filterStringSplit[0].trim()} ${startHour[0]}"
        endDateStr = "${filterStringSplit[0].trim()} ${startHour[1]}"

        return Pair(dateTimeStringToTimestamp(startDateStr), dateTimeStringToTimestamp(endDateStr))

    }

    fun fixMinuteString(minute: Int): String {
        return if (minute < 10) {
            "0${minute}"
        } else {
            minute.toString()
        }
    }
}