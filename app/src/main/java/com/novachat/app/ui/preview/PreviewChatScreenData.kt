package com.novachat.app.ui.preview

import com.novachat.app.domain.model.Message
import com.novachat.app.domain.model.MessageId
import com.novachat.app.domain.model.MessageSender
import com.novachat.app.presentation.model.ChatUiState
import java.time.Instant

object PreviewChatScreenData {
    fun initialState(): ChatUiState = ChatUiState.Initial

    fun loadingState(): ChatUiState = ChatUiState.Loading

    fun successSingleExchange(): ChatUiState {
        val now = Instant.now()
        return ChatUiState.Success(
            messages = listOf(
                Message(
                    id = MessageId("user-1"),
                    content = "Hello! Can you help me with Compose previews?",
                    sender = MessageSender.USER,
                    timestamp = now
                ),
                Message(
                    id = MessageId("ai-1"),
                    content = "Sure. I can show you how to structure previews for multiple states.",
                    sender = MessageSender.ASSISTANT,
                    timestamp = now.plusSeconds(2)
                )
            ),
            isProcessing = false,
            error = null
        )
    }

    fun successProcessing(): ChatUiState {
        val now = Instant.now()
        return ChatUiState.Success(
            messages = listOf(
                Message(
                    id = MessageId("user-2"),
                    content = "What is the difference between previews and runtime?",
                    sender = MessageSender.USER,
                    timestamp = now
                )
            ),
            isProcessing = true,
            error = null
        )
    }

    fun successLongConversation(): ChatUiState {
        val now = Instant.now()
        val messages = buildList {
            for (index in 1..10) {
                add(
                    Message(
                        id = MessageId("user-$index"),
                        content = "User message $index with some extra text to test wrapping.",
                        sender = MessageSender.USER,
                        timestamp = now.minusSeconds((10L - index) * 5)
                    )
                )
                add(
                    Message(
                        id = MessageId("ai-$index"),
                        content = "AI response $index explaining Compose preview details and edge cases.",
                        sender = MessageSender.ASSISTANT,
                        timestamp = now.minusSeconds((10L - index) * 5 - 2)
                    )
                )
            }
        }
        return ChatUiState.Success(
            messages = messages,
            isProcessing = false,
            error = null
        )
    }

    fun successWithErrorBanner(): ChatUiState {
        val now = Instant.now()
        return ChatUiState.Success(
            messages = listOf(
                Message(
                    id = MessageId("user-3"),
                    content = "This message failed to send.",
                    sender = MessageSender.USER,
                    timestamp = now
                )
            ),
            isProcessing = false,
            error = "Network error. Check your connection and retry."
        )
    }

    fun criticalError(): ChatUiState =
        ChatUiState.Error(
            message = "Unable to load chat. Please restart the app.",
            cause = null,
            isRecoverable = true
        )
}
