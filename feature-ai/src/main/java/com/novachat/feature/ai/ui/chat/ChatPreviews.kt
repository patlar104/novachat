package com.novachat.feature.ai.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.presentation.model.ChatUiState
import java.util.UUID

@Preview(showBackground = true)
@Composable
fun ChatScreenContentPreview() {
    ChatScreenContent(
        uiState = ChatUiState.Initial,
        draftMessage = "",
        snackbarHostState = remember { SnackbarHostState() },
        onEvent = {},
        onDraftMessageChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ChatScreenContentLoadingPreview() {
    ChatScreenContent(
        uiState = ChatUiState.Loading,
        draftMessage = "",
        snackbarHostState = remember { SnackbarHostState() },
        onEvent = {},
        onDraftMessageChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ChatScreenContentSuccessPreview() {
    val messages =
        listOf(
            Message(MessageId(UUID.randomUUID().toString()), "Hello!", MessageSender.USER),
            Message(MessageId(UUID.randomUUID().toString()), "Hi there!", MessageSender.ASSISTANT)
        )
    ChatScreenContent(
        uiState = ChatUiState.Success(messages),
        draftMessage = "Testing",
        snackbarHostState = remember { SnackbarHostState() },
        onEvent = {},
        onDraftMessageChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ChatScreenContentErrorPreview() {
    ChatScreenContent(
        uiState = ChatUiState.Error("Something went wrong", isRecoverable = true),
        draftMessage = "",
        snackbarHostState = remember { SnackbarHostState() },
        onEvent = {},
        onDraftMessageChange = {}
    )
}

@Preview(showBackground = true)
@Composable
fun MessageBubbleUserPreview() {
    MessageBubble(
        message =
            Message(MessageId(UUID.randomUUID().toString()), "This is a user message", MessageSender.USER)
    )
}

@Preview(showBackground = true)
@Composable
fun MessageBubbleModelPreview() {
    MessageBubble(
        message =
            Message(MessageId(UUID.randomUUID().toString()), "This is a model message", MessageSender.ASSISTANT)
    )
}

@Preview(showBackground = true)
@Composable
fun MessageInputBarPreview() {
    MessageInputBar(
        messageText = "Hello world",
        onMessageTextChange = {},
        onSendMessage = {},
        isLoading = false
    )
}

@Preview(showBackground = true)
@Composable
fun MessageInputBarLoadingPreview() {
    MessageInputBar(
        messageText = "Thinking...",
        onMessageTextChange = {},
        onSendMessage = {},
        isLoading = true
    )
}

@Preview(showBackground = true)
@Composable
fun ErrorBannerPreview() {
    ErrorBanner(message = "This is an error message", onDismiss = {})
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    EmptyState()
}

@Preview(showBackground = true)
@Composable
fun ErrorStatePreview() {
    ErrorState(message = "Something went wrong", isRecoverable = true, onDismiss = {})
}

@Preview(showBackground = true)
@Composable
fun ErrorStateNotRecoverablePreview() {
    ErrorState(message = "Something went wrong", isRecoverable = false, onDismiss = {})
}
