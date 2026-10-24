package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nova_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("has_completed_onboarding")
        val KEY_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val KEY_SPEECH_SPEED = floatPreferencesKey("speech_speed")
        val KEY_SELECTED_VOICE = stringPreferencesKey("selected_voice_name")
        val KEY_AUTO_SPEAK = booleanPreferencesKey("auto_speak_response")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_KEY] ?: ""
    }

    val preferredLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PREFERRED_LANGUAGE] ?: "bn" // Default to Bengali as requested
    }

    val speechSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_SPEECH_SPEED] ?: 1.0f
    }

    val selectedVoice: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_VOICE] ?: ""
    }

    val autoSpeak: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_SPEAK] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_API_KEY] = key.trim()
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_API_KEY)
        }
    }

    suspend fun setPreferredLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PREFERRED_LANGUAGE] = languageCode
        }
    }

    suspend fun setSpeechSpeed(speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SPEECH_SPEED] = speed
        }
    }

    suspend fun setSelectedVoice(voiceName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_VOICE] = voiceName
        }
    }

    suspend fun setAutoSpeak(autoSpeak: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_SPEAK] = autoSpeak
        }
    }
}
