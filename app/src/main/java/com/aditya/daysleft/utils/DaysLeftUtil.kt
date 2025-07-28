package com.aditya.daysleft.utils

import java.util.concurrent.TimeUnit
import java.util.Calendar

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
        
        // Get calendar instances for both dates
        val currentCal = Calendar.getInstance().apply { 
            timeInMillis = currentMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val eventCal = Calendar.getInstance().apply { 
            timeInMillis = eventDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val daysDiff = ((eventCal.timeInMillis - currentCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
        
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