package com.novachat.feature.ai.data.model

/**
 * Data model for persisting messages.
 *
 * This model is optimized for storage and differs from the domain Message model.
 * It uses simple types that can be easily serialized to storage formats.
 *
 * This separation allows the data layer to evolve independently from the domain layer,
 * following Clean Architecture principles.
 *
 * @property id Unique message identifier (UUID string)
 * @property content The message text content
 * @property senderType Who sent the message ("USER" or "ASSISTANT")
 * @property timestampMillis Timestamp in milliseconds since epoch
 * @property statusType Current status ("SENT", "PROCESSING", "FAILED")
 * @property errorMessage Error message if status is FAILED (null otherwise)
 * @property isRetryable Whether a failed message can be retried
 *
 * @since 1.0.0
 */
data class MessageEntity(
    val id: String,
    val content: String,
    val senderType: String,
    val timestampMillis: Long,
    val statusType: String,
    val errorMessage: String? = null,
    val isRetryable: Boolean = true
) {
    companion object {
        /**
         * Sender type constants for data storage.
         */
        const val SENDER_USER = "USER"
        const val SENDER_ASSISTANT = "ASSISTANT"

        /**
         * Status type constants for data storage.
         */
        const val STATUS_SENT = "SENT"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_FAILED = "FAILED"
    }

    /**
     * Validates that this entity has valid data.
     *
     * @return true if all required fields are properly formatted
     */
    fun isValid(): Boolean {
        return id.isNotBlank() &&
                content.isNotBlank() &&
                (senderType == SENDER_USER || senderType == SENDER_ASSISTANT) &&
                timestampMillis > 0 &&
                (statusType == STATUS_SENT || statusType == STATUS_PROCESSING || statusType == STATUS_FAILED)
    }

    /**
     * Checks if this message is from the user.
     */
    fun isFromUser(): Boolean = senderType == SENDER_USER

    /**
     * Checks if this message is from the assistant.
     */
    fun isFromAssistant(): Boolean = senderType == SENDER_ASSISTANT

    /**
     * Checks if this message has failed.
     */
    fun hasFailed(): Boolean = statusType == STATUS_FAILED

    /**
     * Checks if this message is being processed.
     */
    fun isProcessing(): Boolean = statusType == STATUS_PROCESSING
}

/**
 * Data model for persisting AI configuration.
 *
 * This model stores configuration in a serializable format optimized for DataStore.
 * All complex types from the domain model are flattened to primitives.
 *
 * @property aiModeValue The AI mode as a string ("ONLINE" or "OFFLINE")
 * @property apiKeyValue Deprecated - Always null. Kept for backward compatibility. Firebase Functions handles authentication.
 * @property temperature Model temperature parameter (0.0 - 2.0)
 * @property topK Model top-K parameter
 * @property topP Model top-P parameter (0.0 - 1.0)
 * @property maxOutputTokens Maximum tokens in response
 *
 * @since 1.0.0
 */
data class AiConfigurationEntity(
    val aiModeValue: String,
    val apiKeyValue: String? = null, // Deprecated - Firebase Functions proxy handles authentication
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 2048
) {
    companion object {
        /**
         * AI mode constants for data storage.
         */
        const val MODE_ONLINE = "ONLINE"
        const val MODE_OFFLINE = "OFFLINE"

        /**
         * Default configuration values.
         */
        val DEFAULT = AiConfigurationEntity(
            aiModeValue = MODE_ONLINE,
            apiKeyValue = null,
            temperature = 0.7f,
            topK = 40,
            topP = 0.95f,
            maxOutputTokens = 2048
        )
    }

    /**
     * Validates that this entity has valid data.
     *
     * @return true if all fields are within valid ranges
     */
    fun isValid(): Boolean {
        return (aiModeValue == MODE_ONLINE || aiModeValue == MODE_OFFLINE) &&
                temperature in 0.0f..2.0f &&
                topK > 0 &&
                topP in 0.0f..1.0f &&
                maxOutputTokens > 0
    }

    /**
     * Checks if this configuration is for online mode.
     */
    fun isOnlineMode(): Boolean = aiModeValue == MODE_ONLINE

    /**
     * Checks if this configuration is for offline mode.
     */
    fun isOfflineMode(): Boolean = aiModeValue == MODE_OFFLINE

    /**
     * Checks if an API key is configured.
     *
     * **Deprecated**: Always returns false. API keys are not used with Firebase Functions proxy.
     */
    fun hasApiKey(): Boolean = false // Always false - Firebase Functions handles authentication
}
