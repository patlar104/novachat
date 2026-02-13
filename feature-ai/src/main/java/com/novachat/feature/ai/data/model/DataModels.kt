package com.novachat.feature.ai.data.model

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
        const val SENDER_USER = "USER"
        const val SENDER_ASSISTANT = "ASSISTANT"

        const val STATUS_SENT = "SENT"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_FAILED = "FAILED"
    }

    fun isValid(): Boolean {
        return id.isNotBlank() &&
            content.isNotBlank() &&
            (senderType == SENDER_USER || senderType == SENDER_ASSISTANT) &&
            timestampMillis > 0 &&
            (statusType == STATUS_SENT ||
                statusType == STATUS_PROCESSING ||
                statusType == STATUS_FAILED)
    }

    fun isFromUser(): Boolean = senderType == SENDER_USER

    fun isFromAssistant(): Boolean = senderType == SENDER_ASSISTANT

    fun hasFailed(): Boolean = statusType == STATUS_FAILED

    fun isProcessing(): Boolean = statusType == STATUS_PROCESSING
}

data class AiConfigurationEntity(
    val aiModeValue: String,
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 2048
) {
    companion object {
        const val MODE_ONLINE = "ONLINE"
        const val MODE_OFFLINE = "OFFLINE"

        val DEFAULT = AiConfigurationEntity(
            aiModeValue = MODE_ONLINE,
            temperature = 0.7f,
            topK = 40,
            topP = 0.95f,
            maxOutputTokens = 2048
        )
    }

    fun isValid(): Boolean {
        return (aiModeValue == MODE_ONLINE || aiModeValue == MODE_OFFLINE) &&
            temperature in 0.0f..2.0f &&
            topK > 0 &&
            topP in 0.0f..1.0f &&
            maxOutputTokens > 0
    }

    fun isOnlineMode(): Boolean = aiModeValue == MODE_ONLINE

    fun isOfflineMode(): Boolean = aiModeValue == MODE_OFFLINE
}
