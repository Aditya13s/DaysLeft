package com.aditya.daysleft.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val UPCOMING_EVENTS_CHANNEL_ID = "upcoming_events"
    const val IMPORTANT_EVENTS_CHANNEL_ID = "important_events"
    const val DAILY_DIGEST_CHANNEL_ID = "daily_digest"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Upcoming Events Channel - Default priority
            val upcomingEventsChannel = NotificationChannel(
                UPCOMING_EVENTS_CHANNEL_ID,
                "Upcoming Events",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for upcoming events and reminders"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(false)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            
            // Important Events Channel - High priority
            val importantEventsChannel = NotificationChannel(
                IMPORTANT_EVENTS_CHANNEL_ID,
                "Important Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority notifications for important events"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)  // Allow important events to bypass Do Not Disturb
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
                vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 300)
            }
            
            // Daily Digest Channel - Low priority
            val dailyDigestChannel = NotificationChannel(
                DAILY_DIGEST_CHANNEL_ID,
                "Daily Digest",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily summary of events and reminders"
                enableVibration(false)
                enableLights(false)
                setBypassDnd(false)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            
            notificationManager.createNotificationChannel(upcomingEventsChannel)
            notificationManager.createNotificationChannel(importantEventsChannel)
            notificationManager.createNotificationChannel(dailyDigestChannel)
        }
    }
}