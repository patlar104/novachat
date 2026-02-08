# NovaChat: Current Architecture (February 2026)

> **Last Updated:** February 5, 2026  
> **Status:** Active Development  
> **Architecture:** MVVM + Clean Architecture

## Version Constraints (Strict - Do NOT Hallucinate)

```kotlin
Kotlin:        2.2.21 (STRICT - no 2.1.x versions)
Android:       API 36 (Android 16)
Compose BOM:   2026.01.01
AGP:           9.0.0
JVM Target:    Java 17
Min SDK:       28
Target SDK:    35
Compile SDK:   36
```

## Architecture Pattern

**MVVM + Clean Architecture** with Unidirectional Data Flow

- **State Management:** StateFlow (not LiveData)
- **Dependency Injection:** Manual AppContainer (not Hilt, not Koin)
- **UI:** Jetpack Compose (100% declarative, no XML layouts)
- **Async:** Kotlin Coroutines + Flow

## Project Structure

```
app/src/main/java/com/novachat/app/
├── presentation/               # ViewModels & UI contracts
│   ├── viewmodel/
│   │   ├── ChatViewModel.kt
│   │   └── SettingsViewModel.kt
│   └── model/
│       ├── ChatUiState.kt      # Sealed interfaces
│       ├── ChatUiEvent.kt
│       ├── SettingsUiState.kt
│       ├── SettingsUiEvent.kt
│       ├── UiEffect.kt
│       └── NavigationDestination.kt
│
├── domain/                     # Business logic (Android-agnostic)
│   ├── usecase/
│   │   ├── SendMessageUseCase.kt
│   │   ├── ObserveMessagesUseCase.kt
│   │   ├── ClearConversationUseCase.kt
│   │   ├── RetryMessageUseCase.kt
│   │   ├── GetAiConfigurationUseCase.kt
│   │   └── SaveAiConfigurationUseCase.kt
│   ├── model/
│   │   ├── Message.kt
│   │   ├── AiConfiguration.kt
│   │   ├── AiMode.kt
│   │   └── MessageSender.kt
│   └── repository/             # Interface contracts only
│       ├── AiRepository.kt
│       ├── MessageRepository.kt
│       └── PreferencesRepository.kt
│
├── data/                       # Data layer implementations
│   ├── repository/
│   │   ├── AiRepositoryImpl.kt
│   │   ├── MessageRepositoryImpl.kt
│   │   └── PreferencesRepositoryImpl.kt
│   ├── mapper/
│   │   ├── MessageMapper.kt
│   │   └── AiConfigurationMapper.kt
│   └── model/
│       └── DataModels.kt
│
├── ui/                         # Jetpack Compose screens
│   ├── ChatScreen.kt
│   ├── SettingsScreen.kt
│   ├── components/
│   │   ├── MessageBubble.kt
│   │   └── ChatInput.kt
│   └── theme/                  # Material 3 theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── di/                         # Manual dependency injection
│   └── AppContainer.kt
│
├── MainActivity.kt             # Navigation host
└── NovaChatApplication.kt      # App initialization
```

## State Management Pattern

All screens use sealed interfaces for type-safe state management:

```kotlin
// UI Contract (presentation/model/)
sealed interface ChatUiState { }        // All possible screen states
sealed interface ChatUiEvent { }        // All user actions/events
sealed interface UiEffect { }           // One-time actions (toast, nav)

// ViewModel exposes state
class ChatViewModel(...) : ViewModel() {
    val uiState: StateFlow<ChatUiState>
    val uiEffect: Flow<UiEffect>
    fun onEvent(event: ChatUiEvent)     // Single entry point
}

// Composable observes reactively
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { /* handle one-time effects */ }
    }

    when (uiState) { /* exhaustive rendering */ }
}
```

## Data Flow Architecture

```
User Interaction
    ↓
UI (Composable) emits Event
    ↓
ViewModel.onEvent(event) processes
    ↓
UseCase invoked
    ↓
Repository called
    ↓
Data Source (API/DataStore)
    ↓
Result<T> returned back up
    ↓
ViewModel updates StateFlow
    ↓
UI recomposes automatically
```

