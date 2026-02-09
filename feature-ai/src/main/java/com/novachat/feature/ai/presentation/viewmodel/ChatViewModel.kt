package com.novachat.feature.ai.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.usecase.ClearConversationUseCase
import com.novachat.feature.ai.domain.usecase.ObserveMessagesUseCase
import com.novachat.feature.ai.domain.usecase.RetryMessageUseCase
import com.novachat.feature.ai.domain.usecase.SendMessageUseCase
import com.novachat.feature.ai.presentation.model.ChatUiEvent
import com.novachat.feature.ai.presentation.model.ChatUiState
import com.novachat.feature.ai.presentation.model.NavigationDestination
import com.novachat.feature.ai.presentation.model.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the chat screen following 2026 Android best practices.
 *
 * This ViewModel:
 * - Uses use cases instead of repositories directly (Clean Architecture)
 * - Implements SavedStateHandle for process death survival
 * - Uses sealed UI state classes for type safety
 * - Separates one-time effects from persistent state
 * - Handles all user events through a single entry point
 * - Provides comprehensive error handling
 *
 * Architecture:
 * - UI emits ChatUiEvent → ViewModel processes → Updates ChatUiState
 * - One-time actions → UiEffect channel → UI observes and reacts
 *
 * @property savedStateHandle For surviving process death
 * @property sendMessageUseCase Handles sending messages
 * @property observeMessagesUseCase Observes conversation messages
 * @property clearConversationUseCase Clears conversation history
 * @property retryMessageUseCase Retries failed messages
 *
 * @since 1.0.0
 */
class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sendMessageUseCase: SendMessageUseCase,
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val clearConversationUseCase: ClearConversationUseCase,
    private val retryMessageUseCase: RetryMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    companion object {
        internal const val KEY_DRAFT_MESSAGE = "draft_message"
    }

    val draftMessage: StateFlow<String> = savedStateHandle.getStateFlow(
        key = KEY_DRAFT_MESSAGE,
        initialValue = ""
    )

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            observeMessagesUseCase()
                .catch { exception ->
                    _uiState.update {
                        ChatUiState.Error(
                            message = "Failed to load messages: ${exception.message}",
                            isRecoverable = true
                        )
                    }
                }
                .collect { messages ->
                    _uiState.update { currentState ->
                        when (currentState) {
                            is ChatUiState.Initial,
                            is ChatUiState.Loading,
                            is ChatUiState.Error -> {
                                ChatUiState.Success(
                                    messages = messages,
                                    isProcessing = false,
                                    error = null
                                )
                            }
                            is ChatUiState.Success -> {
                                currentState.copy(messages = messages)
                            }
                        }
                    }
                }
        }
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.SendMessage -> handleSendMessage(event.text)
            is ChatUiEvent.ClearConversation -> handleClearConversation()
            is ChatUiEvent.RetryMessage -> handleRetryMessage(event.messageId)
            is ChatUiEvent.DismissError -> handleDismissError()
            is ChatUiEvent.NavigateToSettings -> handleNavigateToSettings()
            is ChatUiEvent.ScreenLoaded -> handleScreenLoaded()
        }
    }

    fun updateDraftMessage(text: String) {
        savedStateHandle[KEY_DRAFT_MESSAGE] = text
    }

    private fun handleSendMessage(messageText: String) {
        if (messageText.isBlank()) {
            emitEffect(UiEffect.ShowToast("Please enter a message"))
            return
        }

        _uiState.update { currentState ->
            when (currentState) {
                is ChatUiState.Success -> currentState.copy(
                    isProcessing = true,
                    error = null
                )
                else -> ChatUiState.Loading
            }
        }

        viewModelScope.launch {
            val result = sendMessageUseCase(messageText)

            result.fold(
                onSuccess = {
                    updateDraftMessage("")
                    _uiState.update { currentState ->
                        when (currentState) {
                            is ChatUiState.Success -> currentState.copy(
                                isProcessing = false,
                                error = null
                            )
                            else -> currentState
                        }
                    }
                },
                onFailure = { exception ->
                    val errorMessage = exception.message ?: "Failed to send message"

                    _uiState.update { currentState ->
                        when (currentState) {
                            is ChatUiState.Success -> currentState.copy(
                                isProcessing = false,
                                error = errorMessage
                            )
                            else -> ChatUiState.Error(
                                message = errorMessage,
                                isRecoverable = true
                            )
                        }
                    }

                    emitEffect(
                        UiEffect.ShowSnackbar(
                            message = errorMessage,
                            actionLabel = "Dismiss"
                        )
                    )
                }
            )
        }
    }

    private fun handleClearConversation() {
        viewModelScope.launch {
            val result = clearConversationUseCase()

            result.fold(
                onSuccess = {
                    emitEffect(UiEffect.ShowToast("Conversation cleared"))
                },
                onFailure = { exception ->
                    emitEffect(
                        UiEffect.ShowSnackbar(
                            message = "Failed to clear: ${exception.message}",
                            actionLabel = "Dismiss"
                        )
                    )
                }
            )
        }
    }

    private fun handleRetryMessage(messageId: MessageId) {
        _uiState.update { currentState ->
            when (currentState) {
                is ChatUiState.Success -> currentState.copy(isProcessing = true)
                else -> currentState
            }
        }

        viewModelScope.launch {
            val result = retryMessageUseCase(messageId)

            result.fold(
                onSuccess = {
                    _uiState.update { currentState ->
                        when (currentState) {
                            is ChatUiState.Success -> currentState.copy(
                                isProcessing = false,
                                error = null
                            )
                            else -> currentState
                        }
                    }
                },
                onFailure = { exception ->
                    _uiState.update { currentState ->
                        when (currentState) {
                            is ChatUiState.Success -> currentState.copy(
                                isProcessing = false,
                                error = exception.message
                            )
                            else -> currentState
                        }
                    }

                    emitEffect(
                        UiEffect.ShowSnackbar(
                            message = "Retry failed: ${exception.message}",
                            actionLabel = "Dismiss"
                        )
                    )
                }
            )
        }
    }

    private fun handleDismissError() {
        _uiState.update { currentState ->
            when (currentState) {
                is ChatUiState.Success -> currentState.copy(error = null)
                is ChatUiState.Error -> ChatUiState.Initial
                else -> currentState
            }
        }
    }

    private fun handleNavigateToSettings() {
        emitEffect(UiEffect.Navigate(NavigationDestination.Settings))
    }

    private fun handleScreenLoaded() {
        // Currently no action needed
    }

    private fun emitEffect(effect: UiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
