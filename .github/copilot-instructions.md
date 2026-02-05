# NovaChat - Copilot Instructions

NovaChat is a modern Android AI chatbot built with **Jetpack Compose + MVVM + Clean Architecture**. It demonstrates 2026 Android best practices with dual AI mode support (online Gemini API + planned offline AICore).

> **⚠️ CRITICAL**: All development MUST follow [DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md):
> - **Zero-Elision Policy**: Never use placeholders (`// ... rest of code`)  
> - **Complete Implementations**: Write full, working code only
> - **Input Disambiguation**: Ask when requests are ambiguous
> - **Cross-File Dependencies**: Analyze ripple effects before changes
> - **Self-Validation**: Check completeness, imports, syntax before output

## Quick Facts

- **Versions**: Kotlin 2.2.21, AGP 9.0.0, Gradle 9.1.0, Compose BOM 2026.01.01
- **SDK**: Target 35, Compile 36, Min 28 (Android 9+)
- **Architecture**: MVVM + Clean Architecture (presentation/domain/data layers)
- **DI**: Manual `AppContainer` (no Hilt/Koin) - lazy singletons pattern
- **State**: Sealed interfaces + StateFlow (persistent) + Channel (one-time effects)
- **⚠️ AI Mode**: ONLINE only (Gemini 1.5 Flash) - AICore commented out in [build.gradle.kts](../app/build.gradle.kts)

## Multi-Agent System

This project uses specialized agents with clear boundaries. See [AGENTS.md](AGENTS.md) for complete documentation.

| Agent | Scope | Key Files |
|-------|-------|----------|
| **Planner** | Task breakdown, architecture planning | Creates implementation plans |
| **UI** | Composables, Material 3, layouts | `ui/**/*.kt`, `ui/theme/*.kt` |
| **Preview** | @Preview annotations, preview data | `ui/preview/*.kt` (no ViewModels) |
| **Backend** | ViewModels, use cases, repositories, DI | `presentation/viewmodel/*.kt`, `domain/**`, `data/**`, `di/*.kt` |
| **Testing** | Unit tests, Compose UI tests | `**/*Test.kt` (test dirs only) |
| **Build** | Gradle, dependencies, manifest | `build.gradle.kts`, `AndroidManifest.xml` |
| **Reviewer** | Code quality, security, architecture | Reviews all layers |

**Critical Rule**: Agents NEVER work outside their scope. Hand off when boundaries are crossed.

---

## Core Architecture Patterns

### 1. State & Event-Driven UI (Sealed Interfaces)

NovaChat uses sealed interfaces for type-safe state management. See [presentation/model/UiState.kt](../app/src/main/java/com/novachat/app/presentation/model/UiState.kt) for complete implementation.

```kotlin
// UI Contract (presentation/model/)
sealed interface ChatUiState { }        // All possible screen states
sealed interface ChatUiEvent { }        // All user actions
sealed interface UiEffect { }           // One-time actions (toast, nav)

// ViewModel (presentation/viewmodel/ChatViewModel.kt)
class ChatViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()
    
    fun onEvent(event: ChatUiEvent) { /* single entry point */ }
}

// Composable (ui/ChatScreen.kt)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {  // Use Unit key for one-time collection
        viewModel.uiEffect.collect { effect -> /* handle */ }
    }
    
    when (uiState) { /* exhaustive when */ }
}
```

**Key Pattern**: StateFlow for persistent state, Channel for one-time effects. Always use `LaunchedEffect(Unit)` for effect collection.

### 2. Clean Architecture Layers

```
presentation/              # ViewModels & UI contracts
  viewmodel/               # ChatViewModel.kt, SettingsViewModel.kt
  model/                   # UiState, UiEvent, UiEffect interfaces
  
domain/                    # Business logic (Android-agnostic)
  usecase/                 # SendMessageUseCase, ObserveMessagesUseCase, etc.
  model/                   # Message, AiConfiguration (no Android imports)
  repository/              # Interfaces only
  
data/                      # Data layer implementations
  repository/              # AiRepositoryImpl, MessageRepositoryImpl, etc.
  mapper/                  # Domain ↔ Data conversion
  model/                   # Data-specific models
  
ui/                        # Jetpack Compose screens
  ChatScreen.kt, SettingsScreen.kt
  theme/                   # Material 3 theme
  
di/                        # Manual DI
  AppContainer.kt          # Lazy singletons
```

