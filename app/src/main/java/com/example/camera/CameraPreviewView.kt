package com.example.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.model.HandPose
import java.util.concurrent.Executors

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    isFrontCamera: Boolean = true,
    isDetectionPaused: Boolean = false,
    antiFaceFilterEnabled: Boolean = true,
    onHandPoseDetected: (HandPose?) -> Unit = {},
    onTelemetryUpdate: (isHandPresent: Boolean, fingerCount: Int, info: String) -> Unit = { _, _, _ -> },
    onError: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val analyzerExecutor = remember { Executors.newSingleThreadExecutor() }
    var currentAnalyzer: HandVisionAnalyzer? = remember { null }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentAnalyzer?.close()
            analyzerExecutor.shutdown()
        }
    }

    LaunchedEffect(isFrontCamera, isDetectionPaused, antiFaceFilterEnabled) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()

                currentAnalyzer?.close()
                val analyzer = HandVisionAnalyzer(
                    context = context,
                    isFrontCamera = { isFrontCamera },
                    isDetectionPaused = { isDetectionPaused },
                    antiFaceFilterEnabled = { antiFaceFilterEnabled },
                    onHandPoseDetected = { pose ->
                        ContextCompat.getMainExecutor(context).execute {
                            onHandPoseDetected(pose)
                        }
                    },
                    onHandTelemetry = { isHand, fingers, info ->
                        ContextCompat.getMainExecutor(context).execute {
                            onTelemetryUpdate(isHand, fingers, info)
                        }
                    }
                )
                currentAnalyzer = analyzer

                imageAnalysis.setAnalyzer(analyzerExecutor, analyzer)

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("CameraPreviewView", "Error binding camera lifecycle with analyzer", e)
                onError(e.localizedMessage ?: "Error al iniciar cámara")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
