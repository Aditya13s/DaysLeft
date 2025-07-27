package com.aditya.daysleft.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.aditya.daysleft.data.local.dao.EventDao
import com.aditya.daysleft.data.local.entity.EventEntity
import com.aditya.daysleft.domain.model.Event

class EventRepositoryImpl(private val dao: EventDao) : EventRepository {
    override fun getEvents(): LiveData<List<Event>> {
        val liveData = MediatorLiveData<List<Event>>()
        liveData.addSource(dao.getEvents()) { entities: List<EventEntity> ->
            liveData.value = entities.map { entity ->
                Event(
                    id = entity.id, title = entity.title, dateMillis = entity.dateMillis
                )
            }
        }
        return liveData
    }

    override suspend fun addEvent(event: Event) {
        dao.addEvent(EventEntity(event.id, event.title, event.dateMillis))
    }

    override suspend fun updateEvent(event: Event) {
        dao.updateEvent(EventEntity(event.id, event.title, event.dateMillis))
    }

    override suspend fun deleteEvent(event: Event) {
        dao.deleteEvent(EventEntity(event.id, event.title, event.dateMillis))
    }
}