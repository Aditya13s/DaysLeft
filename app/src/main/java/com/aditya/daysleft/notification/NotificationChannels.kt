package com.aditya.daysleft.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val UPCOMING_EVENTS_CHANNEL_ID = "upcoming_events"
    const val DAILY_DIGEST_CHANNEL_ID = "daily_digest"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Upcoming Events Channel
            val upcomingEventsChannel = NotificationChannel(
                UPCOMING_EVENTS_CHANNEL_ID,
                "Upcoming Events",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming events"
                enableVibration(true)
                enableLights(true)
            }
            
            // Daily Digest Channel
            val dailyDigestChannel = NotificationChannel(
                DAILY_DIGEST_CHANNEL_ID,
                "Daily Digest",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily summary of events"
                enableVibration(false)
                enableLights(false)
            }
            
            notificationManager.createNotificationChannel(upcomingEventsChannel)
            notificationManager.createNotificationChannel(dailyDigestChannel)
        }
    }
}