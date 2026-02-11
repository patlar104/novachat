package com.novachat.feature.ai.domain.repository

sealed interface AiServiceStatus {
    data class Available(val modelVersion: String) : AiServiceStatus

    data class Unavailable(val reason: String) : AiServiceStatus

    data class Error(
        val error: Throwable,
        val isRecoverable: Boolean = true
    ) : AiServiceStatus
}
