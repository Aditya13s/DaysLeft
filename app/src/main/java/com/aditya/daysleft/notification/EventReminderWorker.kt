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
            val eventTitle = inputData.getString("event_title") ?: return@withContext Result.failure()
            val eventDateMillis = inputData.getLong("event_date", -1L)
            val reminderDays = inputData.getInt("reminder_days", 1)
            
            if (eventId == -1 || eventDateMillis == -1L) {
                return@withContext Result.failure()
            }
            
            // Check if the event still exists and has reminders enabled
            val dao = AppDatabase.getInstance(applicationContext).eventDao()
            val repository = EventRepositoryImpl(dao)
            
            // Show the notification
            showReminderNotification(eventTitle, eventDateMillis, reminderDays)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun showReminderNotification(eventTitle: String, eventDateMillis: Long, reminderDays: Int) {
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
        
        val title = when (reminderDays) {
            1 -> "Event Tomorrow!"
            else -> "Event in $reminderDays days"
        }
        
        val content = when {
            daysLeft == 0L -> "$eventTitle is today!"
            daysLeft == 1L -> "$eventTitle is tomorrow ($eventDate)"
            else -> "$eventTitle is in $daysLeft days ($eventDate)"
        }
        
        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.UPCOMING_EVENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        NotificationManagerCompat.from(applicationContext).notify(
            "event_reminder_$eventTitle".hashCode(), 
            notification
        )
    }
}