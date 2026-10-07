package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import com.example.model.HandLandmark
import com.example.model.HandPose
import com.example.ui.theme.IndexColor
import com.example.ui.theme.MiddleColor
import com.example.ui.theme.PinkyColor
import com.example.ui.theme.RingColor
import com.example.ui.theme.SkeletonLineColor
import com.example.ui.theme.ThumbColor
import com.example.ui.theme.WristColor

@Composable
fun HandSkeletonVisualizer(
    pose: HandPose?,
    modifier: Modifier = Modifier,
    showJointLabels: Boolean = false,
    mirrorHorizontally: Boolean = false
) {
    Box(
        modifier = modifier
            .background(Color.Transparent)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (pose == null || pose.landmarks.size < 21) return@Canvas

            val canvasWidth = size.width
            val canvasHeight = size.height

            fun getScreenCoord(lm: HandLandmark): Offset {
                val normX = if (mirrorHorizontally) (1f - lm.x) else lm.x
                return Offset(
                    x = normX * canvasWidth,
                    y = lm.y * canvasHeight
                )
            }

            val coords = pose.landmarks.map { getScreenCoord(it) }

            // 1. Draw Skeleton Bones (MediaPipe links)
            for ((fromIdx, toIdx) in HandPose.SKELETON_CONNECTIONS) {
                if (fromIdx < coords.size && toIdx < coords.size) {
                    val p1 = coords[fromIdx]
                    val p2 = coords[toIdx]

                    // Subtle outer glow
                    drawLine(
                        color = Color(0x3300F5D4),
                        start = p1,
                        end = p2,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )

                    // Core skeleton line
                    drawLine(
                        color = SkeletonLineColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Draw Palm filled triangle/polygon
            val wrist = coords[0]
            val indexMcp = coords[5]
            val pinkyMcp = coords[17]
            val middleMcp = coords[9]

            // 3. Draw Landmark Joints (Nodes)
            for (i in coords.indices) {
                val pt = coords[i]
                val (nodeColor, radius) = when (i) {
                    0 -> WristColor to 9f // Wrist
                    4 -> ThumbColor to 8f // Thumb Tip
                    1, 2, 3 -> ThumbColor to 5.5f
                    8 -> IndexColor to 8f // Index Tip
                    5, 6, 7 -> IndexColor to 5.5f
                    12 -> MiddleColor to 8f // Middle Tip
                    9, 10, 11 -> MiddleColor to 5.5f
                    16 -> RingColor to 8f // Ring Tip
                    13, 14, 15 -> RingColor to 5.5f
                    20 -> PinkyColor to 8f // Pinky Tip
                    17, 18, 19 -> PinkyColor to 5.5f
                    else -> Color.White to 5f
                }

                // Outer halo
                drawCircle(
                    color = nodeColor.copy(alpha = 0.4f),
                    radius = radius + 4f,
                    center = pt
                )
                // Solid joint center
                drawCircle(
                    color = nodeColor,
                    radius = radius,
                    center = pt
                )
                // White highlight core
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.4f,
                    center = pt
                )
            }
        }
    }
}
