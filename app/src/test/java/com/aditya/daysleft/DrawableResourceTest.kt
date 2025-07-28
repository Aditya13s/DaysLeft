package com.aditya.daysleft

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test to validate that drawable resources load correctly without crashes.
 * This specifically tests the fix for the spinner background drawable.
 */
@RunWith(AndroidJUnit4::class)
class DrawableResourceTest {

    @Test
    fun testDropdownBackgroundDrawableLoads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // This should not throw an exception after the fix
        val drawable = context.getDrawable(R.drawable.ic_dropdown_background)
        assertNotNull("Dropdown background drawable should load successfully", drawable)
    }
    
    @Test
    fun testArrowDropDownDrawableLoads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val drawable = context.getDrawable(R.drawable.ic_arrow_drop_down)
        assertNotNull("Arrow drop down drawable should load successfully", drawable)
    }
}