## Dependency Injection

Manual container pattern in `di/AppContainer.kt`:

```kotlin
class AppContainer(context: Context) {
    // Repositories (eager initialization)
    val aiRepository: AiRepository = AiRepositoryImpl(context)
    val messageRepository: MessageRepository = MessageRepositoryImpl()
    val preferencesRepository: PreferencesRepository =
        PreferencesRepositoryImpl(context)

    // Use Cases (lazy initialization)
    val sendMessageUseCase: SendMessageUseCase by lazy {
        SendMessageUseCase(
            messageRepository = messageRepository,
            aiRepository = aiRepository,
            preferencesRepository = preferencesRepository
        )
    }
    // ... other use cases
}
```

Access from Composables via custom ViewModelFactory:

```kotlin
val viewModel: ChatViewModel = viewModel(
    factory = ViewModelFactory(
        context = LocalContext.current,
        appContainer = (LocalContext.current.applicationContext as NovaChatApplication).appContainer
    )
)
```

## Error Handling Strategy

All async operations return `Result<T>`:

```kotlin
// Repository layer
override suspend fun sendMessage(
    message: String,
    useOnlineMode: Boolean
): Result<String> {
    return try {
        val response = if (useOnlineMode) {
            sendToGemini(message)
        } else {
            sendToAiCore(message)
        }
        Result.success(response)
    } catch (e: Exception) {
        Log.e(TAG, "Send message failed", e)
        Result.failure(e)
    }
}

// Use case layer
suspend operator fun invoke(message: String): Result<Message> {
    return sendMessageUseCase(message).fold(
        onSuccess = { aiMessage ->
            Result.success(aiMessage)
        },
        onFailure = { error ->
            Result.failure(Exception("Failed to send: ${error.message}"))
        }
    )
}

// ViewModel layer
fun onEvent(event: ChatUiEvent.SendMessage) {
    viewModelScope.launch {
        sendMessageUseCase(event.text).fold(
            onSuccess = { message ->
                _uiState.update { /* update state */ }
            },
            onFailure = { error ->
                _uiEffect.send(UiEffect.ShowSnackbar(error.message))
            }
        )
    }
}
```

## AI Mode Status (Critical Constraint)

| Mode | Status | Implementation |
|------|--------|----------------|
| **ONLINE (Gemini API)** | ✅ Available | Fully implemented with API key validation |
| **OFFLINE (AICore)** | ❌ Not Available | Dependency commented out in build.gradle.kts |

**Why OFFLINE mode is unavailable:**
- AICore library not yet published to Google Maven (as of Feb 2026)
- Dependency line is commented out: `// implementation("androidx.ai:ai-core:1.0.0-alpha01")`
- Attempting to use offline mode will fail validation

**Validation Pattern:**
```kotlin
suspend fun validateAndSend(message: String, config: AiConfiguration): Result<String> {
    if (config.mode == AiMode.ONLINE && config.apiKey == null) {
        return Result.failure(Exception("API key required for online mode"))
    }
    if (config.mode == AiMode.OFFLINE) {
        return Result.failure(Exception("Offline mode (AICore) not yet available"))
    }
    return aiRepository.sendMessage(message, useOnlineMode = true)
}
```

## Current Features

### 1. Chat Interface
- Message input with multiline support
- Conversation display with message bubbles
- User/AI message differentiation
- Error display with retry mechanism
- Loading states during AI processing
- Draft message persistence (survives rotation)

### 2. Settings Screen
- AI mode selection (Online/Offline - offline disabled)
- API key management (encrypted storage)
- Temperature control (0.0 - 2.0 slider)
- Max tokens configuration
- Configuration persistence via DataStore

### 3. Data Persistence
- **Messages:** In-memory only (Flow-based)
- **Preferences:** DataStore with Preferences API
- **API Keys:** DataStore (encrypted by Android file encryption)
- **Draft Messages:** SavedStateHandle (survives rotation, not process death)

