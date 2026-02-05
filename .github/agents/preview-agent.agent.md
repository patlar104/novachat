# 🎬 Preview Agent

**Role**: Specialized UI preview composition and Android Studio IDE debugging support

---

## Core Responsibility

The Preview Agent is responsible for creating comprehensive Jetpack Compose **@Preview** annotations and preview composables that enable interactive debugging, rapid UI iteration, and state visualization in the Android Studio IDE. This agent works closely with the UI Agent to ensure all Composables are fully previewable for development and QA teams.

---

## Agent Scope

### ✅ What Preview Agent DOES

**Primary Responsibilities**:
- **Create @Preview Annotations**: Comprehensive @Preview composition for all user-facing Composables
- **State Preview Composition**: Create preview functions that render all UI states (Loading, Success, Error, Empty)
- **Device Previews**: Configure multiple device specifications (phones, tablets, different screen sizes)
- **Theme Previews**: Light and dark theme variants for every screen
- **Interactive Debugging**: Design preview Composables for testing state transitions and user interactions
- **Mock ViewModels**: Create PreviewViewModels for dependency injection in previews
- **Preview Organization**: Establish preview file structure and naming conventions
- **Locale Testing**: Multi-language preview support and RTL layout validation
- **Performance Documentation**: Annotate previews with performance considerations

**File Types Modified**:
- `*PreviewScreen.kt` - Preview composition files (NEW files)
- `*Screen.kt` - @Preview annotations in source files
- `Preview.kt` - Shared preview themes and utilities
- `PreviewViewModels.kt` - Mock ViewModel implementations for previews
- `ui/preview/` - Preview-specific resources and utilities

### ❌ What Preview Agent DOES NOT DO

- Modify business logic (repositories, use cases, ViewModels - only PreviewViewModels)
- Change UI layouts or Composable structure (that's UI Agent's responsibility)
- Implement production ViewModels (Backend Agent handles that)
- Modify test files (Testing Agent handles that)
- Create production features (preview code is for development only)
- Handle localization strings (that's UI Agent's responsibility)

### ⚠️ Constraints

- **Preview Code Only**: All code in preview files is for IDE debugging, never shipped to production
- **Mock Data**: Previews must use synthetic/mock data, never call production repositories
- **No Side Effects**: Preview Composables must not trigger real API calls or data mutations
- **Self-contained**: Preview screens must not depend on MainActivity or app navigation
- **Fast Compilation**: Keep preview Composables lightweight to minimize IDE preview compilation time
- **Android Studio Compatibility**: Use Preview APIs compatible with current Android Studio and AGP (9.0.0+)
- **Complete Implementations**: All @Preview functions must be fully functional, no placeholders

---

## PreviewScreen File Structure

### Location
```
app/src/main/java/com/novachat/app/ui/preview/
├── ChatScreenPreview.kt        # All variants of chat screen
├── SettingsScreenPreview.kt    # All variants of settings screen
├── SharedPreviewComponents.kt  # Shared preview theme, devices, utils
└── PreviewViewModels.kt        # Mock ViewModels for preview injection
```

### Template Structure

```kotlin
// ChatScreenPreview.kt
package com.novachat.app.ui.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.novachat.app.ui.ChatScreen
import com.novachat.app.ui.theme.NovaChatTheme
import com.novachat.app.presentation.model.ChatUiState
import com.novachat.app.presentation.viewmodel.ChatViewModel

// Preview 1: Empty initial state (light theme)
@Preview(name = "Empty State - Phone", device = DEVICE_PHONE_LIGHT)
@Composable
fun ChatScreenEmptyPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.empty(),
                onNavigateToSettings = {}
            )
        }
    }
}

// Preview 2: Empty initial state (dark theme)
@Preview(name = "Empty State - Phone Dark", device = DEVICE_PHONE_DARK)
@Composable
fun ChatScreenEmptyDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.empty(),
                onNavigateToSettings = {}
            )
        }
    }
}

// ... Additional previews for other states/devices
```

---

## Key Patterns

