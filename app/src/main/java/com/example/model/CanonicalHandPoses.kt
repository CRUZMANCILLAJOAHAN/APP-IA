package com.example.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Generates canonical 21-landmark hand poses for each of the 16 LSM signs.
 * Supports adding natural noise and variations for synthetic dataset augmentation.
 */
object CanonicalHandPoses {

    data class FingerAngles(
        val thumbExtension: Float, // 0.0 (curled) to 1.0 (fully extended)
        val indexExtension: Float,
        val middleExtension: Float,
        val ringExtension: Float,
        val pinkyExtension: Float,
        val thumbSpread: Float = 0.5f,
        val fingerSeparation: Float = 0.0f // separation between index and middle (e.g. for V)
    )

    fun getPoseForSign(signId: String, jitterStdDev: Float = 0.0f, random: Random = Random): HandPose {
        val config = when (signId) {
            "A" -> FingerAngles(thumbExtension = 0.95f, indexExtension = 0.08f, middleExtension = 0.08f, ringExtension = 0.08f, pinkyExtension = 0.08f, thumbSpread = 0.2f)
            "B" -> FingerAngles(thumbExtension = 0.05f, indexExtension = 0.98f, middleExtension = 0.98f, ringExtension = 0.98f, pinkyExtension = 0.98f, thumbSpread = 0.0f, fingerSeparation = 0.0f)
            "C" -> FingerAngles(thumbExtension = 0.35f, indexExtension = 0.28f, middleExtension = 0.28f, ringExtension = 0.28f, pinkyExtension = 0.28f, thumbSpread = 0.85f)
            "D" -> FingerAngles(thumbExtension = 0.45f, indexExtension = 0.98f, middleExtension = 0.35f, ringExtension = 0.35f, pinkyExtension = 0.35f, thumbSpread = 0.3f)
            "E" -> FingerAngles(thumbExtension = 0.3f, indexExtension = 0.20f, middleExtension = 0.20f, ringExtension = 0.20f, pinkyExtension = 0.20f, thumbSpread = 0.05f)
            "I" -> FingerAngles(thumbExtension = 0.2f, indexExtension = 0.08f, middleExtension = 0.08f, ringExtension = 0.08f, pinkyExtension = 0.98f, thumbSpread = 0.1f)
            "L" -> FingerAngles(thumbExtension = 0.95f, indexExtension = 0.98f, middleExtension = 0.08f, ringExtension = 0.08f, pinkyExtension = 0.08f, thumbSpread = 0.95f)
            "O" -> FingerAngles(thumbExtension = 0.22f, indexExtension = 0.18f, middleExtension = 0.18f, ringExtension = 0.18f, pinkyExtension = 0.18f, thumbSpread = 0.55f)
            "U" -> FingerAngles(thumbExtension = 0.2f, indexExtension = 0.95f, middleExtension = 0.95f, ringExtension = 0.08f, pinkyExtension = 0.08f, thumbSpread = 0.1f, fingerSeparation = 0.01f)
            "V" -> FingerAngles(thumbExtension = 0.2f, indexExtension = 0.95f, middleExtension = 0.95f, ringExtension = 0.08f, pinkyExtension = 0.08f, thumbSpread = 0.1f, fingerSeparation = 0.18f)
            "W" -> FingerAngles(thumbExtension = 0.2f, indexExtension = 0.95f, middleExtension = 0.95f, ringExtension = 0.95f, pinkyExtension = 0.08f, thumbSpread = 0.1f, fingerSeparation = 0.12f)
            "Y" -> FingerAngles(thumbExtension = 0.95f, indexExtension = 0.08f, middleExtension = 0.08f, ringExtension = 0.08f, pinkyExtension = 0.95f, thumbSpread = 0.9f)
            "HOLA" -> FingerAngles(thumbExtension = 0.98f, indexExtension = 0.98f, middleExtension = 0.98f, ringExtension = 0.98f, pinkyExtension = 0.98f, thumbSpread = 0.85f, fingerSeparation = 0.12f)
            "GRACIAS" -> FingerAngles(thumbExtension = 0.60f, indexExtension = 0.95f, middleExtension = 0.35f, ringExtension = 0.95f, pinkyExtension = 0.95f, thumbSpread = 0.4f)
            "TE_QUIERO" -> FingerAngles(thumbExtension = 0.95f, indexExtension = 0.95f, middleExtension = 0.08f, ringExtension = 0.08f, pinkyExtension = 0.95f, thumbSpread = 0.95f)
            "AYUDA" -> FingerAngles(thumbExtension = 0.98f, indexExtension = 0.08f, middleExtension = 0.08f, ringExtension = 0.08f, pinkyExtension = 0.08f, thumbSpread = 0.5f)
            else -> FingerAngles(0.5f, 0.5f, 0.5f, 0.5f, 0.5f)
        }

        return generatePose(config, jitterStdDev, random)
    }

