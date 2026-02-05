package com.novachat.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novachat.app.R
import com.novachat.app.domain.model.Message
import com.novachat.app.domain.model.MessageId
import com.novachat.app.domain.model.MessageSender
import com.novachat.app.presentation.model.ChatUiEvent
import com.novachat.app.presentation.model.ChatUiState
import com.novachat.app.presentation.model.UiEffect
import com.novachat.app.presentation.viewmodel.ChatViewModel
import java.util.UUID
import kotlinx.coroutines.launch

/**
 * Chat screen composable using the new architecture.
 *
 * Demonstrates:
 * - Observing UI state with sealed classes
 * - Handling UI effects (one-time events)
 * - Event-driven architecture
 * - Reactive UI updates
 *
 * @param viewModel The chat ViewModel
 * @param onNavigateToSettings Navigation callback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, onNavigateToSettings: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val draftMessage by viewModel.draftMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel,
                        duration = SnackbarDuration.Long
                    )
                }
                is UiEffect.Navigate -> {
                    when (effect.destination) {
                        is com.novachat.app.presentation.model.NavigationDestination.Settings -> onNavigateToSettings()
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    ChatScreenContent(
        uiState = uiState,
        draftMessage = draftMessage,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onDraftMessageChange = viewModel::updateDraftMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    draftMessage: String,
    snackbarHostState: SnackbarHostState,
    onEvent: (ChatUiEvent) -> Unit,
    onDraftMessageChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_title)) },
                actions = {
                    IconButton(onClick = { onEvent(ChatUiEvent.ClearConversation) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear_chat)
                        )
                    }
                    IconButton(onClick = { onEvent(ChatUiEvent.NavigateToSettings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MessageInputBar(
                messageText = draftMessage,
                onMessageTextChange = onDraftMessageChange,
                onSendMessage = {
                    onEvent(ChatUiEvent.SendMessage(draftMessage))
                },
                isLoading = when (uiState) {
                    is ChatUiState.Success -> uiState.isProcessing
                    is ChatUiState.Loading -> true
                    else -> false
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ChatUiState.Initial -> {
                EmptyState(modifier = Modifier.padding(paddingValues))
            }
            is ChatUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ChatUiState.Success -> {
                ChatContent(
                    messages = state.messages,
                    isProcessing = state.isProcessing,
                    error = state.error,
                    onDismissError = { onEvent(ChatUiEvent.DismissError) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ChatUiState.Error -> {
                ErrorState(
                    message = state.message,
                    isRecoverable = state.isRecoverable,
                    onDismiss = { onEvent(ChatUiEvent.DismissError) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun ChatContent(
    messages: List<Message>,
    isProcessing: Boolean,
    error: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Error banner
        error?.let {
            ErrorBanner(message = it, onDismiss = onDismissError)
        }

        // Messages list
        if (messages.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id.value }) { message ->
                    MessageBubble(message = message)
                }

                // Loading indicator
                if (isProcessing) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean
) {
    Surface(shadowElevation = 8.dp, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                placeholder = { Text(stringResource(R.string.type_message)) },
                enabled = !isLoading,
                maxLines = 4
            )

            IconButton(onClick = onSendMessage, enabled = messageText.isNotBlank() && !isLoading) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send),
                    tint = if (messageText.isNotBlank() && !isLoading) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.dismiss))
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.emoji_wave), style = MaterialTheme.typography.displayLarge) // Fixed: Hardcoded string
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.empty_state_title), // Fixed: Hardcoded string
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_state_subtitle), // Fixed: Hardcoded string
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    isRecoverable: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null, // The message below explains the error, so no content description is needed for the icon.
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp) // Large size to match the visual weight of displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            if (isRecoverable) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.try_again)) // Fixed: Hardcoded string
                }
            }
        }
    }
}

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
            Message(MessageId(UUID.randomUUID().toString()), "Hi there!", MessageSender.ASSISTANT) // Corrected from MODEL
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
        uiState = ChatUiState.Error("Something went wrong", isRecoverable = true), // Added isRecoverable
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
            Message(MessageId(UUID.randomUUID().toString()), "This is a model message", MessageSender.ASSISTANT) // Corrected from MODEL
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