**Critical**: ViewModels never import from `ui/` package (testable without Android UI).

### 3. Manual Dependency Injection

See [di/AppContainer.kt](../app/src/main/java/com/novachat/app/di/AppContainer.kt) - lightweight pattern without Hilt/Koin.

```kotlin
class AppContainer(context: Context) {
    // Repositories (eager)
    val aiRepository: AiRepository = AiRepositoryImpl(context)
    val messageRepository: MessageRepository = MessageRepositoryImpl()
    
    // Use Cases (lazy)
    val sendMessageUseCase: SendMessageUseCase by lazy {
        SendMessageUseCase(messageRepository, aiRepository, preferencesRepository)
    }
}

// Access from Composables
val viewModel: ChatViewModel = viewModel(
    factory = ViewModelFactory(LocalContext.current.appContainer)
)
```

### 4. Error Handling with Result<T>

All async operations return `Result<T>`. Use `.fold()` for handling:

```kotlin
// Repository layer
suspend fun sendMessage(text: String): Result<String> {
    return try {
        val response = api.send(text)
        Result.success(response)
    } catch (e: Exception) {
        Log.e(TAG, "Send failed", e)
        Result.failure(e)
    }
}

// ViewModel layer (use fold)
sendMessageUseCase(text).fold(
    onSuccess = { message -> _uiState.update { /* success state */ } },
    onFailure = { error -> _uiEffect.send(UiEffect.ShowSnackbar(error.message)) }
)
```

---

## Development Commands

```bash
# Build & Install
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Build + install to device

# Testing
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests

# Quality
./gradlew clean build            # Clean build
./gradlew lint                   # Run lint checks
```

---

## Project Structure

```
app/src/main/java/com/novachat/app/
├── presentation/           # ViewModels & UI contracts
│   ├── viewmodel/         ChatViewModel.kt, SettingsViewModel.kt
│   └── model/             UiState, UiEvent, UiEffect, NavigationDestination
│
├── domain/                # Business logic (no Android dependencies)
│   ├── usecase/           SendMessageUseCase, ObserveMessagesUseCase, etc.
│   ├── model/             Message, AiConfiguration, AiMode
│   └── repository/        Interfaces: AiRepository, MessageRepository, PreferencesRepository
│
├── data/                  # Data layer implementations
│   ├── repository/        AiRepositoryImpl, MessageRepositoryImpl, PreferencesRepositoryImpl
│   ├── mapper/            MessageMapper, AiConfigurationMapper
│   └── model/             DataModels.kt
│
├── ui/                    # Jetpack Compose screens
│   ├── ChatScreen.kt, SettingsScreen.kt
│   ├── components/        MessageBubble.kt, etc.
│   ├── preview/           ChatScreenPreview.kt, preview data providers
│   └── theme/             Color.kt, Theme.kt, Type.kt (Material 3)
│
├── di/                    # Manual dependency injection
│   └── AppContainer.kt    Lazy singleton container
│
├── MainActivity.kt        # Navigation host
└── NovaChatApplication.kt # App initialization
```

**Key References**:
- [ChatViewModel.kt](../app/src/main/java/com/novachat/app/presentation/viewmodel/ChatViewModel.kt) - Complete ViewModel pattern
- [UiState.kt](../app/src/main/java/com/novachat/app/presentation/model/UiState.kt) - State/Event/Effect definitions
- [AppContainer.kt](../app/src/main/java/com/novachat/app/di/AppContainer.kt) - DI wiring pattern
- [ChatScreen.kt](../app/src/main/java/com/novachat/app/ui/ChatScreen.kt) - Compose UI patterns

