package com.novachat.feature.ai.domain.model

data class AiConfiguration(
    val mode: AiMode,
    val modelParameters: ModelParameters = ModelParameters.DEFAULT
) {
    fun validate(): Result<Unit> {
        return when (mode) {
            AiMode.ONLINE -> Result.success(Unit)
            AiMode.OFFLINE -> Result.success(Unit)
        }
    }

    fun supportsMode(targetMode: AiMode): Boolean {
        return when (targetMode) {
            AiMode.ONLINE -> true
            AiMode.OFFLINE -> true
        }
    }
}

sealed interface AiMode {
    data object ONLINE : AiMode

    data object OFFLINE : AiMode

    companion object {
        const val DEFAULT_MODEL_NAME = "gemini-2.5-flash"

        fun fromString(value: String): AiMode = when (value.uppercase()) {
            "ONLINE" -> ONLINE
            "OFFLINE" -> OFFLINE
            else -> ONLINE
        }

        fun toString(mode: AiMode): String = when (mode) {
            ONLINE -> "ONLINE"
            OFFLINE -> "OFFLINE"
        }
    }
}

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
        val DEFAULT = ModelParameters(
            temperature = 0.7f,
            topK = 40,
            topP = 0.95f,
            maxOutputTokens = 2048
        )

        val CREATIVE = ModelParameters(
            temperature = 0.9f,
            topK = 50,
            topP = 0.98f,
            maxOutputTokens = 2048
        )

        val PRECISE = ModelParameters(
            temperature = 0.3f,
            topK = 20,
            topP = 0.85f,
            maxOutputTokens = 1024
        )
    }

    fun clampValues(): ModelParameters = ModelParameters(
        temperature = temperature.coerceIn(0.0f, 2.0f),
        topK = topK.coerceAtLeast(1),
        topP = topP.coerceIn(0.0f, 1.0f),
        maxOutputTokens = maxOutputTokens.coerceAtLeast(1)
    )
}
