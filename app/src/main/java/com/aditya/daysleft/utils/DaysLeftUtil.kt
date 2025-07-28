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
}