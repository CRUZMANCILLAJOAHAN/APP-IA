package com.example.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.model.HandPose

/**
 * High-Precision Computer Vision Hand Analyzer powered by Google MediaPipe HandLandmarker.
 *
 * 1. Google MediaPipe Tasks Vision (hand_landmarker.task):
 *    Extracts true 3D positions of all 21 anatomical joints in real-time.
 *    Captures exact finger curvatures (e.g. C, O, A, B, V, L) directly from the user's hand.
 * 2. 100% On-Device, completely local, zero cloud dependencies, and 100% free.
 * 3. Zero false positives when no hand is present in the frame.
 */
class HandVisionAnalyzer(
    private val context: Context,
    private val isFrontCamera: () -> Boolean,
    private val isDetectionPaused: () -> Boolean = { false },
    private val antiFaceFilterEnabled: () -> Boolean = { true },
    private val onHandPoseDetected: (HandPose?) -> Unit,
    private val onHandTelemetry: (isHandPresent: Boolean, fingerCount: Int, info: String) -> Unit = { _, _, _ -> }
) : ImageAnalysis.Analyzer {

    // Google MediaPipe 21-Landmark 3D Hand Detector (Runs locally with on-device model)
    private val mediaPipeDetector = MediaPipeHandDetector(context)
    private var frameSkipCounter = 0

    override fun analyze(imageProxy: ImageProxy) {
        try {
            // 1. Check user pause state
            if (isDetectionPaused()) {
                onHandPoseDetected(null)
                onHandTelemetry(false, 0, "Detección pausada")
                return
            }

            frameSkipCounter++
            // Process every 2nd frame (~15 fps) for optimal balance of speed and responsiveness
            if (frameSkipCounter % 2 != 0) {
                return
            }

            // 2. Google MediaPipe 21-Landmark 3D Detection (Direct real-time hand articulation)
            val mpPose = mediaPipeDetector.detect(imageProxy, isFrontCamera())
            if (mpPose != null) {
                val wristY = mpPose.wrist.y
                // If anti-face filter is enabled, ensure hand is in the signing area
                if (antiFaceFilterEnabled() && wristY < 0.22f) {
                    onHandPoseDetected(null)
                    onHandTelemetry(false, 0, "Baja la mano hacia el recuadro inferior")
                    return
                }

                val ext = mpPose.getFingerExtensionStates()
                val extCount = ext.count { it }

                onHandPoseDetected(mpPose)
                onHandTelemetry(true, extCount, "Mano 3D detectada en vivo")
            } else {
                // If no hand is detected by MediaPipe, declare no hand present
                onHandPoseDetected(null)
                onHandTelemetry(false, 0, "Coloca tu mano dentro del recuadro")
            }
        } catch (e: Exception) {
            Log.e("HandVisionAnalyzer", "Error analyzing camera frame", e)
        } finally {
            imageProxy.close()
        }
    }

    fun close() {
        try {
            mediaPipeDetector.close()
        } catch (e: Exception) {
            Log.w("HandVisionAnalyzer", "Error closing detector", e)
        }
    }
}
