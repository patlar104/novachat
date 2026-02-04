package com.novachat.app.domain.model

/**
 * Domain model representing AI configuration settings.
 *
 * This model encapsulates all configuration parameters needed for AI interactions,
 * following the principle of explicit configuration over implicit defaults.
 *
 * @property mode The AI execution mode (online cloud-based or offline on-device)
 * @property apiKey Optional API key for online mode, null for offline mode
 * @property modelParameters Parameters controlling AI generation behavior
 *
 * @since 1.0.0
 */
data class AiConfiguration(
    val mode: AiMode,
    val apiKey: ApiKey?,
    val modelParameters: ModelParameters = ModelParameters.DEFAULT
) {
    /**
     * Validates that the configuration is properly set up for the selected mode.
     *
     * @return Result.success(Unit) if valid, Result.failure with explanation if invalid
     */
    fun validate(): Result<Unit> {
        return when (mode) {
            AiMode.ONLINE -> {
                if (apiKey == null || !apiKey.isValid()) {
                    Result.failure(
                        IllegalStateException("API key is required for online mode")
                    )
                } else {
                    Result.success(Unit)
                }
            }
            AiMode.OFFLINE -> {
                // Offline mode doesn't require API key but needs device support
                Result.success(Unit)
            }
        }
    }

    /**
     * Checks if this configuration supports the specified mode.
     *
     * @param targetMode The mode to check support for
     * @return true if the configuration can support the target mode
     */
    fun supportsMode(targetMode: AiMode): Boolean {
        return when (targetMode) {
            AiMode.ONLINE -> apiKey != null && apiKey.isValid()
            AiMode.OFFLINE -> true // Always attempt offline if requested
        }
    }
}

/**
 * Represents the AI execution mode.
 *
 * Using a sealed interface allows for future extension (e.g., HYBRID mode)
 * while maintaining exhaustive pattern matching.
 */
sealed interface AiMode {
    /**
     * Online mode using cloud-based AI services (Google Gemini).
     * Requires internet connectivity and a valid API key.
     */
    data object ONLINE : AiMode

    /**
     * Offline mode using on-device AI (Google AICore).
     * Requires device support but works without internet.
     */
    data object OFFLINE : AiMode

    companion object {
        /**
         * Converts a string representation to an AiMode.
         * Used for deserialization from storage.
         *
         * @param value String representation of the mode
         * @return Corresponding AiMode, defaults to ONLINE if unrecognized
         */
        fun fromString(value: String): AiMode = when (value.uppercase()) {
            "ONLINE" -> ONLINE
            "OFFLINE" -> OFFLINE
            else -> ONLINE // Safe default
        }

        /**
         * Converts an AiMode to its string representation.
         * Used for serialization to storage.
         *
         * @param mode The mode to convert
         * @return String representation of the mode
         */
        fun toString(mode: AiMode): String = when (mode) {
            ONLINE -> "ONLINE"
            OFFLINE -> "OFFLINE"
        }
    }
}

/**
 * Type-safe wrapper for API keys with validation.
 *
 * Using a value class provides type safety and ensures API keys
 * are never confused with regular strings.
 *
 * @property value The raw API key string
 */
@JvmInline
value class ApiKey private constructor(val value: String) {
    companion object {
        private const val MIN_KEY_LENGTH = 20
        
        /**
         * Creates an ApiKey from a string, validating basic format.
         *
         * @param key The API key string
         * @return ApiKey if valid, null if invalid
         */
        fun create(key: String): ApiKey? {
            return if (key.isNotBlank() && key.length >= MIN_KEY_LENGTH) {
                ApiKey(key.trim())
            } else {
                null
            }
        }

        /**
         * Creates an ApiKey without validation.
         * Use only when deserializing from trusted storage.
         *
         * @param key The API key string
         * @return ApiKey wrapping the provided value
         */
        fun unsafe(key: String): ApiKey = ApiKey(key)
    }

    /**
     * Validates that this API key meets basic requirements.
     *
     * @return true if the key appears valid (not a security check)
     */
    fun isValid(): Boolean = value.isNotBlank() && value.length >= MIN_KEY_LENGTH

    /**
     * Returns a redacted version of the API key for logging.
     * Shows only the first 4 and last 4 characters.
     *
     * @return Redacted API key string
     */
    fun toRedactedString(): String {
        return if (value.length <= 8) {
            "****"
        } else {
            "${value.take(4)}...${value.takeLast(4)}"
        }
    }

    override fun toString(): String = toRedactedString()
}

/**
 * Parameters controlling AI model generation behavior.
 *
 * These parameters affect how the AI generates responses.
 * All values are clamped to valid ranges.
 *
 * @property temperature Controls randomness (0.0 = deterministic, 1.0 = creative)
 * @property topK Limits token selection to top K candidates
 * @property topP Nucleus sampling threshold
 * @property maxOutputTokens Maximum length of generated response
 *
 * @since 1.0.0
 */
data class ModelParameters(
    val temperature: Float,
    val topK: Int,
    val topP: Float,
    val maxOutputTokens: Int
) {
    init {
        require(temperature in 0.0f..2.0f) { 
            "Temperature must be between 0.0 and 2.0, got $temperature" 
        }
        require(topK > 0) { 
            "TopK must be positive, got $topK" 
        }
        require(topP in 0.0f..1.0f) { 
            "TopP must be between 0.0 and 1.0, got $topP" 
        }
        require(maxOutputTokens > 0) { 
            "MaxOutputTokens must be positive, got $maxOutputTokens" 
        }
    }

    companion object {
        /**
         * Default parameters optimized for conversational responses.
         * These values provide a good balance between creativity and coherence.
         */
        val DEFAULT = ModelParameters(
            temperature = 0.7f,
            topK = 40,
            topP = 0.95f,
            maxOutputTokens = 2048
        )

        /**
         * Parameters for more creative, varied responses.
         */
        val CREATIVE = ModelParameters(
            temperature = 0.9f,
            topK = 50,
            topP = 0.98f,
            maxOutputTokens = 2048
        )

        /**
         * Parameters for more focused, deterministic responses.
         */
        val PRECISE = ModelParameters(
            temperature = 0.3f,
            topK = 20,
            topP = 0.85f,
            maxOutputTokens = 1024
        )
    }

    /**
     * Creates a copy with clamped values to ensure they're within valid ranges.
     *
     * @return A new ModelParameters with all values clamped to valid ranges
     */
    fun clampValues(): ModelParameters = ModelParameters(
        temperature = temperature.coerceIn(0.0f, 2.0f),
        topK = topK.coerceAtLeast(1),
        topP = topP.coerceIn(0.0f, 1.0f),
        maxOutputTokens = maxOutputTokens.coerceAtLeast(1)
    )
}
