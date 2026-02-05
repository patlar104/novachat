# NovaChat - Copilot Instructions

NovaChat is an Android AI chatbot (Jetpack Compose + MVVM + Clean Architecture) supporting both online (Gemini API) and offline (AICore) AI modes. Built with Kotlin 2.3.0, AGP 9.0.0, and Compose BOM 2026.01.01.

> **⚠️ CRITICAL**: All development work MUST follow the [DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md) guidelines. This includes:
> - **Zero-Elision Policy**: Never use placeholders like `// ... rest of code`
> - **Complete Implementations**: Write full, working code only
> - **Input Disambiguation**: Ask for clarification when requests are ambiguous
> - **Cross-File Dependencies**: Analyze ripple effects before changes
> - **Self-Validation**: Check completeness, imports, and syntax before output

## Quick Facts

- **Target SDK**: 36 (compiling), 35 (targeting), 28 (minimum)
- **JVM Target**: Java 17, Kotlin 2.3.0
- **DI Pattern**: Manual `AppContainer` (no Hilt/Koin)
- **State Management**: Sealed interfaces + StateFlow + Channel (for effects)
- **⚠️ OFFLINE MODE NOT AVAILABLE**: AICore dependency is commented out in build.gradle.kts; only ONLINE mode (Gemini) works

## Agent-Specific Guidance

### 🎨 UI Agent
**Focus Areas**: Sections marked with [UI-FOCUS]
- Essential Architecture Patterns (sections 1-2: State & Event-Driven UI, Clean Architecture)
- Repository Structure (understand navigation of UI layer only)
- NovaChat-Specific Patterns (all sections - demonstrates UI patterns)
- Creating New Screens (steps 7-8: Composable creation, navigation)
- Key Dev Conventions: SavedStateHandle (3), Effect Channel Lifecycle (6), Material Design (11)
- Code Quality Requirements (all - mandatory for all agents)

**Skip or Reference Only**: Sections on AI Repository, Use Cases, DataStore patterns (backend will handle)
**Handoff to**: Preview Agent after creating Composables (for @Preview annotations)

### 🎬 Preview Agent
**Focus Areas**: Sections marked with [PREVIEW-FOCUS]
- Essential Architecture Patterns (sections 1-2: State understanding for preview composition)
- Repository Structure (understand UI layer structure for preview file placement)
- NovaChat-Specific Patterns (Material Design 11 for theme variations)
- Material Design (11) - Theme preview variants
- Preview Best Practices (dedicated section in agent file)
- Code Quality Requirements (all - mandatory for all agents)

**Scope**: @Preview annotations, preview Composables, preview data providers, device specifications
**Skip or Reference Only**: Backend logic, repositories, use cases (no ViewModel usage in previews)
**Receive from**: UI Agent (after Composable creation)
**Handoff to**: Testing Agent (for automated UI tests)

### ⚙️ Backend Agent  
**Focus Areas**: Sections marked with [BACKEND-FOCUS]
- Essential Architecture Patterns (sections 3-5: DI, Error Handling, AI Repository)
- Repository Structure (understand data and domain layers)
- Creating New Screens (steps 1-6: Models, repositories, use cases, ViewModels, DI)
- Key Dev Conventions: AI Mode Validation (5), Data Flow (8), Error Handling (9), DataStore Patterns (4), Use Case Extension (13)
- Code Quality Requirements (all - mandatory for all agents)

**Skip or Reference Only**: Compose pattern details, Material Design (UI agent will handle)

### 🧪 Testing Agent
**Focus Areas**: Sections marked with [TESTING-FOCUS]
- Essential Architecture Patterns (understand overall structure for test strategy)
- Repository Structure (understand packages and dependencies to test)
- Creating New Screens (understand the layers being tested)
- Key Dev Conventions: Error Handling (9), Async Patterns (10), ViewModel Event Handling pattern
- Code Quality Requirements (all - mandatory for all agents)

**Important**: Analysis report shows Testing patterns section was MISSING from codebase. Start tests from actual implementations in ChatViewModel.kt as reference.

### 🔧 Build Agent
**Focus Areas**: Sections marked with [BUILD-FOCUS]
- Quick Facts (tech versions: Kotlin 2.3.0, AGP 9.0.0, Compose BOM 2026.01.01)
- Development Commands (build/test commands your agent should know)
- AI Mode Validation (5) - Understand that AICore is NOT available (commented out in build.gradle.kts)
- Code Quality Requirements - Dependencies section

