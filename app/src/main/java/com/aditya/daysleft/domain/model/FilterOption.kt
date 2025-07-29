package com.aditya.daysleft.domain.model

enum class FilterOption(val displayName: String) {
    ALL("All Events"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    PAST("Past"),
    NEXT_7_DAYS("Next 7 Days")
}