package com.novachat.app.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.novachat.app.presentation.model.ChatUiState
import com.novachat.app.ui.previews.createPreviewChatViewModel
import com.novachat.app.ui.previews.longMessageTestMessages
import com.novachat.app.ui.previews.longTestMessages
import com.novachat.app.ui.previews.mixedLengthMessages
import com.novachat.app.ui.previews.previewAiMessage
import com.novachat.app.ui.previews.previewUserMessage
import com.novachat.app.ui.previews.shortMessageTestMessages
import com.novachat.app.ui.previews.shortTestMessages
import com.novachat.app.ui.previews.testMessages
import com.novachat.app.ui.theme.NovaChatTheme

/**
 * Preview composables for the ChatScreen.
 *
 * This file contains comprehensive previews demonstrating:
 * - All UI states (Initial, Loading, Success, Error)
 * - Multiple device sizes
 * - Theme variations (Light, Dark)
 * - Accessibility testing (Font scaling, RTL)
 * - Localization support
 * - Component-level previews
 *
 * Organized hierarchically using preview groups for easy navigation in IDE.
 *
 * @see ChatScreen
 * @see com.novachat.app.ui.previews
 */

// ============================================================
// CHAT SCREEN - UI STATE PREVIEWS
// ============================================================
// Test all possible states of the chat screen
// These previews validate behavior at each state transition

@Preview(
    name = "Initial - Empty State",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenInitialPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Initial
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Loading - Spinner",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenLoadingPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Loading
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Success - Empty Conversation",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenSuccessEmptyPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = emptyList(),
                    isProcessing = false,
                    error = null
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Success - With Messages",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenSuccessWithMessagesPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false,
                    error = null
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Success - Processing (Loading Indicator)",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenSuccessProcessingPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = shortTestMessages,
                    isProcessing = true,
                    error = null
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Success - With Error Banner",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenSuccessWithErrorBannerPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false,
                    error = "Failed to send message. Tap to retry."
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Success - Long Conversation (Scroll Test)",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenSuccessLongConversationPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = longTestMessages,
                    isProcessing = false,
                    error = null
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Success - Long Messages (Text Wrapping)",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenSuccessLongMessagesPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = longMessageTestMessages,
                    isProcessing = false,
                    error = null
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Error - Recoverable",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenErrorRecoverablePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Error(
                    message = "Failed to load messages. Please check your connection.",
                    isRecoverable = true
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Error - Not Recoverable",
    group = "ChatScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ChatScreenErrorNotRecoverablePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Error(
                    message = "Critical error: Invalid configuration. Please reinstall the app.",
                    isRecoverable = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

// ============================================================
// CHAT SCREEN - DEVICE PREVIEWS
// ============================================================
// Test on different device sizes: phone, tablet, foldable
// Validates responsive layout behavior

@Preview(
    name = "Compact Phone (320dp)",
    device = "spec:width=320dp,height=640dp,dpi=420",
    group = "ChatScreen/Devices",
    showSystemUi = true
)
@Composable
fun ChatScreenCompactPhonePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = shortTestMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Standard Phone (412dp - Pixel 6)",
    device = Devices.PIXEL_6,
    group = "ChatScreen/Devices",
    showSystemUi = true
)
@Composable
fun ChatScreenStandardPhonePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Large Phone (480dp - Pixel 7 Pro)",
    device = Devices.PIXEL_7_PRO,
    group = "ChatScreen/Devices",
    showSystemUi = true
)
@Composable
fun ChatScreenLargePhonePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Tablet Portrait (600dp)",
    device = "spec:width=600dp,height=800dp,dpi=160",
    group = "ChatScreen/Devices",
    showSystemUi = true
)
@Composable
fun ChatScreenTabletPortraitPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Tablet Landscape (1000dp x 600dp)",
    device = "spec:width=1000dp,height=600dp,dpi=160",
    group = "ChatScreen/Devices",
    showSystemUi = true
)
@Composable
fun ChatScreenTabletLandscapePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Foldable",
    device = Devices.FOLDABLE,
    group = "ChatScreen/Devices",
    showSystemUi = true
)
@Composable
fun ChatScreenFoldablePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