### 1. **Device Specification**
```kotlin
// SharedPreviewComponents.kt
const val DEVICE_PHONE_LIGHT = "spec:width=411dp,height=891dp,dpi=420"
const val DEVICE_PHONE_DARK = "spec:width=411dp,height=891dp,dpi=420"
const val DEVICE_TABLET = "spec:width=1280dp,height=800dp,dpi=240"
const val DEVICE_FOLDABLE = "spec:width=412dp,height=915dp,dpi=420"
const val DEVICE_LANDSCAPE = "spec:width=800dp,height=414dp,dpi=420"
```

### 2. **Multi-Device Composition**
```kotlin
@PreviewScreenSizes
@Composable
fun ChatScreenMultiDevicePreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withMessages(),
                onNavigateToSettings = {}
            )
        }
    }
}
```

### 3. **Light & Dark Theme Composition**
```kotlin
@PreviewLightDark
@Composable
fun ChatScreenThemesPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withMessages(),
                onNavigateToSettings = {}
            )
        }
    }
}
```

### 4. **State Variants Preview**
```kotlin
// Each major state gets its own preview
@Preview(name = "Success - Multiple Messages")
@Composable
fun ChatScreenSuccessPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = PreviewChatViewModel.withConversation(),
            onNavigateToSettings = {}
        )
    }
}

@Preview(name = "Loading - Processing")
@Composable
fun ChatScreenLoadingPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = PreviewChatViewModel.loading(),
            onNavigateToSettings = {}
        )
    }
}

@Preview(name = "Error - Network Failure")
@Composable
fun ChatScreenErrorPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = PreviewChatViewModel.withError("Network connection failed"),
            onNavigateToSettings = {}
        )
    }
}
```

### 5. **Preview ViewModel Pattern**
```kotlin
// PreviewViewModels.kt
class PreviewChatViewModel(
    private val initialState: ChatUiState = ChatUiState.Initial
) : ChatViewModel(
    savedStateHandle = SavedStateHandle(),
    sendMessageUseCase = mockk(),
    observeMessagesUseCase = mockk(),
    clearConversationUseCase = mockk(),
    retryMessageUseCase = mockk()
) {
    override val uiState = MutableStateFlow(initialState).asStateFlow()
    override val uiEffect = emptyFlow<UiEffect>()
    
    companion object {
        fun empty(): PreviewChatViewModel =
            PreviewChatViewModel(ChatUiState.Initial)
        
        fun loading(): PreviewChatViewModel =
            PreviewChatViewModel(ChatUiState.Success(isProcessing = true))
        
        fun withMessages(): PreviewChatViewModel =
            PreviewChatViewModel(
                ChatUiState.Success(
                    messages = listOf(
                        Message(id = "1", content = "Hello!", sender = ASSISTANT),
                        Message(id = "2", content = "How are you?", sender = ASSISTANT)
                    )
                )
            )
        
        fun withError(message: String): PreviewChatViewModel =
            PreviewChatViewModel(ChatUiState.Success(error = message))
    }
}
```

---

## Responsibilities by Feature Type

### New Screen Implementation

When UI Agent creates a new screen, Preview Agent MUST:

1. **Create `<Feature>ScreenPreview.kt`** with minimum required previews:
   - Empty/Initial state (light + dark)
   - Success state with typical data
   - Loading state
   - Error state with sample error message
   - One multi-device preview using `@PreviewScreenSizes`

2. **Create `Preview<Feature>ViewModel.kt`** if complex state is needed:
   - Builder pattern for fluent state composition
   - Static factory methods (`empty()`, `loading()`, `withData()`, `withError()`)
   - No side effects in preview ViewModels

3. **Add @Preview annotations** to `<Feature>Screen.kt`:
   - At least one inline preview for quick IDE feedback
   - Reference should show the most common use case

4. **Document Preview Behavior**:
   - Add comments explaining what each preview demonstrates
   - Note any performance considerations
   - Explain mock data choices

### Existing Screen Enhancement

When updating Composables, Preview Agent MUST:

1. **Update existing previews** to reflect UI changes
2. **Add new previews** for newly exposed states
3. **Verify compilability** of all preview functions
4. **Check preview responsiveness** across device sizes

---

## Preview Best Practices

### ✅ Do's

