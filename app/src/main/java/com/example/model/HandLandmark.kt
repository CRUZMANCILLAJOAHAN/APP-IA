package com.example.model

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Represents a single 3D hand landmark according to MediaPipe Hands model (21 points).
 */
data class HandLandmark(
    val id: Int,
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float = 0f
) {
    fun distanceTo(other: HandLandmark): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

/**
 * 21 landmarks of a hand detection.
 */
data class HandPose(
    val landmarks: List<HandLandmark>,
    val handedness: String = "Right", // "Left" or "Right"
    val score: Float = 1.0f
) {
    init {
        require(landmarks.size == 21) { "HandPose requires exactly 21 landmarks" }
    }

    val wrist: HandLandmark get() = landmarks[0]
    val thumbCmc: HandLandmark get() = landmarks[1]
    val thumbMcp: HandLandmark get() = landmarks[2]
    val thumbIp: HandLandmark get() = landmarks[3]
    val thumbTip: HandLandmark get() = landmarks[4]

    val indexMcp: HandLandmark get() = landmarks[5]
    val indexPip: HandLandmark get() = landmarks[6]
    val indexDip: HandLandmark get() = landmarks[7]
    val indexTip: HandLandmark get() = landmarks[8]

    val middleMcp: HandLandmark get() = landmarks[9]
    val middlePip: HandLandmark get() = landmarks[10]
    val middleDip: HandLandmark get() = landmarks[11]
    val middleTip: HandLandmark get() = landmarks[12]

    val ringMcp: HandLandmark get() = landmarks[13]
    val ringPip: HandLandmark get() = landmarks[14]
    val ringDip: HandLandmark get() = landmarks[15]
    val ringTip: HandLandmark get() = landmarks[16]

    val pinkyMcp: HandLandmark get() = landmarks[17]
    val pinkyPip: HandLandmark get() = landmarks[18]
    val pinkyDip: HandLandmark get() = landmarks[19]
    val pinkyTip: HandLandmark get() = landmarks[20]

    /**
     * Normalized relative coordinates centered at wrist and scaled by hand size.
     * Returns a 63-element FloatArray [dx0, dy0, dz0, dx1, dy1, dz1, ...].
     */
    fun extractNormalizedFeatures(): FloatArray {
        val w = wrist
        val handScale = maxOf(0.001f, w.distanceTo(middleMcp))

        val features = FloatArray(63)
        for (i in landmarks.indices) {
            val lm = landmarks[i]
            features[i * 3] = (lm.x - w.x) / handScale
            features[i * 3 + 1] = (lm.y - w.y) / handScale
            features[i * 3 + 2] = (lm.z - w.z) / handScale
        }
        return features
    }

    /**
     * Determines whether each finger is extended (true) or folded/curled (false).
     */
    fun getFingerExtensionStates(): BooleanArray {
        val w = wrist
        // Thumb: check tip distance from pinky MCP vs IP distance
        val thumbExtended = thumbTip.distanceTo(pinkyMcp) > thumbMcp.distanceTo(pinkyMcp) * 1.25f

        // Index, Middle, Ring, Pinky: tip farther from wrist than PIP
        val indexExtended = indexTip.distanceTo(w) > indexPip.distanceTo(w)
        val middleExtended = middleTip.distanceTo(w) > middlePip.distanceTo(w)
        val ringExtended = ringTip.distanceTo(w) > ringPip.distanceTo(w)
        val pinkyExtended = pinkyTip.distanceTo(w) > pinkyPip.distanceTo(w)

        return booleanArrayOf(thumbExtended, indexExtended, middleExtended, ringExtended, pinkyExtended)
    }

    companion object {
        // Standard MediaPipe skeleton bone links (pairs of landmark indices)
        val SKELETON_CONNECTIONS = listOf(
            // Palm base
            0 to 1, 1 to 2, 2 to 3, 3 to 4, // Thumb
            0 to 5, 5 to 6, 6 to 7, 7 to 8, // Index
            0 to 9, 9 to 10, 10 to 11, 11 to 12, // Middle
            0 to 13, 13 to 14, 14 to 15, 15 to 16, // Ring
            0 to 17, 17 to 18, 18 to 19, 19 to 20, // Pinky
            // Palm connects
            5 to 9, 9 to 13, 13 to 17
        )

        val LANDMARK_NAMES = listOf(
            "WRIST",
            "THUMB_CMC", "THUMB_MCP", "THUMB_IP", "THUMB_TIP",
            "INDEX_MCP", "INDEX_PIP", "INDEX_DIP", "INDEX_TIP",
            "MIDDLE_MCP", "MIDDLE_PIP", "MIDDLE_DIP", "MIDDLE_TIP",
            "RING_MCP", "RING_PIP", "RING_DIP", "RING_TIP",
            "PINKY_MCP", "PINKY_PIP", "PINKY_DIP", "PINKY_TIP"
        )
    }
}
