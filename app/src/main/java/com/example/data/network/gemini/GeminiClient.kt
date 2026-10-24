package com.example.data.network.gemini

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiClient {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private val MODEL_CANDIDATES = listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash")
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    suspend fun testConnection(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
        }

        val testPayload = GeminiRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = "Hello, please reply with 'pong'."))
                )
            )
        )
        val jsonBody = requestAdapter.toJson(testPayload)
        val body = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

        // Try primary model first, fallback if not found
        for (model in MODEL_CANDIDATES) {
            val url = "$BASE_URL/$model:generateContent?key=$trimmedKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    val responseString = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val parsed = responseAdapter.fromJson(responseString)
                        val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) {
                            return@withContext Result.success(true)
                        }
                    } else if (response.code == 404 && model != MODEL_CANDIDATES.last()) {
                        // Model not available in this region/version, try next candidate
                        return@use
                    } else {
                        val parsedError = try {
                            responseAdapter.fromJson(responseString)?.error?.message
                        } catch (e: Exception) {
                            null
                        }
                        val errorMsg = when (response.code) {
                            400 -> parsedError ?: "Invalid API key or request format."
                            403 -> parsedError ?: "API key lacks permission or Gemini API is not enabled."
                            429 -> "API quota exceeded. Please check Google AI Studio."
                            else -> parsedError ?: "Connection failed with HTTP ${response.code}."
                        }
                        return@withContext Result.failure(IOException(errorMsg))
                    }
                }
            } catch (e: Exception) {
                if (model == MODEL_CANDIDATES.last()) {
                    return@withContext Result.failure(e)
                }
            }
        }

        Result.failure(IOException("Failed to connect to Gemini API. Please check your network and API key."))
    }

    suspend fun askGemini(
        prompt: String,
        apiKey: String,
        preferredLanguage: String,
        contextHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Gemini API key is not configured."))
        }

        val languageInstruction = when (preferredLanguage.lowercase()) {
            "bn", "bengali" -> "You are NOVA AI, an intelligent, helpful, and friendly voice personal assistant. The user prefers Bengali (বাংলা). Answer clearly, conversationally, and accurately in Bengali. Keep answers concise and suitable for spoken text."
            "hi", "hindi" -> "You are NOVA AI, an intelligent, helpful, and friendly voice personal assistant. The user prefers Hindi (हिन्दी). Answer clearly, conversationally, and accurately in Hindi. Keep answers concise and suitable for spoken text."
            "ar", "arabic" -> "You are NOVA AI, an intelligent, helpful, and friendly voice personal assistant. The user prefers Arabic. Answer clearly, conversationally, and accurately in Arabic. Keep answers concise and suitable for spoken text."
            "es", "spanish" -> "You are NOVA AI, an intelligent, helpful, and friendly voice personal assistant. The user prefers Spanish. Answer clearly, conversationally, and accurately in Spanish. Keep answers concise and suitable for spoken text."
            else -> "You are NOVA AI, an intelligent, helpful, and friendly voice personal assistant. Answer clearly, conversationally, and accurately in English. Keep answers concise, natural, and suitable for spoken text."
        }

        val contentsList = mutableListOf<ContentItem>()

        // Recent conversation context (up to last 3 exchanges)
        for (item in contextHistory.takeLast(3)) {
            contentsList.add(ContentItem(role = "user", parts = listOf(PartItem(text = item.first))))
            contentsList.add(ContentItem(role = "model", parts = listOf(PartItem(text = item.second))))
        }

        // Current question
        contentsList.add(ContentItem(role = "user", parts = listOf(PartItem(text = prompt))))

        val geminiRequest = GeminiRequest(
            contents = contentsList,
            systemInstruction = ContentItem(
                parts = listOf(PartItem(text = languageInstruction))
            )
        )

        val jsonBody = requestAdapter.toJson(geminiRequest)
        val body = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

        for (model in MODEL_CANDIDATES) {
            val url = "$BASE_URL/$model:generateContent?key=$trimmedKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    val responseString = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val parsed = responseAdapter.fromJson(responseString)
                        val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) {
                            return@withContext Result.success(text.trim())
                        } else {
                            return@withContext Result.failure(IOException("Empty response received from Gemini."))
                        }
                    } else if (response.code == 404 && model != MODEL_CANDIDATES.last()) {
                        return@use
                    } else {
                        val errorDetails = try {
                            responseAdapter.fromJson(responseString)?.error?.message
                        } catch (e: Exception) {
                            null
                        }
                        val msg = when (response.code) {
                            400 -> errorDetails ?: "Invalid request format."
                            403 -> errorDetails ?: "Invalid API key or unauthorized."
                            429 -> "Gemini rate limit exceeded. Please wait a moment."
                            else -> errorDetails ?: "HTTP ${response.code}: Failed to get response."
                        }
                        return@withContext Result.failure(IOException(msg))
                    }
                }
            } catch (e: Exception) {
                if (model == MODEL_CANDIDATES.last()) {
                    return@withContext Result.failure(e)
                }
            }
        }

        Result.failure(IOException("Network error communicating with Gemini."))
    }
}
