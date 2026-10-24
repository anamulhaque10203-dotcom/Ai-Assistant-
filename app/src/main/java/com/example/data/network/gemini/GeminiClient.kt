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
    private val listModelsAdapter = moshi.adapter(ListModelsResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    companion object {
        private val TEST_ENDPOINTS = listOf(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent",
            "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent"
        )
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        @Volatile
        private var cachedWorkingEndpoint: String? = null
    }

    private fun cleanKey(key: String): String {
        return key.trim().trim('"', '\'', ' ', '\n', '\r')
    }

    suspend fun testConnection(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val trimmedKey = cleanKey(apiKey)
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
        }

        // 1. Try to discover supported models via ListModels (non-fatal if restricted)
        val discoveredEndpoints = mutableListOf<String>()
        try {
            val listUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=$trimmedKey"
            val listReq = Request.Builder()
                .url(listUrl)
                .header("x-goog-api-key", trimmedKey)
                .get()
                .build()

            okHttpClient.newCall(listReq).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val listResponse = listModelsAdapter.fromJson(bodyString)
                    val supported = listResponse?.models?.filter {
                        it.supportedGenerationMethods?.contains("generateContent") == true
                    }?.map { it.name.removePrefix("models/") }

                    if (!supported.isNullOrEmpty()) {
                        // Prefer 2.0-flash, 2.5-flash, or any flash model
                        val sorted = supported.sortedByDescending {
                            when {
                                it.contains("2.0-flash") -> 100
                                it.contains("2.5-flash") -> 90
                                it.contains("3.5-flash") -> 80
                                it.contains("flash") -> 70
                                else -> 10
                            }
                        }
                        for (model in sorted) {
                            discoveredEndpoints.add("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // Combine discovered endpoints with known reliable endpoints
        val candidateEndpoints = (discoveredEndpoints + TEST_ENDPOINTS).distinct()

        val testPayload = GeminiRequest(
            contents = listOf(
                ContentItem(
                    role = "user",
                    parts = listOf(PartItem(text = "Hello, reply with OK."))
                )
            )
        )
        val jsonBody = requestAdapter.toJson(testPayload)
        val body = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

        var lastErrorMsg: String? = null

        for (endpoint in candidateEndpoints) {
            val fullUrl = "$endpoint?key=$trimmedKey"
            val request = Request.Builder()
                .url(fullUrl)
                .header("x-goog-api-key", trimmedKey)
                .post(body)
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    val responseString = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val parsed = responseAdapter.fromJson(responseString)
                        val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) {
                            cachedWorkingEndpoint = endpoint
                            return@withContext Result.success(true)
                        }
                    } else if (response.code == 404) {
                        // Model not supported on this endpoint, continue trying others
                        lastErrorMsg = "Model not found on endpoint. Trying alternatives..."
                    } else {
                        val parsedError = try {
                            responseAdapter.fromJson(responseString)?.error?.message
                        } catch (_: Exception) {
                            null
                        }
                        lastErrorMsg = when (response.code) {
                            400 -> parsedError ?: "API key is invalid. Please check your key in Google AI Studio."
                            403 -> parsedError ?: "API key lacks permission or Gemini API is not enabled in Google Cloud."
                            429 -> "API quota exceeded. Please check Google AI Studio."
                            else -> parsedError ?: "HTTP ${response.code}: Connection failed."
                        }
                        if (response.code == 400 || response.code == 403) {
                            return@withContext Result.failure(IOException(lastErrorMsg))
                        }
                    }
                }
            } catch (e: Exception) {
                lastErrorMsg = e.message
            }
        }

        Result.failure(IOException(lastErrorMsg ?: "Could not connect to Gemini API. Please verify your internet and API key."))
    }

    suspend fun askGemini(
        prompt: String,
        apiKey: String,
        preferredLanguage: String,
        contextHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = cleanKey(apiKey)
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

        for (item in contextHistory.takeLast(3)) {
            contentsList.add(ContentItem(role = "user", parts = listOf(PartItem(text = item.first))))
            contentsList.add(ContentItem(role = "model", parts = listOf(PartItem(text = item.second))))
        }

        contentsList.add(ContentItem(role = "user", parts = listOf(PartItem(text = prompt))))

        val geminiRequest = GeminiRequest(
            contents = contentsList,
            systemInstruction = ContentItem(
                parts = listOf(PartItem(text = languageInstruction))
            )
        )

        val jsonBody = requestAdapter.toJson(geminiRequest)
        val body = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

        val baseEndpoint = cachedWorkingEndpoint ?: TEST_ENDPOINTS.first()
        val candidateEndpoints = listOf(baseEndpoint) + TEST_ENDPOINTS.filter { it != baseEndpoint }

        for (endpoint in candidateEndpoints) {
            val fullUrl = "$endpoint?key=$trimmedKey"
            val request = Request.Builder()
                .url(fullUrl)
                .header("x-goog-api-key", trimmedKey)
                .post(body)
                .build()

            try {
                okHttpClient.newCall(request).execute().use { response ->
                    val responseString = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val parsed = responseAdapter.fromJson(responseString)
                        val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) {
                            cachedWorkingEndpoint = endpoint
                            return@withContext Result.success(text.trim())
                        } else {
                            return@withContext Result.failure(IOException("Empty response received from Gemini."))
                        }
                    } else if (response.code == 404 && endpoint != candidateEndpoints.last()) {
                        return@use
                    } else {
                        val errorDetails = try {
                            responseAdapter.fromJson(responseString)?.error?.message
                        } catch (_: Exception) {
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
                if (endpoint == candidateEndpoints.last()) {
                    return@withContext Result.failure(e)
                }
            }
        }

        Result.failure(IOException("Network error communicating with Gemini."))
    }
}
