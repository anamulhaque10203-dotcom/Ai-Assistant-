package com.example.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class NovaVoiceOption(
    val id: String,
    val displayName: String,
    val localeTag: String,
    val isFemale: Boolean
)

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<NovaVoiceOption>>(emptyList())
    val availableVoices: StateFlow<List<NovaVoiceOption>> = _availableVoices.asStateFlow()

    private var onSpeechDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isReady.value = true
            setupProgressListener()
            refreshAvailableVoices()
        } else {
            _isReady.value = false
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onSpeechDoneCallback?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
            }
        })
    }

    fun refreshAvailableVoices() {
        val ttsInstance = tts ?: return
        val voiceList = mutableListOf<NovaVoiceOption>()

        try {
            val installedVoices = ttsInstance.voices
            if (!installedVoices.isNullOrEmpty()) {
                for (voice in installedVoices) {
                    val loc = voice.locale
                    val name = voice.name
                    val isFemale = name.contains("female", ignoreCase = true) || name.contains("-f-", ignoreCase = true)
                    val label = when {
                        loc.language == "bn" -> if (isFemale) "Google Female (bn-BD)" else "Google Male (bn-BD)"
                        loc.language == "hi" -> if (isFemale) "Google Female (hi-IN)" else "Google Male (hi-IN)"
                        loc.language == "en" -> if (isFemale) "Google Female (en-US)" else "Google Male (en-US)"
                        loc.language == "es" -> if (isFemale) "Google Female (es-ES)" else "Google Male (es-ES)"
                        loc.language == "ar" -> if (isFemale) "Google Female (ar-SA)" else "Google Male (ar-SA)"
                        else -> voice.name
                    }
                    voiceList.add(NovaVoiceOption(id = voice.name, displayName = label, localeTag = loc.toLanguageTag(), isFemale = isFemale))
                }
            }
        } catch (_: Exception) {}

        // Fallback default options if device list is empty or restricted
        if (voiceList.isEmpty()) {
            voiceList.addAll(
                listOf(
                    NovaVoiceOption("en_us_female", "Google Female (en-US)", "en-US", true),
                    NovaVoiceOption("en_us_male", "Google Male (en-US)", "en-US", false),
                    NovaVoiceOption("bn_bd_female", "Google Female (bn-BD)", "bn-BD", true),
                    NovaVoiceOption("bn_bd_male", "Google Male (bn-BD)", "bn-BD", false),
                    NovaVoiceOption("hi_in_female", "Google Female (hi-IN)", "hi-IN", true)
                )
            )
        }

        // Distinct by displayName to keep UI neat
        _availableVoices.value = voiceList.distinctBy { it.displayName }
    }

    fun speak(
        text: String,
        languageCode: String,
        speechRate: Float = 1.0f,
        voiceName: String? = null,
        onDone: (() -> Unit)? = null
    ) {
        val ttsInstance = tts ?: return
        if (!_isReady.value || text.isBlank()) return

        onSpeechDoneCallback = onDone

        val locale = when (languageCode.lowercase()) {
            "bn", "bengali" -> Locale.forLanguageTag("bn-BD")
            "hi", "hindi" -> Locale.forLanguageTag("hi-IN")
            "ar", "arabic" -> Locale.forLanguageTag("ar-SA")
            "es", "spanish" -> Locale.forLanguageTag("es-ES")
            else -> Locale.US
        }

        ttsInstance.language = locale
        ttsInstance.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))

        // Set voice if user specified and available
        if (!voiceName.isNullOrBlank()) {
            try {
                val matched = ttsInstance.voices?.firstOrNull { it.name == voiceName }
                if (matched != null) {
                    ttsInstance.voice = matched
                }
            } catch (_: Exception) {}
        }

        // Filter out markdown formatting asterisks/hashes for smoother spoken speech
        val spokenText = text
            .replace(Regex("[*#_`~>•]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "nova_ai_utterance_${System.currentTimeMillis()}")
        }

        ttsInstance.speak(spokenText, TextToSpeech.QUEUE_FLUSH, params, "nova_speech_id")
        _isSpeaking.value = true
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        _isReady.value = false
        _isSpeaking.value = false
    }
}
