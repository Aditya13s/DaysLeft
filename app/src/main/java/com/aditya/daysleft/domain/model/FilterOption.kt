package com.aditya.daysleft.domain.model

enum class FilterOption(val displayName: String) {
    ALL("All Events"),
    UPCOMING("Upcoming"),
    UPCOMING_ONLY("Upcoming Only"), // Default option for upcoming events without past events
    PAST("Past"),
    NEXT_7_DAYS("Next 7 Days")
}