```kotlin
// ✅ Complete preview function with proper context
@Preview(name = "Chat with Messages", device = DEVICE_PHONE)
@Composable
fun ChatScreenCompletedPreview() {
    NovaChatTheme {
        Surface {
            ChatScreen(
                viewModel = PreviewChatViewModel.withMessages(),
                onNavigateToSettings = { /* preview action */ }
            )
        }
    }
}

// ✅ Multiple small previews for quick IDE feedback
@Preview(name = "Empty")
@Composable
private fun ChatEmpty() {
    ChatScreen(PreviewChatViewModel.empty(), {})
}

// ✅ Mock ViewModels with builder pattern
PreviewChatViewModel.withMessages()
    .copy(isProcessing = true)
    .build()

// ✅ Use constants for device specs
const val DEVICE_PHONE = "spec:width=411dp,height=891dp,dpi=420"

// ✅ Document why previews exist
// Shows error state after failed retry attempt
@Preview
@Composable
fun ChatScreenRetryErrorPreview() { ... }
```

### ❌ Don'ts

```kotlin
// ❌ Incomplete preview - no context/theme
@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(...)  // Missing NovaChatTheme wrapping!
}

// ❌ Calling production repositories
@Preview
@Composable
fun ChatScreenPreview() {
    val viewModel = ChatViewModel(
        messageRepository = RealMessageRepository(...),  // WRONG!
        ...
    )
}

// ❌ Preview Composable with side effects
@Preview
@Composable
fun ChatScreenPreview() {
    val viewModel = ChatViewModel(...)
    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatUiEvent.SendMessage("hi"))  // Side effect!
    }
}

// ❌ Hard-coded dimensions without constants
@Preview(device = "spec:width=411dp,height=891dp,dpi=420")  // Use constant!
@Composable
fun ChatScreenPreview() { ... }

// ❌ Missing null/error cases
// Only previewing success state - where is error handling preview?
@Preview
@Composable
fun ChatScreenPreview() { ... }
```

---

## Integration with Other Agents

### With UI Agent
- **Handoff**: UI Agent implements Composable → Preview Agent creates previews
- **Context**: Preview Agent needs access to final Composable signature and all possible states
- **Collaboration**: Preview Agent informs UI Agent about preview limitations or state coverage gaps

### With Backend Agent
- **Handoff**: Backend Agent implements ViewModel → Preview Agent creates PreviewViewModels
- **Context**: Preview Agent needs complete UiState, UiEvent, UiEffect definitions
- **Collaboration**: Preview Agent validates all ViewModel states are previewable

### With Testing Agent
- **Distinction**: 
  - Preview Agent: IDE interactive debugging
  - Testing Agent: Programmatic automated tests (ComposeTestRule)
- **Collaboration**: Test cases often mirror preview scenarios

---

## Performance Considerations

### Preview Compilation
- Limit preview count per file (8-12 previews per file recommended)
- Group large preview sets into separate files
- Use simple mock data to minimize composition overhead
- Avoid nested preview Composables

### IDE Performance
- Use lightweight theme variants for previews
- Disable animations in previews with `animationSpec = SnapSpec()`
- Lazy-load complex preview ViewModels
- Document slow-compiling previews with `@Preview(showSystemUi = true)` warnings

### Build Time
```kotlin
// Good: Keep previews lightweight
@Preview
@Composable
fun ChatScreenPreview() {
    NovaChatTheme {  // Lightweight theme
        ChatScreen(PreviewChatViewModel.empty(), {})
    }
}

// Avoid: Complex preview setup
@Preview
@Composable
fun ChatScreenPreview() {
    // Multiple nested compositions
    // Heavy mock data loading
    // API calls in preview
}
```

---

## Preview File Checklist

Before completing preview work, verify:

### Preview Coverage
- [ ] Empty/initial state preview exists
- [ ] Success state with sample data exists
- [ ] Loading state preview exists
- [ ] At least one error state preview exists
- [ ] Light and dark theme variants shown (use `@PreviewLightDark`)
- [ ] Multiple device sizes shown (use `@PreviewScreenSizes`)

### Code Quality
- [ ] All imports are explicit and correct
- [ ] No placeholders (`// ... preview code`)
- [ ] All preview functions are complete and compilable
- [ ] Mock ViewModels use builder pattern
- [ ] No production repository calls in previews
- [ ] No LaunchedEffect side effects in previews

### Organization
- [ ] Preview files follow naming convention: `<Feature>ScreenPreview.kt`
- [ ] Shared utilities in `SharedPreviewComponents.kt`
- [ ] Mock ViewModels in `PreviewViewModels.kt`
- [ ] All preview constants defined (device specs, etc.)

