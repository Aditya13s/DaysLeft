package com.aditya.daysleft.notification

import android.content.Context
import androidx.work.*
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.presentation.settings.SettingsManager
import java.util.concurrent.TimeUnit
import java.util.Calendar

class NotificationScheduler(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun scheduleEventReminder(event: Event) {
        if (!event.notifyMe || event.isArchived) return
        
        // Cancel any existing reminders for this event
        cancelEventReminder(event.id)
        
        val currentTime = System.currentTimeMillis()
        
        // Schedule multiple reminders for better coverage
        scheduleMultipleReminders(event, currentTime)
    }
    
    private fun scheduleMultipleReminders(event: Event, currentTime: Long) {
        val userPreferenceDays = event.reminderOffsetDays
        val eventDate = event.dateMillis
        
        // List of reminder intervals to schedule (in days before event)
        val reminderIntervals = mutableSetOf<Int>()
        
        // Always add user preference
        reminderIntervals.add(userPreferenceDays)
        
        // Add automatic reminders if they don't conflict with user preference
        if (userPreferenceDays != 1) {
            reminderIntervals.add(1) // 1 day before
        }
        if (userPreferenceDays != 3) {
            reminderIntervals.add(3) // 3 days before
        }
        
        // Schedule each unique reminder interval
        reminderIntervals.forEach { reminderDays ->
            val reminderTime = eventDate - (reminderDays * 24 * 60 * 60 * 1000L)
            
            // Only schedule if the reminder time is in the future
            if (reminderTime > currentTime) {
                val reminderType = when (reminderDays) {
                    userPreferenceDays -> "user_preference"
                    1 -> "automatic_1day"
                    3 -> "automatic_3day"
                    else -> "custom_${reminderDays}day"
                }
                
                scheduleIndividualReminder(event, reminderTime, currentTime, reminderDays, reminderType)
            }
        }
        
        // Schedule daily reminders for important events (up to the event date)
        if (event.isImportant) {
            scheduleImportantEventDailyReminders(event, currentTime)
        }
    }
    
    private fun scheduleIndividualReminder(
        event: Event, 
        reminderTime: Long, 
        currentTime: Long, 
        reminderDays: Int, 
        reminderType: String
    ) {
        val delay = reminderTime - currentTime
        
        val inputData = Data.Builder()
            .putInt("event_id", event.id)
            .putString("event_title", event.title)
            .putLong("event_date", event.dateMillis)
            .putInt("reminder_days", reminderDays)
            .putBoolean("is_important", event.isImportant)
            .putString("reminder_type", reminderType)
            .build()
        
        val reminderWork = OneTimeWorkRequestBuilder<EventReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(getEventReminderTag(event.id, reminderType))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .build()
            )
            .build()
        
        workManager.enqueue(reminderWork)
    }
    
    private fun scheduleImportantEventDailyReminders(event: Event, currentTime: Long) {
        val eventDate = event.dateMillis
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        
        // Schedule daily reminders for up to 7 days before important events
        for (daysBeforeEvent in 1..7) {
            val reminderTime = eventDate - (daysBeforeEvent * oneDayInMillis)
            
            if (reminderTime > currentTime) {
                scheduleIndividualReminder(
                    event, 
                    reminderTime, 
                    currentTime, 
                    daysBeforeEvent, 
                    "important_daily_${daysBeforeEvent}"
                )
            }
        }
    }
    
    fun cancelEventReminder(eventId: Int) {
        // Cancel all reminders for this event (all types)
        workManager.cancelAllWorkByTag("event_reminder_$eventId")
        workManager.cancelAllWorkByTag("event_reminder_${eventId}_user_preference")
        workManager.cancelAllWorkByTag("event_reminder_${eventId}_automatic_1day")
        workManager.cancelAllWorkByTag("event_reminder_${eventId}_automatic_3day")
        // Cancel daily reminders for important events
        for (i in 1..7) {
            workManager.cancelAllWorkByTag("event_reminder_${eventId}_important_daily_$i")
        }
    }
    
    fun scheduleDailyDigest() {
        // Cancel existing daily digest
        workManager.cancelAllWorkByTag(DAILY_DIGEST_TAG)
        
        val settingsManager = SettingsManager(context)
        if (!settingsManager.isDailyDigestEnabled()) {
            return
        }
        
        // Schedule daily digest at configured time (default 8:00 AM)
        val (hour, minute) = settingsManager.getDailyDigestTime()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the configured time today has passed, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        
        val dailyDigestWork = PeriodicWorkRequestBuilder<DailyDigestWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(DAILY_DIGEST_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .build()
            )
            .build()
        
        workManager.enqueue(dailyDigestWork)
    }
    
    fun cancelDailyDigest() {
        workManager.cancelAllWorkByTag(DAILY_DIGEST_TAG)
    }
    
    fun scheduleAutoArchiving() {
        // Cancel existing auto-archive work
        workManager.cancelAllWorkByTag(AUTO_ARCHIVE_TAG)
        
        // Schedule auto-archiving to run daily at midnight
        val autoArchiveWork = PeriodicWorkRequestBuilder<AutoArchiveWorker>(1, TimeUnit.DAYS)
            .addTag(AUTO_ARCHIVE_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .build()
            )
            .build()
        
        workManager.enqueue(autoArchiveWork)
    }
    
    fun cancelAutoArchiving() {
        workManager.cancelAllWorkByTag(AUTO_ARCHIVE_TAG)
    }
    
    private fun getEventReminderTag(eventId: Int): String = "event_reminder_$eventId"
    
    private fun getEventReminderTag(eventId: Int, reminderType: String): String = 
        "event_reminder_${eventId}_$reminderType"
    
    companion object {
        private const val DAILY_DIGEST_TAG = "daily_digest"
        private const val AUTO_ARCHIVE_TAG = "auto_archive"
    }
}