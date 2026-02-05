package com.novachat.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novachat.app.R
import com.novachat.app.domain.model.Message
import com.novachat.app.domain.model.MessageSender
import com.novachat.app.presentation.model.ChatUiEvent
import com.novachat.app.presentation.model.ChatUiState
import com.novachat.app.presentation.model.UiEffect
import com.novachat.app.presentation.viewmodel.ChatViewModel
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
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit
) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
            ErrorBanner(
                message = it,
                onDismiss = onDismissError
            )
        }
        
        // Messages list
        if (messages.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxSize())
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
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
    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text(stringResource(R.string.type_message)) },
                enabled = !isLoading,
                maxLines = 4
            )
            
            IconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
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
            Text(
                text = "👋",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Start a conversation!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Type a message below to chat with AI",
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
            Text(
                text = "❌",
                style = MaterialTheme.typography.displayLarge
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
                    Text("Try Again")
                }
            }
        }
    }
}