---

## NovaChat-Specific Patterns

### ViewModel Event Handling Pattern
All ViewModels use a single `onEvent(event: UiEvent)` entry point:
```kotlin
// ViewModel receives event, processes, and updates state
fun onEvent(event: ChatUiEvent) {
    when (event) {
        is ChatUiEvent.SendMessage -> handleSendMessage(event.text)
        is ChatUiEvent.ClearConversation -> handleClearConversation()
        // ... exhaustive
    }
}

// Use cases with fold() for error handling
result.fold(
    onSuccess = { data -> _uiState.update { it.copy(data = data) } },
    onFailure = { error -> emitEffect(UiEffect.ShowSnackbar(error.message)) }
)
```

### UI State Transitions Pattern
```kotlin
// Use update { } to transform state atomically
_uiState.update { currentState ->
    when (currentState) {
        is ChatUiState.Success -> currentState.copy(isProcessing = false)
        else -> currentState
    }
}
```

### Key File Patterns
1. **ChatViewModel** - Copy pattern for: event handling, state updates, effect emission
2. **ChatScreen** - Copy pattern for: state collection, effect handling, Compose layout
3. **AppContainer** - Copy pattern for: use case wiring, lazy-loaded singletons
4. **UiState.kt** - Copy pattern for: sealed interfaces, helper methods, exhaustive handling

### Common Imports to Include
```kotlin
// State Management
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

// Lifecycle & Coroutines
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.CoroutineDispatcher

// Compose
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
```

---

## Creating New Screens - Step-by-Step Pattern

When adding a new screen, follow this sequence to avoid common mistakes:

1. **Define Domain Models** (if needed in `domain/model/`)
   - Only Android-agnostic data classes
   - Never import Android frameworks

2. **Create Repository Interfaces** (in `domain/repository/`)
   - One interface per responsibility

3. **Create Use Cases** (in `domain/usecase/`)
   - Each action = one use case
   - All return `Result<T>` for error handling
   - Use `fold()` for error transformation

4. **Define UI Contract** (in `presentation/model/UiState.kt`)
   - Add sealed interface `<Feature>UiState`
   - Add sealed interface `<Feature>UiEvent`
   - Add relevant `UiEffect` variants

5. **Create ViewModel** (in `presentation/viewmodel/<Feature>ViewModel.kt`)
   - Accept `SavedStateHandle` as first parameter
   - Implement single `onEvent(event: UiEvent)` entry point
   - Return `StateFlow<UiState>` and `receiveAsFlow()` for effects

6. **Update DI Container** (`di/AppContainer.kt`)
   - Add repository instance (if new)
   - Add use case lazy property
   - Add ViewModel factory handling

7. **Create Composable Screen** (`ui/<Feature>Screen.kt`)
   - Use `collectAsStateWithLifecycle()` for state
   - Use `LaunchedEffect(Unit)` for effect handling
   - Never call ViewModels directly; inject via factory

8. **Add Navigation** (`MainActivity.kt`)
   - Add route to `NavigationDestination` sealed interface
   - Register in `NavHost` composable

9. **Write Tests**
   - ViewModel unit tests with fake use cases
   - Use case tests with fake repositories
   - Compose tests for critical UI paths

## Code Quality Requirements (MANDATORY)

### Before ANY Code Output - Self-Validation Checklist

All code submissions MUST pass these checks (from DEVELOPMENT_PROTOCOL.md):

- [ ] **Completeness**: Full file written, NO placeholders like `// ... code`
- [ ] **Imports**: Every required import explicitly included
- [ ] **Syntax**: All brackets `{ }` and parentheses `( )` balanced
- [ ] **Logic**: Implementation is complete and makes sense
- [ ] **Standards**: Uses 2026 best practices (Kotlin 2.2.21, Compose BOM 2026.01.01)
- [ ] **Dependencies**: Cross-file impacts analyzed and addressed
- [ ] **Architecture**: Follows MVVM + Clean Architecture patterns

### Verification & Duplication Guard

