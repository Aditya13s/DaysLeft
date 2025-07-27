package com.aditya.daysleft

import android.app.Application
import com.google.android.material.color.DynamicColors

class DaysLeftApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}