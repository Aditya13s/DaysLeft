package com.aditya.daysleft.data.repository

import androidx.lifecycle.LiveData
import com.aditya.daysleft.domain.model.Event

interface EventRepository {
    fun getEvents(): LiveData<List<Event>>
    suspend fun addEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
}