// ============================================================
// CHAT SCREEN - THEME & COLOR MODE PREVIEWS
// ============================================================
// Test light and dark theme rendering
// Ensures colors and contrast meet accessibility standards

@Preview(
    name = "Light Mode - Default",
    group = "ChatScreen/Themes",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun ChatScreenLightThemePreview() {
    NovaChatTheme(darkTheme = false) {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Dark Mode - Default",
    group = "ChatScreen/Themes",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ChatScreenDarkThemePreview() {
    NovaChatTheme(darkTheme = true) {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Light Mode - Error State",
    group = "ChatScreen/Themes",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun ChatScreenLightThemeErrorPreview() {
    NovaChatTheme(darkTheme = false) {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Error(
                    message = "Connection failed",
                    isRecoverable = true
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Dark Mode - Error State",
    group = "ChatScreen/Themes",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ChatScreenDarkThemeErrorPreview() {
    NovaChatTheme(darkTheme = true) {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Error(
                    message = "Connection failed",
                    isRecoverable = true
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

// ============================================================
// CHAT SCREEN - ACCESSIBILITY PREVIEWS
// ============================================================
// Test font scaling for accessibility
// Ensures text remains readable and layouts don't break

@Preview(
    name = "Font Scale Normal (1x)",
    group = "ChatScreen/Accessibility",
    fontScale = 1f,
    showBackground = true
)
@Composable
fun ChatScreenNormalFontPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = testMessages,
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Font Scale Large (1.5x)",
    group = "ChatScreen/Accessibility",
    fontScale = 1.5f,
    showBackground = true
)
@Composable
fun ChatScreenLargeFontPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = shortTestMessages,  // Use shorter messages for larger font
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Font Scale Extra Large (2x)",
    group = "ChatScreen/Accessibility",
    fontScale = 2f,
    showBackground = true
)
@Composable
fun ChatScreenExtraLargeFontPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(
                    messages = shortMessageTestMessages,  // Very short messages for extra large font
                    isProcessing = false
                )
            ),
            onNavigateToSettings = {}
        )
    }
}

// ============================================================
// CHAT SCREEN - LOCALIZATION PREVIEWS
// ============================================================
// Test with different locales
// Validates right-to-left (RTL) support for Arabic, Hebrew, etc.

@Preview(
    name = "Locale: English (US)",
    group = "ChatScreen/Locales",
    locale = "en-US",
    showBackground = true
)
@Composable
fun ChatScreenEnglishUSPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(messages = testMessages)
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Locale: Spanish",
    group = "ChatScreen/Locales",
    locale = "es-ES",
    showBackground = true
)
@Composable
fun ChatScreenSpanishPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(messages = testMessages)
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Locale: Japanese",
    group = "ChatScreen/Locales",
    locale = "ja-JP",
    showBackground = true
)
@Composable
fun ChatScreenJapanesePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(messages = testMessages)
            ),
            onNavigateToSettings = {}
        )
    }
}

@Preview(
    name = "Locale: Arabic (RTL)",
    group = "ChatScreen/Locales",
    locale = "ar-SA",
    showBackground = true
)
@Composable
fun ChatScreenArabicPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(messages = testMessages)
            ),
            onNavigateToSettings = {}
        )
    }
}

// ============================================================
// MESSAGE BUBBLE COMPONENT PREVIEWS
// ============================================================
// Component-level previews for reusable UI elements