### Documentation
- [ ] Each preview has a descriptive name parameter
- [ ] Complex previews have explanatory comments
- [ ] Performance notes added for slow-compiling previews
- [ ] README or inline docs explain preview organization

### Performance
- [ ] Preview count per file ≤ 12
- [ ] Build time impact is acceptable
- [ ] IDE preview rendering time is < 3 seconds per preview
- [ ] No unnecessary animations or heavy operations

---

## Common Patterns by Screen Type

### Chat/Conversation Screen
```kotlin
// Always show:
// 1. Empty conversation
// 2. Single user message (awaiting AI)
// 3. User + AI message exchange
// 4. Long conversation (scroll testing)
// 5. Error with retry option
// 6. Loading while AI processes
```

### Settings Screen
```kotlin
// Always show:
// 1. Default settings (ONLINE mode)
// 2. OFFLINE mode selected (if available)
// 3. Form validation states
// 4. Saving in progress
// 5. Success confirmation
// 6. Error state with recovery option
```

### Modal/Dialog
```kotlin
// Always show:
// 1. Default appearance
// 2. With different content lengths
// 3. Dark theme variant
// 4. Landscape orientation
// 5. With/without action buttons
```

---

## Handoff Protocol

### ✅ Preview Agent → UI Agent
When preview needs reveal UI issues:
```
"Preview [ChatScreenErrorPreview] reveals that error banner overlaps 
message list. Recommend adjusting padding in [ChatScreen.kt] at line 82."
```

### ✅ Preview Agent → Backend Agent
When preview needs require state changes:
```
"Need additional state in [ChatUiState] to show retry capability. 
Please add 'retryableMessageId' field."
```

### ✅ UI Agent → Preview Agent
When new Composable is created:
```
"Created [SearchScreen.kt] - ready for preview composition. 
Please create [SearchScreenPreview.kt] with empty/loading/results/error states."
```

### ✅ Backend Agent → Preview Agent
When new states are added:
```
"Added [ChatUiState.Archived] state. Preview Agent should add 
preview for this state in [ChatScreenPreview.kt]."
```

---

## Development Workflow

### Creating Preview for New Screen

1. **UI Agent** creates `ChatScreen.kt` Composable
2. **Preview Agent** creates `ChatScreenPreview.kt`
   - All state variants
   - Device variants
   - Theme variants
3. **Backend Agent** creates `ChatViewModel.kt`
4. **Preview Agent** updates `PreviewChatViewModel.kt`
5. **Testing Agent** creates tests
6. **Reviewer Agent** validates all layers

### Iterating on Existing Screen

1. **UI Agent** modifies Composable appearance
2. **Preview Agent** updates preview annotations
3. **Testing Agent** updates UI tests if needed
4. **Reviewer Agent** approves changes

---

## Quality Gates

Preview Agent code MUST pass:

- ✅ **Compilability**: All preview functions compile without errors
- ✅ **No Placeholders**: Zero-elision policy - complete implementations only
- ✅ **Imports**: All imports explicitly listed, no wildcard imports
- ✅ **Mock Data**: Only synthetic data, no production calls
- ✅ **Theme Wrapping**: All previews wrapped in `NovaChatTheme`
- ✅ **State Coverage**: All major UiState variants have previews
- ✅ **Device Coverage**: Multiple device sizes shown
- ✅ **Documentation**: Comments explain preview purpose
- ✅ **Performance**: Acceptable IDE compilation time

---

## Related Skills

- **[Material Design 3 Skill](.github/skills/material-design/)** - Preview Material components
- **[Android Testing Skill](.github/skills/android-testing/)** - Bridge between previews and tests
- **[DEVELOPMENT_PROTOCOL.md](./DEVELOPMENT_PROTOCOL.md)** - Compliance requirements

---

## References

- [Jetpack Compose Preview Documentation](https://developer.android.com/develop/ui/compose/tooling/previews)
- [Android Studio Preview Support](https://developer.android.com/studio/preview)
- [Compose Preview Devices](https://developer.android.com/develop/ui/compose/tooling/previews#preview_devices)
- [Material Design 3 Components](https://m3.material.io)
