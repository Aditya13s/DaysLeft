package com.aditya.daysleft.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aditya.daysleft.R
import com.aditya.daysleft.data.local.AppDatabase
import com.aditya.daysleft.data.repository.EventRepositoryImpl
import com.aditya.daysleft.presentation.MainActivity
import com.aditya.daysleft.utils.DaysLeftUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EventReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val eventId = inputData.getInt("event_id", -1)
            val eventTitle = inputData.getString("event_title")
            val eventDateMillis = inputData.getLong("event_date", -1L)
            val reminderDays = inputData.getInt("reminder_days", 1)
            val isImportant = inputData.getBoolean("is_important", false)
            
            if (eventId == -1 || eventDateMillis == -1L || eventTitle.isNullOrBlank()) {
                return@withContext Result.failure()
            }
            
            // Check if the event still exists and has reminders enabled
            val dao = AppDatabase.getInstance(applicationContext).eventDao()
            val repository = EventRepositoryImpl(dao)
            
            // Verify event still exists and notifications are enabled
            val event = repository.getEventById(eventId)
            if (event == null || !event.notifyMe || event.isArchived) {
                return@withContext Result.success() // Event no longer needs reminders
            }
            
            // Show the notification
            showReminderNotification(eventTitle, eventDateMillis, reminderDays, isImportant)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun showReminderNotification(
        eventTitle: String, 
        eventDateMillis: Long, 
        reminderDays: Int, 
        isImportant: Boolean
    ) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val eventDate = dateFormatter.format(Date(eventDateMillis))
        val daysLeft = DaysLeftUtil.daysLeft(eventDateMillis)
        
        val title = when {
            daysLeft == 0L -> if (isImportant) "⭐ Important Event Today!" else "📅 Event Today!"
            daysLeft == 1L -> if (isImportant) "⭐ Important Event Tomorrow!" else "📅 Event Tomorrow!"
            isImportant -> "⭐ Important Event in $daysLeft days"
            else -> "📅 Event in $daysLeft days"
        }
        
        val content = when {
            daysLeft == 0L -> "$eventTitle is TODAY! ($eventDate)"
            daysLeft == 1L -> "$eventTitle is TOMORROW ($eventDate)"
            else -> "$eventTitle is in $daysLeft days ($eventDate)"
        }
        
        val priority = if (isImportant || daysLeft <= 1) 
            NotificationCompat.PRIORITY_HIGH 
        else 
            NotificationCompat.PRIORITY_DEFAULT
        
        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.UPCOMING_EVENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .apply {
                if (isImportant || daysLeft <= 1) {
                    setVibrate(longArrayOf(0, 300, 100, 300, 100, 300))
                    setLights(0xFFFF0000.toInt(), 1000, 1000)
                    setDefaults(NotificationCompat.DEFAULT_SOUND)
                }
            }
            .build()
            
        // Use a simpler notification ID based on event title hash
        val notificationId = eventTitle.hashCode()
        NotificationManagerCompat.from(applicationContext).notify(
            notificationId, 
            notification
        )
    }
}