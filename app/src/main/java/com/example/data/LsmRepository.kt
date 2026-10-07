package com.example.data

import com.example.model.CanonicalHandPoses
import com.example.model.LsmSign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlin.random.Random

class LsmRepository(
    private val signDatasetDao: SignDatasetDao,
    private val recognitionHistoryDao: RecognitionHistoryDao
) {
    val allSamples: Flow<List<SignDatasetEntity>> = signDatasetDao.getAllSamples()
    val totalSampleCount: Flow<Int> = signDatasetDao.getTotalSampleCount()
    val recentHistory: Flow<List<RecognitionHistoryEntity>> = recognitionHistoryDao.getRecentHistory()

    fun getSamplesForSign(signId: String): Flow<List<SignDatasetEntity>> {
        return signDatasetDao.getSamplesForSign(signId)
    }

    suspend fun saveSample(
        signId: String,
        features: FloatArray,
        source: String = "MANUAL_CAPTURE"
    ): Long = withContext(Dispatchers.IO) {
        val sign = LsmSign.findById(signId)
        val currentCount = signDatasetDao.getCountForSign(signId)
        val entity = SignDatasetEntity(
            signId = signId,
            signName = sign?.displayName ?: signId,
            featuresCsv = features.joinToString(separator = ",") { "%.4f".format(it) },
            timestamp = System.currentTimeMillis(),
            source = source,
            sampleIndex = currentCount + 1
        )
        signDatasetDao.insertSample(entity)
    }

    suspend fun deleteSample(id: Long) = withContext(Dispatchers.IO) {
        signDatasetDao.deleteById(id)
    }

    suspend fun clearDataset() = withContext(Dispatchers.IO) {
        signDatasetDao.clearAll()
    }

    suspend fun recordRecognition(
        signId: String,
        confidence: Float,
        classifierUsed: String,
        inferenceTimeMs: Float,
        spokenText: String
    ) = withContext(Dispatchers.IO) {
        val sign = LsmSign.findById(signId)
        val entry = RecognitionHistoryEntity(
            signId = signId,
            signName = sign?.displayName ?: signId,
            confidence = confidence,
            classifierUsed = classifierUsed,
            inferenceTimeMs = inferenceTimeMs,
            spokenText = spokenText
        )
        recognitionHistoryDao.insert(entry)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        recognitionHistoryDao.clearHistory()
    }

    /**
     * Seeds initial training samples if database is empty so dataset tab has rich data immediately.
     */
    suspend fun seedInitialDatasetIfNeeded() = withContext(Dispatchers.IO) {
        val current = totalSampleCount.firstOrNull() ?: 0
        if (current == 0) {
            val samplesToInsert = mutableListOf<SignDatasetEntity>()
            val random = Random(123)

            for (sign in LsmSign.VOCABULARY) {
                // Generate 5 base samples for each sign
                for (i in 1..5) {
                    val pose = CanonicalHandPoses.getPoseForSign(sign.id, jitterStdDev = 0.02f, random = random)
                    val features = pose.extractNormalizedFeatures()
                    samplesToInsert.add(
                        SignDatasetEntity(
                            signId = sign.id,
                            signName = sign.displayName,
                            featuresCsv = features.joinToString(",") { "%.4f".format(it) },
                            timestamp = System.currentTimeMillis() - (16 - samplesToInsert.size) * 60000L,
                            source = "DATASET_BASE",
                            sampleIndex = i
                        )
                    )
                }
            }
            signDatasetDao.insertSamples(samplesToInsert)
        }
    }
}
