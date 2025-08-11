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
        
        // Schedule original reminder based on user's preference
        scheduleOriginalReminder(event, currentTime)
        
        // Schedule additional automatic reminders (1 day and 3 days before)
        scheduleAutomaticReminders(event, currentTime)
        
        // For important events, also schedule daily reminders
        if (event.isImportant && event.dateMillis > currentTime) {
            scheduleImportantEventDailyReminders(event)
        }
    }
    
    private fun scheduleOriginalReminder(event: Event, currentTime: Long) {
        val reminderTime = event.dateMillis - (event.reminderOffsetDays * 24 * 60 * 60 * 1000L)
        
        // Only schedule if the reminder time is in the future
        if (reminderTime > currentTime) {
            val delay = reminderTime - currentTime
            
            val inputData = Data.Builder()
                .putInt("event_id", event.id)
                .putString("event_title", event.title)
                .putLong("event_date", event.dateMillis)
                .putInt("reminder_days", event.reminderOffsetDays)
                .putBoolean("is_important", event.isImportant)
                .putString("reminder_type", "user_preference")
                .build()
            
            val reminderWork = OneTimeWorkRequestBuilder<EventReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(getEventReminderTag(event.id))
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
    }
    
    private fun scheduleAutomaticReminders(event: Event, currentTime: Long) {
        // Schedule 1-day before reminder (if different from user preference and in future)
        val oneDayBefore = event.dateMillis - (1 * 24 * 60 * 60 * 1000L)
        if (oneDayBefore > currentTime && event.reminderOffsetDays != 1) {
            scheduleAdditionalReminder(event, oneDayBefore, currentTime, 1, "automatic_1day")
        }
        
        // Schedule 3-day before reminder (if different from user preference and in future)
        val threeDaysBefore = event.dateMillis - (3 * 24 * 60 * 60 * 1000L)
        if (threeDaysBefore > currentTime && event.reminderOffsetDays != 3) {
            scheduleAdditionalReminder(event, threeDaysBefore, currentTime, 3, "automatic_3day")
        }
    }
    
    private fun scheduleAdditionalReminder(event: Event, reminderTime: Long, currentTime: Long, 
                                         reminderDays: Int, reminderType: String) {
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
            .addTag(getEventReminderTag(event.id))
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
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .build()
                )
                .build()
            
            workManager.enqueue(importantDailyWork)
        }
    }
    
    companion object {
        private const val DAILY_DIGEST_TAG = "daily_digest"
        private const val AUTO_ARCHIVE_TAG = "auto_archive"
    }
}