package com.novachat.feature.ai.presentation.model

import com.novachat.feature.ai.domain.model.MessageId

sealed interface ChatUiEvent {
    data class SendMessage(val text: String) : ChatUiEvent

    data object ClearConversation : ChatUiEvent

    data class RetryMessage(val messageId: MessageId) : ChatUiEvent

    data object DismissError : ChatUiEvent

    data object NavigateToSettings : ChatUiEvent
}
