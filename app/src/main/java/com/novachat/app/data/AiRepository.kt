package com.novachat.app.data

import android.content.Context
// AICore import commented out as the library is not yet publicly available (Jan 2026)
// import androidx.ai.edge.aicore.GenerativeModel
import com.google.ai.client.generativeai.GenerativeModel as GeminiModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for AI model interactions
 */
class AiRepository(private val context: Context) {
    
    /**
     * Send message to online Gemini API
     */
    suspend fun sendMessageToGemini(message: String, apiKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val generativeModel = GeminiModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey,
                    generationConfig = generationConfig {
                        temperature = 0.7f
                        topK = 40
                        topP = 0.95f
                        maxOutputTokens = 1024
                    }
                )
                
                val response = generativeModel.generateContent(message)
                val text = response.text ?: "No response from AI"
                Result.success(text)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Send message to on-device AICore model
     * NOTE: AICore is not yet publicly available on Maven (as of Jan 2026)
     * This function returns an error indicating the feature is unavailable
     */
    suspend fun sendMessageToAiCore(message: String): Result<String> {
        return withContext(Dispatchers.IO) {
            // AICore is not yet available in public Maven repositories
            // When it becomes available, uncomment the implementation below:
            /*
            try {
                val model = GenerativeModel.getDefault(context)
                val response = model.generateContent(message)
                val text = response.text ?: "No response from on-device AI"
                Result.success(text)
            } catch (e: Exception) {
                Result.failure(Exception("On-device AI is not available on this device. ${e.message}"))
            }
            */
            
            // Temporary fallback until AICore is available
            Result.failure(Exception("On-device AI (AICore) is not yet available. This feature requires the androidx.ai.edge.aicore library which is currently in development. Please use Online mode instead."))
        }
    }
}
