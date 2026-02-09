package com.novachat.app.domain.repository

import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.ThemePreferences
import com.novachat.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing chat messages.
 *
 * This interface defines the contract for message persistence and retrieval,
 * following the Repository pattern. It abstracts the data source implementation
 * from the domain and presentation layers.
 *
 * The repository operates on domain models (Message) rather than data models,
 * ensuring the domain layer remains independent of data layer concerns.
 *
 * @since 1.0.0
 */
interface MessageRepository {
    /**
     * Observes all messages in the current conversation.
     *
     * Returns a Flow that emits the complete list of messages whenever
     * the underlying data changes. This enables reactive UI updates.
     *
     * Messages are ordered by timestamp in ascending order (oldest first).
     *
     * @return Flow emitting lists of messages, starting with current state
     */
    fun observeMessages(): Flow<List<Message>>

    /**
     * Adds a new message to the conversation.
     *
     * The message is persisted and will appear in the flow from observeMessages().
     * This operation is idempotent - adding the same message twice has no effect.
     *
     * @param message The message to add
     * @return Result.success if added successfully, Result.failure on error
     */
    suspend fun addMessage(message: Message): Result<Unit>

    /**
     * Updates an existing message in the conversation.
     *
     * Typically used to update message status (e.g., from Processing to Sent/Failed).
     * If the message doesn't exist, this operation fails.
     *
     * @param message The updated message (identified by id)
     * @return Result.success if updated successfully, Result.failure if not found or error
     */
    suspend fun updateMessage(message: Message): Result<Unit>

    /**
     * Retrieves a specific message by its identifier.
     *
     * @param id The message identifier
     * @return The message if found, null if not found
     */
    suspend fun getMessage(id: com.novachat.app.domain.model.MessageId): Message?

    /**
     * Deletes all messages from the conversation.
     *
     * This permanently removes all messages. There is no undo.
     *
     * @return Result.success if cleared successfully, Result.failure on error
     */
    suspend fun clearAllMessages(): Result<Unit>

    /**
     * Gets the count of messages in the conversation.
     *
     * @return The total number of messages
     */
    suspend fun getMessageCount(): Int
}

/**
 * Repository interface for managing AI service interactions.
 *
 * This interface defines the contract for communicating with AI services,
 * abstracting the implementation details of specific AI providers (Gemini, AICore).
 *
 * The repository handles both online (cloud-based) and offline (on-device) AI modes,
 * managing the complexity of different service types behind a unified interface.
 *
 * @since 1.0.0
 */
interface AiRepository {
    /**
     * Sends a message to the AI service and receives a response.
     *
     * This is the primary method for AI interaction. It handles:
     * - Selecting the appropriate AI service based on configuration
     * - Network communication (for online mode)
     * - Error handling and retry logic
     * - Response parsing and validation
     *
     * The operation is cancellable and respects coroutine cancellation.
     *
     * @param message The user's message to send to the AI
     * @param configuration The AI configuration specifying mode, API key, and parameters
     * @return Result.success with AI response text, Result.failure with error details
     */
    suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String>

    /**
     * Checks if the specified AI mode is available on this device.
     *
     * For ONLINE mode: Checks network connectivity
     * For OFFLINE mode: Checks if AICore is installed and models are available
     *
     * @param mode The AI mode to check
     * @return true if the mode is currently available, false otherwise
     */
    suspend fun isModeAvailable(mode: com.novachat.app.domain.model.AiMode): Boolean

    /**
     * Gets information about the current AI service status.
     *
     * Provides details like:
     * - Service availability
     * - Model version
     * - Last error (if any)
     *
     * @return Flow emitting current service status
     */
    fun observeServiceStatus(): Flow<AiServiceStatus>
}

/**
 * Represents the current status of the AI service.
 *
 * This sealed interface provides type-safe status information,
 * enabling UI to react appropriately to different service states.
 */
sealed interface AiServiceStatus {
    /**
     * Service is ready and available for use.
     *
     * @property modelVersion Version of the AI model being used
     */
    data class Available(val modelVersion: String) : AiServiceStatus

    /**
     * Service is currently unavailable.
     *
     * @property reason Human-readable explanation of why service is unavailable
     */
    data class Unavailable(val reason: String) : AiServiceStatus

    /**
     * Service encountered an error.
     *
     * @property error The error that occurred
     * @property isRecoverable Whether the error might resolve itself
     */
    data class Error(
        val error: Throwable,
        val isRecoverable: Boolean = true
    ) : AiServiceStatus
}

/**
 * Repository interface for managing user preferences and settings.
 *
 * This interface provides access to persistent user preferences,
 * abstracting the storage mechanism (DataStore, SharedPreferences, etc.).
 *
 * All preferences are exposed as Flows to enable reactive updates when
 * preferences change, even from other parts of the application.
 *
 * @since 1.0.0
 */
interface PreferencesRepository {
    /**
     * Observes the current AI configuration.
     *
     * Emits the complete configuration whenever any part of it changes
     * (mode, API key, or model parameters).
     *
     * @return Flow emitting current AI configuration
     */
    fun observeAiConfiguration(): Flow<AiConfiguration>

    /**
     * Updates the AI configuration.
     *
     * Persists the new configuration. The change will be reflected in
     * observeAiConfiguration() flow.
     *
     * @param configuration The new configuration to save
     * @return Result.success if saved successfully, Result.failure on error
     */
    suspend fun updateAiConfiguration(configuration: AiConfiguration): Result<Unit>

    /**
     * Updates only the AI mode, preserving other configuration.
     *
     * This is a convenience method for the common case of switching modes.
     *
     * @param mode The new AI mode
     * @return Result.success if saved successfully, Result.failure on error
     */
    suspend fun updateAiMode(mode: com.novachat.app.domain.model.AiMode): Result<Unit>

    /**
     * Updates only the API key, preserving other configuration.
     *
     * This is a convenience method for setting/changing the API key.
     *
     * @param apiKey The new API key (null to clear)
     * @return Result.success if saved successfully, Result.failure on error
     */
    suspend fun updateApiKey(apiKey: com.novachat.app.domain.model.ApiKey?): Result<Unit>

    /**
     * Clears all stored preferences, resetting to defaults.
     *
     * @return Result.success if cleared successfully, Result.failure on error
     */
    suspend fun clearAll(): Result<Unit>

    /**
     * Observes theme preferences (dark mode, dynamic color).
     *
     * @return Flow emitting current theme preferences
     */
    fun observeThemePreferences(): Flow<ThemePreferences>

    /**
     * Updates theme preferences.
     *
     * @param preferences The new theme preferences to save
     * @return Result.success if saved successfully, Result.failure on error
     */
    suspend fun updateThemePreferences(preferences: ThemePreferences): Result<Unit>
}
