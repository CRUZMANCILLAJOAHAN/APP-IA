package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecognitionHistoryDao {
    @Query("SELECT * FROM recognition_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<RecognitionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RecognitionHistoryEntity): Long

    @Query("DELETE FROM recognition_history")
    suspend fun clearHistory()
}