**Reference**: Look at actual build.gradle.kts for dependency versions and constraints

### 👁️ Reviewer Agent
**Focus Areas**: ALL sections - reviewers need complete context
- Use entire document as reference for code quality assessment
- Focus on Code Quality Requirements (self-validation checklist)
- Verify cross-file dependencies are analyzed (Creating New Screens checklist)
- Check for DEVELOPMENT_PROTOCOL.md compliance (zero-elision policy, completeness)

---

## Agent Focus Quick Reference

| Section | UI | Preview | Backend | Testing | Build | Reviewer |
|---------|----|---------|---------:|-------:|----------|----------|
| Quick Facts | ✓ | - | ✓ | ✓ | ✓✓ | ✓ |
| Architecture Patterns 1-2 | ✓✓ | ✓ | ✓ | ✓ | - | ✓ |
| Architecture Patterns 3-5 | - | - | ✓✓ | ✓ | ✓ | ✓ |
| Development Commands | ✓ | ✓ | ✓ | ✓ | ✓✓ | ✓ |
| Repository Structure | ✓ | ✓ | ✓✓ | ✓ | - | ✓ |
| ViewModel Patterns | ✓ | - | ✓ | ✓ | - | ✓ |
| New Screen Checklist | ✓ | ✓ | ✓✓ | ✓ | - | ✓ |
| SavedStateHandle (Conv. 3) | ✓✓ | - | ✓ | - | - | ✓ |
| DataStore (Conv. 4) | - | - | ✓✓ | - | - | ✓ |
| AI Mode Validation (Conv. 5) | - | - | ✓✓ | ✓ | ✓ | ✓ |
| Effect Channel (Conv. 6) | ✓✓ | - | ✓ | - | - | ✓ |
| Data Flow (Conv. 8) | ✓ | - | ✓✓ | ✓✓ | - | ✓ |
| Error Handling (Conv. 9) | ✓ | - | ✓✓ | ✓✓ | - | ✓ |
| Material Design (Conv. 11) | ✓✓ | ✓✓ | - | - | - | ✓ |
| API Security (Conv. 12) | - | - | ✓✓ | ✓ | ✓ | ✓ |
| Testing Patterns | - | - | - | ✓✓ | - | ✓ |
| Preview Patterns | - | ✓✓ | - | ✓ | - | ✓ |
| Anti-Patterns | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

**Legend**: ✓✓ = Critical, ✓ = Important, - = Reference only

---

## Essential Architecture Patterns

### 1. **State & Event-Driven UI** [UI-FOCUS][PREVIEW-FOCUS]
NovaChat uses sealed interfaces for type-safe state management:
```kotlin
// Presentation layer defines UI contract
sealed interface ChatUiState { ... }  // All possible screen states
sealed interface ChatUiEvent { ... }  // All user actions/events
sealed interface UiEffect { ... }     // One-time actions (toast, nav)

// ViewModel transforms events to state/effects
class ChatViewModel(...) : ViewModel() {
    private val _uiState = MutableStateFlow<ChatUiState>(...)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()
    
    fun onEvent(event: ChatUiEvent) { ... }  // Single entry point
}

// Composable subscribes reactively
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle effects (one-time, won't re-trigger on recompose)
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect -> /* handle */ }
    }
    
    when (uiState) { ... }  // Exhaustive when on sealed interface
}
```

### 2. **Clean Architecture Layers** [UI-FOCUS][BACKEND-FOCUS][TESTING-FOCUS]
```
presentation/          ← ViewModels, UI state/events/effects
    viewmodel/
    model/             (UiState, UiEvent, UiEffect)
domain/               ← Business logic (use cases)
    usecase/           (SendMessageUseCase, etc)
    model/             (Message, AiConfiguration - not Android-aware)
    repository/        (Interfaces only)
data/                 ← Data sources (APIs, local storage)
    repository/        (Implementations)
    mapper/            (Domain ↔ Data translation)
    model/             (Data-specific models)
ui/                   ← Jetpack Compose screens & theme
    theme/
```
**Critical**: ViewModels never import from `ui` package. ViewModels must be testable without Android UI.

