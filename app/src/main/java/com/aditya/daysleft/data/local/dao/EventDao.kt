package com.aditya.daysleft.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aditya.daysleft.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY dateMillis ASC")
    fun getEvents() : LiveData<List<EventEntity>>
    
    // Sorting queries
    @Query("SELECT * FROM events WHERE isArchived = 0 ORDER BY dateMillis ASC")
    fun getEventsSortedByDate() : LiveData<List<EventEntity>>
    
    // Filter queries
    @Query("SELECT * FROM events WHERE isArchived = 0 AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis ASC")
    fun getEventsInDateRange(startMillis: Long, endMillis: Long) : LiveData<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE isArchived = 0 AND dateMillis >= :afterMillis ORDER BY dateMillis ASC")
    fun getEventsAfterDate(afterMillis: Long) : LiveData<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE isArchived = 0 AND dateMillis < :beforeMillis ORDER BY dateMillis DESC")
    fun getEventsBeforeDate(beforeMillis: Long) : LiveData<List<EventEntity>>
    
    // Archive-related queries
    @Query("SELECT * FROM events WHERE isArchived = 0 ORDER BY dateMillis ASC")
    fun getActiveEvents() : LiveData<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE isArchived = 1 ORDER BY dateMillis DESC")
    fun getArchivedEvents() : LiveData<List<EventEntity>>
    
    @Query("UPDATE events SET isArchived = 1 WHERE dateMillis < :cutoffMillis AND isArchived = 0")
    suspend fun archiveOldEvents(cutoffMillis: Long)
    
    @Query("UPDATE events SET isArchived = 0 WHERE id = :eventId")
    suspend fun restoreEvent(eventId: Int)
    
    // Reminder-related queries
    @Query("SELECT * FROM events WHERE notifyMe = 1 AND isArchived = 0 AND dateMillis > :currentTimeMillis ORDER BY dateMillis ASC")
    fun getEventsWithReminders(currentTimeMillis: Long) : LiveData<List<EventEntity>>
    
    // Count queries for notifications
    @Query("SELECT COUNT(*) FROM events WHERE isArchived = 0 AND dateMillis BETWEEN :startMillis AND :endMillis")
    suspend fun countEventsInDateRange(startMillis: Long, endMillis: Long): Int
    
    @Query("SELECT COUNT(*) FROM events WHERE isArchived = 0 AND isImportant = 1 AND dateMillis >= :startMillis")
    suspend fun countImportantUpcomingEvents(startMillis: Long): Int
}