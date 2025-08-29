package com.aditya.daysleft.domain.model

enum class ReminderOffset(val displayName: String, val days: Int) {
    ONE_DAY("1 day before", 1),
    THREE_DAYS("3 days before", 3),
    ONE_WEEK("1 week before", 7)
}