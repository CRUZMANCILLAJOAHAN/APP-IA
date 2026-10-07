package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recognition_history")
data class RecognitionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val signId: String,
    val signName: String,
    val confidence: Float,
    val classifierUsed: String,
    val inferenceTimeMs: Float,
    val spokenText: String,
    val timestamp: Long = System.currentTimeMillis()
)
