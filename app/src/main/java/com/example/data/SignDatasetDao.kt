package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SignDatasetDao {
    @Query("SELECT * FROM sign_dataset_samples ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<SignDatasetEntity>>

    @Query("SELECT * FROM sign_dataset_samples WHERE signId = :signId ORDER BY timestamp DESC")
    fun getSamplesForSign(signId: String): Flow<List<SignDatasetEntity>>

    @Query("SELECT COUNT(*) FROM sign_dataset_samples")
    fun getTotalSampleCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sign_dataset_samples WHERE signId = :signId")
    suspend fun getCountForSign(signId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: SignDatasetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<SignDatasetEntity>)

    @Query("DELETE FROM sign_dataset_samples WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sign_dataset_samples WHERE signId = :signId")
    suspend fun deleteBySign(signId: String)

    @Query("DELETE FROM sign_dataset_samples")
    suspend fun clearAll()
}
