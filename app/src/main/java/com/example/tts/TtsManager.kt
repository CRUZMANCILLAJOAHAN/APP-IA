package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val localeMx = Locale.Builder().setLanguage("es").setRegion("MX").build()
            var result = tts?.setLanguage(localeMx)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to general Spanish
                val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()
                result = tts?.setLanguage(localeEs)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val defaultLocale = Locale.getDefault()
                    tts?.setLanguage(defaultLocale)
                }
            }

            tts?.setSpeechRate(0.95f) // Slightly relaxed pace for sign clarity
            tts?.setPitch(1.0f)
            _isReady.value = true
        } else {
            Log.e("TtsManager", "TTS initialization failed with status $status")
            _isReady.value = false
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        if (text.isBlank()) return
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "LSM_TTS_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.w("TtsManager", "Error shutting down TTS", e)
        }
    }
}
