package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.CameraPreviewView
import com.example.model.ClassifierType
import com.example.model.LsmSign
import com.example.ui.LsmViewModel
import com.example.ui.components.HandSkeletonVisualizer
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealSecondary

@Composable
fun RecognitionScreen(
    viewModel: LsmViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPose by viewModel.currentPose.collectAsStateWithLifecycle()
    val activeResult by viewModel.activeClassification.collectAsStateWithLifecycle()
    val selectedClassifier by viewModel.selectedClassifier.collectAsStateWithLifecycle()
    val isCameraActive by viewModel.isCameraActive.collectAsStateWithLifecycle()
    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()
    val isSimulatorMode by viewModel.isSimulatorMode.collectAsStateWithLifecycle()
    val selectedSimSign by viewModel.selectedSimulatorSign.collectAsStateWithLifecycle()
    val simJitter by viewModel.simulatorJitter.collectAsStateWithLifecycle()
    val currentSentence by viewModel.currentSentence.collectAsStateWithLifecycle()
    val autoTranslateEnabled by viewModel.autoTranslateEnabled.collectAsStateWithLifecycle()
    val autoSpeakEnabled by viewModel.autoSpeakEnabled.collectAsStateWithLifecycle()
    val confidenceThreshold by viewModel.confidenceThreshold.collectAsStateWithLifecycle()
    val isHandDetectedInCamera by viewModel.isHandDetectedInCamera.collectAsStateWithLifecycle()
    val cameraTelemetryInfo by viewModel.cameraTelemetryInfo.collectAsStateWithLifecycle()
    val lastAppendedSign by viewModel.lastAppendedSign.collectAsStateWithLifecycle()
    val hearingResponseDisplay by viewModel.hearingResponseDisplay.collectAsStateWithLifecycle()
    val isDetectionPaused by viewModel.isDetectionPaused.collectAsStateWithLifecycle()
    val antiFaceFilterEnabled by viewModel.antiFaceFilterEnabled.collectAsStateWithLifecycle()
    val holdDurationMs by viewModel.holdDurationMs.collectAsStateWithLifecycle()
    val holdProgress by viewModel.holdProgress.collectAsStateWithLifecycle()
    val candidateHoldingSign by viewModel.candidateHoldingSign.collectAsStateWithLifecycle()
    val lastConfirmedSignFlash by viewModel.lastConfirmedSignFlash.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        viewModel.setCameraPermissionGranted(granted)
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var showTuneControls by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Model Selector Header (KNN, SVM, Random Forest, MLP)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MODELO DE CLASIFICACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    activeResult?.let { res ->
                        Text(
                            text = "Latencia: %.1f ms".format(res.inferenceTimeMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(ClassifierType.entries.toTypedArray()) { classifier ->
                        val isSelected = classifier == selectedClassifier
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectClassifier(classifier) },
                            label = {
                                Text(
                                    text = classifier.shortName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("chip_classifier_${classifier.shortName.lowercase()}")
                        )
                    }
                }
            }
        }

        // 2. Camera View & MediaPipe Landmark Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A))
                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            // Camera feed or Simulator Canvas
            if (isCameraActive && hasCameraPermission && !isSimulatorMode) {
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize(),
                    isFrontCamera = isFrontCamera,
                    isDetectionPaused = isDetectionPaused,
                    antiFaceFilterEnabled = antiFaceFilterEnabled,
                    onHandPoseDetected = { pose ->
                        viewModel.onCameraPoseDetected(pose)
                    },
                    onTelemetryUpdate = { isHand, fingers, info ->
                        viewModel.onCameraTelemetryUpdated(isHand, fingers, info)
                    }
                )
            } else {
                // Simulator / Hand Pose Studio background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF020617))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!hasCameraPermission && !isSimulatorMode) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideocamOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Permiso de cámara no concedido",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                            ) {
                                Text("Activar Cámara", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }

            // Real-time Hand Target ROI Guide Box (Isolates hand & eliminates face detection)
            if (!isSimulatorMode && hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 58.dp, bottom = 32.dp, start = 32.dp, end = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isHandInside = currentPose != null && isHandDetectedInCamera && !isDetectionPaused
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = if (isHandInside) 2.5.dp else 1.5.dp,
                                color = if (isDetectionPaused) Color(0x88FFA000)
                                        else if (isHandInside) EmeraldSuccess
                                        else Color(0x66FFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(
                                if (isHandInside) EmeraldSuccess.copy(alpha = 0.06f)
                                else Color.Transparent
                            )
                    ) {
                        // Top badge of target box
                        Surface(
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                            color = if (isDetectionPaused) Color(0xCCB91C1C)
                                    else if (isHandInside) EmeraldSuccess.copy(alpha = 0.90f)
                                    else Color(0xAA000000),
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            Text(
                                text = if (isDetectionPaused) "DETECCIÓN EN PAUSA"
                                       else if (isHandInside) "MANO EN RECUADRO ✓"
                                       else "COLOCA TU MANO AQUÍ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        // Subtitle reminder that face is excluded
                        if (!isHandInside && !isDetectionPaused) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                color = Color(0x88000000),
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                Text(
                                    text = "Rostro y fondo excluidos",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xDDFFFFFF),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real-time Hand Landmark Skeleton Overlay (21 points)
            HandSkeletonVisualizer(
                pose = currentPose,
                modifier = Modifier.fillMaxSize(),
                mirrorHorizontally = isFrontCamera && !isSimulatorMode
            )

            // Top Camera Bar overlay controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode indicator badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC000000)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDetectionPaused) Color(0xFFFFA000)
                                    else if (isSimulatorMode) TealSecondary
                                    else EmeraldSuccess
                                )
                        )
                        Text(
                            text = if (isDetectionPaused) "EN PAUSA"
                                   else if (isSimulatorMode) "ESTUDIO LSM"
                                   else "CÁMARA VIVO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Pause / Resume Detection Button
                    if (!isSimulatorMode) {
                        IconButton(
                            onClick = { viewModel.toggleDetectionPause() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDetectionPaused) Color(0xFFE11D48) else Color(0xAA000000))
                                .testTag("btn_toggle_pause")
                        ) {
                            Icon(
                                imageVector = if (isDetectionPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isDetectionPaused) "Reanudar detección" else "Pausar detección",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.setSimulatorMode(!isSimulatorMode) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xAA000000))
                            .testTag("btn_toggle_simulator")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Cambiar a modo Estudio/Simulador",
                            tint = if (isSimulatorMode) CyanPrimary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!isSimulatorMode && hasCameraPermission) {
                        IconButton(
                            onClick = { viewModel.toggleCameraLens() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xAA000000))
                                .testTag("btn_flip_camera")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Cambiar cámara frontal/trasera",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quick instant capture button
                    if (!isSimulatorMode && candidateHoldingSign != null) {
                        IconButton(
                            onClick = { viewModel.captureCandidateSignNow() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary)
                                .testTag("btn_capture_now")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Registrar seña ahora",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showTuneControls = !showTuneControls },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xAA000000))
                            .testTag("btn_tune_controls")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Configurar parámetros",
                            tint = if (showTuneControls) CyanPrimary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom camera overlay: Live Telemetry & Hand Status
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD000000)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (currentPose != null) EmeraldSuccess else Color(0xFFFFA000))
                        )
                        Text(
                            text = if (isSimulatorMode) {
                                "Estudio Biomecánico LSM • 21 Puntos"
                            } else {
                                cameraTelemetryInfo
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentPose != null) CyanPrimary else Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Confirmation Celebration Flash Banner
            if (lastConfirmedSignFlash != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 54.dp)
                ) {
                    lastConfirmedSignFlash?.let { confirmed ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldSuccess.copy(alpha = 0.95f),
                            shadowElevation = 6.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "¡Seña ${confirmed.displayName} confirmada! (${confirmed.spokenText})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else if (candidateHoldingSign != null && !isDetectionPaused) {
                // Real-time 2.0s Hold-to-Confirm Stabilization Bar (Prevents accidental sign floods)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 48.dp)
                        .fillMaxWidth(0.92f)
                ) {
                    candidateHoldingSign?.let { candidate ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xEE0F172A),
                            border = BorderStroke(1.5.dp, CyanPrimary),
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = candidate.iconEmoji,
                                            fontSize = 18.sp
                                        )
                                        Column {
                                            Text(
                                                text = "Sosteniendo: ${candidate.displayName}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            val remainingSeconds = ((holdDurationMs * (1f - holdProgress)) / 1000f).coerceAtLeast(0f)
                                            Text(
                                                text = "%.1fs restantes para registrar".format(remainingSeconds),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyanPrimary,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.captureCandidateSignNow() },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = "Confirmar ya",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                }

                                LinearProgressIndicator(
                                    progress = { holdProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = CyanPrimary,
                                    trackColor = Color(0x44FFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Sign Assistant Carousel (Allows testing and selecting any sign with 1 tap)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "GUÍA Y SELECTOR RÁPIDO DE SEÑAS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "16 señas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(LsmSign.VOCABULARY) { sign ->
                        val isCurrentActive = activeResult?.sign?.id == sign.id
                        FilterChip(
                            selected = isCurrentActive,
                            onClick = {
                                viewModel.forceSelectSign(sign.id)
                            },
                            label = { Text("${sign.displayName} ${sign.iconEmoji}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanPrimary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // 4. Tune Controls (Auto-translate, Auto-speak, Confidence Threshold)
        AnimatedVisibility(visible = showTuneControls, enter = fadeIn(), exit = fadeOut()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recopilación automática al orador",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Va acumulando palabras según se reconocen las señas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoTranslateEnabled,
                            onCheckedChange = { viewModel.toggleAutoTranslate() },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Filtro Anti-Rostro Inteligente",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Excluye el rostro y cuello para evitar letras falsas involuntarias",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = antiFaceFilterEnabled,
                            onCheckedChange = { viewModel.toggleAntiFaceFilter() },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pausar Detección en Cámara",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Detiene la emisión de señas cuando no estés gesticulando",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDetectionPaused,
                            onCheckedChange = { viewModel.toggleDetectionPause() },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lectura de voz automática (TTS)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = autoSpeakEnabled,
                            onCheckedChange = { viewModel.toggleAutoSpeak() },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Umbral de Confianza de Detección",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${(confidenceThreshold * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = confidenceThreshold,
                            onValueChange = { viewModel.setConfidenceThreshold(it) },
                            valueRange = 0.40f..0.90f,
                            colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                        )
                    }

                    // Hold Confirmation Duration (User requested: ~2 seconds)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Tiempo de sostenido para confirmar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "%.1fs".format(holdDurationMs / 1000f),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Sostén la seña fija este tiempo para registrarla y evitar saturación accidental.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val options = listOf(1000L to "1.0s", 1500L to "1.5s", 2000L to "2.0s ★", 2500L to "2.5s", 3000L to "3.0s")
                            options.forEach { (ms, label) ->
                                val selected = holdDurationMs == ms
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setHoldDuration(ms) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanPrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // 100% Free / On-Device Assurance Card
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0x2200F5D4),
                        border = BorderStroke(1.dp, Color(0x4400F5D4))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "100% On-Device, Gratuito y Privado",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Sin suscripciones ni APIs de pago. Google ML Kit, síntesis TTS y clasificadores corren localmente en tu teléfono sin conexión ni costos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. TRADUCTOR EN VIVO (Sign-to-Sentence Progressive Conversation Translator)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.testTag("translator_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "TRADUCTOR EN VIVO • ORACIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (autoTranslateEnabled) EmeraldSuccess.copy(alpha = 0.15f) else Color(0x22888888)
                    ) {
                        Text(
                            text = if (autoTranslateEnabled) "● RECOPILANDO" else "PAUSADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (autoTranslateEnabled) EmeraldSuccess else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Subtitle Display Area (Large readable text for the hearing person)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF020617))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentSentence.isNotBlank()) {
                            Text(
                                text = currentSentence,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 28.sp
                            )
                        } else {
                            Text(
                                text = "Realiza señas frente a la cámara. La app irá recopilando y armando la oración en tiempo real para que puedas entender la conversación...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8),
                                lineHeight = 22.sp
                            )
                        }

                        // Last appended sign indicator
                        lastAppendedSign?.let { sign ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyanPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Última seña agregada: ${sign.displayName} ${sign.iconEmoji}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Controls for Sentence Builder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.speakCurrentSentence() },
                        enabled = currentSentence.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hablar Oración",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.addSpaceToSentence() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SpaceBar,
                            contentDescription = "Agregar Espacio",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.backspaceSentence() },
                        enabled = currentSentence.isNotEmpty(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Borrar Última Letra",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Traducción LSM", currentSentence))
                            Toast.makeText(context, "Oración copiada al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        enabled = currentSentence.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar Oración",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearSentence() },
                        enabled = currentSentence.isNotEmpty(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar Oración",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // 6. Two-Way Communication: Hearing Person Quick Reply Panel
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "RESPUESTAS RÁPIDAS PARA EL OYENTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca para responder y reproducir voz al signante:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val quickReplies = listOf(
                    "¡Mucho gusto! 👋" to "Mucho gusto",
                    "De nada, con gusto 😊" to "De nada, con gusto",
                    "¿Cómo estás? ❓" to "¿Cómo estás?",
                    "Sí, de acuerdo 👍" to "Sí, de acuerdo",
                    "No, gracias ✋" to "No, gracias",
                    "¿En qué te puedo ayudar? 🤝" to "¿En qué te puedo ayudar?"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickReplies) { (display, spoken) ->
                        OutlinedButton(
                            onClick = { viewModel.sendHearingReply(spoken) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = display, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // If hearing reply is active, show banner
                hearingResponseDisplay?.let { reply ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyanPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💬 Oyente dijo: \"$reply\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                            Text(
                                text = "Cerrar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { viewModel.dismissHearingReply() }
                            )
                        }
                    }
                }
            }
        }

        // 7. Active Recognition Details Card (Sign anatomy & probabilities)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.testTag("recognition_result_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DETALLE DE SEÑA ACTUAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    activeResult?.let { res ->
                        val isHighConfidence = res.confidence >= confidenceThreshold
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isHighConfidence) EmeraldSuccess.copy(alpha = 0.15f) else Color(0x22FFA000)
                        ) {
                            Text(
                                text = "${(res.confidence * 100).toInt()}% Confianza",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isHighConfidence) EmeraldSuccess else Color(0xFFFFA000),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (activeResult != null) {
                    val res = activeResult!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Emoji & Sign icon box
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CyanPrimary.copy(alpha = 0.15f))
                                .border(1.5.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res.sign.iconEmoji,
                                fontSize = 30.sp
                            )
                        }

                        // Sign name & instructions
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = res.sign.displayName,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = res.sign.category,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Pronounce button
                        IconButton(
                            onClick = { viewModel.speakSign(res.sign) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyanPrimary)
                                .testTag("btn_speak_sign"),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronunciar seña en voz alta"
                            )
                        }

                        // Add to sentence builder
                        IconButton(
                            onClick = { viewModel.appendSignToSentence(res.sign) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("btn_add_to_sentence")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar a la frase",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Confidence bar
                    LinearProgressIndicator(
                        progress = { res.confidence },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (res.confidence >= confidenceThreshold) CyanPrimary else Color(0xFFFFA000),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    // Description text
                    Text(
                        text = res.sign.instructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    // Top 3 candidate probabilities breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Probabilidades calculadas por ${selectedClassifier.shortName}:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        val top3 = res.classProbabilities.entries
                            .sortedByDescending { it.value }
                            .take(3)
                        top3.forEach { (signId, prob) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = LsmSign.findById(signId)?.displayName ?: signId,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "%.1f%%".format(prob * 100),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (signId == res.sign.id) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Muestra tu mano frente a la cámara o selecciona una seña en la guía rápida para comenzar a reconocer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