- **No guessing**: Read the current file(s) before editing; never infer content.
- **Find duplicates**: If adding sections, confirm the topic does not already exist elsewhere in the file.
- **Edits may be wrong**: After edits/searches, re-read the target area to confirm correctness.
- **If missing**: When you cannot find something, assume it may be in a different file or location and broaden the search (do not invent).

### Input Handling Protocol

When receiving a request:

1. **Check Current State**: Use `grep_search` or `semantic_search` to find existing implementations
2. **Identify Duplicates**: If feature exists, clarify: "This exists in [File]. Modify or create new?"
3. **Plan Dependencies**: List all files that will be created/modified
4. **Implement Atomically**: One complete file at a time
5. **Verify Imports**: Ensure all cross-layer dependencies are correct

### Prohibited Practices

❌ **NEVER** use these patterns:
```kotlin
// ... rest of implementation     // FORBIDDEN
// ... existing code               // FORBIDDEN  
// TODO: implement                 // FORBIDDEN
// Add other methods here          // FORBIDDEN
```

✅ **ALWAYS** write complete implementations:
```kotlin
// Every function fully implemented
// Every import explicitly stated
// Every bracket properly closed
```

### Documentation vs. Production Code Distinction

**Important Clarification**: Code examples in this document may use `{ ... }` shorthand for function bodies as documentation conventions. This is acceptable for documentation brevity.

However, all **agent-generated production code** (actual code files you create for the project) MUST follow the **Zero-Elision Policy** with complete implementations. This means:
- Every code block you generate must be 100% complete and compilable
- No placeholders like `// ...` or `{ ... }`
- All imports fully specified
- All brackets and braces balanced
- All methods/functions fully implemented

This applies to all code files you create:
- ViewModels, Composables, Screens
- Use cases, Repositories, Data models
- Test files
- Extension functions, utilities

**Exception**: Code examples in this documentation that contain `{ ... }` are shortened only for brevity and clarity. They represent complete implementations in the actual codebase.

---

## Key Development Conventions

1. **File Organization**
   - Each sealed interface lives in its own file or grouped logically in `presentation/model/`
   - Keep ViewModels to <300 lines; split logic into use cases if larger
   - Place utility functions near where they're used

2. **Naming Conventions**
   - UI States: `<Feature>UiState` (e.g., ChatUiState)
   - UI Events: `<Feature>UiEvent` (e.g., ChatUiEvent)
   - ViewModels: `<Feature>ViewModel` (e.g., ChatViewModel)
   - Use Cases: `<Action>UseCase` (e.g., SendMessageUseCase)
   - Immutability: Use `val` by default, `var` only when state must change

---

## Common Anti-Patterns (What NOT to Do)

These patterns violate NovaChat's architecture and will cause problems:

### ❌ UI Layer Anti-Patterns

1. **Calling ViewModels from Composables directly**
   ```kotlin
   // WRONG:
   val chatViewModel = ChatViewModel(...)  // Direct instantiation!
   
   // RIGHT:
   val chatViewModel = viewModel<ChatViewModel>(factory = viewModelFactory)
   ```
   Why: Violates DI, makes testing impossible

2. **Storing UI state in Composables**
   ```kotlin
   // WRONG:
   var messages by remember { mutableStateOf(emptyList()) }  // Won't survive rotation!
   
   // RIGHT:
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   ```
   Why: Lost on configuration changes; ViewModels handle this

3. **Using LaunchedEffect with State parameters**
   ```kotlin
   // WRONG:
   LaunchedEffect(uiState) {  // Re-runs every recomposition!
       // Side effect code
   }
   
   // RIGHT:
   LaunchedEffect(Unit) {  // Runs once
       viewModel.uiEffect.collect { /* handle */ }
   }
   ```
   Why: Causes repeated executions; use Unit or stable keys

4. **Emit state AND effect for same action**
   ```kotlin
   // WRONG:
   _uiState.update { it.copy(showDialog = true) }
   emitEffect(UiEffect.ShowDialog(...))
   
   // RIGHT: Use one or the other
   // For persistent UI elements: state
   // For one-time actions: effect
   ```

