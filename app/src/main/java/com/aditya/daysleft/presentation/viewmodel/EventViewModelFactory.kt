package com.aditya.daysleft.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aditya.daysleft.domain.usecases.EventUseCases

class EventViewModelFactory(
    private val application: Application,
    private val useCases: EventUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(application, useCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}