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
}