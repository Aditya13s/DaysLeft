package com.aditya.daysleft.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aditya.daysleft.data.local.dao.EventDao
import com.aditya.daysleft.data.local.entity.EventEntity

@Database(entities = [EventEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE events ADD COLUMN notifyMe INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE events ADD COLUMN reminderOffsetDays INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE events ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE events ADD COLUMN isImportant INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "event_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}