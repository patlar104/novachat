package com.novachat.app.ui.previews

import androidx.lifecycle.SavedStateHandle
import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.presentation.model.ChatUiEvent
import com.novachat.app.presentation.model.ChatUiState
import com.novachat.app.presentation.model.SettingsUiEvent
import com.novachat.app.presentation.model.SettingsUiState
import com.novachat.app.presentation.model.UiEffect
import com.novachat.app.presentation.viewmodel.ChatViewModel
import com.novachat.app.presentation.viewmodel.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Factory functions for creating mock ViewModels for preview rendering.
 *
 * These functions create fully functional mock ViewModels that don't require
 * actual use cases, repositories, or dependencies. They're optimized for preview
 * rendering in Android Studio's Design Preview pane.
 *
 * Usage in previews:
 * ```
 * @Preview
 * @Composable
 * fun MyScreenPreview() {
 *     MyScreen(
 *         viewModel = createPreviewChatViewModel(
 *             initialState = ChatUiState.Loading
 *         ),
 *         onNavigateToSettings = {}
 *     )
 * }
 * ```
 *
 * @see ChatViewModel
 * @see SettingsViewModel
 */

// ============================================================
// CHAT VIEWMODEL PREVIEW FACTORY
// ============================================================

/**
 * Creates a mock ChatViewModel for preview rendering.
 *
 * All flows are properly initialized with test values. The mock responds to
 * event calls appropriately, simulating real ViewModel behavior without
 * needing actual dependencies.
 *
 * @param initialState Initial UI state to display
 * @param draftMessage Initial draft message text
 * @return Fully configured mock ChatViewModel
 */
fun createPreviewChatViewModel(
    initialState: ChatUiState = ChatUiState.Success(
        messages = listOf(
            previewUserMessage("Hello!"),
            previewAiMessage("Hi! How can I help?")
        ),
        isProcessing = false,
        error = null
    ),
    draftMessage: String = ""
): ChatViewModel {
    val mockViewModel = mockk<ChatViewModel>(relaxed = true)
    
    // Initialize state flows
    val stateFlow = MutableStateFlow(initialState)
    val draftFlow = MutableStateFlow(draftMessage)
    
    // Configure StateFlow properties
    every { mockViewModel.uiState } returns stateFlow.asStateFlow()
    every { mockViewModel.draftMessage } returns draftFlow.asStateFlow()
    
    // Configure effect flow (empty for previews - no side effects needed)
    every { mockViewModel.uiEffect } returns emptyFlow()
    
    // Configure event handling to update state realistically
    every { mockViewModel.onEvent(any()) } answers {
        val event = firstArg<ChatUiEvent>()
        when (event) {
            is ChatUiEvent.SendMessage -> {
                // Simulate sending message: add user message, then AI response
                stateFlow.update { currentState ->
                    val success = (currentState as? ChatUiState.Success) ?: return@update currentState
                    success.copy(
                        messages = success.messages + listOf(
                            previewUserMessage(event.text),
                            previewAiMessage("This is a mock AI response to: ${event.text}")
                        ),
                        isProcessing = false
                    )
                }
                draftFlow.value = ""
            }
            
            is ChatUiEvent.ClearConversation -> {
                // Clear all messages, reset to empty
                stateFlow.value = ChatUiState.Success(
                    messages = emptyList(),
                    isProcessing = false,
                    error = null
                )
            }
            
            is ChatUiEvent.DismissError -> {
                // Remove error from current state
                stateFlow.update { currentState ->
                    val success = (currentState as? ChatUiState.Success) ?: return@update currentState
                    success.copy(error = null)
                }
            }
            
            is ChatUiEvent.RetryMessage -> {
                // Simulate retry (in preview, just resend the message)
                stateFlow.update { currentState ->
                    val success = (currentState as? ChatUiState.Success) ?: return@update currentState
                    success.copy(isProcessing = false, error = null)
                }
            }
            
            is ChatUiEvent.NavigateToSettings -> {
                // Navigation would be handled by UI in real scenario
                // For preview, just log that it was called
            }
            
            is ChatUiEvent.ScreenLoaded -> {
                // Initialize screen - already done in preview
            }
        }
    }
    
    // Configure draft message updates
    every { mockViewModel.updateDraftMessage(any()) } answers {
        draftFlow.value = firstArg<String>()
    }
    
    return mockViewModel
}

