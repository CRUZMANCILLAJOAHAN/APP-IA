package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SignDatasetEntity::class, RecognitionHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun signDatasetDao(): SignDatasetDao
    abstract fun recognitionHistoryDao(): RecognitionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lsm_recognition_database.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
