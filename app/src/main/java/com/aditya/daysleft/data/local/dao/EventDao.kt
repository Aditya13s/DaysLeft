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
    @Query("SELECT * FROM events ORDER BY dateMillis ASC")
    fun getEventsSortedByDate() : LiveData<List<EventEntity>>
    
    // Filter queries
    @Query("SELECT * FROM events WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis ASC")
    fun getEventsInDateRange(startMillis: Long, endMillis: Long) : LiveData<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE dateMillis >= :afterMillis ORDER BY dateMillis ASC")
    fun getEventsAfterDate(afterMillis: Long) : LiveData<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE dateMillis < :beforeMillis ORDER BY dateMillis DESC")
    fun getEventsBeforeDate(beforeMillis: Long) : LiveData<List<EventEntity>>
}