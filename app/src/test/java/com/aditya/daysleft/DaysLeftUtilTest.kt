package com.aditya.daysleft

import com.aditya.daysleft.utils.DaysLeftUtil
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for DaysLeftUtil relative date formatting functionality.
 */
class DaysLeftUtilTest {
    
    private fun getDateMillis(daysFromToday: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysFromToday)
        }.timeInMillis
    }
    
    @Test
    fun getRelativeDateText_yesterday_returnsYesterday() {
        val yesterdayMillis = getDateMillis(-1)
        assertEquals("YESTERDAY", DaysLeftUtil.getRelativeDateText(yesterdayMillis))
    }
    
    @Test
    fun getRelativeDateText_today_returnsToday() {
        val todayMillis = getDateMillis(0)
        assertEquals("TODAY", DaysLeftUtil.getRelativeDateText(todayMillis))
    }
    
    @Test
    fun getRelativeDateText_tomorrow_returnsTomorrow() {
        val tomorrowMillis = getDateMillis(1)
        assertEquals("TOMORROW", DaysLeftUtil.getRelativeDateText(tomorrowMillis))
    }
    
    @Test
    fun getRelativeDateText_twoDaysAgo_returnsCorrectFormat() {
        val twoDaysAgoMillis = getDateMillis(-2)
        assertEquals("2 days ago", DaysLeftUtil.getRelativeDateText(twoDaysAgoMillis))
    }
    
    @Test
    fun getRelativeDateText_threeDaysLeft_returnsCorrectFormat() {
        val threeDaysFromNowMillis = getDateMillis(3)
        assertEquals("3 days left", DaysLeftUtil.getRelativeDateText(threeDaysFromNowMillis))
    }
    
    @Test
    fun getRelativeDateText_oneWeekAgo_returnsCorrectFormat() {
        val oneWeekAgoMillis = getDateMillis(-7)
        assertEquals("7 days ago", DaysLeftUtil.getRelativeDateText(oneWeekAgoMillis))
    }
    
    @Test
    fun getRelativeDateText_oneWeekLeft_returnsCorrectFormat() {
        val oneWeekFromNowMillis = getDateMillis(7)
        assertEquals("7 days left", DaysLeftUtil.getRelativeDateText(oneWeekFromNowMillis))
    }
    
    @Test
    fun getNext7DaysRange_returnsCorrectRange() {
        val (start, end) = DaysLeftUtil.getNext7DaysRange()
        val startOfToday = DaysLeftUtil.getStartOfToday()
        
        // Start should be start of today
        assertEquals(startOfToday, start)
        
        // End should be 7 days from start of today (end of day 7)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startOfToday
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        
        assertEquals(calendar.timeInMillis, end)
    }
    
    @Test
    fun daysLeft_futureDate_returnsPositiveValue() {
        val futureMillis = getDateMillis(5)
        val days = DaysLeftUtil.daysLeft(futureMillis)
        assertTrue("Days left should be positive for future events", days >= 0)
    }
    
    @Test
    fun daysLeft_pastDate_returnsNegativeValue() {
        val pastMillis = getDateMillis(-5)
        val days = DaysLeftUtil.daysLeft(pastMillis)
        assertTrue("Days left should be negative for past events", days < 0)
    }
    
    @Test
    fun isTodayEvent_todayEvent_returnsTrue() {
        val todayMillis = getDateMillis(0)
        assertTrue("Today event should return true", DaysLeftUtil.isTodayEvent(todayMillis))
    }
    
    @Test
    fun isTodayEvent_yesterdayEvent_returnsFalse() {
        val yesterdayMillis = getDateMillis(-1)
        assertFalse("Yesterday event should return false", DaysLeftUtil.isTodayEvent(yesterdayMillis))
    }
    
    @Test
    fun isTodayEvent_tomorrowEvent_returnsFalse() {
        val tomorrowMillis = getDateMillis(1)
        assertFalse("Tomorrow event should return false", DaysLeftUtil.isTodayEvent(tomorrowMillis))
    }
    
    @Test
    fun isPastEvent_yesterdayEvent_returnsTrue() {
        val yesterdayMillis = getDateMillis(-1)
        assertTrue("Yesterday event should be past", DaysLeftUtil.isPastEvent(yesterdayMillis))
    }
    
    @Test
    fun isPastEvent_todayEvent_returnsFalse() {
        val todayMillis = getDateMillis(0)
        assertFalse("Today event should not be past", DaysLeftUtil.isPastEvent(todayMillis))
    }
    
    @Test
    fun isPastEvent_tomorrowEvent_returnsFalse() {
        val tomorrowMillis = getDateMillis(1)
        assertFalse("Tomorrow event should not be past", DaysLeftUtil.isPastEvent(tomorrowMillis))
    }
    
    @Test
    fun isUpcomingEvent_tomorrowEvent_returnsTrue() {
        val tomorrowMillis = getDateMillis(1)
        assertTrue("Tomorrow event should be upcoming", DaysLeftUtil.isUpcomingEvent(tomorrowMillis))
    }
    
    @Test
    fun isUpcomingEvent_todayEvent_returnsFalse() {
        val todayMillis = getDateMillis(0)
        assertFalse("Today event should not be upcoming", DaysLeftUtil.isUpcomingEvent(todayMillis))
    }
    
    @Test
    fun isUpcomingEvent_yesterdayEvent_returnsFalse() {
        val yesterdayMillis = getDateMillis(-1)
        assertFalse("Yesterday event should not be upcoming", DaysLeftUtil.isUpcomingEvent(yesterdayMillis))
    }
    
    @Test
    fun isUpcomingButNotToday_tomorrowEvent_returnsTrue() {
        val tomorrowMillis = getDateMillis(1)
        assertTrue("Tomorrow event should be upcoming but not today", DaysLeftUtil.isUpcomingButNotToday(tomorrowMillis))
    }
    
    @Test
    fun isUpcomingButNotToday_todayEvent_returnsFalse() {
        val todayMillis = getDateMillis(0)
        assertFalse("Today event should not be upcoming but not today", DaysLeftUtil.isUpcomingButNotToday(todayMillis))
    }
    
    @Test
    fun filteringConsistency_noOverlapBetweenTodayAndPast() {
        val todayMillis = getDateMillis(0)
        
        // Today event should be TODAY but not PAST
        assertTrue("Today event should be today", DaysLeftUtil.isTodayEvent(todayMillis))
        assertFalse("Today event should not be past", DaysLeftUtil.isPastEvent(todayMillis))
        assertFalse("Today event should not be upcoming", DaysLeftUtil.isUpcomingEvent(todayMillis))
    }
    
    @Test
    fun filteringConsistency_pastEventNotToday() {
        val pastMillis = getDateMillis(-1)
        
        // Past event should be PAST but not TODAY
        assertTrue("Past event should be past", DaysLeftUtil.isPastEvent(pastMillis))
        assertFalse("Past event should not be today", DaysLeftUtil.isTodayEvent(pastMillis))
        assertFalse("Past event should not be upcoming", DaysLeftUtil.isUpcomingEvent(pastMillis))
    }
    
    @Test
    fun filteringConsistency_upcomingEventNotToday() {
        val futureMillis = getDateMillis(1)
        
        // Future event should be UPCOMING but not TODAY
        assertTrue("Future event should be upcoming", DaysLeftUtil.isUpcomingEvent(futureMillis))
        assertFalse("Future event should not be today", DaysLeftUtil.isTodayEvent(futureMillis))
        assertFalse("Future event should not be past", DaysLeftUtil.isPastEvent(futureMillis))
    }
}