// ============================================================
// SETTINGS VIEWMODEL PREVIEW FACTORY
// ============================================================

/**
 * Creates a mock SettingsViewModel for preview rendering.
 *
 * @param initialState Initial settings UI state
 * @param draftApiKey Initial draft API key
 * @param showSaveSuccess Whether to show save success message
 * @return Fully configured mock SettingsViewModel
 */
fun createPreviewSettingsViewModel(
    initialState: SettingsUiState = SettingsUiState.Success(
        configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            apiKey = "sk-proj-abc123def456-example-key"
        )
    ),
    draftApiKey: String = "sk-proj-abc123def456-example-key",
    showSaveSuccess: Boolean = false
): SettingsViewModel {
    val mockViewModel = mockk<SettingsViewModel>(relaxed = true)
    
    // Initialize state flows
    val stateFlow = MutableStateFlow(initialState)
    val draftFlow = MutableStateFlow(draftApiKey)
    val successFlow = MutableStateFlow(showSaveSuccess)
    
    // Configure properties
    every { mockViewModel.uiState } returns stateFlow.asStateFlow()
    every { mockViewModel.draftApiKey } returns draftFlow.asStateFlow()
    every { mockViewModel.showSaveSuccess } returns successFlow.asStateFlow()
    every { mockViewModel.uiEffect } returns emptyFlow()
    
    // Configure event handling
    every { mockViewModel.onEvent(any()) } answers {
        val event = firstArg<SettingsUiEvent>()
        when (event) {
            is SettingsUiEvent.SaveApiKey -> {
                // Save the API key and show success message
                stateFlow.update { currentState ->
                    val success = (currentState as? SettingsUiState.Success) ?: return@update currentState
                    success.copy(
                        configuration = success.configuration.copy(
                            apiKey = event.apiKey
                        )
                    )
                }
                successFlow.value = true
                draftFlow.value = event.apiKey
            }
            
            is SettingsUiEvent.ChangeAiMode -> {
                // Change the AI mode
                stateFlow.update { currentState ->
                    val success = (currentState as? SettingsUiState.Success) ?: return@update currentState
                    success.copy(
                        configuration = success.configuration.copy(mode = event.mode)
                    )
                }
            }
            
            is SettingsUiEvent.DismissSaveSuccess -> {
                // Hide the success message
                successFlow.value = false
            }
            
            is SettingsUiEvent.NavigateBack -> {
                // Navigation handled by UI
            }
            
            is SettingsUiEvent.TestConfiguration -> {
                // Test would be handled in real scenario
            }
            
            is SettingsUiEvent.ScreenLoaded -> {
                // Already initialized in preview
            }
        }
    }
    
    // Configure draft API key updates
    every { mockViewModel.updateDraftApiKey(any()) } answers {
        draftFlow.value = firstArg<String>()
    }
    
    return mockViewModel
}

// ============================================================
// VIEWMODEL VERIFICATION HELPERS
// ============================================================

/**
 * Verifies that a specific event was sent to the ViewModel.
 *
 * Useful for testing that preview interactions work correctly.
 * Example: `verifyChatViewModelEvent(viewModel, ChatUiEvent.ClearConversation)`
 */
fun verifyChatViewModelEvent(
    viewModel: ChatViewModel,
    event: ChatUiEvent
) {
    verify { viewModel.onEvent(event) }
}

/**
 * Verifies that draft message was updated.
 */
fun verifyDraftMessageUpdate(
    viewModel: ChatViewModel,
    message: String
) {
    verify { viewModel.updateDraftMessage(message) }
}
