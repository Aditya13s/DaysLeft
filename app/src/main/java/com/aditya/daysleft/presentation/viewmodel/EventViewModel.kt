package com.aditya.daysleft.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.usecases.EventUseCases
import kotlinx.coroutines.launch

class EventViewModel(
    application: Application, private val useCases: EventUseCases
) : AndroidViewModel(application) {
    val events: LiveData<List<Event>> = useCases.getEvents()

    fun addEvent(event: Event) = viewModelScope.launch { useCases.addEvent(event) }
    fun updateEvent(event: Event) = viewModelScope.launch { useCases.updateEvent(event) }
    fun deleteEvent(event: Event) = viewModelScope.launch { useCases.deleteEvent(event) }
}