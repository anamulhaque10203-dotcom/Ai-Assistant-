package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.PreferencesManager
import com.example.data.network.gemini.GeminiClient
import com.example.speech.NovaVoiceOption
import com.example.speech.SpeechRecognitionManager
import com.example.speech.TextToSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val conversationDao = db.conversationDao()
    private val preferencesManager = PreferencesManager(application)
    private val geminiClient = GeminiClient()

    val speechRecognitionManager = SpeechRecognitionManager(application)
    val textToSpeechManager = TextToSpeechManager(application)

    private val _voiceState = MutableStateFlow(NovaVoiceState.IDLE)
    val voiceState: StateFlow<NovaVoiceState> = _voiceState.asStateFlow()

    private val _currentQuestion = MutableStateFlow("")
    val currentQuestion: StateFlow<String> = _currentQuestion.asStateFlow()

    private val _currentAnswer = MutableStateFlow("")
    val currentAnswer: StateFlow<String> = _currentAnswer.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _testConnectionLoading = MutableStateFlow(false)
    val testConnectionLoading: StateFlow<Boolean> = _testConnectionLoading.asStateFlow()

    private val _isApiConnected = MutableStateFlow<Boolean?>(null)
    val isApiConnected: StateFlow<Boolean?> = _isApiConnected.asStateFlow()

    private val _testConnectionMessage = MutableStateFlow<String?>(null)
    val testConnectionMessage: StateFlow<String?> = _testConnectionMessage.asStateFlow()

    val isOnboardingCompleted: StateFlow<Boolean> = preferencesManager.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val savedApiKey: StateFlow<String> = preferencesManager.apiKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val preferredLanguage: StateFlow<String> = preferencesManager.preferredLanguage
        .stateIn(viewModelScope, SharingStarted.Eagerly, "bn")

    val speechSpeed: StateFlow<Float> = preferencesManager.speechSpeed
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)

    val selectedVoice: StateFlow<String> = preferencesManager.selectedVoice
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val allConversations: StateFlow<List<ConversationEntity>> = conversationDao.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableVoices: StateFlow<List<NovaVoiceOption>> = textToSpeechManager.availableVoices

    private var currentProcessingJob: Job? = null

    init {
        // Collect TTS and speech recognition states to keep UI synchronized
        viewModelScope.launch {
            speechRecognitionManager.errorMessage.collect { err ->
                if (err != null && _voiceState.value == NovaVoiceState.LISTENING) {
                    _voiceState.value = NovaVoiceState.IDLE
                    _errorMessage.value = err
                }
            }
        }
    }

    fun getEffectiveApiKey(): String {
        val userKey = savedApiKey.value.trim()
        if (userKey.isNotEmpty()) return userKey

        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        if (buildKey.isNotEmpty() && !buildKey.startsWith("MY_GEMINI_API_KEY")) {
            return buildKey
        }
        return ""
    }

    fun startListening() {
        if (_voiceState.value == NovaVoiceState.SPEAKING) {
            stopSpeaking()
        }
        _voiceState.value = NovaVoiceState.LISTENING
        _errorMessage.value = null

        speechRecognitionManager.startListening(preferredLanguage.value) { resultText ->
            if (resultText.isNotBlank()) {
                submitQuestion(resultText)
            } else {
                _voiceState.value = NovaVoiceState.IDLE
            }
        }
    }

    fun stopListening() {
        speechRecognitionManager.stopListening()
        val text = speechRecognitionManager.partialText.value
        if (text.isNotBlank()) {
            submitQuestion(text)
        } else {
            _voiceState.value = NovaVoiceState.IDLE
        }
    }

    fun cancelListening() {
        speechRecognitionManager.cancelListening()
        _voiceState.value = NovaVoiceState.IDLE
    }

    fun submitQuestion(prompt: String) {
        val query = prompt.trim()
        if (query.isEmpty()) return

        speechRecognitionManager.stopListening()
        if (_voiceState.value == NovaVoiceState.SPEAKING) {
            stopSpeaking()
        }

        _currentQuestion.value = query
        _currentAnswer.value = ""
        _voiceState.value = NovaVoiceState.PROCESSING
        _errorMessage.value = null

        val apiKey = getEffectiveApiKey()
        if (apiKey.isEmpty()) {
            _voiceState.value = NovaVoiceState.ERROR
            _errorMessage.value = "Gemini API key is not configured. Please add your API key in Settings or Setup screen."
            return
        }

        currentProcessingJob?.cancel()
        currentProcessingJob = viewModelScope.launch {
            val contextPairs = allConversations.value.take(4).map { it.question to it.answer }
            val result = geminiClient.askGemini(
                prompt = query,
                apiKey = apiKey,
                preferredLanguage = preferredLanguage.value,
                contextHistory = contextPairs
            )

            result.onSuccess { answer ->
                _currentAnswer.value = answer
                _voiceState.value = NovaVoiceState.SPEAKING

                // Save to local database
                conversationDao.insertConversation(
                    ConversationEntity(
                        question = query,
                        answer = answer,
                        languageCode = preferredLanguage.value
                    )
                )

                // Speak response
                textToSpeechManager.speak(
                    text = answer,
                    languageCode = preferredLanguage.value,
                    speechRate = speechSpeed.value,
                    voiceName = selectedVoice.value.ifBlank { null }
                ) {
                    if (_voiceState.value == NovaVoiceState.SPEAKING) {
                        _voiceState.value = NovaVoiceState.IDLE
                    }
                }
            }.onFailure { ex ->
                _voiceState.value = NovaVoiceState.ERROR
                _errorMessage.value = ex.message ?: "Failed to get AI response. Please try again."
            }
        }
    }

    fun stopSpeaking() {
        textToSpeechManager.stop()
        if (_voiceState.value == NovaVoiceState.SPEAKING) {
            _voiceState.value = NovaVoiceState.IDLE
        }
    }

    fun speakCurrentAnswer() {
        val text = _currentAnswer.value
        if (text.isNotBlank()) {
            _voiceState.value = NovaVoiceState.SPEAKING
            textToSpeechManager.speak(
                text = text,
                languageCode = preferredLanguage.value,
                speechRate = speechSpeed.value,
                voiceName = selectedVoice.value.ifBlank { null }
            ) {
                if (_voiceState.value == NovaVoiceState.SPEAKING) {
                    _voiceState.value = NovaVoiceState.IDLE
                }
            }
        }
    }

    fun retryLastQuestion() {
        val lastQuestion = _currentQuestion.value
        if (lastQuestion.isNotBlank()) {
            submitQuestion(lastQuestion)
        }
    }

    fun dismissError() {
        _errorMessage.value = null
        _voiceState.value = NovaVoiceState.IDLE
    }

    fun testApiConnection(key: String, onFinished: ((Boolean, String) -> Unit)? = null) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            _isApiConnected.value = false
            _testConnectionMessage.value = "Please enter a valid API key."
            onFinished?.invoke(false, "Please enter a valid API key.")
            return
        }

        _testConnectionLoading.value = true
        _testConnectionMessage.value = null

        viewModelScope.launch {
            val result = geminiClient.testConnection(trimmed)
            _testConnectionLoading.value = false
            result.onSuccess {
                _isApiConnected.value = true
                val msg = "Connection successful! Your API key is valid."
                _testConnectionMessage.value = msg
                onFinished?.invoke(true, msg)
            }.onFailure { ex ->
                _isApiConnected.value = false
                val msg = ex.message ?: "Connection test failed."
                _testConnectionMessage.value = msg
                onFinished?.invoke(false, msg)
            }
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            preferencesManager.setApiKey(key.trim())
            _isApiConnected.value = true
        }
    }

    fun deleteApiKey() {
        viewModelScope.launch {
            preferencesManager.clearApiKey()
            _isApiConnected.value = null
            _testConnectionMessage.value = null
        }
    }

    fun savePreferredLanguage(lang: String) {
        viewModelScope.launch {
            preferencesManager.setPreferredLanguage(lang)
        }
    }

    fun saveSpeechSpeed(speed: Float) {
        viewModelScope.launch {
            preferencesManager.setSpeechSpeed(speed)
        }
    }

    fun saveSelectedVoice(voiceName: String) {
        viewModelScope.launch {
            preferencesManager.setSelectedVoice(voiceName)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            conversationDao.deleteConversationById(id)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            conversationDao.clearAllConversations()
        }
    }

    fun testSampleVoice(voiceOption: NovaVoiceOption) {
        val sampleText = when {
            voiceOption.localeTag.startsWith("bn") -> "নমস্কার, আমি নোভা এআই আপনার ব্যক্তিগত ভয়েস অ্যাসিস্ট্যান্ট।"
            voiceOption.localeTag.startsWith("hi") -> "नमस्ते, मैं नोवा एआई हूँ, आपकी व्यक्तिगत वॉयस असिस्टेंट।"
            voiceOption.localeTag.startsWith("es") -> "Hola, soy NOVA AI, tu asistente personal de voz."
            voiceOption.localeTag.startsWith("ar") -> "مرحبا، أنا نوفا المساعد الصوتي الشخصي."
            else -> "Hello, I am NOVA AI, your personal voice assistant."
        }
        textToSpeechManager.speak(
            text = sampleText,
            languageCode = voiceOption.localeTag,
            speechRate = speechSpeed.value,
            voiceName = voiceOption.id
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
        textToSpeechManager.shutdown()
    }
}
