package com.aditya.daysleft.domain.model

enum class FilterOption(val displayName: String) {
    ALL("All Events"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    UPCOMING_ONLY("Upcoming Only"), // New option for upcoming events without past events
    PAST("Past"),
    NEXT_7_DAYS("Next 7 Days")
}