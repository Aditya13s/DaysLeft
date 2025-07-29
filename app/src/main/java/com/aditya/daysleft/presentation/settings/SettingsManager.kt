package com.aditya.daysleft.presentation.settings

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun getDailyDigestTime(): Pair<Int, Int> {
        val hour = sharedPreferences.getInt(KEY_DIGEST_HOUR, DEFAULT_DIGEST_HOUR)
        val minute = sharedPreferences.getInt(KEY_DIGEST_MINUTE, DEFAULT_DIGEST_MINUTE)
        return Pair(hour, minute)
    }
    
    fun setDailyDigestTime(hour: Int, minute: Int) {
        sharedPreferences.edit()
            .putInt(KEY_DIGEST_HOUR, hour)
            .putInt(KEY_DIGEST_MINUTE, minute)
            .apply()
    }
    
    fun isDailyDigestEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DIGEST_ENABLED, true)
    }
    
    fun setDailyDigestEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_DIGEST_ENABLED, enabled)
            .apply()
    }
    
    companion object {
        private const val PREFS_NAME = "days_left_settings"
        private const val KEY_DIGEST_HOUR = "digest_hour"
        private const val KEY_DIGEST_MINUTE = "digest_minute"
        private const val KEY_DIGEST_ENABLED = "digest_enabled"
        
        const val DEFAULT_DIGEST_HOUR = 8
        const val DEFAULT_DIGEST_MINUTE = 0
    }
}