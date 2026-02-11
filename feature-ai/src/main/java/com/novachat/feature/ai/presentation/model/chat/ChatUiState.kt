package com.novachat.feature.ai.presentation.model

import com.novachat.feature.ai.domain.model.Message

sealed interface ChatUiState {
    data object Initial : ChatUiState

    data object Loading : ChatUiState

    data class Success(
        val messages: List<Message>,
        val isProcessing: Boolean = false,
        val error: String? = null
    ) : ChatUiState {
        fun hasMessages(): Boolean = messages.isNotEmpty()

        fun getLastMessage(): Message? = messages.lastOrNull()

        fun getMessageStats(): MessageStats {
            val userCount = messages.count { it.isFromUser() }
            val aiCount = messages.count { it.isFromAssistant() }
            return MessageStats(userCount = userCount, aiCount = aiCount)
        }
    }

    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val isRecoverable: Boolean = true
    ) : ChatUiState
}
