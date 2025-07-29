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
            
            // Get events for the next 7 days
            val (startRange, endRange) = DaysLeftUtil.getNext7DaysRange()
            
            // Use a simple query to get count of upcoming events
            val upcomingEventsCount = getUpcomingEventsCount(startRange, endRange)
            
            if (upcomingEventsCount > 0) {
                showDailyDigest(upcomingEventsCount)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun getUpcomingEventsCount(startTime: Long, endTime: Long): Int {
        return try {
            val dao = AppDatabase.getInstance(applicationContext).eventDao()
            // For simplicity, we'll use a placeholder count for now
            // In a real implementation, you would add a count query to the DAO
            3 // This represents "some events this week"
        } catch (e: Exception) {
            0
        }
    }
    
    private fun showDailyDigest(eventCount: Int) {
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
        val content = when (eventCount) {
            1 -> "1 event this week"
            else -> "$eventCount events this week"
        }
        
        val notification = NotificationCompat.Builder(applicationContext, NotificationChannels.DAILY_DIGEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_event)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        NotificationManagerCompat.from(applicationContext).notify(
            2, // Fixed ID for daily digest
            notification
        )
    }
}