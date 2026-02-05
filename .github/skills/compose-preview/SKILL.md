# 🎬 Jetpack Compose Preview Skill

Reusable patterns and best practices for creating comprehensive @Preview annotations and preview Composables in Jetpack Compose.

---

## Table of Contents

1. [Core Preview Patterns](#core-preview-patterns)
2. [Device Specifications](#device-specifications)
3. [Mock ViewModels](#mock-viewmodels)
4. [Theme Previews](#theme-previews)
5. [State Composition](#state-composition)
6. [Performance Optimization](#performance-optimization)
7. [Common Patterns by Component](#common-patterns-by-component)
8. [Advanced Techniques](#advanced-techniques)

---

## Core Preview Patterns

### Basic Preview Structure

```kotlin
// Always wrap Composables in theme
@Preview(name = "Chat Screen")
@Composable
fun ChatScreenBasicPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.empty(),
                onNavigateToSettings = {}
            )
        }
    }
}
```

### Multi-Preview Organization

```kotlin
// Group related previews with clear separation

// === EMPTY STATE ===
@Preview(name = "Empty - Light")
@Composable
fun ChatScreenEmptyLightPreview() {
    NovaChatTheme(darkTheme = false) {
        Surface {
            ChatScreen(PreviewChatViewModel.empty(), {})
        }
    }
}

@Preview(name = "Empty - Dark")
@Composable
fun ChatScreenEmptyDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        Surface {
            ChatScreen(PreviewChatViewModel.empty(), {})
        }
    }
}

// === SUCCESS STATE ===
@Preview(name = "With Messages - Light")
@Composable
fun ChatScreenSuccessLightPreview() {
    NovaChatTheme(darkTheme = false) {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}

// === ERROR STATE ===
@Preview(name = "Error - Network")
@Composable
fun ChatScreenErrorNetworkPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                PreviewChatViewModel.withError("Network connection failed"),
                {}
            )
        }
    }
}
```

---

## Device Specifications

### Standard Device Constants

```kotlin
// SharedPreviewComponents.kt
object PreviewDevices {
    // Phone Devices
    const val DEVICE_PHONE_SMALL = "spec:width=360dp,height=740dp,dpi=420"      // Smaller phones
    const val DEVICE_PHONE = "spec:width=411dp,height=891dp,dpi=420"             // Standard phone
    const val DEVICE_PHONE_LARGE = "spec:width=480dp,height=854dp,dpi=420"       // Larger phones
    
    // Folding Devices
    const val DEVICE_FOLDABLE = "spec:width=412dp,height=915dp,dpi=420"          // Folded phone
    const val DEVICE_TABLET = "spec:width=1280dp,height=800dp,dpi=240"           // Tablet
    
    // Landscape
    const val DEVICE_PHONE_LANDSCAPE = "spec:width=854dp,height=480dp,dpi=420"   // Phone landscape
    const val DEVICE_TABLET_LANDSCAPE = "spec:width=1280dp,height=800dp,dpi=240" // Tablet landscape
}

// Using constants in previews
@Preview(device = PreviewDevices.DEVICE_PHONE)
@Composable
fun ChatScreenPhonePreview() { ... }
```

### @PreviewScreenSizes Annotation

```kotlin
// Automatically shows preview in multiple standard sizes
@PreviewScreenSizes
@Composable
fun ChatScreenMultiSizePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}
```

### Custom Device Configurations

```kotlin
// Wallmart tall phone (19:9 ratio)
@Preview(
    name = "Tall Phone (19:9)",
    device = "spec:width=360dp,height=800dp,dpi=420"
)
@Composable
fun ChatScreenTallPhonePreview() { ... }

// iPad-like tablet (4:3 ratio)
@Preview(
    name = "Large Tablet (4:3)",
    device = "spec:width=1024dp,height=768dp,dpi=160"
)
@Composable
fun ChatScreenTabletPreview() { ... }
```

---

## Mock ViewModels

### Builder Pattern for Preview ViewModels

```kotlin
// PreviewChatViewModel.kt
class PreviewChatViewModel(
    val state: ChatUiState = ChatUiState.Initial,
    val isDraftPersisted: String = ""
) : ChatViewModel(
    savedStateHandle = SavedStateHandle(),
    sendMessageUseCase = mockk(),
    observeMessagesUseCase = mockk(),
    clearConversationUseCase = mockk(),
    retryMessageUseCase = mockk()
) {
    override val uiState = MutableStateFlow(state).asStateFlow()
    override val uiEffect = emptyFlow<UiEffect>()
    
    companion object {
        // Factory methods for common preview states
        fun empty(): PreviewChatViewModel =
            PreviewChatViewModel(ChatUiState.Initial)
        
        fun loading(): PreviewChatViewModel =
            PreviewChatViewModel(
                ChatUiState.Success(
                    messages = emptyList(),
                    isProcessing = true,
                    error = null
                )
            )
        
        fun withMessages(): PreviewChatViewModel =
            PreviewChatViewModel(
                ChatUiState.Success(
                    messages = SAMPLE_MESSAGES,
                    isProcessing = false,
                    error = null
                )
            )
        
        fun withError(message: String): PreviewChatViewModel =
            PreviewChatViewModel(
                ChatUiState.Success(
                    messages = emptyList(),
                    isProcessing = false,
                    error = message
                )
            )
        
        fun withLongConversation(): PreviewChatViewModel =
            PreviewChatViewModel(
                ChatUiState.Success(
                    messages = (1..20).map { index ->
                        if (index % 2 == 0) {
                            SAMPLE_ASSISTANT_MESSAGE.copy(
                                id = MessageId("ai-$index"),
                                content = "Response $index from AI assistant"
                            )
                        } else {
                            SAMPLE_USER_MESSAGE.copy(
                                id = MessageId("user-$index"),
                                content = "User message $index"
                            )
                        }
                    },
                    isProcessing = false,
                    error = null
                )
            )
        
        fun withDraft(draft: String): PreviewChatViewModel =
            PreviewChatViewModel(
                state = ChatUiState.Success(),
                isDraftPersisted = draft
            )
    }
}

// Sample data for previews
private val SAMPLE_USER_MESSAGE = Message(
    id = MessageId("user-1"),
    content = "Hello, how are you today?",
    sender = MessageSender.USER,
    timestamp = System.currentTimeMillis()
)

private val SAMPLE_ASSISTANT_MESSAGE = Message(
    id = MessageId("ai-1"),
    content = "I'm doing great! How can I help you today?",
    sender = MessageSender.ASSISTANT,
    timestamp = System.currentTimeMillis() + 1000
)

private val SAMPLE_MESSAGES = listOf(
    SAMPLE_USER_MESSAGE,
    SAMPLE_ASSISTANT_MESSAGE,
    SAMPLE_USER_MESSAGE.copy(
        id = MessageId("user-2"),
        content = "Can you help me with Kotlin?"
    ),
    SAMPLE_ASSISTANT_MESSAGE.copy(
        id = MessageId("ai-2"),
        content = "Of course! Kotlin is a great language. What would you like to know?"
    )
)
```

### Mocking Dependencies

```kotlin
// When Preview needs complete ViewModel mock
class PreviewSettingsViewModel : SettingsViewModel(
    savedStateHandle = SavedStateHandle(),
    getAiConfigurationUseCase = mockk(),
    updateAiConfigurationUseCase = mockk(),
    validateApiKeyUseCase = mockk()
) {
    override val uiState = MutableStateFlow(
        SettingsUiState.Success(
            mode = AiMode.ONLINE,
            apiKey = "preview-api-key",
            isSaving = false,
            validationError = null
        )
    ).asStateFlow()
}
```

---

## Theme Previews

### Light & Dark Theme Composition

```kotlin
// Use @PreviewLightDark for automatic light/dark variants
@PreviewLightDark
@Composable
fun ChatScreenThemePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}

// Manual light/dark explicitness (when you need more control)
@Preview(name = "Light Theme")
@Composable
fun ChatScreenLightPreview() {
    NovaChatTheme(darkTheme = false) {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}

@Preview(name = "Dark Theme")
@Composable
fun ChatScreenDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}
```

### High Contrast Theme Preview

```kotlin
// For accessibility testing
@Preview(
    name = "High Contrast Dark",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
@Composable
fun ChatScreenHighContrastPreview() {
    NovaChatTheme(darkTheme = true) {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}
```

---

## State Composition

### All States Pattern

```kotlin
// Comprehensive preview file covering all UI states
// ChatScreenPreview.kt

package com.novachat.app.ui.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.novachat.app.ui.ChatScreen
import com.novachat.app.ui.theme.NovaChatTheme
import com.novachat.app.presentation.model.ChatUiState

// INITIAL STATE
@Preview(name = "Initial State")
@Composable
fun ChatScreenInitialPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.empty(),
                onNavigateToSettings = {}
            )
        }
    }
}

// LOADING STATE
@Preview(name = "Loading - Waiting for AI")
@Composable
fun ChatScreenLoadingPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.loading(),
                onNavigateToSettings = {}
            )
        }
    }
}

// SUCCESS STATE - Single Message
@Preview(name = "Success - Single Exchange")
@Composable
fun ChatScreenSingleMessagePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withMessages(),
                onNavigateToSettings = {}
            )
        }
    }
}

// SUCCESS STATE - Long Conversation
@Preview(name = "Success - Long Conversation")
@Composable
fun ChatScreenLongConversationPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withLongConversation(),
                onNavigateToSettings = {}
            )
        }
    }
}

// ERROR STATE
@Preview(name = "Error - Network Failed")
@Composable
fun ChatScreenErrorNetworkPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withError(
                    "Network connection failed. Check your internet."
                ),
                onNavigateToSettings = {}
            )
        }
    }
}

@Preview(name = "Error - API Error")
@Composable
fun ChatScreenErrorApiPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withError(
                    "API error: Invalid API key"
                ),
                onNavigateToSettings = {}
            )
        }
    }
}

// LIGHT & DARK THEME
@PreviewLightDark
@Composable
fun ChatScreenThemePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withMessages(),
                onNavigateToSettings = {}
            )
        }
    }
}

// MULTIPLE DEVICES
@Preview(name = "Small Phone", device = PreviewDevices.DEVICE_PHONE_SMALL)
@Composable
fun ChatScreenSmallPhonePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}

@Preview(name = "Large Phone", device = PreviewDevices.DEVICE_PHONE_LARGE)
@Composable
fun ChatScreenLargePhonePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}

@Preview(name = "Tablet", device = PreviewDevices.DEVICE_TABLET)
@Composable
fun ChatScreenTabletPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}

// LANDSCAPE
@Preview(
    name = "Landscape Phone",
    device = PreviewDevices.DEVICE_PHONE_LANDSCAPE
)
@Composable
fun ChatScreenLandscapePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}
```

---

## Performance Optimization

### Lightweight Preview Theme

```kotlin
// For faster preview compilation, sometimes needed for complex screens

@Preview
@Composable
fun ChatScreenFastPreview() {
    // Use minimal theming for fast IDE preview
    NovaChatTheme(
        useDynamicColor = false  // Disable dynamic color in previews
    ) {
        Surface {
            ChatScreen(PreviewChatViewModel.withMessages(), {})
        }
    }
}
```

### Lazy-Loading Preview Data

```kotlin
// For heavy preview data, use lazy evaluation
class PreviewChatViewModel : ChatViewModel(...) {
    companion object {
        // Compute sample messages only when needed
        fun withHeavyData(): PreviewChatViewModel {
            val messages = generateSampleMessages(1000)  // Computed on demand
            return PreviewChatViewModel(
                ChatUiState.Success(messages = messages)
            )
        }
        
        private fun generateSampleMessages(count: Int): List<Message> {
            return (1..count).map { ... }
        }
    }
}

// Only use this preview when specifically testing scroll behavior
@Preview(name = "Heavy Data - Scroll Performance", showSystemUi = true)
@Composable
fun ChatScreenScrollPerformancePreview() {
    NovaChatTheme {
        ChatScreen(PreviewChatViewModel.withHeavyData(), {})
    }
}
```

### Preview Organization by File Count

```kotlin
// Best Practice: 8-12 previews per file
// If exceeding this, split into multiple files:

// ChatScreenPreview.kt (state variants)
@Preview  fun ChatScreenInitialPreview() { ... }
@Preview  fun ChatScreenLoadingPreview() { ... }
@Preview  fun ChatScreenSuccessPreview() { ... }
@Preview  fun ChatScreenErrorPreview() { ... }
@Preview  fun ChatScreenThemePreview() { ... }

// ChatScreenDevicePreview.kt (device variants)
@Preview(device = DEVICE_PHONE_SMALL)        fun ChatScreenSmallPreview() { ... }
@Preview(device = DEVICE_PHONE)              fun ChatScreenPhonePreview() { ... }
@Preview(device = DEVICE_TABLET)             fun ChatScreenTabletPreview() { ... }
@Preview(device = DEVICE_TABLET_LANDSCAPE)   fun ChatScreenTabletLandscapePreview() { ... }
```

---

## Common Patterns by Component

### Chat/Message Screen

```kotlin
object ChatScreenPreviewPatterns {
    // Pattern 1: Empty conversation
    @Preview(name = "Empty State")
    @Composable
    fun Empty() {
        ChatScreen(PreviewChatViewModel.empty(), {})
    }
    
    // Pattern 2: Single message awaiting response
    @Preview(name = "User Message Awaiting AI")
    @Composable
    fun UserMessagePending() {
        ChatScreen(
            PreviewChatViewModel(
                ChatUiState.Success(
                    messages = listOf(
                        SAMPLE_USER_MESSAGE
                    ),
                    isProcessing = true
                )
            ),
            {}
        )
    }
    
    // Pattern 3: Exchange showing both sides
    @Preview(name = "Message Exchange")
    @Composable
    fun Exchange() {
        ChatScreen(PreviewChatViewModel.withMessages(), {})
    }
    
    // Pattern 4: Long conversation for scroll testing
    @Preview(name = "Long Conversation")
    @Composable
    fun LongConversation() {
        ChatScreen(PreviewChatViewModel.withLongConversation(), {})
    }
    
    // Pattern 5: Error with retry button visible
    @Preview(name = "Error with Retry")
    @Composable
    fun ErrorWithRetry() {
        ChatScreen(
            PreviewChatViewModel.withError("Network error. Tap to retry."),
            {}
        )
    }
}
```

### Settings/Configuration Screen

```kotlin
object SettingsScreenPreviewPatterns {
    // Pattern 1: Default settings (ONLINE mode)
    @Preview(name = "Default - ONLINE Mode")
    @Composable
    fun DefaultOnlineMode() {
        SettingsScreen(
            PreviewSettingsViewModel(
                AiConfiguration(
                    mode = AiMode.ONLINE,
                    apiKey = "****key",
                    temperature = 0.7f
                )
            ),
            {}
        )
    }
    
    // Pattern 2: Form with errors
    @Preview(name = "Form Validation Error")
    @Composable
    fun FormWithError() {
        SettingsScreen(
            PreviewSettingsViewModel(
                AiConfiguration(mode = AiMode.ONLINE, apiKey = ""),
                validationError = "API key cannot be empty"
            ),
            {}
        )
    }
    
    // Pattern 3: Saving in progress
    @Preview(name = "Saving Configuration")
    @Composable
    fun SavingProgress() {
        SettingsScreen(
            PreviewSettingsViewModel(isSaving = true),
            {}
        )
    }
    
    // Pattern 4: Success confirmation
    @Preview(name = "Changes Saved")
    @Composable
    fun SavedSuccess() {
        SettingsScreen(
            PreviewSettingsViewModel(showSaveSuccess = true),
            {}
        )
    }
}
```

### Modal/Dialog

```kotlin
object DialogPreviewPatterns {
    // Pattern 1: Default appearance
    @Preview(name = "Default Dialog")
    @Composable
    fun Default() {
        NovaChatTheme {
            ClearConversationDialog(
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
    
    // Pattern 2: Long content
    @Preview(name = "Dialog - Long Content")
    @Composable
    fun WithLongContent() {
        NovaChatTheme {
            ErrorDialog(
                title = "Error occurred",
                message = "A very long error message explaining in detail what went wrong " +
                    "and what the user can do to recover from this error state.",
                onDismiss = {}
            )
        }
    }
    
    // Pattern 3: Dark theme
    @Preview(name = "Dialog - Dark Theme")
    @Composable
    fun DarkTheme() {
        NovaChatTheme(darkTheme = true) {
            ConfirmDialog(onConfirm = {}, onCancel = {})
        }
    }
    
    // Pattern 4: Landscape orientation
    @Preview(
        name = "Dialog - Landscape",
        device = PreviewDevices.DEVICE_PHONE_LANDSCAPE
    )
    @Composable
    fun Landscape() {
        NovaChatTheme {
            ConfirmDialog(onConfirm = {}, onCancel = {})
        }
    }
}
```

---

## Advanced Techniques

### Interactive Preview (Preview with State)

```kotlin
// For interactive previews that respond to clicks
@Preview
@Composable
fun ChatScreenInteractivePreview() {
    var isLoading by remember { mutableStateOf(false) }
    
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = if (isLoading) {
                    PreviewChatViewModel.loading()
                } else {
                    PreviewChatViewModel.withMessages()
                },
                onNavigateToSettings = {}
            )
        }
        
        // Button in preview to toggle state
        if (!isLoading) {
            FloatingActionButton(onClick = { isLoading = true }) {
                Text("Start Loading")
            }
        }
    }
}
```

### Parameterized Previews

```kotlin
// Create flexible preview for testing variations
@Preview(name = "Messages with {count}")
@Composable
private fun ChatScreenWithMessageCount(
    @IntRange(from = 1, to = 50) messageCount: Int = 5
) {
    val messages = (1..messageCount).map { index ->
        if (index % 2 == 0) {
            SAMPLE_ASSISTANT_MESSAGE.copy(id = MessageId("ai-$index"))
        } else {
            SAMPLE_USER_MESSAGE.copy(id = MessageId("user-$index"))
        }
    }
    
    ChatScreen(
        PreviewChatViewModel(
            ChatUiState.Success(messages = messages)
        ),
        {}
    )
}
```

### Type-Safe Preview Resources

```kotlin
// Avoid magic strings in previews
object PreviewRes {
    const val SAMPLE_API_ERROR = "API error: Invalid request (401 Unauthorized)"
    const val SAMPLE_NETWORK_ERROR = "Network connection failed. Check your internet."
    const val SAMPLE_UNKNOWN_ERROR = "An unknown error occurred. Please try again."
    
    val SAMPLE_CONVERSATION = listOf(
        Message(
            id = MessageId("1"),
            content = "What is Kotlin?",
            sender = MessageSender.USER,
            timestamp = System.currentTimeMillis()
        ),
        Message(
            id = MessageId("2"),
            content = "Kotlin is a statically typed programming language that runs on the JVM...",
            sender = MessageSender.ASSISTANT,
            timestamp = System.currentTimeMillis() + 1000
        )
    )
}

@Preview
@Composable
fun ChatScreenWithTypeSafeData() {
    ChatScreen(
        PreviewChatViewModel.withError(PreviewRes.SAMPLE_NETWORK_ERROR),
        {}
    )
}
```

---

## Preview Checklist

Before committing preview code:

- [ ] All preview functions are complete (no placeholders)
- [ ] All imports are explicit
- [ ] Theme wrapping is consistent (`NovaChatTheme { }`)
- [ ] Mock ViewModels use builder pattern
- [ ] No production repository calls in previews
- [ ] All major UI states are covered
- [ ] Multiple device sizes are shown
- [ ] Light and dark themes are tested
- [ ] Preview file naming follows convention
- [ ] No side effects (LaunchedEffect, etc.) in previews
- [ ] Build time is acceptable

---

## References

- [Jetpack Compose Preview Docs](https://developer.android.com/develop/ui/compose/tooling/previews)
- [Preview Devices](https://developer.android.com/develop/ui/compose/tooling/previews#preview_devices)
- [Material Design 3 Compose](https://m3.material.io)
- [Android Studio Preview Support](https://developer.android.com/studio/preview)