### ❌ Backend Layer Anti-Patterns

1. **Not using Result<T> for error handling**
   ```kotlin
   // WRONG:
   suspend fun sendMessage(): String {
       throw Exception("Network error")  // Exceptions propagate!
   }
   
   // RIGHT:
   suspend fun sendMessage(): Result<String> {
       return try {
           Result.success(response)
       } catch (e: Exception) {
           Result.failure(e)
       }
   }
   ```
   Why: Uncaught exceptions crash; Result<T> makes errors explicit

2. **Silent error handling without logging**
   ```kotlin
   // WRONG:
   .catch { /* silently ignore */ }
   
   // RIGHT:
   .catch { exception ->
       Log.e("TAG", "DataStore failed", exception)
       emit(defaultValue)
   }
   ```
   Why: Impossible to debug silent failures

3. **Use cases calling other use cases**
   ```kotlin
   // WRONG:
   class SendMessageUseCase(private val retryUseCase: RetryUseCase) {
       invoke() -> retryUseCase.invoke()  // Use case chaining!
   }
   
   // RIGHT:
   class SendMessageUseCase(private val messageRepository: MessageRepository) {
       invoke() -> messageRepository.sendMessage()  // Use repository
   }
   ```
   Why: Violates single responsibility; use case should not depend on other use cases

4. **Storing Critical Data in SavedStateHandle**
   ```kotlin
   // WRONG:
   savedStateHandle["apiKey"] = userApiKey  // Lost on process death!
   
   // RIGHT:
   preferencesRepository.saveApiKey(userApiKey)  // Persistent
   ```
   Why: SavedStateHandle ≠ persistent storage; only use for draft messages

5. **Changing AI mode without validation**
   ```kotlin
   // WRONG:
   fun onEvent(ChangeAiMode(mode)) {
       _mode.value = mode  // No validation!
   }
   
   // RIGHT:
   fun onEvent(ChangeAiMode(mode)) {
       if (!aiRepository.isModeAvailable(mode)) {
           emitEffect(UiEffect.ShowSnackbar("Mode not available"))
           return
       }
       updateConfiguration(AiConfiguration(mode = mode, ...))
   }
   ```
   Why: OFFLINE mode is unavailable; silent failures confuse users

### ❌ Testing Anti-Patterns

1. **Testing sealed interface instantiation**
   ```kotlin
   // WRONG:
   val state = ChatUiState.Success(...)  // Not testable directly
   when (state) {
       is ChatUiState.Success -> { /* test something */ }
   }
   
   // RIGHT:
   // Test through ViewModel behavior that produces the state
   viewModel.onEvent(ChatUiEvent.SendMessage("test"))
   viewModel.uiState.test {
       expectMostRecent().should.beInstanceOf<ChatUiState.Success>()
   }
   ```
   Why: Sealed interfaces are implementation; test through behavior

2. **Using real repositories in unit tests**
   ```kotlin
   // WRONG:
   class ChatViewModelTest {
       private val viewModel = ChatViewModel(
           sendMessageUseCase = RealSendMessageUseCase(RealAiRepository(...))  // Real dependency!
       )
   }
   
   // RIGHT:
   class ChatViewModelTest {
       private val mockUseCase = mockk<SendMessageUseCase>()
       private val viewModel = ChatViewModel(mockUseCase)
   }
   ```
   Why: Unit tests should isolate the component; use fakes/mocks for dependencies

3. **Not handling cancellation in tests**
   ```kotlin
   // WRONG:
   viewModel.onEvent(...)
   // Assert immediately without waiting
   
   // RIGHT:
   val state = viewModel.uiState.test {
       emit(ChatUiEvent.SendMessage("test"))
       awaitItem().should.equal(/* expected state */)
   }
   ```
   Why: Coroutines are async; tests must wait for state updates

### ❌ Multi-Agent Anti-Patterns