@Preview(
    name = "User Message Bubble",
    group = "Components/MessageBubble",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun MessageBubbleUserPreview() {
    NovaChatTheme {
        MessageBubble(message = previewUserMessage("Hello, how are you?"))
    }
}

@Preview(
    name = "AI Message Bubble",
    group = "Components/MessageBubble",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun MessageBubbleAiPreview() {
    NovaChatTheme {
        MessageBubble(message = previewAiMessage("I'm doing great! How can I help?"))
    }
}

@Preview(
    name = "Long User Message",
    group = "Components/MessageBubble",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    widthDp = 320
)
@Composable
fun MessageBubbleLongUserPreview() {
    NovaChatTheme {
        MessageBubble(
            message = previewUserMessage(
                "This is a long message that demonstrates how text wrapping works " +
                "in message bubbles when content exceeds the maximum width constraint. " +
                "The bubble expands vertically to accommodate all content."
            )
        )
    }
}

@Preview(
    name = "User Message - Dark Mode",
    group = "Components/MessageBubble",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MessageBubbleUserDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        MessageBubble(message = previewUserMessage("Hello in dark mode!"))
    }
}

@Preview(
    name = "AI Message - Dark Mode",
    group = "Components/MessageBubble",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MessageBubbleAiDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        MessageBubble(message = previewAiMessage("Response in dark mode!"))
    }
}

// ============================================================
// MESSAGE INPUT BAR COMPONENT PREVIEWS
// ============================================================

@Preview(
    name = "Empty Input",
    group = "Components/MessageInputBar",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun MessageInputBarEmptyPreview() {
    NovaChatTheme {
        MessageInputBar(
            messageText = "",
            onMessageTextChange = {},
            onSendMessage = {},
            isLoading = false
        )
    }
}

@Preview(
    name = "With Text",
    group = "Components/MessageInputBar",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun MessageInputBarFilledPreview() {
    NovaChatTheme {
        MessageInputBar(
            messageText = "This is my message to the AI...",
            onMessageTextChange = {},
            onSendMessage = {},
            isLoading = false
        )
    }
}

@Preview(
    name = "Multi-line Text",
    group = "Components/MessageInputBar",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun MessageInputBarMultilinePreview() {
    NovaChatTheme {
        MessageInputBar(
            messageText = "Line 1\nLine 2\nLine 3\nLine 4",
            onMessageTextChange = {},
            onSendMessage = {},
            isLoading = false
        )
    }
}

@Preview(
    name = "Loading State",
    group = "Components/MessageInputBar",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun MessageInputBarLoadingPreview() {
    NovaChatTheme {
        MessageInputBar(
            messageText = "Sending...",
            onMessageTextChange = {},
            onSendMessage = {},
            isLoading = true
        )
    }
}

@Preview(
    name = "Dark Mode",
    group = "Components/MessageInputBar",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MessageInputBarDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        MessageInputBar(
            messageText = "Dark mode input",
            onMessageTextChange = {},
            onSendMessage = {},
            isLoading = false
        )
    }
}

// ============================================================
// ERROR BANNER COMPONENT PREVIEWS
// ============================================================

@Preview(
    name = "Error Banner - Light",
    group = "Components/ErrorBanner",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ErrorBannerLightPreview() {
    NovaChatTheme(darkTheme = false) {
        ErrorBanner(
            message = "Failed to send message. Tap to retry.",
            onDismiss = {}
        )
    }
}

@Preview(
    name = "Error Banner - Dark",
    group = "Components/ErrorBanner",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ErrorBannerDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        ErrorBanner(
            message = "Failed to send message. Tap to retry.",
            onDismiss = {}
        )
    }
}

@Preview(
    name = "Error Banner - Long Message",
    group = "Components/ErrorBanner",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun ErrorBannerLongMessagePreview() {
    NovaChatTheme {
        ErrorBanner(
            message = "Failed to send message because the network connection is unavailable.",
            onDismiss = {}
        )
    }
}

// ============================================================
// EMPTY STATE COMPONENT PREVIEWS
// ============================================================

@Preview(
    name = "Empty State - Light",
    group = "Components/EmptyState",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun EmptyStateLightPreview() {
    NovaChatTheme(darkTheme = false) {
        EmptyState()
    }
}

@Preview(
    name = "Empty State - Dark",
    group = "Components/EmptyState",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EmptyStateDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        EmptyState()
    }
}
