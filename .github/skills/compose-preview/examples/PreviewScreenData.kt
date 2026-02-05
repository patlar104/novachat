package com.novachat.app.ui.previews

import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.model.Message
import com.novachat.app.domain.model.MessageSender
import com.novachat.app.presentation.model.ChatUiState
import com.novachat.app.presentation.model.SettingsUiState
import java.time.Instant

private val previewBaseTime = Instant.parse("2026-01-01T00:00:00Z")

fun previewUserMessage(content: String, offsetSeconds: Long = 0): Message = Message(
    content = content,
    sender = MessageSender.USER,
    timestamp = previewBaseTime.plusSeconds(offsetSeconds)
)

fun previewAiMessage(content: String, offsetSeconds: Long = 30): Message = Message(
    content = content,
    sender = MessageSender.ASSISTANT,
    timestamp = previewBaseTime.plusSeconds(offsetSeconds)
)

val testMessages = listOf(
    previewUserMessage("Hello!"),
    previewAiMessage("Hi! How can I help today?")
)

val shortTestMessages = listOf(
    previewUserMessage("Help me plan a trip."),
    previewAiMessage("Sure - where do you want to go?")
)

val longMessageTestMessages = listOf(
    previewUserMessage("This is a long user message to validate wrapping and spacing on narrow screens."),
    previewAiMessage(
        "Here is a longer response that spans multiple lines to check bubble layout " +
            "and text wrapping in previews."
    )
)

val shortMessageTestMessages = listOf(
    previewUserMessage("Ok."),
    previewAiMessage("Great.")
)

val mixedLengthMessages = listOf(
    previewUserMessage("Short."),
    previewAiMessage("A slightly longer response that should wrap."),
    previewUserMessage(
        "A very long message that keeps going to test list sizing and line breaks " +
            "in the preview list."
    )
)

val longTestMessages = listOf(
    previewUserMessage("Hello!"),
    previewAiMessage("Hi there."),
    previewUserMessage("Can you summarize the meeting notes?"),
    previewAiMessage(
        "Sure. The team agreed to prioritize reliability, update docs, and schedule a follow-up."
    ),
    previewUserMessage("Any blockers?"),
    previewAiMessage("The main blocker is review bandwidth this week."),
    previewUserMessage("Thanks!"),
    previewAiMessage("Anytime.")
)

object PreviewChatScreenData {
    fun initialState(): ChatUiState = ChatUiState.Initial

    fun loadingState(): ChatUiState = ChatUiState.Loading

    fun successEmpty(): ChatUiState = ChatUiState.Success(messages = emptyList())

    fun successSingleExchange(): ChatUiState = ChatUiState.Success(messages = testMessages)

    fun successProcessing(): ChatUiState = ChatUiState.Success(
        messages = shortTestMessages,
        isProcessing = true
    )

    fun successWithErrorBanner(): ChatUiState = ChatUiState.Success(
        messages = testMessages,
        error = "Failed to send message. Tap to retry."
    )

    fun errorRecoverable(): ChatUiState = ChatUiState.Error(
        message = "Failed to load messages. Please check your connection.",
        isRecoverable = true
    )

    fun errorNotRecoverable(): ChatUiState = ChatUiState.Error(
        message = "Critical error: Invalid configuration. Please reinstall the app.",
        isRecoverable = false
    )
}

object PreviewSettingsScreenData {
    fun initialState(): SettingsUiState = SettingsUiState.Initial

    fun loadingState(): SettingsUiState = SettingsUiState.Loading

    fun successOnline(): SettingsUiState = SettingsUiState.Success(
        configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            apiKey = "sk-proj-abc123def456-example-key"
        )
    )

    fun successOnlineMissingKey(): SettingsUiState = SettingsUiState.Success(
        configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            apiKey = null
        )
    )

    fun successOfflineUnavailable(): SettingsUiState = SettingsUiState.Success(
        configuration = AiConfiguration(
            mode = AiMode.OFFLINE,
            apiKey = null
        )
    )

    fun errorRecoverable(): SettingsUiState = SettingsUiState.Error(
        message = "Failed to load settings. Please try again.",
        isRecoverable = true
    )
}
