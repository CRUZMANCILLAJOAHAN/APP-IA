package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.model.HandLandmark
import com.example.model.HandPose
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker

class MediaPipeHandDetector(private val context: Context) {

    private var handLandmarker: HandLandmarker? = null

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(0.5f)
                .setMinHandPresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setNumHands(1)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            Log.d("MediaPipeHandDetector", "Google MediaPipe HandLandmarker initialized successfully")
        } catch (e: Exception) {
            Log.e("MediaPipeHandDetector", "Failed to initialize MediaPipe HandLandmarker", e)
        }
    }

    fun detect(imageProxy: ImageProxy, isFrontCamera: Boolean): HandPose? {
        val landmarker = handLandmarker ?: return null
        return try {
            val bitmap = imageProxy.toBitmap()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val rotatedBitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply {
                    postRotate(rotationDegrees.toFloat())
                }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            val result = landmarker.detect(mpImage)

            val landmarksList = result.landmarks()
            if (landmarksList.isEmpty() || landmarksList[0].isEmpty()) {
                return null
            }

            val firstHandLandmarks = landmarksList[0]
            val landmarks = firstHandLandmarks.mapIndexed { index, landmark ->
                HandLandmark(
                    id = index,
                    name = HandPose.LANDMARK_NAMES.getOrElse(index) { "LANDMARK_$index" },
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z()
                )
            }

            val handedness = if (result.handedness().isNotEmpty() && result.handedness()[0].isNotEmpty()) {
                result.handedness()[0][0].displayName()
            } else {
                if (isFrontCamera) "Right" else "Left"
            }

            val score = if (result.handedness().isNotEmpty() && result.handedness()[0].isNotEmpty()) {
                result.handedness()[0][0].score()
            } else {
                0.95f
            }

            HandPose(
                landmarks = landmarks,
                handedness = handedness,
                score = score
            )
        } catch (e: Exception) {
            Log.e("MediaPipeHandDetector", "Error running hand detection", e)
            null
        }
    }

    fun close() {
        try {
            handLandmarker?.close()
            handLandmarker = null
        } catch (e: Exception) {
            Log.w("MediaPipeHandDetector", "Error closing landmarker", e)
        }
    }
}
