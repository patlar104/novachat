package com.novachat.feature.ai.presentation.model

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId

/**
 * UI state representing the chat screen.
 *
 * This sealed interface provides type-safe state management for the chat UI,
 * ensuring all possible states are handled explicitly in the presentation layer.
 *
 * Following 2026 best practices, this uses a sealed interface instead of a sealed class
 * for better extensibility and allows data objects for states without properties.
 *
 * @since 1.0.0
 */
sealed interface ChatUiState {
    /**
     * Initial state when the screen is first loaded.
     * No data is available yet.
     */
    data object Initial : ChatUiState

    /**
     * Loading state while initial data is being fetched.
     * Shows a loading indicator to the user.
     */
    data object Loading : ChatUiState

    /**
     * Success state with conversation data.
     *
     * @property messages List of all messages in the conversation, ordered by timestamp
     * @property isProcessing Whether an AI response is currently being generated
     * @property error Current error message, if any (doesn't block the UI)
     */
    data class Success(
        val messages: List<Message>,
        val isProcessing: Boolean = false,
        val error: String? = null
    ) : ChatUiState {
        /**
         * Checks if there are any messages in the conversation.
         */
        fun hasMessages(): Boolean = messages.isNotEmpty()

        /**
         * Gets the most recent message, if any.
         */
        fun getLastMessage(): Message? = messages.lastOrNull()

        /**
         * Counts user messages vs AI messages.
         */
        fun getMessageStats(): MessageStats {
            val userCount = messages.count { it.isFromUser() }
            val aiCount = messages.count { it.isFromAssistant() }
            return MessageStats(userCount = userCount, aiCount = aiCount)
        }
    }

    /**
     * Error state when a critical error prevents the chat from functioning.
     *
     * @property message Human-readable error message
     * @property cause The underlying exception, if available
     * @property isRecoverable Whether the user can retry or recover from this error
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val isRecoverable: Boolean = true
    ) : ChatUiState
}

/**
 * Statistics about messages in a conversation.
 *
 * @property userCount Number of messages sent by the user
 * @property aiCount Number of messages sent by the AI
 */
data class MessageStats(
    val userCount: Int,
    val aiCount: Int
) {
    /**
     * Total number of messages.
     */
    val total: Int get() = userCount + aiCount

    /**
     * Checks if the conversation is empty.
     */
    fun isEmpty(): Boolean = total == 0
}

/**
 * UI events that can be triggered from the chat screen.
 *
 * This sealed interface represents all possible user actions and system events
 * that the chat screen can emit. Using a sealed interface ensures exhaustive
 * when expressions and type-safe event handling.
 *
 * @since 1.0.0
 */
sealed interface ChatUiEvent {
    /**
     * User wants to send a message.
     *
     * @property text The message text to send
     */
    data class SendMessage(val text: String) : ChatUiEvent

    /**
     * User wants to clear the conversation history.
     */
    data object ClearConversation : ChatUiEvent

    /**
     * User wants to retry a failed message.
     *
     * @property messageId The ID of the failed message to retry
     */
    data class RetryMessage(val messageId: MessageId) : ChatUiEvent

    /**
     * User dismissed the current error message.
     */
    data object DismissError : ChatUiEvent

    /**
     * User navigated to the settings screen.
     */
    data object NavigateToSettings : ChatUiEvent

    /**
     * Screen was loaded/resumed.
     */
    data object ScreenLoaded : ChatUiEvent
}

/**
 * UI state representing the settings screen.
 *
 * This sealed interface provides type-safe state management for the settings UI.
 *
 * @since 1.0.0
 */
sealed interface SettingsUiState {
    /**
     * Initial state when the screen is first loaded.
     */
    data object Initial : SettingsUiState

    /**
     * Loading state while settings are being fetched.
     */
    data object Loading : SettingsUiState

    /**
     * Success state with current settings.
     *
     * @property configuration Current AI configuration
     */
    data class Success(
        val configuration: AiConfiguration
    ) : SettingsUiState {
        /**
         * Checks if the current configuration is valid.
         */
        fun isValidConfiguration(): Boolean = configuration.validate().isSuccess

        /**
         * Gets a user-friendly validation message if configuration is invalid.
         */
        fun getValidationMessage(): String? =
            configuration.validate().exceptionOrNull()?.message
    }

    /**
     * Error state when settings cannot be loaded or saved.
     *
     * @property message Human-readable error message
     * @property isRecoverable Whether the user can retry
     */
    data class Error(
        val message: String,
        val isRecoverable: Boolean = true
    ) : SettingsUiState
}

/**
 * UI events that can be triggered from the settings screen.
 *
 * @since 1.0.0
 */
sealed interface SettingsUiEvent {
    /**
     * User wants to save a new API key.
     *
     * **Deprecated**: API keys are not used with Firebase Functions proxy.
     * This event is kept for backward compatibility but is handled as a no-op.
     *
     * @property apiKey The API key to save (ignored)
     */
    data class SaveApiKey(val apiKey: String) : SettingsUiEvent

    /**
     * User wants to change the AI mode.
     *
     * @property mode The new AI mode to use
     */
    data class ChangeAiMode(val mode: com.novachat.feature.ai.domain.model.AiMode) : SettingsUiEvent

    /**
     * User wants to test the current configuration.
     */
    data object TestConfiguration : SettingsUiEvent

    /**
     * User dismissed the save success message.
     */
    data object DismissSaveSuccess : SettingsUiEvent

    /**
     * User wants to navigate back.
     */
    data object NavigateBack : SettingsUiEvent

    /**
     * Screen was loaded/resumed.
     */
    data object ScreenLoaded : SettingsUiEvent
}

/**
 * One-time UI effect that should be consumed only once.
 *
 * This sealed interface represents side effects that should trigger
 * one-time actions in the UI (like showing a toast or navigating).
 *
 * Following modern Android architecture, effects are separated from state
 * to avoid reprocessing them on configuration changes.
 *
 * @since 1.0.0
 */
sealed interface UiEffect {
    /**
     * Show a toast message to the user.
     *
     * @property message The message to display
     */
    data class ShowToast(val message: String) : UiEffect

    /**
     * Show a snackbar with an optional action.
     *
     * @property message The message to display
     * @property actionLabel Label for the action button (null for no action)
     * @property action The action to perform when the action button is clicked
     */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null
    ) : UiEffect

    /**
     * Navigate to a specific destination.
     *
     * @property destination The navigation destination
     */
    data class Navigate(val destination: NavigationDestination) : UiEffect

    /**
     * Navigate back to the previous screen.
     */
    data object NavigateBack : UiEffect

    /**
     * Request focus on a specific input field.
     *
     * @property fieldId Identifier for the field to focus
     */
    data class RequestFocus(val fieldId: String) : UiEffect

    /**
     * Hide the keyboard.
     */
    data object HideKeyboard : UiEffect

    /**
     * Show the keyboard.
     */
    data object ShowKeyboard : UiEffect
}

/**
 * Navigation destinations in the app.
 *
 * This sealed interface provides type-safe navigation routing.
 *
 * @since 1.0.0
 */
sealed interface NavigationDestination {
    /**
     * Navigate to the chat screen.
     */
    data object Chat : NavigationDestination

    /**
     * Navigate to the settings screen.
     */
    data object Settings : NavigationDestination

    /**
     * Route identifier for navigation framework.
     */
    val route: String
        get() = when (this) {
            Chat -> "chat"
            Settings -> "settings"
        }
}
