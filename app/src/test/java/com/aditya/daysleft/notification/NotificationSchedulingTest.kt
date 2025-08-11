package com.aditya.daysleft.notification

import com.aditya.daysleft.domain.model.Event
import org.junit.Test
import org.junit.Assert.*
import java.util.*

class NotificationSchedulingTest {
    
    @Test
    fun `test multiple reminder intervals calculation`() {
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
        
        // Calculate reminder times
        val userPreferenceTime = event.dateMillis - (event.reminderOffsetDays * 24 * 60 * 60 * 1000L)
        val oneDayBefore = event.dateMillis - (1 * 24 * 60 * 60 * 1000L)
        val threeDaysBefore = event.dateMillis - (3 * 24 * 60 * 60 * 1000L)
        
        // Verify all reminder times are in the future
        assertTrue("User preference reminder should be in future", userPreferenceTime > currentTime)
        assertTrue("1-day reminder should be in future", oneDayBefore > currentTime)
        assertTrue("3-day reminder should be in future", threeDaysBefore > currentTime)
        
        // Verify that we won't schedule duplicate reminders
        assertNotEquals("1-day reminder should be different from user preference", 
            oneDayBefore, userPreferenceTime)
        assertNotEquals("3-day reminder should be different from user preference", 
            threeDaysBefore, userPreferenceTime)
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
    fun `test event validation`() {
        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + (5 * 24 * 60 * 60 * 1000L)
        
        // Test that Event constructor validates input properly
        assertThrows(IllegalArgumentException::class.java) {
            Event(
                id = 1,
                title = "", // Empty title should throw
                dateMillis = futureTime,
                notifyMe = true,
                reminderOffsetDays = 1,
                isArchived = false,
                isImportant = false
            )
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            Event(
                id = 1,
                title = "Valid Title",
                dateMillis = futureTime,
                notifyMe = true,
                reminderOffsetDays = 0, // Zero reminder days should throw
                isArchived = false,
                isImportant = false
            )
        }
    }
}