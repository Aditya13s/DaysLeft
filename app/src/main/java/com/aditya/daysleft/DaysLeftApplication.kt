package com.aditya.daysleft

import android.app.Application
import com.aditya.daysleft.notification.NotificationChannels
import com.aditya.daysleft.notification.NotificationScheduler
import com.google.android.material.color.DynamicColors

class DaysLeftApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        
        // Initialize notification system
        NotificationChannels.createNotificationChannels(this)
        
        // Schedule periodic tasks
        val notificationScheduler = NotificationScheduler(this)
        notificationScheduler.scheduleDailyDigest()
        notificationScheduler.scheduleAutoArchiving()
    }
}