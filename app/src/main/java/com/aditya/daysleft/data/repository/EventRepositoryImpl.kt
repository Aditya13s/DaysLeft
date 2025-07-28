package com.aditya.daysleft.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.aditya.daysleft.data.local.dao.EventDao
import com.aditya.daysleft.data.local.entity.EventEntity
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.model.SortOption
import com.aditya.daysleft.domain.model.FilterOption
import com.aditya.daysleft.utils.DaysLeftUtil

class EventRepositoryImpl(private val dao: EventDao) : EventRepository {
    override fun getEvents(): LiveData<List<Event>> {
        return getEvents(SortOption.DATE, FilterOption.ALL)
    }
    
    override fun getEvents(sortOption: SortOption, filterOption: FilterOption): LiveData<List<Event>> {
        val liveData = MediatorLiveData<List<Event>>()
        
        // Get the appropriate source based on filter option
        val source = when (filterOption) {
            FilterOption.ALL -> {
                when (sortOption) {
                    SortOption.DATE -> dao.getEventsSortedByDate()
                    SortOption.ALPHABETICAL -> dao.getEventsSortedAlphabetically() 
                    SortOption.DAYS_LEFT -> dao.getEventsSortedByDate() // We'll sort by days left in memory
                }
            }
            FilterOption.NEXT_7_DAYS -> {
                val (start, end) = DaysLeftUtil.getNext7DaysRange()
                dao.getEventsInDateRange(start, end)
            }
            FilterOption.THIS_MONTH -> {
                val (start, end) = DaysLeftUtil.getThisMonthRange()
                dao.getEventsInDateRange(start, end)
            }
        }
        
        liveData.addSource(source) { entities: List<EventEntity> ->
            val events = entities.map { entity ->
                Event(
                    id = entity.id, 
                    title = entity.title, 
                    dateMillis = entity.dateMillis
                )
            }
            
            // Apply sorting if needed (especially for DAYS_LEFT which requires calculation)
            val sortedEvents = when (sortOption) {
                SortOption.DATE -> events.sortedBy { it.dateMillis }
                SortOption.ALPHABETICAL -> events.sortedBy { it.title.lowercase() }
                SortOption.DAYS_LEFT -> events.sortedBy { DaysLeftUtil.daysLeft(it.dateMillis) }
            }
            
            liveData.value = sortedEvents
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