### 3. **Dependency Injection (Manual AppContainer)** [BACKEND-FOCUS]
Located in `di/AppContainer.kt`. No Hilt, lightweight pattern:
```kotlin
// Two relationships: Repository → Use Case → ViewModel
val sendMessageUseCase: SendMessageUseCase by lazy {
    SendMessageUseCase(
        messageRepository = messageRepository,
        aiRepository = aiRepository,
        preferencesRepository = preferencesRepository
    )
}

// Access from Composables via viewModelFactory
ViewModelFactory(context.appContainer)
```

### 4. **Result<T> for Error Handling** [BACKEND-FOCUS][TESTING-FOCUS]
All repository methods return `Result<String>` or `Result<Unit>`:
```kotlin
// Use cases use fold() for error handling
result.fold(
    onSuccess = { data -> /* update state */ },
    onFailure = { error -> /* emit error, log */ }
)
```

### 5. **AI Repository Pattern** [BACKEND-FOCUS][BUILD-FOCUS]
Handles both online (Gemini) and offline (AICore) modes:
```kotlin
class AiRepositoryImpl(context: Context) : AiRepository {
    suspend fun sendMessage(message: String, useOnlineMode: Boolean): Result<String>
    
    private suspend fun sendToGemini(message: String): Result<String> { ... }
    private suspend fun sendToAiCore(message: String): Result<String> { ... }
}
```

## Development Commands [BUILD-FOCUS]

- **Build Debug**: `./gradlew assembleDebug`
- **Build + Install**: `./gradlew installDebug`
- **Run Tests**: `./gradlew test`
- **Run Instrumented Tests**: `./gradlew connectedAndroidTest`
- **Clean Build**: `./gradlew clean build`
- **Check Lint**: `./gradlew lint`

## Repository Structure

```
app/src/main/java/com/novachat/app/
├── presentation/           # ViewModels & UI state contracts
│   ├── viewmodel/         (ChatViewModel, SettingsViewModel)
│   └── model/             (ChatUiState, ChatUiEvent, UiEffect, NavigationDestination)
├── domain/                # Business logic, interfaces, domain models
│   ├── usecase/           (SendMessageUseCase, ObserveMessagesUseCase, etc)
│   ├── model/             (Message, AiConfiguration, no Android imports)
│   └── repository/        (Interface contracts: AiRepository, MessageRepository, PreferencesRepository)
├── data/                  # Implementations, API clients, local storage
│   ├── repository/        (AiRepositoryImpl, MessageRepositoryImpl, PreferencesRepositoryImpl)
│   ├── mapper/            (Mappers between data and domain models)
│   └── model/             (DataModels.kt - data layer representations)
├── ui/                    # Jetpack Compose screens & Material 3 theme
│   ├── ChatScreen.kt      (Main chat, demonstrates state/effect handling)
│   ├── SettingsScreen.kt
│   └── theme/             (Color.kt, Theme.kt, Type.kt - Material 3)
├── di/                    # Dependency injection container
│   └── AppContainer.kt    (Manual DI, lazy-loaded singletons)
├── MainActivity.kt        # App entry point, navigation
└── NovaChatApplication.kt # Custom Application class
```

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
- [ ] **Standards**: Uses 2026 best practices (Kotlin 2.3.0, Compose BOM 2026.01.01)
- [ ] **Dependencies**: Cross-file impacts analyzed and addressed
- [ ] **Architecture**: Follows MVVM + Clean Architecture patterns

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

## Creating New Screens - Step-by-Step Pattern [BACKEND-FOCUS][UI-FOCUS][TESTING-FOCUS]

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

## Key Development Conventions [ALL AGENTS]

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

3. **SavedStateHandle for Draft Messages** [UI-FOCUS][BACKEND-FOCUS]
   - Use `getStateFlow()` to expose drafts as persistent state
   - Must manually clear drafts after successful send: `updateDraftMessage("")`
   - SavedStateHandle survives configuration changes (rotation) but NOT process death
   - Never use SavedStateHandle for critical data; use DataStore instead
   - Document whether draft is in-memory or persisted to avoid user confusion

