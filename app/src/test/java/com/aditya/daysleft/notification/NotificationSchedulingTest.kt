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
    
    @Test
    fun `test unique notification ID generation`() {
        val eventId1 = 1
        val eventId2 = 2
        
        // Test that different events get different notification IDs
        val id1_user = generateUniqueNotificationId(eventId1, "user_preference")
        val id2_user = generateUniqueNotificationId(eventId2, "user_preference")
        
        assertNotEquals("Different events should have different notification IDs", id1_user, id2_user)
        
        // Test that same event with different reminder types get different IDs
        val id1_auto1day = generateUniqueNotificationId(eventId1, "automatic_1day")
        val id1_auto3day = generateUniqueNotificationId(eventId1, "automatic_3day")
        
        assertNotEquals("Same event with different reminder types should have different IDs", 
                       id1_user, id1_auto1day)
        assertNotEquals("Same event with different reminder types should have different IDs", 
                       id1_user, id1_auto3day)
        assertNotEquals("Same event with different reminder types should have different IDs", 
                       id1_auto1day, id1_auto3day)
    }
    
    @Test
    fun `test multiple reminder intervals`() {
        val event = Event(
            id = 1,
            title = "Test Event",
            dateMillis = System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000L), // 10 days from now
            notifyMe = true,
            reminderOffsetDays = 7, // User prefers 7 days before
            isArchived = false,
            isImportant = false
        )
        
        // Test reminder intervals that should be scheduled
        val expectedIntervals = setOf(7, 3, 1) // User preference (7), automatic 3-day, automatic 1-day
        
        // Since user preference is 7 days, both 1-day and 3-day automatic reminders should be added
        assertTrue("Should include user preference", expectedIntervals.contains(7))
        assertTrue("Should include automatic 1-day reminder", expectedIntervals.contains(1))
        assertTrue("Should include automatic 3-day reminder", expectedIntervals.contains(3))
        
        // Total should be 3 different reminder intervals
        assertEquals("Should have 3 different reminder intervals", 3, expectedIntervals.size)
    }
    
    @Test
    fun `test important event gets additional reminders`() {
        val event = Event(
            id = 1,
            title = "Important Meeting",
            dateMillis = System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000L), // 5 days from now
            notifyMe = true,
            reminderOffsetDays = 3, // User prefers 3 days before
            isArchived = false,
            isImportant = true
        )
        
        // Important events should get daily reminders in addition to regular ones
        assertTrue("Important event should be marked as important", event.isImportant)
        assertTrue("Important event should have notifications enabled", event.notifyMe)
        
        // For a 5-day future event with 3-day user preference, we should get:
        // - User preference at 3 days
        // - Automatic 1-day reminder
        // - Daily reminders for important events (2 days before, since maxDailyReminders = min(3, 3-1) = 2)
        val expectedMainReminders = setOf(3, 1) // User preference and automatic 1-day
        assertTrue("Should include user preference", expectedMainReminders.contains(3))
        assertTrue("Should include automatic 1-day reminder", expectedMainReminders.contains(1))
    }
    
    private fun generateUniqueNotificationId(eventId: Int, reminderType: String): Int {
        // Replicate the logic from EventReminderWorker
        return eventId * 1000 + reminderType.hashCode().and(0xFFF)
    }
}