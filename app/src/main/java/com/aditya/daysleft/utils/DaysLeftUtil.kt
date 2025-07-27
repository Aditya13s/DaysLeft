package com.aditya.daysleft.utils

import java.util.concurrent.TimeUnit

object DaysLeftUtil {
    fun daysLeft(eventDateMillis: Long): Int {
        val currentMillis = System.currentTimeMillis()
        val diff = eventDateMillis - currentMillis
        return if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff).toInt() else 0
    }
}