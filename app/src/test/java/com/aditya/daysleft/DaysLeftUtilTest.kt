package com.aditya.daysleft

import com.aditya.daysleft.utils.DaysLeftUtil
import org.junit.Test
import org.junit.Assert.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Unit tests for DaysLeftUtil relative date formatting functionality.
 */
class DaysLeftUtilTest {
    
    @Test
    fun getRelativeDateText_yesterday_returnsYesterday() {
        val yesterdayMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        assertEquals("YESTERDAY", DaysLeftUtil.getRelativeDateText(yesterdayMillis))
    }
    
    @Test
    fun getRelativeDateText_today_returnsToday() {
        val todayMillis = System.currentTimeMillis()
        assertEquals("TODAY", DaysLeftUtil.getRelativeDateText(todayMillis))
    }
    
    @Test
    fun getRelativeDateText_tomorrow_returnsTomorrow() {
        val tomorrowMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        assertEquals("TOMORROW", DaysLeftUtil.getRelativeDateText(tomorrowMillis))
    }
    
    @Test
    fun getRelativeDateText_twoDaysAgo_returnsCorrectFormat() {
        val twoDaysAgoMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
        assertEquals("2 days ago", DaysLeftUtil.getRelativeDateText(twoDaysAgoMillis))
    }
    
    @Test
    fun getRelativeDateText_threeDaysLeft_returnsCorrectFormat() {
        val threeDaysFromNowMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3)
        assertEquals("3 days left", DaysLeftUtil.getRelativeDateText(threeDaysFromNowMillis))
    }
    
    @Test
    fun getRelativeDateText_oneWeekAgo_returnsCorrectFormat() {
        val oneWeekAgoMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        assertEquals("7 days ago", DaysLeftUtil.getRelativeDateText(oneWeekAgoMillis))
    }
    
    @Test
    fun getRelativeDateText_oneWeekLeft_returnsCorrectFormat() {
        val oneWeekFromNowMillis = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
        assertEquals("7 days left", DaysLeftUtil.getRelativeDateText(oneWeekFromNowMillis))
    }
}