### 4. Navigation
- Navigation Compose with sealed NavigationDestination
- Chat screen (default)
- Settings screen (modal navigation)

## Testing Strategy

### Test Coverage
- **Unit Tests:** ViewModels with mocked use cases
- **Integration Tests:** Use cases with fake repositories
- **UI Tests:** Compose UI tests with ComposeTestRule

### Test Patterns
```kotlin
// ViewModel test
@Test
fun sendMessage_success_updates_state() = runTest {
    val mockUseCase = mockk<SendMessageUseCase>()
    coEvery { mockUseCase("hello") } returns Result.success(aiMessage)
    val viewModel = ChatViewModel(mockUseCase, ...)

    viewModel.onEvent(ChatUiEvent.SendMessage("hello"))

    val state = viewModel.uiState.value
    assertThat(state).isInstanceOf(ChatUiState.Success::class.java)
}

// Compose UI test
@Test
fun chatScreen_displays_welcome_when_empty() {
    composeTestRule.setContent {
        ChatScreen(viewModel = mockViewModel, ...)
    }

    composeTestRule
        .onNodeWithText("Start a conversation")
        .assertIsDisplayed()
}
```

## Key Development Conventions

### 1. Zero-Elision Policy
**NEVER use placeholder comments:**
```kotlin
❌ BAD:
fun sendMessage() {
    // ... implementation
}

✅ GOOD:
fun sendMessage() {
    viewModelScope.launch {
        sendMessageUseCase(message).fold(
            onSuccess = { /* complete handling */ },
            onFailure = { /* complete error handling */ }
        )
    }
}
```

### 2. SavedStateHandle Usage
- **Purpose:** Draft messages only (temporary UI state)
- **Limitation:** Survives rotation, NOT process death
- **Pattern:**
```kotlin
class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    // ... use cases
) : ViewModel() {
    val draftMessage: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_DRAFT_MESSAGE, "")

    fun updateDraftMessage(text: String) {
        savedStateHandle[KEY_DRAFT_MESSAGE] = text
    }
}
```

### 3. DataStore Patterns
- **Use for:** Persistent configuration, API keys, preferences
- **Always handle IOException:**
```kotlin
override fun observeAiConfiguration(): Flow<AiConfiguration> {
    return context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "DataStore read failed", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs -> AiConfigurationMapper.toDomain(prefs) }
}
```

### 4. Effect Channel Lifecycle
```kotlin
// In ViewModel
private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
val uiEffect = _uiEffect.receiveAsFlow()

// In Composable
LaunchedEffect(Unit) {  // Use Unit key for one-time collection
    viewModel.uiEffect.collect { effect ->
        when (effect) {
            is UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            is UiEffect.Navigate -> navController.navigate(effect.route)
        }
    }
}
```

### 5. Material Design 3 Theming
- All colors from `MaterialTheme.colorScheme`
- All typography from `MaterialTheme.typography`
- All shapes from `MaterialTheme.shapes`
- Support both light and dark themes automatically
- Use dynamic color (Android 12+) when available

### 6. Async Patterns
- Launch work in `viewModelScope` (auto-cancellation)
- Use `.catch { }` on flows for error handling
- Use `distinctUntilChanged()` on DataStore flows
- Never block the main thread

## Known Constraints & Limitations

### 1. Message Storage
- **Current:** In-memory only (lost on app close)
- **Reason:** Simplicity for prototype
- **Future:** Room database for persistence

### 2. AI Mode Validation
- **Current:** Only ONLINE mode works
- **Validation:** Must check API key exists before sending
- **Error:** User-friendly messages for missing API key or offline mode attempt

### 3. Retry Logic Assumptions
- Assumes user cannot send while AI is processing (`isProcessing` flag)
- Retry finds user message by looking backward in message list
- If conversation cleared between failure and retry, retry fails

### 4. Draft Message Persistence
- SavedStateHandle survives rotation only
- Cleared after successful message send
- NOT stored in DataStore (intentional - drafts are temporary)

## Documentation References

