package com.aditya.daysleft.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aditya.daysleft.data.local.dao.EventDao
import com.aditya.daysleft.data.local.entity.EventEntity

@Database(entities = [EventEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "event_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}