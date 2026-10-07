package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sign_dataset_samples")
data class SignDatasetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val signId: String,
    val signName: String,
    val featuresCsv: String, // 63 comma-separated normalized floats
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "MANUAL_CAPTURE", // "CAMERA", "MANUAL_CAPTURE", "SYNTHETIC"
    val sampleIndex: Int = 1
)