1. **UI Agent modifying repositories**
   ```kotlin
   // WRONG - UI Agent task:
   class ChatScreen {
       fun clearConversation() {
           messageRepository.clearAll()  // Backend responsibility!
       }
   }
   
   // RIGHT - UI Agent calls ViewModel
   viewModel.onEvent(ChatUiEvent.ClearConversation)
   ```
   Why: Violates layer separation and agent scope

2. **Backend Agent implementing Compose UI logic**
   ```kotlin
   // WRONG - Backend Agent task:
   class ChatViewModel {
       val shouldShowLoadingSpinner: StateFlow<Boolean> = ...  // UI decision!
   }
   
   // RIGHT - ViewModel provides state, UI decides display
   class ChatViewModel {
       sealed interface ChatUiState {
           data object Loading : ChatUiState
       }
   }
   ```
   Why: ViewModels are testable without UI; don't leak UI concerns

3. **Testing Agent modifying production code**
   ```
   // WRONG:
   Testing Agent NEVER modifies production files
   
   // RIGHT:
   If tests fail, analyze root cause and hand off to appropriate agent
   (Build Agent for dependency issues, Backend Agent for logic issues, etc.)
   ```
   Why: Testing Agent scope is tests only; maintains isolation

---

## Testing Patterns

### ViewModel Unit Testing
Test ViewModel behavior by triggering events and asserting state changes:
```kotlin
@Test
fun sendMessage_success_updates_state() = runTest {
    val mockUseCase = mockk<SendMessageUseCase>()
    coEvery { mockUseCase("hello") } returns Result.success(aiMessage)
    val viewModel = ChatViewModel(
        savedStateHandle = SavedStateHandle(),
        sendMessageUseCase = mockUseCase,
        observeMessagesUseCase = mockk(),
        clearConversationUseCase = mockk(),
        retryMessageUseCase = mockk()
    )
    
    viewModel.onEvent(ChatUiEvent.SendMessage("hello"))
    
    val state = viewModel.uiState.value
    state.should.beInstanceOf<ChatUiState.Success>()
}
```

### Use Case Testing
Test use case logic with mocked repositories:
```kotlin
@Test
fun sendMessage_calls_ai_and_stores_response() = runTest {
    val messageRepo = mockk<MessageRepository>()
    val aiRepo = mockk<AiRepository>()
    coEvery { messageRepo.addMessage(any()) } returns Result.success(Unit)
    coEvery { aiRepo.sendMessage(any(), any()) } returns Result.success("AI response")
    
    val useCase = SendMessageUseCase(messageRepo, aiRepo, prefsRepo)
    val result = useCase("hello")
    
    result.isSuccess.should.be.true()
    coVerify { messageRepo.addMessage(any()) }
}
```

### Compose UI Testing
Test critical UI paths with ComposeTestRule:
```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun chatScreen_displays_initial_message() {
    composeTestRule.setContent {
        val viewModel = mockk<ChatViewModel>()
        every { viewModel.uiState } returns flowOf(ChatUiState.Initial).stateIn(...)
        ChatScreen(viewModel = viewModel, onNavigateToSettings = {})
    }
    
    composeTestRule.onNodeWithText("Start a conversation!").assertIsDisplayed()
}
```

### Common Test Dependencies
```kotlin
// build.gradle.kts test dependencies
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.14.9")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
testImplementation("app.cash.turbine:turbine:1.2.1")  // For Flow testing
testImplementation("io.kotest:kotest-assertions-core:6.1.2")  // For readable assertions

// For Compose UI tests
androidTestImplementation(platform("androidx.compose:compose-bom:2026.01.01"))
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.compose.ui:ui-test-manifest")
```

---


## References

- **[DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md)**: Comprehensive development guidelines (MANDATORY)
- **[AGENTS.md](AGENTS.md)**: Multi-agent system documentation
- **Skills**: Reusable patterns in `.github/skills/`
  - `android-testing/`: Testing patterns and examples
  - `material-design/`: Compose UI patterns
  - `security-check/`: Security best practices
