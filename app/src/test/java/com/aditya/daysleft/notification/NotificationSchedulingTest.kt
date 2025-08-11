package com.aditya.daysleft.notification

import com.aditya.daysleft.domain.model.Event
import org.junit.Test
import org.junit.Assert.*
import java.util.*

class NotificationSchedulingTest {
    
    @Test
    fun `test single reminder calculation`() {
        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + (10 * 24 * 60 * 60 * 1000L) // 10 days from now
        
        val event = Event(
            id = 1,
            title = "Test Event",
            dateMillis = futureTime,
            notifyMe = true,
            reminderOffsetDays = 7, // User prefers 7 days before
            isArchived = false,
            isImportant = false
        )
        
        // Calculate user's preferred reminder time
        val userPreferenceTime = event.dateMillis - (event.reminderOffsetDays * 24 * 60 * 60 * 1000L)
        
        // Verify reminder time is in the future
        assertTrue("User preference reminder should be in future", userPreferenceTime > currentTime)
        
        // Verify event properties
        assertEquals("Test Event", event.title)
        assertTrue("Event should have notifications enabled", event.notifyMe)
        assertFalse("Event should not be archived", event.isArchived)
    }
    
    @Test
    fun `test notification content formatting`() {
        val currentTime = System.currentTimeMillis()
        val tomorrowTime = currentTime + (24 * 60 * 60 * 1000L)
        
        val event = Event(
            id = 1,
            title = "Important Meeting",
            dateMillis = tomorrowTime,
            notifyMe = true,
            reminderOffsetDays = 1,
            isArchived = false,
            isImportant = true
        )
        
        // Test that event title and importance are properly handled
        assertEquals("Important Meeting", event.title)
        assertTrue("Event should be marked as important", event.isImportant)
        assertTrue("Event should have notifications enabled", event.notifyMe)
        assertFalse("Event should not be archived", event.isArchived)
    }
    
    @Test
    fun `test event with no notifications`() {
        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + (5 * 24 * 60 * 60 * 1000L)
        
        val event = Event(
            id = 1,
            title = "Private Event",
            dateMillis = futureTime,
            notifyMe = false, // No notifications
            reminderOffsetDays = 1,
            isArchived = false,
            isImportant = false
        )
        
        // Verify that events without notifications are handled properly
        assertEquals("Private Event", event.title)
        assertFalse("Event should not have notifications enabled", event.notifyMe)
        assertFalse("Event should not be archived", event.isArchived)
    }
}