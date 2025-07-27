package com.aditya.daysleft.domain.usecases

import com.aditya.daysleft.data.repository.EventRepository
import com.aditya.daysleft.domain.model.Event


class GetEvents(private val repo: EventRepository) {
    operator fun invoke() = repo.getEvents()
}

class AddEvent(private val repo: EventRepository) {
    suspend operator fun invoke(event: Event) = repo.addEvent(event)
}

class UpdateEvent(private val repo: EventRepository) {
    suspend operator fun invoke(event: Event) = repo.updateEvent(event)
}

class DeleteEvent(private val repo: EventRepository) {
    suspend operator fun invoke(event: Event) = repo.deleteEvent(event)
}

data class EventUseCases(
    val getEvents: GetEvents,
    val addEvent: AddEvent,
    val updateEvent: UpdateEvent,
    val deleteEvent: DeleteEvent
)