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
        return getEvents(SortOption.DAYS_LEFT, FilterOption.UPCOMING_ONLY)
    }
    
    override fun getEvents(sortOption: SortOption, filterOption: FilterOption): LiveData<List<Event>> {
        val liveData = MediatorLiveData<List<Event>>()
        
        // Get the appropriate source based on filter option
        // Use consistent date-based filtering instead of timestamp-based for better UX
        val source = when (filterOption) {
            FilterOption.ALL -> {
                when (sortOption) {
                    SortOption.DATE -> dao.getActiveEvents() // Only show active events by default
                    SortOption.DAYS_LEFT -> dao.getActiveEvents() // We'll sort by days left in memory
                }
            }
            FilterOption.UPCOMING -> {
                // Get events starting from tomorrow (start of tomorrow), excluding archived
                val startOfTomorrow = DaysLeftUtil.getStartOfToday() + (24 * 60 * 60 * 1000)
                dao.getEventsAfterDate(startOfTomorrow - 1) // -1 to make it inclusive of startOfTomorrow
            }
            FilterOption.UPCOMING_ONLY -> {
                // Get events from today onwards (includes today and future), excluding archived
                val startOfToday = DaysLeftUtil.getStartOfToday()
                dao.getEventsAfterDate(startOfToday - 1) // -1 to make it inclusive of startOfToday
            }
            FilterOption.PAST -> {
                // Use start of today to exclude today's events from past, excluding archived
                val startOfToday = DaysLeftUtil.getStartOfToday()
                dao.getEventsBeforeDate(startOfToday)
            }
            FilterOption.NEXT_7_DAYS -> {
                // Get events in next 7 days, excluding archived
                val (start, end) = DaysLeftUtil.getNext7DaysRange()
                dao.getEventsInDateRange(start, end)
            }
            FilterOption.ARCHIVED -> {
                dao.getArchivedEvents()
            }
        }
        
        liveData.addSource(source) { entities: List<EventEntity> ->
            val events = entities.map { entity ->
                Event(
                    id = entity.id, 
                    title = entity.title, 
                    dateMillis = entity.dateMillis,
                    notifyMe = entity.notifyMe,
                    reminderOffsetDays = entity.reminderOffsetDays,
                    isArchived = entity.isArchived
                )
            }
            
            // Apply sorting if needed (especially for DAYS_LEFT which requires calculation)
            val sortedEvents = when (sortOption) {
                SortOption.DATE -> events.sortedBy { it.dateMillis }
                SortOption.DAYS_LEFT -> events.sortedBy { DaysLeftUtil.daysLeft(it.dateMillis) }
            }
            
            liveData.value = sortedEvents
        }
        return liveData
    }

    override suspend fun addEvent(event: Event) {
        dao.addEvent(EventEntity(
            event.id, 
            event.title, 
            event.dateMillis,
            event.notifyMe,
            event.reminderOffsetDays,
            event.isArchived
        ))
    }

    override suspend fun updateEvent(event: Event) {
        dao.updateEvent(EventEntity(
            event.id, 
            event.title, 
            event.dateMillis,
            event.notifyMe,
            event.reminderOffsetDays,
            event.isArchived
        ))
    }

    override suspend fun deleteEvent(event: Event) {
        dao.deleteEvent(EventEntity(
            event.id, 
            event.title, 
            event.dateMillis,
            event.notifyMe,
            event.reminderOffsetDays,
            event.isArchived
        ))
    }
    
    override suspend fun archiveOldEvents(cutoffMillis: Long) {
        dao.archiveOldEvents(cutoffMillis)
    }
    
    override suspend fun restoreEvent(eventId: Int) {
        dao.restoreEvent(eventId)
    }
    
    override fun getEventsWithReminders(currentTimeMillis: Long): LiveData<List<Event>> {
        val liveData = MediatorLiveData<List<Event>>()
        val source = dao.getEventsWithReminders(currentTimeMillis)
        
        liveData.addSource(source) { entities: List<EventEntity> ->
            val events = entities.map { entity ->
                Event(
                    id = entity.id, 
                    title = entity.title, 
                    dateMillis = entity.dateMillis,
                    notifyMe = entity.notifyMe,
                    reminderOffsetDays = entity.reminderOffsetDays,
                    isArchived = entity.isArchived
                )
            }
            liveData.value = events
        }
        return liveData
    }
}