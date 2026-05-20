package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Choose current model as specified in gemini-api skill
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Executes a prompt and returns the text result.
     */
    suspend fun generateResponse(prompt: String, systemInstruction: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or uses placeholder.")
            return@withContext "API Key Required: Please add your GEMINI_API_KEY inside the Secrets Panel or .env file to enable full AI writing powers."
        }

        try {
            val endpoint = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            
            // Build request JSON using standard org.json objects for 100% runtime safety
            val requestBodyJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            // System Instruction
            if (systemInstruction.isNotEmpty()) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                requestBodyJson.put("systemInstruction", sysInstObj)
            }

            // Generation Config
            val generationConfigJson = JSONObject()
            generationConfigJson.put("temperature", 0.7)
            requestBodyJson.put("generationConfig", generationConfigJson)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBodyStr = response.body?.string()
                if (!response.isSuccessful || responseBodyStr == null) {
                    val errMsg = "HTTP ${response.code}: ${response.message}\nBody: $responseBodyStr"
                    Log.e(TAG, errMsg)
                    return@withContext "Error while calling AI features. Status: ${response.code}"
                }

                // Parse standard Gemini JSON response structure
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text field returned from Gemini API.")
                        }
                    }
                }
                
                return@withContext "AI didn't provide suggestions. Please try another phrase."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network or JSON error during Gemini query", e)
            return@withContext "Connection timeout or API issue. Details: ${e.localizedMessage}"
        }
    }

    /**
     * Rephrases or Translates a Sinhala (or Singlish) phrase to English, formal Sinhala, or recommends replies and emojis.
     */
    suspend fun translateOrRephrase(text: String, mode: String): String {
        val instruction = when (mode) {
            "formal" -> "You are a Sri Lankan Sinhala language expert assistant. Translate or convert the given text (which may be informal Sinhala or Singlish phonetic characters) into elegant, formal Sinhala. Return ONLY the translation text. Do not add any extra explanations or greetings."
            "english" -> "Translate the given Sinhala/Singlish text into elegant natural English. Return ONLY the translated English text, without comments, prefixes, or greetings."
            "emoji" -> "You are an emoji and response suggester for a customized smart Sri Lankan keyboard. Recommend a brief response (in English and Sinhala) paired with three awesome typing emoji options based on this. Keep it extremely brief (max 2 lines) and direct."
            else -> "Help the user rephrase and polish their typing text. Be concise."
        }
        return generateResponse(prompt = text, systemInstruction = instruction)
    }
}
