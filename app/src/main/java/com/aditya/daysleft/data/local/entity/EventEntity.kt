package com.aditya.daysleft.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateMillis: Long,
    val notifyMe: Boolean = false,
    val reminderOffsetDays: Int = 1,
    val isArchived: Boolean = false
)