4. **DataStore Configuration Patterns** [BACKEND-FOCUS]
   ```kotlin
   // Pattern: Always use catch to handle IOException  
   override fun observeAiConfiguration(): Flow<AiConfiguration> {
       return context.dataStore.data
           .catch { exception ->
               if (exception is IOException) {
                   Log.e("Preferences", "DataStore read failed", exception)
                   emit(emptyPreferences())
               } else {
                   throw exception
               }
           }
           .map { preferences ->
               try {
                   AiConfigurationMapper.toDomain(preferences)
               } catch (e: Exception) {
                   Log.e("Preferences", "Corrupted data", e)
                   throw CorruptDataException("Invalid preferences data", e)
               }
           }
   }
   ```
   - Always log errors in DataStore operations
   - Never silently return defaults on errors
   - Use `.catch { }` to distinguish IOException from other exceptions
   - Consider validation layer for corrupted data

5. **AI Mode Validation - CRITICAL CONSTRAINT** [BACKEND-FOCUS][BUILD-FOCUS]
   - **OFFLINE mode is currently unavailable** - AICore not yet on Maven (as of Feb 2026)
   - Only ONLINE mode (Gemini API) should be used in production
   - Before using ONLINE mode: Verify API key exists and is non-empty
   - Pattern: Return `Result.failure(ValidationError(...))` for invalid configurations, never silently default
   ```kotlin
   // Validation pattern before message send
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
   - When adding offline mode support: Test against `aiRepository.isModeAvailable()`
   - Update SettingsVM to validate mode availability before saving

6. **Effect Channel Collection - Lifecycle Safety** [UI-FOCUS][BACKEND-FOCUS]
   - Use `LaunchedEffect(Unit)` in Composables to start collecting effects
   - Effects collected with `LaunchedEffect(Unit)` won't re-collect if screen navigates away and back (by design)
   - This is correct for persistent one-time effects; if effects must survive navigation, use `repeatOnLifecycle()`
   - Never collect effects outside LaunchedEffect (will leak resources)
   - Pattern: `collectAsStateWithLifecycle()` for state, `LaunchedEffect` for effects

7. **Message Retry Logic Assumptions** [BACKEND-FOCUS]
   - Retry assumes user cannot send message while AI is processing (enforced by `isProcessing` flag in state)
   - Retry finds user message by looking backward: works because UI prevents concurrent sends
   - If user clears conversation between failure and retry, retry fails with "message not found"
   - Error messages should distinguish "message deleted" from "network failed"

8. **Data Flow** [BACKEND-FOCUS][TESTING-FOCUS]
   - UI (Composable) → Event → ViewModel → UseCase → Repository → Data Source
   - State flows back up: Data Source → Repository → UseCase → ViewModel StateFlow → UI recompose
   - One-time actions use UiEffect channel (never StateFlow for single-occurrence events)

9. **Error Handling** [BACKEND-FOCUS][TESTING-FOCUS]
   - All suspending functions return `Result<T>` from use cases/repositories
   - UI updates state with error message instead of throwing
   - Log errors with `Log.e()` but emit user-friendly messages to UI
   - Distinguish between recoverable and non-recoverable errors

10. **Async Patterns** [BACKEND-FOCUS]
    - Launch work in `viewModelScope` (auto-cancellation on ViewModel destruction)
    - Use `.catch { }` on flows to handle exceptions without crashing
    - For Preferences flows: use `.flow.distinctUntilChanged()` to avoid duplicate emissions
    - Use `SuspendCancellationException` for cleanup in finally blocks

11. **Material Design 3 & Theming** [UI-FOCUS][PREVIEW-FOCUS]
    - All colors come from `MaterialTheme.colorScheme`
    - Typography from `MaterialTheme.typography`
    - Use predefined shapes: `MaterialTheme.shapes.small/medium/large`
    - Support both light and dark themes automatically
    - Test UI in preview with `@Preview(showSystemUi = true)`

12. **API Key Security** [BACKEND-FOCUS][BUILD-FOCUS]
    - Store in DataStore with file encryption (handled by Android)
    - Never log the actual key
    - Validate format before saving (basic length check)
    - Offer ability to clear/replace without losing other settings

13. **Use Case Extension Pattern** [BACKEND-FOCUS]
    - Each use case = single responsibility
    - If adding feature that requires multiple steps, create separate use cases and coordinate in ViewModel
    - Don't make use cases call other use cases (use repositories instead)
    - Error handling: transform low-level errors to domain-specific errors with `fold()` and context

## Testing Patterns [TESTING-FOCUS]

### ViewModel Unit Testing

**Pattern**: Test ViewModel behavior by triggering events and asserting state changes
```kotlin
@Test
fun sendMessage_success_updates_state() = runTest {
    // Arrange: Create ViewModel with mocked dependencies
    val mockUseCase = mockk<SendMessageUseCase>()
    coEvery { mockUseCase("hello") } returns Result.success(
        Message(id = MessageId("1"), content = "response", sender = MessageSender.ASSISTANT)
    )
    val viewModel = ChatViewModel(
        savedStateHandle = SavedStateHandle(),
        sendMessageUseCase = mockUseCase,
        observeMessagesUseCase = mockk(),
        clearConversationUseCase = mockk(),
        retryMessageUseCase = mockk()
    )
    
    // Act: Send event
    viewModel.onEvent(ChatUiEvent.SendMessage("hello"))
    
    // Assert: Verify state updated
    val state = viewModel.uiState.value
    state.should.beInstanceOf<ChatUiState.Success>()
    (state as ChatUiState.Success).isProcessing.should.be.false()
}

