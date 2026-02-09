package com.novachat.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.repository.AiRepository
import com.novachat.app.domain.repository.AiServiceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException

/**
 * Implementation of AiRepository using Firebase Cloud Functions as a proxy.
 *
 * This implementation handles both online (via Firebase Functions proxy) and offline (AICore) modes,
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
    
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("us-central1")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Current service status.
     * Updated based on the last operation result.
     */
    private val serviceStatusFlow = MutableStateFlow<AiServiceStatus>(
        AiServiceStatus.Available(AiMode.DEFAULT_MODEL_NAME)
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
     * @param configuration AI configuration including mode and model parameters
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
     * Generates a response using Firebase Cloud Functions proxy.
     *
     * This method calls the Firebase Function 'aiProxy' which handles
     * authentication (Firebase Auth), server-side API key management, and communication with Gemini API.
     * No client-side API key is required - authentication is handled automatically.
     *
     * @param message The user's message
     * @param configuration AI configuration with model parameters
     * @return Result with response text or error
     */
    private suspend fun generateOnlineResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure user is authenticated (required for Firebase Functions)
                if (auth.currentUser == null) {
                    val error = SecurityException(
                        "Authentication required. Please wait for sign-in to complete."
                    )
                    updateServiceStatus(
                        AiServiceStatus.Error(error = error, isRecoverable = true)
                    )
                    return@withContext Result.failure(error)
                }

                // Prepare request data
                val data = hashMapOf(
                    "message" to message,
                    "modelParameters" to hashMapOf(
                        "temperature" to configuration.modelParameters.temperature,
                        "topK" to configuration.modelParameters.topK,
                        "topP" to configuration.modelParameters.topP,
                        "maxOutputTokens" to configuration.modelParameters.maxOutputTokens
                    )
                )

                // Call Firebase Function
                val function = functions.getHttpsCallable("aiProxy")
                val result = function.call(data).await()
                
                // Extract response from result
                @Suppress("UNCHECKED_CAST")
                val resultData = result.data as? Map<String, Any>
                val responseText = resultData?.get("response") as? String

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
                    AiServiceStatus.Available(AiMode.DEFAULT_MODEL_NAME)
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

            } catch (e: FirebaseFunctionsException) {
                // Firebase Functions error
                val errorMessage = when (e.code) {
                    FirebaseFunctionsException.Code.UNAUTHENTICATED -> 
                        "Authentication required. Please sign in."
                    FirebaseFunctionsException.Code.PERMISSION_DENIED -> 
                        "Permission denied. Please check your account."
                    FirebaseFunctionsException.Code.INVALID_ARGUMENT -> 
                        "Invalid request. Please check your message."
                    FirebaseFunctionsException.Code.INTERNAL -> 
                        "Server error. Please try again later."
                    FirebaseFunctionsException.Code.UNAVAILABLE -> 
                        "Service unavailable. Please try again later."
                    else -> 
                        "AI service error: ${e.message}"
                }
                
                val error = Exception(errorMessage, e)
                val isRecoverable = e.code != FirebaseFunctionsException.Code.PERMISSION_DENIED
                
                updateServiceStatus(
                    AiServiceStatus.Error(
                        error = error,
                        isRecoverable = isRecoverable
                    )
                )
                Result.failure(error)

            } catch (e: SecurityException) {
                // Auth error
                val error = SecurityException("Authentication error: ${e.message}", e)
                updateServiceStatus(
                    AiServiceStatus.Error(error = error, isRecoverable = true)
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
