package com.aditya.daysleft.presentation.eventlist

import com.aditya.daysleft.domain.model.Event

sealed class EventListItem {
    data class SectionHeader(val title: String) : EventListItem()
    data class EventItem(val event: Event) : EventListItem()
}

enum class EventSection(val title: String) {
    UPCOMING("UPCOMING"),
    PAST("PAST")
}