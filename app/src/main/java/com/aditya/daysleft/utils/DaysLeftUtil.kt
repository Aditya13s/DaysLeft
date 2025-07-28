package com.aditya.daysleft.utils

import java.util.concurrent.TimeUnit

object DaysLeftUtil {
    fun daysLeft(eventDateMillis: Long): Int {
        val currentMillis = System.currentTimeMillis()
        val diff = eventDateMillis - currentMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
    
    fun isPastEvent(eventDateMillis: Long): Boolean {
        return eventDateMillis < System.currentTimeMillis()
    }
    
    fun isUpcomingEvent(eventDateMillis: Long): Boolean {
        return eventDateMillis >= System.currentTimeMillis()
    }
    
    fun daysSinceOrUntil(eventDateMillis: Long): Int {
        val currentMillis = System.currentTimeMillis()
        val diff = Math.abs(eventDateMillis - currentMillis)
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
    
    fun getRelativeDateText(eventDateMillis: Long): String {
        val currentMillis = System.currentTimeMillis()
        val diff = eventDateMillis - currentMillis
        val daysDiff = TimeUnit.MILLISECONDS.toDays(diff).toInt()
        
        return when (daysDiff) {
            -1 -> "YESTERDAY"
            0 -> "TODAY"
            1 -> "TOMORROW"
            else -> {
                val absDays = Math.abs(daysDiff)
                if (daysDiff < 0) {
                    "$absDays days ago"
                } else {
                    "$absDays days left"
                }
            }
        }
    }
}