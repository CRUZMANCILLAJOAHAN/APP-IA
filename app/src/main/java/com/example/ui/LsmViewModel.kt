package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.LsmRepository
import com.example.data.RecognitionHistoryEntity
import com.example.data.SignDatasetEntity
import com.example.ml.LsmClassifier
import com.example.ml.LsmModelManager
import com.example.model.BenchmarkData
import com.example.model.CanonicalHandPoses
import com.example.model.ClassificationResult
import com.example.model.ClassifierType
import com.example.model.HandPose
import com.example.model.LsmSign
import com.example.model.ModelBenchmark
import com.example.tts.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class LsmViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = LsmRepository(database.signDatasetDao(), database.recognitionHistoryDao())
    val modelManager = LsmModelManager()
    val ttsManager = TtsManager(application)

    // Current active classifier (default to Random Forest / KNN for fast, robust inference)
    private val _selectedClassifier = MutableStateFlow(ClassifierType.RANDOM_FOREST)
    val selectedClassifier: StateFlow<ClassifierType> = _selectedClassifier.asStateFlow()

    // Hand pose & classification state
    private val _currentPose = MutableStateFlow<HandPose?>(null)
    val currentPose: StateFlow<HandPose?> = _currentPose.asStateFlow()

    private val _activeClassification = MutableStateFlow<ClassificationResult?>(null)
    val activeClassification: StateFlow<ClassificationResult?> = _activeClassification.asStateFlow()

    // Mode toggles
    private val _isCameraActive = MutableStateFlow(true)
    val isCameraActive: StateFlow<Boolean> = _isCameraActive.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    // Live Camera Telemetry
    private val _isHandDetectedInCamera = MutableStateFlow(false)
    val isHandDetectedInCamera: StateFlow<Boolean> = _isHandDetectedInCamera.asStateFlow()

    private val _cameraTelemetryInfo = MutableStateFlow("Coloca tu mano dentro del recuadro")
    val cameraTelemetryInfo: StateFlow<String> = _cameraTelemetryInfo.asStateFlow()

    private val _isDetectionPaused = MutableStateFlow(false)
    val isDetectionPaused: StateFlow<Boolean> = _isDetectionPaused.asStateFlow()

    private val _antiFaceFilterEnabled = MutableStateFlow(true)
    val antiFaceFilterEnabled: StateFlow<Boolean> = _antiFaceFilterEnabled.asStateFlow()

    // Simulator / Studio mode
    private val _isSimulatorMode = MutableStateFlow(false)
    val isSimulatorMode: StateFlow<Boolean> = _isSimulatorMode.asStateFlow()

    private val _selectedSimulatorSign = MutableStateFlow("HOLA")
    val selectedSimulatorSign: StateFlow<String> = _selectedSimulatorSign.asStateFlow()

    private val _simulatorJitter = MutableStateFlow(0.015f)
    val simulatorJitter: StateFlow<Float> = _simulatorJitter.asStateFlow()

    // Real-Time Sentence Translator Engine
    private val _currentSentence = MutableStateFlow("")
    val currentSentence: StateFlow<String> = _currentSentence.asStateFlow()

    private val _autoTranslateEnabled = MutableStateFlow(true)
    val autoTranslateEnabled: StateFlow<Boolean> = _autoTranslateEnabled.asStateFlow()

    private val _autoSpeakEnabled = MutableStateFlow(true)
    val autoSpeakEnabled: StateFlow<Boolean> = _autoSpeakEnabled.asStateFlow()

    private val _confidenceThreshold = MutableStateFlow(0.65f)
    val confidenceThreshold: StateFlow<Float> = _confidenceThreshold.asStateFlow()

    private val _lastAppendedSign = MutableStateFlow<LsmSign?>(null)
    val lastAppendedSign: StateFlow<LsmSign?> = _lastAppendedSign.asStateFlow()

    // Hold-to-Confirm Timer Engine (User request: 2.0 seconds stabilization before committing sign)
    private val _holdDurationMs = MutableStateFlow(2000L)
    val holdDurationMs: StateFlow<Long> = _holdDurationMs.asStateFlow()

    private val _holdProgress = MutableStateFlow(0f)
    val holdProgress: StateFlow<Float> = _holdProgress.asStateFlow()

    private val _candidateHoldingSign = MutableStateFlow<LsmSign?>(null)
    val candidateHoldingSign: StateFlow<LsmSign?> = _candidateHoldingSign.asStateFlow()

    private val _lastConfirmedSignFlash = MutableStateFlow<LsmSign?>(null)
    val lastConfirmedSignFlash: StateFlow<LsmSign?> = _lastConfirmedSignFlash.asStateFlow()

    private var currentHoldingSignId: String? = null
    private var holdStartTimestamp = 0L
    private var lastConfirmedTimestamp = 0L

    // Hearing person's active spoken message / response
    private val _hearingResponseDisplay = MutableStateFlow<String?>(null)
    val hearingResponseDisplay: StateFlow<String?> = _hearingResponseDisplay.asStateFlow()

    // Dataset & History
    val datasetSamples: StateFlow<List<SignDatasetEntity>> = repository.allSamples
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSampleCount: StateFlow<Int> = repository.totalSampleCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentHistory: StateFlow<List<RecognitionHistoryEntity>> = repository.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live continuous recognition tracker
    private var recognitionLoopJob: Job? = null
    private var lastCandidateSignId: String? = null
    private var sustainedDetectionCounter = 0
    private var lastAppendedTimestamp = 0L
    private var lastPoseReceivedTimestamp = 0L

    init {
        viewModelScope.launch {
            repository.seedInitialDatasetIfNeeded()
        }
        startRecognitionLoop()
    }

    fun setCameraPermissionGranted(granted: Boolean) {
        _hasCameraPermission.value = granted
    }

    fun toggleCameraLens() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun toggleCameraActive() {
        _isCameraActive.value = !_isCameraActive.value
    }

    fun setSimulatorMode(active: Boolean) {
        _isSimulatorMode.value = active
        if (active) {
            setSimulatorSign(_selectedSimulatorSign.value)
        } else {
            _currentPose.value = null
            _activeClassification.value = null
        }
    }

    fun setSimulatorSign(signId: String) {
        _selectedSimulatorSign.value = signId
        val pose = CanonicalHandPoses.getPoseForSign(signId, jitterStdDev = _simulatorJitter.value)
        _currentPose.value = pose
        processCurrentPose(pose)
    }

    fun updateSimulatorJitter(jitter: Float) {
        _simulatorJitter.value = jitter
        val pose = CanonicalHandPoses.getPoseForSign(_selectedSimulatorSign.value, jitterStdDev = jitter)
        _currentPose.value = pose
        processCurrentPose(pose)
    }

    fun selectClassifier(type: ClassifierType) {
        _selectedClassifier.value = type
        _currentPose.value?.let { processCurrentPose(it) }
    }

    fun toggleAutoSpeak() {
        _autoSpeakEnabled.value = !_autoSpeakEnabled.value
    }

    fun toggleAutoTranslate() {
        _autoTranslateEnabled.value = !_autoTranslateEnabled.value
    }

    fun setConfidenceThreshold(threshold: Float) {
        _confidenceThreshold.value = threshold
    }

    fun toggleDetectionPause() {
        _isDetectionPaused.value = !_isDetectionPaused.value
        if (_isDetectionPaused.value) {
            _currentPose.value = null
            _activeClassification.value = null
            _cameraTelemetryInfo.value = "Detección pausada"
            sustainedDetectionCounter = 0
            lastCandidateSignId = null
        }
    }

    fun toggleAntiFaceFilter() {
        _antiFaceFilterEnabled.value = !_antiFaceFilterEnabled.value
    }

    fun setHoldDuration(durationMs: Long) {
        _holdDurationMs.value = durationMs
    }

    fun captureCandidateSignNow() {
        val candidate = _candidateHoldingSign.value ?: _activeClassification.value?.sign
        if (candidate != null) {
            commitConfirmedSign(candidate)
        }
    }

    private fun resetHoldState() {
        currentHoldingSignId = null
        holdStartTimestamp = 0L
        _holdProgress.value = 0f
        _candidateHoldingSign.value = null
    }

    /**
     * Called by CameraPreviewView's HandVisionAnalyzer when a live camera frame is analyzed.
     */
    fun onCameraPoseDetected(pose: HandPose?) {
        if (_isSimulatorMode.value || _isDetectionPaused.value) return

        if (pose == null) {
            _isHandDetectedInCamera.value = false
            _cameraTelemetryInfo.value = "Coloca tu mano dentro del recuadro"
            _currentPose.value = null
            _activeClassification.value = null
            sustainedDetectionCounter = 0
            lastCandidateSignId = null
            resetHoldState()
            return
        }

        lastPoseReceivedTimestamp = System.currentTimeMillis()
        _isHandDetectedInCamera.value = true
        _currentPose.value = pose
        processCurrentPose(pose)
    }

    fun onCameraTelemetryUpdated(isHand: Boolean, fingerCount: Int, info: String) {
        if (!_isSimulatorMode.value) {
            _isHandDetectedInCamera.value = isHand
            _cameraTelemetryInfo.value = info
            if (!isHand) {
                _currentPose.value = null
                _activeClassification.value = null
                sustainedDetectionCounter = 0
                lastCandidateSignId = null
                resetHoldState()
            }
        }
    }

    /**
     * Executes classification on the given hand pose using the currently active model.
     * Incorporates 2.0s hold-to-confirm stabilization before committing to translator.
     */
    fun processCurrentPose(pose: HandPose) {
        val classifier = modelManager.getClassifier(_selectedClassifier.value)
        val result = classifier.predict(pose)
        _activeClassification.value = result

        val now = System.currentTimeMillis()
        if (result.confidence >= _confidenceThreshold.value) {
            val signId = result.sign.id
            if (signId == currentHoldingSignId) {
                val elapsed = now - holdStartTimestamp
                val target = _holdDurationMs.value
                val progress = (elapsed.toFloat() / target).coerceIn(0f, 1f)
                _holdProgress.value = progress

                // When held steadily for the target duration (default: 2.0 seconds)
                if (progress >= 1.0f && (now - lastConfirmedTimestamp > 1400L)) {
                    commitConfirmedSign(result.sign)
                }
            } else {
                currentHoldingSignId = signId
                holdStartTimestamp = now
                _candidateHoldingSign.value = result.sign
                _holdProgress.value = 0f
            }
        } else {
            resetHoldState()
        }
    }

    private fun commitConfirmedSign(sign: LsmSign) {
        val now = System.currentTimeMillis()
        lastConfirmedTimestamp = now
        _lastConfirmedSignFlash.value = sign
        _lastAppendedSign.value = sign
        _holdProgress.value = 0f
        holdStartTimestamp = now

        if (_autoTranslateEnabled.value) {
            appendSignToSentence(sign)
        }

        if (_autoSpeakEnabled.value) {
            ttsManager.speak(sign.spokenText)
        }

        viewModelScope.launch {
            repository.recordRecognition(
                signId = sign.id,
                confidence = _activeClassification.value?.confidence ?: 0.95f,
                classifierUsed = _selectedClassifier.value.shortName,
                inferenceTimeMs = _activeClassification.value?.inferenceTimeMs ?: 5f,
                spokenText = sign.spokenText
            )
            // Celebration flash visible for 1.2 seconds
            delay(1200)
            if (_lastConfirmedSignFlash.value?.id == sign.id) {
                _lastConfirmedSignFlash.value = null
            }
        }
    }

    private fun startRecognitionLoop() {
        recognitionLoopJob?.cancel()
        recognitionLoopJob = viewModelScope.launch {
            while (true) {
                delay(100) // 10Hz watchdog & simulator loop
                if (_isSimulatorMode.value) {
                    val current = _currentPose.value
                    if (current != null) {
                        if (_simulatorJitter.value > 0f) {
                            val jittered = CanonicalHandPoses.getPoseForSign(
                                _selectedSimulatorSign.value,
                                jitterStdDev = _simulatorJitter.value,
                                random = Random
                            )
                            _currentPose.value = jittered
                            processCurrentPose(jittered)
                        } else {
                            processCurrentPose(current)
                        }
                    }
                } else if (!_isDetectionPaused.value) {
                    // Watchdog: If no hand pose has been reported by camera analyzer within 250ms,
                    // immediately clear state to prevent stuck "green" or phantom sign states.
                    val now = System.currentTimeMillis()
                    if (_currentPose.value != null && (now - lastPoseReceivedTimestamp > 250L)) {
                        _isHandDetectedInCamera.value = false
                        _currentPose.value = null
                        _activeClassification.value = null
                        _cameraTelemetryInfo.value = "Coloca tu mano dentro del recuadro"
                        sustainedDetectionCounter = 0
                        lastCandidateSignId = null
                        resetHoldState()
                    }
                }
            }
        }
    }

    // Sentence builder actions for the Translator
    fun appendSignToSentence(sign: LsmSign) {
        val current = _currentSentence.value
        val addition = if (sign.category == "Palabras Clave") {
            if (current.isEmpty() || current.endsWith(" ")) "${sign.displayName} " else " ${sign.displayName} "
        } else {
            // Letters concatenate (e.g. H + O + L + A)
            sign.displayName
        }
        _currentSentence.value = current + addition
        _lastAppendedSign.value = sign
    }

    fun addSpaceToSentence() {
        if (_currentSentence.value.isNotEmpty() && !_currentSentence.value.endsWith(" ")) {
            _currentSentence.value += " "
        }
    }

    fun backspaceSentence() {
        val current = _currentSentence.value
        if (current.isNotEmpty()) {
            _currentSentence.value = current.dropLast(1)
        }
    }

    fun clearSentence() {
        _currentSentence.value = ""
        _lastAppendedSign.value = null
    }

    fun speakCurrentSentence() {
        if (_currentSentence.value.isNotBlank()) {
            ttsManager.speak(_currentSentence.value)
        }
    }

    fun speakSign(sign: LsmSign) {
        ttsManager.speak(sign.spokenText)
    }

    // Two-way communication: Hearing person speaks or sends quick response
    fun sendHearingReply(reply: String) {
        _hearingResponseDisplay.value = reply
        ttsManager.speak(reply)
        viewModelScope.launch {
            delay(5000)
            if (_hearingResponseDisplay.value == reply) {
                _hearingResponseDisplay.value = null
            }
        }
    }

    fun dismissHearingReply() {
        _hearingResponseDisplay.value = null
    }

    // Quick override helper for the user to force or test a sign instantly
    fun forceSelectSign(signId: String) {
        val pose = CanonicalHandPoses.getPoseForSign(signId, jitterStdDev = 0.005f)
        _currentPose.value = pose
        processCurrentPose(pose)
    }

    // Dataset Recording
    fun recordSampleForSign(signId: String) {
        viewModelScope.launch {
            val pose = _currentPose.value ?: CanonicalHandPoses.getPoseForSign(signId)
            val features = pose.extractNormalizedFeatures()
            val source = if (_isSimulatorMode.value) "SIMULATOR" else "CAMERA_CAPTURE"
            repository.saveSample(signId, features, source)
            modelManager.addSampleToAll(signId, features)
        }
    }

    fun deleteDatasetSample(id: Long) {
        viewModelScope.launch {
            repository.deleteSample(id)
        }
    }

    fun clearDataset() {
        viewModelScope.launch {
            repository.clearDataset()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun getBenchmarkFor(type: ClassifierType): ModelBenchmark {
        return BenchmarkData.getBenchmark(type)
    }

    override fun onCleared() {
        super.onCleared()
        recognitionLoopJob?.cancel()
        ttsManager.shutdown()
    }
}
