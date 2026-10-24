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
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private val FALLBACK_MODELS = listOf(
            "gemini-2.5-flash",
            "gemini-3.5-flash",
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-pro"
        )
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        @Volatile
        private var cachedActiveModel: String? = null
    }

    private suspend fun resolveSupportedModel(apiKey: String): String = withContext(Dispatchers.IO) {
        cachedActiveModel?.let { return@withContext it }

        // Query available models from Google AI Studio
        val listUrl = "$BASE_URL/models?key=$apiKey"
        val request = Request.Builder().url(listUrl).get().build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val listResponse = listModelsAdapter.fromJson(bodyString)
                    val available = listResponse?.models?.filter { model ->
                        model.supportedGenerationMethods?.contains("generateContent") == true
                    }?.map { it.name.removePrefix("models/") }

                    if (!available.isNullOrEmpty()) {
                        // Pick the best flash model or first supported model
                        val chosen = available.firstOrNull { it.contains("2.5-flash") }
                            ?: available.firstOrNull { it.contains("3.5-flash") }
                            ?: available.firstOrNull { it.contains("flash") }
                            ?: available.first()

                        cachedActiveModel = chosen
                        return@withContext chosen
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback default
        FALLBACK_MODELS.first()
    }

    suspend fun testConnection(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
        }

        // 1. First test authentication and discover available models via listModels
        val listUrl = "$BASE_URL/models?key=$trimmedKey"
        val listRequest = Request.Builder().url(listUrl).get().build()

        var discoveredModel: String? = null

        try {
            okHttpClient.newCall(listRequest).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val listResponse = listModelsAdapter.fromJson(responseString)
                    val supported = listResponse?.models?.filter {
                        it.supportedGenerationMethods?.contains("generateContent") == true
                    }?.map { it.name.removePrefix("models/") }

                    if (!supported.isNullOrEmpty()) {
                        discoveredModel = supported.firstOrNull { it.contains("2.5-flash") }
                            ?: supported.firstOrNull { it.contains("3.5-flash") }
                            ?: supported.firstOrNull { it.contains("flash") }
                            ?: supported.first()
                        cachedActiveModel = discoveredModel
                    }
                } else {
                    val parsedError = try {
                        listModelsAdapter.fromJson(responseString)?.error?.message
                    } catch (_: Exception) {
                        null
                    }
                    val errorMsg = when (response.code) {
                        400 -> parsedError ?: "Invalid API key format. Please verify your key."
                        403 -> parsedError ?: "API key is unauthorized or Gemini API is not enabled."
                        429 -> "API quota exceeded. Please check Google AI Studio."
                        else -> parsedError ?: "Connection failed with HTTP ${response.code}."
                    }
                    return@withContext Result.failure(IOException(errorMsg))
                }
            }
        } catch (e: Exception) {
            // Network error during list
            return@withContext Result.failure(IOException("Network error connecting to Gemini API: ${e.message}"))
        }

        // 2. Perform minimal generateContent test
        val modelToUse = discoveredModel ?: cachedActiveModel ?: FALLBACK_MODELS.first()
        val candidateList = if (discoveredModel != null) {
            listOf(discoveredModel) + FALLBACK_MODELS.filter { it != discoveredModel }
        } else {
            FALLBACK_MODELS
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

        for (model in candidateList) {
            val url = "$BASE_URL/models/$model:generateContent?key=$trimmedKey"
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
                            cachedActiveModel = model
                            return@withContext Result.success(true)
                        }
                    } else if (response.code == 404 && model != candidateList.last()) {
                        // Model not supported, continue to next candidate
                        return@use
                    } else {
                        val parsedError = try {
                            responseAdapter.fromJson(responseString)?.error?.message
                        } catch (_: Exception) {
                            null
                        }
                        val errorMsg = when (response.code) {
                            400 -> parsedError ?: "Invalid API request format."
                            403 -> parsedError ?: "API key lacks permission."
                            429 -> "API quota exceeded. Please check Google AI Studio."
                            else -> parsedError ?: "HTTP ${response.code}: Model connection failed."
                        }
                        return@withContext Result.failure(IOException(errorMsg))
                    }
                }
            } catch (e: Exception) {
                if (model == candidateList.last()) {
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

        val activeModel = resolveSupportedModel(trimmedKey)

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

        val candidateList = listOf(activeModel) + FALLBACK_MODELS.filter { it != activeModel }

        for (model in candidateList) {
            val url = "$BASE_URL/models/$model:generateContent?key=$trimmedKey"
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
                            cachedActiveModel = model
                            return@withContext Result.success(text.trim())
                        } else {
                            return@withContext Result.failure(IOException("Empty response received from Gemini."))
                        }
                    } else if (response.code == 404 && model != candidateList.last()) {
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
                if (model == candidateList.last()) {
                    return@withContext Result.failure(e)
                }
            }
        }

        Result.failure(IOException("Network error communicating with Gemini."))
    }
}
