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
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.model.FilterOption
import com.aditya.daysleft.domain.model.SortOption
import com.aditya.daysleft.presentation.MainActivity
import com.aditya.daysleft.utils.DaysLeftUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DailyDigestWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getInstance(applicationContext).eventDao()
            
            // Get events for today and the next few days that have notifications enabled
            val startOfToday = DaysLeftUtil.getStartOfToday()
            val endOfTomorrow = startOfToday + (2 * 24 * 60 * 60 * 1000L) // Today + tomorrow
            
            // Get events that are coming up today or tomorrow and have notifications enabled
            val upcomingEventEntities = dao.getEventsInDateRangeSync(startOfToday, endOfTomorrow)
                .filter { !it.isArchived && it.notifyMe }
            
            // Convert entities to domain models
            val upcomingEvents = upcomingEventEntities.map { entity ->
                Event(
                    id = entity.id,
                    title = entity.title,
                    dateMillis = entity.dateMillis,
                    notifyMe = entity.notifyMe,
                    reminderOffsetDays = entity.reminderOffsetDays,
                    isArchived = entity.isArchived,
                    isImportant = entity.isImportant
                )
            }
            
            if (upcomingEvents.isNotEmpty()) {
                // Send individual notifications for each upcoming event
                showIndividualEventNotifications(upcomingEvents)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun showIndividualEventNotifications(events: List<Event>) {
        events.forEachIndexed { index, event ->
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 
                event.id, // Use event ID to make it unique
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val daysLeft = DaysLeftUtil.daysLeft(event.dateMillis)
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val eventDate = dateFormatter.format(Date(event.dateMillis))
            
            // Create specific, alerting notifications for each event
            val title = when {
                daysLeft == 0 -> if (event.isImportant) "🚨 Important Event TODAY!" else "📅 Event TODAY!"
                daysLeft == 1 -> if (event.isImportant) "⭐ Important Event Tomorrow!" else "⏰ Event Tomorrow!"
                event.isImportant -> "⭐ Important Event in $daysLeft days"
                else -> "📌 Upcoming Event in $daysLeft days"
            }
            
            val content = when {
                daysLeft == 0 -> "🚨 ${event.title} is TODAY! ($eventDate)"
                daysLeft == 1 -> "⏰ ${event.title} is TOMORROW ($eventDate)"
                else -> "📌 ${event.title} is in $daysLeft days ($eventDate)"
            }
            
            // Use high priority for today/tomorrow events and important events
            val priority = if (event.isImportant || daysLeft <= 1) 
                NotificationCompat.PRIORITY_HIGH 
            else 
                NotificationCompat.PRIORITY_DEFAULT
            
            // Choose appropriate notification channel based on importance and urgency
            val channelId = when {
                event.isImportant || daysLeft <= 1 -> NotificationChannels.IMPORTANT_EVENTS_CHANNEL_ID
                else -> NotificationChannels.UPCOMING_EVENTS_CHANNEL_ID
            }
            
            val notification = NotificationCompat.Builder(applicationContext, channelId)
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
                    // Enhanced alerting for important events and events today/tomorrow
                    if (event.isImportant || daysLeft <= 1) {
                        setVibrate(longArrayOf(0, 300, 100, 300, 100, 300))
                        setLights(0xFFFF0000.toInt(), 1000, 1000)
                        setDefaults(NotificationCompat.DEFAULT_SOUND)
                    }
                }
                .build()
                
            // Use unique notification ID for each event to prevent conflicts
            val notificationId = generateDailyDigestNotificationId(event.id)
            NotificationManagerCompat.from(applicationContext).notify(
                notificationId, 
                notification
            )
        }
    }
    
    private fun generateDailyDigestNotificationId(eventId: Int): Int {
        // Generate unique notification ID for daily digest notifications
        // Use different range from event reminders to avoid conflicts
        return 10000 + eventId
    }
}