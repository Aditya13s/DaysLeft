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
import kotlinx.coroutines.launch

class EventViewModel(
    application: Application, private val useCases: EventUseCases
) : AndroidViewModel(application) {
    
    private val _sortOption = MutableLiveData(SortOption.DATE)
    val sortOption: LiveData<SortOption> = _sortOption
    
    private val _filterOption = MutableLiveData(FilterOption.ALL)
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

    fun addEvent(event: Event) = viewModelScope.launch { useCases.addEvent(event) }
    fun updateEvent(event: Event) = viewModelScope.launch { useCases.updateEvent(event) }
    fun deleteEvent(event: Event) = viewModelScope.launch { useCases.deleteEvent(event) }
}