### Core Documentation
- **[DEVELOPMENT_PROTOCOL.md](.github/DEVELOPMENT_PROTOCOL.md)** - Development guidelines (MANDATORY)
- **[AGENTS.md](.github/AGENTS.md)** - Multi-agent system documentation
- **[copilot-instructions.md](.github/copilot-instructions.md)** - Copilot integration guide

### Skills (Reusable Patterns)
- **[android-testing/](.github/skills/android-testing/)** - Complete testing patterns
- **[compose-preview/](.github/skills/compose-preview/)** - Preview annotation patterns
- **[material-design/](.github/skills/material-design/)** - Material 3 Compose patterns
- **[security-check/](.github/skills/security-check/)** - Security best practices

### API Documentation
- **[API.md](API.md)** - External API integration guide
- **[README.md](README.md)** - Project overview and setup

## Development Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build and install
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build

# Run lint checks
./gradlew lint

# Generate build report
./gradlew build --scan
```

## Multi-Agent System

NovaChat development uses specialized agents with clear boundaries:

| Agent | Scope | Tools Used |
|-------|-------|------------|
| **UI Agent** | Composables, Material 3, layouts | `create_file`, `apply_patch` for .kt files in ui/ |
| **Preview Agent** | @Preview annotations, preview data | `apply_patch` for preview functions |
| **Backend Agent** | ViewModels, use cases, repositories | `create_file`, `apply_patch` for presentation/, domain/, data/ |
| **Testing Agent** | Unit, integration, UI tests | `create_file` for *Test.kt files |
| **Build Agent** | Gradle, dependencies, manifest | `apply_patch` for build.gradle.kts, AndroidManifest.xml |

**Critical Rule:** Agents NEVER create documentation summaries. They implement code only.

## Common Anti-Patterns (What NOT to Do)

### ❌ UI Layer
- ✗ Instantiating ViewModels directly in Composables
- ✗ Storing UI state in Composables (use ViewModel StateFlow)
- ✗ Using `LaunchedEffect(uiState)` for effects (use `LaunchedEffect(Unit)`)
- ✗ Calling repositories from Composables (use ViewModel + UseCase)

### ❌ Backend Layer
- ✗ Not using `Result<T>` for error handling
- ✗ Silent error handling without logging
- ✗ Use cases calling other use cases (use repositories instead)
- ✗ Storing critical data in SavedStateHandle (use DataStore)
- ✗ Changing AI mode without validation

### ❌ Testing
- ✗ Testing sealed interface instantiation directly (test through ViewModel behavior)
- ✗ Using real repositories in unit tests (use mocks/fakes)
- ✗ Not handling coroutine cancellation in tests

### ❌ Multi-Agent
- ✗ UI Agent modifying repositories
- ✗ Backend Agent implementing Compose UI logic
- ✗ Testing Agent modifying production code

## Security Practices

### Current Implementation
- ✅ API keys stored in DataStore (encrypted by Android)
- ✅ No hardcoded secrets in code
- ✅ HTTPS only (no cleartext traffic)
- ✅ Input validation on message send
- ✅ ProGuard/R8 rules for release builds

### Security Checklist
- [ ] No sensitive data in logs
- [ ] All network requests use HTTPS
- [ ] Sensitive data encrypted at rest
- [ ] No SQL injection vulnerabilities (using Flow, not raw SQL)
- [ ] Exported components properly protected in manifest
- [ ] Dependencies checked for known vulnerabilities

## Next Development Steps

Refer to [GitHub Issues](https://github.com/patlar104/novachat/issues) for current work items.

All changes must follow:
1. **Zero-Elision Policy:** No placeholder comments
2. **Complete Implementations:** Full, compilable code only
3. **Cross-File Analysis:** Check ripple effects before changes
4. **Architecture Compliance:** MVVM + Clean Architecture adherence
5. **Version Adherence:** Kotlin 2.2.21, Compose BOM 2026.01.01

---

**This document reflects the actual codebase state as of February 5, 2026.**  
**All version numbers and architectural patterns are verified against production code.**
