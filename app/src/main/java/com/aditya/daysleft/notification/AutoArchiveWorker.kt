package com.aditya.daysleft.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aditya.daysleft.data.local.AppDatabase
import com.aditya.daysleft.data.repository.EventRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class AutoArchiveWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val dao = AppDatabase.getInstance(applicationContext).eventDao()
            val repository = EventRepositoryImpl(dao)
            
            // Calculate cutoff time (30 days ago)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -30)
            val cutoffTime = calendar.timeInMillis
            
            // Archive events older than 30 days
            repository.archiveOldEvents(cutoffTime)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}