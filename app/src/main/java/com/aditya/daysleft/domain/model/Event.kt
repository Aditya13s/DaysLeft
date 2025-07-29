package com.aditya.daysleft.domain.model

data class Event(
    val id: Int = 0,
    val title: String,
    val dateMillis: Long,
    val notifyMe: Boolean = false,
    val reminderOffsetDays: Int = ReminderOffset.ONE_DAY.days,
    val isArchived: Boolean = false
)