    private fun generatePose(c: FingerAngles, jitter: Float, random: Random): HandPose {
        fun j(): Float = if (jitter > 0f) (random.nextFloat() * 2f - 1f) * jitter else 0f

        val landmarks = mutableListOf<HandLandmark>()

        // 0: Wrist
        val wristX = 0.50f + j()
        val wristY = 0.80f + j()
        val wristZ = 0.0f
        landmarks.add(HandLandmark(0, "WRIST", wristX, wristY, wristZ))

        // Palm base anchors
        // 1..4: Thumb
        val tAngle = -0.7f - c.thumbSpread * 0.7f
        val tLen = 0.08f + c.thumbExtension * 0.10f
        val tMcpX = wristX - 0.09f + j()
        val tMcpY = wristY - 0.08f + j()
        landmarks.add(HandLandmark(1, "THUMB_CMC", wristX - 0.05f + j(), wristY - 0.04f + j(), -0.01f))
        landmarks.add(HandLandmark(2, "THUMB_MCP", tMcpX, tMcpY, -0.02f))
        val tIpX = tMcpX + (cos(tAngle) * tLen * 0.55f).toFloat() + j()
        val tIpY = tMcpY + (sin(tAngle) * tLen * 0.55f).toFloat() + j()
        landmarks.add(HandLandmark(3, "THUMB_IP", tIpX, tIpY, -0.02f))
        landmarks.add(HandLandmark(4, "THUMB_TIP",
            tMcpX + (cos(tAngle) * tLen).toFloat() + j(),
            tMcpY + (sin(tAngle) * tLen).toFloat() + j(),
            -0.03f
        ))

        // Helper for fingers: MCP, PIP, DIP, TIP
        fun addFinger(mcpIndex: Int, baseName: String, baseX: Float, baseY: Float, ext: Float, angleOffset: Float) {
            val mcpX = baseX + j()
            val mcpY = baseY + j()
            landmarks.add(HandLandmark(mcpIndex, "${baseName}_MCP", mcpX, mcpY, 0.0f))

            // Angle points upward (-Y) with slight angleOffset
            val rad = -1.57f + angleOffset
            val totalLen = 0.08f + ext * 0.22f
            val curlFactor = 1.0f - ext // if ext is low, curl downward (+Y)

            val pipLen = totalLen * 0.38f
            val dipLen = totalLen * 0.32f
            val tipLen = totalLen * 0.30f

            val pipX = mcpX + (cos(rad) * pipLen).toFloat() + j()
            val pipY = mcpY + (sin(rad) * pipLen).toFloat() + j()
            landmarks.add(HandLandmark(mcpIndex + 1, "${baseName}_PIP", pipX, pipY, 0.0f))

            val dipRad = rad + curlFactor * 1.8f
            val dipX = pipX + (cos(dipRad) * dipLen).toFloat() + j()
            val dipY = pipY + (sin(dipRad) * dipLen).toFloat() + j()
            landmarks.add(HandLandmark(mcpIndex + 2, "${baseName}_DIP", dipX, dipY, 0.01f * curlFactor))

            val tipRad = dipRad + curlFactor * 1.4f
            val tipX = dipX + (cos(tipRad) * tipLen).toFloat() + j()
            val tipY = dipY + (sin(tipRad) * tipLen).toFloat() + j()
            landmarks.add(HandLandmark(mcpIndex + 3, "${baseName}_TIP", tipX, tipY, 0.02f * curlFactor))
        }

        // 5..8: Index
        val indexAngle = -0.15f - c.fingerSeparation * 0.8f
        addFinger(5, "INDEX", wristX - 0.08f, wristY - 0.25f, c.indexExtension, indexAngle)

        // 9..12: Middle
        val middleAngle = 0.0f + c.fingerSeparation * 0.3f
        addFinger(9, "MIDDLE", wristX - 0.01f, wristY - 0.27f, c.middleExtension, middleAngle)

        // 13..16: Ring
        addFinger(13, "RING", wristX + 0.06f, wristY - 0.25f, c.ringExtension, 0.12f)

        // 17..20: Pinky
        addFinger(17, "PINKY", wristX + 0.12f, wristY - 0.21f, c.pinkyExtension, 0.28f)

        return HandPose(landmarks = landmarks, handedness = "Right", score = 0.98f)
    }
}
