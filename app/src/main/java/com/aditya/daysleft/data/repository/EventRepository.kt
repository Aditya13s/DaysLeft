package com.aditya.daysleft.data.repository

import androidx.lifecycle.LiveData
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.model.SortOption
import com.aditya.daysleft.domain.model.FilterOption

interface EventRepository {
    fun getEvents(): LiveData<List<Event>>
    fun getEvents(sortOption: SortOption, filterOption: FilterOption): LiveData<List<Event>>
    suspend fun addEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun archiveOldEvents(cutoffMillis: Long)
    suspend fun restoreEvent(eventId: Int)
    fun getEventsWithReminders(currentTimeMillis: Long): LiveData<List<Event>>
    suspend fun getEventById(eventId: Int): Event?
}