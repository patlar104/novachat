package com.novachat.feature.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novachat.feature.ai.R
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.presentation.model.ChatUiEvent
import com.novachat.feature.ai.presentation.model.ChatUiState
import com.novachat.feature.ai.presentation.model.NavigationDestination
import com.novachat.feature.ai.presentation.model.UiEffect
import com.novachat.feature.ai.presentation.viewmodel.ChatViewModel
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
                        NavigationDestination.Settings -> onNavigateToSettings()
                        else -> Unit
                    }
                }
                else -> Unit
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
                title = {
                    Text(
                        text = stringResource(R.string.chat_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { onEvent(ChatUiEvent.ClearConversation) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.clear_chat),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = { onEvent(ChatUiEvent.NavigateToSettings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                },
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(bottom = 4.dp)
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            tonalElevation = if (isUser) 2.dp else 1.dp,
            shadowElevation = if (isUser) 1.dp else 0.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
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
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.type_message),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                enabled = !isLoading,
                maxLines = 4,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            FilledTonalIconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank() && !isLoading,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.send),
                    modifier = Modifier.size(24.dp)
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.emoji_wave),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.empty_state_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.empty_state_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
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
            modifier = Modifier.padding(40.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            if (isRecoverable) {
                Spacer(modifier = Modifier.height(24.dp))
                FilledTonalButton(onClick = onDismiss) {
                    Text(stringResource(R.string.try_again))
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
