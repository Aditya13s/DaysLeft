package com.aditya.daysleft.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.model.SortOption
import com.aditya.daysleft.domain.model.FilterOption
import com.aditya.daysleft.domain.usecases.EventUseCases
import com.aditya.daysleft.notification.NotificationScheduler
import kotlinx.coroutines.launch

class EventViewModel(
    application: Application, private val useCases: EventUseCases
) : AndroidViewModel(application) {
    
    private val _sortOption = MutableLiveData(SortOption.DATE)
    val sortOption: LiveData<SortOption> = _sortOption
    
    private val _filterOption = MutableLiveData(FilterOption.UPCOMING_ONLY)
    val filterOption: LiveData<FilterOption> = _filterOption
    
    val events: LiveData<List<Event>> = _sortOption.switchMap { sortOption ->
        _filterOption.switchMap { filterOption ->
            useCases.getEvents(sortOption, filterOption)
        }
    }
    
    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }
    
    fun setFilterOption(filterOption: FilterOption) {
        _filterOption.value = filterOption
    }

    fun addEvent(event: Event) = viewModelScope.launch { 
        useCases.addEvent(event)
        // Schedule reminder if enabled
        if (event.notifyMe && !event.isArchived) {
            val scheduler = NotificationScheduler(getApplication())
            scheduler.scheduleEventReminder(event)
        }
    }
    
    fun updateEvent(event: Event) = viewModelScope.launch { 
        useCases.updateEvent(event)
        // Update reminder scheduling
        val scheduler = NotificationScheduler(getApplication())
        if (event.notifyMe && !event.isArchived) {
            scheduler.scheduleEventReminder(event)
        } else {
            scheduler.cancelEventReminder(event.id)
        }
    }
    
    fun deleteEvent(event: Event) = viewModelScope.launch { 
        useCases.deleteEvent(event)
        // Cancel any scheduled reminders
        val scheduler = NotificationScheduler(getApplication())
        scheduler.cancelEventReminder(event.id)
    }
    
    fun restoreEvent(eventId: Int) = viewModelScope.launch {
        useCases.restoreEvent(eventId)
    }
    
    fun archiveOldEvents() = viewModelScope.launch {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        useCases.archiveOldEvents(thirtyDaysAgo)
    }
    
    fun rescheduleAllReminders() = viewModelScope.launch {
        val scheduler = NotificationScheduler(getApplication())
        val currentTime = System.currentTimeMillis()
        
        // Get all events with reminders and reschedule them
        useCases.getEventsWithReminders(currentTime).observeForever { events ->
            events?.forEach { event ->
                if (event.notifyMe && !event.isArchived && event.dateMillis > currentTime) {
                    scheduler.scheduleEventReminder(event)
                }
            }
        }
    }
}