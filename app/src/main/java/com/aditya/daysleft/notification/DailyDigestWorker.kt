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
import com.aditya.daysleft.domain.model.FilterOption
import com.aditya.daysleft.domain.model.SortOption
import com.aditya.daysleft.presentation.MainActivity
import com.aditya.daysleft.utils.DaysLeftUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DailyDigestWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getInstance(applicationContext).eventDao()
            
            // Get events for the next 7 days (upcoming only, not past)
            val (startRange, endRange) = DaysLeftUtil.getNext7DaysRange()
            val startOfToday = DaysLeftUtil.getStartOfToday()
            
            // Count only upcoming events from today onwards within next 7 days
            val upcomingEventsCount = dao.countEventsInDateRange(startOfToday, endRange)
            
            // Count important upcoming events from today onwards
            val importantEventsCount = dao.countImportantUpcomingEvents(startOfToday)
            
            if (upcomingEventsCount > 0 || importantEventsCount > 0) {
                showDailyDigest(upcomingEventsCount, importantEventsCount)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun showDailyDigest(upcomingEventCount: Int, importantEventCount: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            1, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = "Daily Digest"
        val content = when {
            importantEventCount > 0 && upcomingEventCount > 0 -> {
                when {
                    importantEventCount == 1 && upcomingEventCount == 1 -> "1 important event coming up"
                    importantEventCount == 1 -> "1 important event among $upcomingEventCount events"
                    importantEventCount == upcomingEventCount -> "$importantEventCount important events this week"
                    else -> "$importantEventCount important events among $upcomingEventCount events"
                }
            }
            importantEventCount > 0 -> {
                when (importantEventCount) {
                    1 -> "1 important event this week"
                    else -> "$importantEventCount important events this week"
                }
            }
            upcomingEventCount > 0 -> {
                when (upcomingEventCount) {
                    1 -> "1 event this week"
                    else -> "$upcomingEventCount events this week"
                }
            }
            else -> "No upcoming events"
        }
        
        val priority = if (importantEventCount > 0) 
            NotificationCompat.PRIORITY_DEFAULT 
        else 
            NotificationCompat.PRIORITY_LOW
        
        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.DAILY_DIGEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        NotificationManagerCompat.from(applicationContext).notify(
            2, // Fixed ID for daily digest
            notification
        )
    }
}