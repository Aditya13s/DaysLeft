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
        
        // Cancel any existing reminder for this event
        cancelEventReminder(event.id)
        
        val reminderTime = event.dateMillis - (event.reminderOffsetDays * 24 * 60 * 60 * 1000L)
        val currentTime = System.currentTimeMillis()
        
        // Only schedule if the reminder time is in the future
        if (reminderTime > currentTime) {
            val delay = reminderTime - currentTime
            
            val inputData = Data.Builder()
                .putInt("event_id", event.id)
                .putString("event_title", event.title)
                .putLong("event_date", event.dateMillis)
                .putInt("reminder_days", event.reminderOffsetDays)
                .putBoolean("is_important", event.isImportant)
                .build()
            
            val reminderWork = OneTimeWorkRequestBuilder<EventReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(getEventReminderTag(event.id))
                .build()
            
            workManager.enqueue(reminderWork)
        }
        
        // For important events, also schedule daily reminders
        if (event.isImportant && event.dateMillis > currentTime) {
            scheduleImportantEventDailyReminders(event)
        }
    }
    
    fun cancelEventReminder(eventId: Int) {
        workManager.cancelAllWorkByTag(getEventReminderTag(eventId))
        workManager.cancelAllWorkByTag(getImportantEventDailyTag(eventId))
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
            .build()
        
        workManager.enqueue(autoArchiveWork)
    }
    
    fun cancelAutoArchiving() {
        workManager.cancelAllWorkByTag(AUTO_ARCHIVE_TAG)
    }
    
    private fun getEventReminderTag(eventId: Int): String = "event_reminder_$eventId"
    
    private fun getImportantEventDailyTag(eventId: Int): String = "important_daily_$eventId"
    
    private fun scheduleImportantEventDailyReminders(event: Event) {
        // Cancel any existing important daily reminders
        workManager.cancelAllWorkByTag(getImportantEventDailyTag(event.id))
        
        val currentTime = System.currentTimeMillis()
        val eventTime = event.dateMillis
        
        // Schedule daily reminders starting from today until the event date
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 9) // 9 AM daily reminder
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If 9 AM today has passed, start from tomorrow
        if (calendar.timeInMillis <= currentTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Only schedule if the event is more than 1 day away
        if (eventTime - currentTime > 24 * 60 * 60 * 1000) {
            val delay = calendar.timeInMillis - currentTime
            
            val inputData = Data.Builder()
                .putInt("event_id", event.id)
                .putString("event_title", event.title)
                .putLong("event_date", event.dateMillis)
                .putBoolean("is_important_daily", true)
                .build()
            
            val importantDailyWork = PeriodicWorkRequestBuilder<EventReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(getImportantEventDailyTag(event.id))
                .build()
            
            workManager.enqueue(importantDailyWork)
        }
    }
    
    companion object {
        private const val DAILY_DIGEST_TAG = "daily_digest"
        private const val AUTO_ARCHIVE_TAG = "auto_archive"
    }
}