@Test
fun sendMessage_error_emits_effect() = runTest {
    // Arrange
    val mockUseCase = mockk<SendMessageUseCase>()
    coEvery { mockUseCase("bad") } returns Result.failure(Exception("Network error"))
    val viewModel = ChatViewModel(
        savedStateHandle = SavedStateHandle(),
        sendMessageUseCase = mockUseCase,
        observeMessagesUseCase = mockk(),
        clearConversationUseCase = mockk(),
        retryMessageUseCase = mockk()
    )
    
    // Act & Assert: Collect effects
    launch {
        viewModel.uiEffect.collect { effect ->
            effect.should.beInstanceOf<UiEffect.ShowSnackbar>()
        }
    }
    
    viewModel.onEvent(ChatUiEvent.SendMessage("bad"))
}

@Test
fun draftMessage_survives_rotation() = runTest {
    val savedStateHandle = SavedStateHandle()
    val viewModel = ChatViewModel(
        savedStateHandle = savedStateHandle,
        sendMessageUseCase = mockk(),
        observeMessagesUseCase = mockk(),
        clearConversationUseCase = mockk(),
        retryMessageUseCase = mockk()
    )
    
    // Act: Update draft
    viewModel.updateDraftMessage("typed message")
    
    // Assert: Draft persists in SavedStateHandle
    savedStateHandle.get<String>("draft_message").should.equal("typed message")
}
```

### Use Case Unit Testing

**Pattern**: Test use case logic with mocked repositories
```kotlin
@Test
fun sendMessage_validates_empty_message() = runTest {
    val useCase = SendMessageUseCase(
        messageRepository = mockk(),
        aiRepository = mockk(),
        preferencesRepository = mockk()
    )
    
    val result = useCase("")
    
    result.isFailure.should.be.true()
}

@Test
fun sendMessage_calls_ai_and_stores_response() = runTest {
    val messageRepo = mockk<MessageRepository>()
    val aiRepo = mockk<AiRepository>()
    val prefsRepo = mockk<PreferencesRepository>()
    
    coEvery { messageRepo.addMessage(any()) } returns Result.success(Unit)
    coEvery { aiRepo.sendMessage(any(), any()) } returns Result.success("AI response")
    coEvery { prefsRepo.getAiConfiguration() } returns Result.success(
        AiConfiguration(mode = AiMode.ONLINE, apiKey = "key")
    )
    
    val useCase = SendMessageUseCase(messageRepo, aiRepo, prefsRepo)
    val result = useCase("hello")
    
    result.isSuccess.should.be.true()
    coVerify { messageRepo.addMessage(any()) } // Verify message was added
}
```

### Compose UI Testing

**Pattern**: Test critical UI paths with ComposeTestRule
```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun chatScreen_displays_initial_message() {
    composeTestRule.setContent {
        val viewModel = mockk<ChatViewModel>()
        every { viewModel.uiState } returns flowOf(ChatUiState.Initial).stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ChatUiState.Initial
        )
        every { viewModel.uiEffect } returns emptyFlow()
        every { viewModel.draftMessage } returns flowOf("").stateIn(...)
        
        ChatScreen(viewModel = viewModel, onNavigateToSettings = {})
    }
    
    // Assert: Empty state is shown
    composeTestRule.onNodeWithText("Start a conversation!").assertIsDisplayed()
}

