package com.aditya.daysleft.domain.usecases

import com.aditya.daysleft.data.repository.EventRepository
import com.aditya.daysleft.domain.model.Event
import com.aditya.daysleft.domain.model.SortOption
import com.aditya.daysleft.domain.model.FilterOption


class GetEvents(private val repo: EventRepository) {
    operator fun invoke() = repo.getEvents()
    operator fun invoke(sortOption: SortOption, filterOption: FilterOption) = 
        repo.getEvents(sortOption, filterOption)
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

class RestoreEvent(private val repo: EventRepository) {
    suspend operator fun invoke(eventId: Int) = repo.restoreEvent(eventId)
}

class ArchiveOldEvents(private val repo: EventRepository) {
    suspend operator fun invoke(cutoffMillis: Long) = repo.archiveOldEvents(cutoffMillis)
}

class GetEventsWithReminders(private val repo: EventRepository) {
    operator fun invoke(currentTimeMillis: Long) = repo.getEventsWithReminders(currentTimeMillis)
}

data class EventUseCases(
    val getEvents: GetEvents,
    val addEvent: AddEvent,
    val updateEvent: UpdateEvent,
    val deleteEvent: DeleteEvent,
    val restoreEvent: RestoreEvent,
    val archiveOldEvents: ArchiveOldEvents,
    val getEventsWithReminders: GetEventsWithReminders
)