package com.novachat.app.data.repository

import android.content.Context
import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.repository.AiRepository
import com.novachat.app.domain.repository.AiServiceStatus
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException

/**
 * Implementation of AiRepository using Google's Generative AI SDK.
 *
 * This implementation handles both online (Gemini) and offline (AICore) modes,
 * though offline mode is currently unavailable as AICore is not yet published
 * to Maven repositories.
 *
 * The implementation includes proper error handling, network error detection,
 * and service status monitoring.
 *
 * @property context Android context for accessing system services
 *
 * @since 1.0.0
 */
class AiRepositoryImpl(
    private val context: Context
) : AiRepository {

    /**
     * Current service status.
     * Updated based on the last operation result.
     */
    private val serviceStatusFlow = MutableStateFlow<AiServiceStatus>(
        AiServiceStatus.Available(AiConfiguration.DEFAULT_MODEL_NAME)
    )

    /**
     * Sends a message to the AI service and receives a response.
     *
     * This method:
     * 1. Validates the configuration
     * 2. Selects the appropriate AI service (online/offline)
     * 3. Sends the message and receives the response
     * 4. Updates service status based on the result
     *
     * @param message The user's message to send
     * @param configuration AI configuration including mode, API key, and parameters
     * @return Result.success with response text, Result.failure with error details
     */
    override suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        // Validate input
        if (message.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Message cannot be blank")
            )
        }

        // Validate configuration
        val validationResult = configuration.validate()
        if (validationResult.isFailure) {
            return Result.failure(
                validationResult.exceptionOrNull() 
                    ?: IllegalStateException("Invalid configuration")
            )
        }

        // Route to appropriate service based on mode
        return when (configuration.mode) {
            AiMode.ONLINE -> generateOnlineResponse(message, configuration)
            AiMode.OFFLINE -> generateOfflineResponse(message, configuration)
        }
    }

    /**
     * Generates a response using the online Gemini API.
     *
     * This method handles network communication, API errors, and response parsing.
     *
     * @param message The user's message
     * @param configuration AI configuration with API key and parameters
     * @return Result with response text or error
     */
    private suspend fun generateOnlineResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // API key is required for online mode
                val apiKey = configuration.apiKey
                    ?: return@withContext Result.failure(
                        IllegalStateException("API key is required for online mode")
                    )

                // Create Gemini model with configuration
                val generativeModel = GenerativeModel(
                    modelName = AiConfiguration.DEFAULT_MODEL_NAME,
                    apiKey = apiKey.value,
                    generationConfig = generationConfig {
                        temperature = configuration.modelParameters.temperature
                        topK = configuration.modelParameters.topK
                        topP = configuration.modelParameters.topP
                        maxOutputTokens = configuration.modelParameters.maxOutputTokens
                    }
                )

                // Generate content
                val response = generativeModel.generateContent(message)
                val responseText = response.text

                // Validate response
                if (responseText.isNullOrBlank()) {
                    updateServiceStatus(
                        AiServiceStatus.Error(
                            error = Exception("AI returned empty response"),
                            isRecoverable = true
                        )
                    )
                    return@withContext Result.failure(
                        Exception("AI returned empty response. Please try again.")
                    )
                }

                // Update status to available
                updateServiceStatus(
                    AiServiceStatus.Available(AiConfiguration.DEFAULT_MODEL_NAME)
                )

                Result.success(responseText)

            } catch (e: UnknownHostException) {
                // Network error - no internet connection
                val error = IOException("No internet connection. Please check your network.", e)
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = true)
                )
                Result.failure(error)

            } catch (e: IOException) {
                // Network error - general connection problem
                val error = IOException("Network error. Please check your connection and try again.", e)
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = true)
                )
                Result.failure(error)

            } catch (e: SecurityException) {
                // API key error
                val error = SecurityException("Invalid API key. Please check your settings.", e)
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = false)
                )
                Result.failure(error)

            } catch (e: IllegalStateException) {
                // Service unavailable or configuration error
                val error = IllegalStateException("AI service error: ${e.message}", e)
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = true)
                )
                Result.failure(error)

            } catch (e: Exception) {
                // Unknown error
                val error = Exception("Unexpected error: ${e.message}", e)
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = true)
                )
                Result.failure(error)
            }
        }
    }

    /**
     * Generates a response using on-device AICore.
     *
     * Currently returns an error as AICore is not yet publicly available
     * on Maven repositories (as of January 2026).
     *
     * @param message The user's message
     * @param configuration AI configuration
     * @return Result with error indicating AICore is unavailable
     *
     * TODO: Implement AICore integration when the androidx.ai.edge.aicore
     *       library becomes publicly available on Maven Central.
     */
    private suspend fun generateOfflineResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val error = UnsupportedOperationException(
                "On-device AI (AICore) is not yet available. " +
                "This feature requires the androidx.ai.edge.aicore library " +
                "which is currently in development and not published to Maven. " +
                "Please use Online mode instead."
            )
            updateServiceStatus(
                AiServiceStatus.Unavailable(
                    "AICore not available - use Online mode"
                )
            )
            Result.failure(error)
        }
    }

    /**
     * Checks if the specified AI mode is available on this device.
     *
     * For ONLINE mode: Always returns true (assuming network is available)
     * For OFFLINE mode: Returns false until AICore is available
     *
     * @param mode The AI mode to check
     * @return true if the mode is available, false otherwise
     */
    override suspend fun isModeAvailable(mode: AiMode): Boolean {
        return when (mode) {
            AiMode.ONLINE -> {
                // Online mode is always available if we have network
                // In a production app, you might want to check actual connectivity
                true
            }
            AiMode.OFFLINE -> {
                // AICore is not yet available
                // When it becomes available, check if it's installed:
                // isAiCoreAvailable()
                false
            }
        }
    }

    /**
     * Gets information about the current AI service status.
     *
     * @return Flow emitting current service status
     */
    override fun observeServiceStatus(): Flow<AiServiceStatus> {
        return serviceStatusFlow.asStateFlow()
    }

    /**
     * Updates the service status and emits to observers.
     *
     * @param status The new service status
     */
    private fun updateServiceStatus(status: AiServiceStatus) {
        serviceStatusFlow.value = status
    }

    /**
     * Checks if AICore is available on this device.
     *
     * This method will be implemented when AICore becomes available.
     * It should check if:
     * 1. The device supports AICore
     * 2. AICore is installed
     * 3. The required models are downloaded
     *
     * @return true if AICore is available, false otherwise
     */
    private fun isAiCoreAvailable(): Boolean {
        // TODO: Implement when AICore is available
        // Example implementation:
        // try {
        //     val aiCoreManager = AiCoreManager.getInstance(context)
        //     return aiCoreManager.isAvailable() && 
        //            aiCoreManager.hasModel("gemini-nano")
        // } catch (e: Exception) {
        //     return false
        // }
        return false
    }
}
