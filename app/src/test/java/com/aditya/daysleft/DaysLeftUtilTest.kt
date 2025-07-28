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
    fun getThisMonthRange_returnsCorrectRange() {
        val (start, end) = DaysLeftUtil.getThisMonthRange()
        
        val calendar = Calendar.getInstance()
        
        // Start should be first day of current month at 00:00:00
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val expectedStart = calendar.timeInMillis
        
        // End should be last day of current month at 23:59:59
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val expectedEnd = calendar.timeInMillis
        
        assertEquals(expectedStart, start)
        assertEquals(expectedEnd, end)
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
}