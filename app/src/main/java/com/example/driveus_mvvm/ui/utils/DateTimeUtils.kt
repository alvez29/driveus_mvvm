package com.example.driveus_mvvm.ui.utils

import com.google.firebase.Timestamp
import java.util.*


object DateTimeUtils {

    fun dateTimeStringToTimestamp(dateTimeStr: String): Timestamp {
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

    fun dateStringToDate(dateStr: String): Date {
        val dateSplit = dateStr.split("/")
        val day: Int = dateSplit[0].toInt()
        val month: Int = dateSplit[1].toInt() - 1
        val year: Int = dateSplit[2].toInt() - 1900

        return Date(year, month, day)
    }

    fun dateStringToTimestamp(dateStr: String): Timestamp {
        return Timestamp(dateStringToDate(dateStr))
    }

    fun strToListIntDaysOfTheWeek(daysOfTheWeekStr: String): List<Int> {
        var daysOfTheWeekStr = daysOfTheWeekStr.replace("[", "")
        daysOfTheWeekStr = daysOfTheWeekStr.replace("]", "")
        val listDayOfWeekStr = daysOfTheWeekStr.split(",")
        val listDayOfWeekInt = mutableListOf<Int>()
        listDayOfWeekStr.forEach {
            when(it.trim()) {
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

    fun fixMinuteString(minute: Int): String {
        return if (minute < 10) {
            "0${minute}"
        } else {
            minute.toString()
        }
    }
}