@Test
fun chatScreen_send_button_enabled_with_text() {
    composeTestRule.setContent {
        // Setup similar to above
        ChatScreen(...)
    }
    
    // Act: Type message
    composeTestRule.onNodeWithTag("MessageInput").performTextInput("hello")
    
    // Assert: Send button is enabled
    composeTestRule.onNodeWithContentDescription("Send").assertIsEnabled()
}

@Test
fun chatScreen_error_banner_shown_on_error() {
    composeTestRule.setContent {
        val viewModel = mockk<ChatViewModel>()
        every { viewModel.uiState } returns flowOf(
            ChatUiState.Success(
                messages = emptyList(),
                error = "Network error"
            )
        ).stateIn(...)
        // ... rest of mocking
        
        ChatScreen(viewModel = viewModel, onNavigateToSettings = {})
    }
    
    // Assert: Error message displayed
    composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
}
```

### Testing with DataStore

**Pattern**: Use in-memory DataStore for repository tests
```kotlin
@Test
fun aiConfigurationRepository_saves_and_retrieves() = runTest {
    val dataStore = DataStoreTestingHelper.createTestDataStore(context)
    val repository = PreferencesRepositoryImpl(dataStore)
    
    // Act: Save configuration
    repository.saveAiConfiguration(
        AiConfiguration(mode = AiMode.ONLINE, apiKey = "test-key")
    )
    
    // Assert: Retrieved configuration matches
    repository.observeAiConfiguration().first().apply {
        mode.should.equal(AiMode.ONLINE)
        apiKey.should.equal("test-key")
    }
}

@Test
fun aiConfigurationRepository_handles_corruption_gracefully() = runTest {
    val dataStore = mockk<DataStore<Preferences>>()
    coEvery { dataStore.data } returns flow { throw IOException("Corrupted") }
    
    val repository = PreferencesRepositoryImpl(dataStore)
    
    // Assert: Error is handled, not thrown
    repository.observeAiConfiguration().test {
        // Flow should emit error or default, not crash
        expectMostRecent().should.notBeNull()
    }
}
```

### Test Utilities

**Helper**: Create reusable test fixtures
```kotlin
// TestDataBuilder.kt
object TestMessageBuilder {
    fun userMessage(content: String = "test message"): Message =
        Message(
            id = MessageId("test-${System.nanoTime()}"),
            content = content,
            sender = MessageSender.USER,
            timestamp = System.currentTimeMillis()
        )
    
    fun aiMessage(content: String = "AI response"): Message =
        Message(
            id = MessageId("ai-${System.nanoTime()}"),
            content = content,
            sender = MessageSender.ASSISTANT,
            timestamp = System.currentTimeMillis()
        )
}

// ChatViewModelTest.kt
@Test
fun observeMessages_returns_conversation_in_order() = runTest {
    val messages = listOf(
        TestMessageBuilder.userMessage("hello"),
        TestMessageBuilder.aiMessage("hi there")
    )
    
    val mockRepo = mockk<MessageRepository>()
    coEvery { mockRepo.observeMessages() } returns flowOf(messages)
    
    val useCase = ObserveMessagesUseCase(mockRepo)
    val result = useCase().first()
    
    result.should.have.size(2)
    result[0].sender.should.equal(MessageSender.USER)
}
```

### Common Test Dependencies

```kotlin
// build.gradle.kts test dependencies
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.5")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
testImplementation("app.cash.turbine:turbine:1.0.0")  // For Flow testing
testImplementation("io.kotest:kotest-assertions-core:5.8.0")  // For readable assertions

// For Compose UI tests
androidTestImplementation(platform("androidx.compose:compose-bom:2026.01.01"))
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.compose.ui:ui-test-manifest")
androidTestImplementation("androidx.test:rules:1.6.1")
```

---

## References

- **[DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md)**: Comprehensive development guidelines (MANDATORY)
- **[AGENTS.md](AGENTS.md)**: Multi-agent system documentation
- **Skills**: Reusable patterns in `.github/skills/`
  - `android-testing/`: Testing patterns and examples
  - `material-design/`: Compose UI patterns
  - `security-check/`: Security best practices
