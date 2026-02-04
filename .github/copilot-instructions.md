# NovaChat - Copilot Instructions

NovaChat is an Android AI chatbot application built with Jetpack Compose, supporting both online (Google Gemini) and offline (on-device AICore) AI modes. Please follow these guidelines when contributing:

> **⚠️ CRITICAL**: All development work MUST follow the [DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md) guidelines. This includes:
> - **Zero-Elision Policy**: Never use placeholders like `// ... rest of code`
> - **Complete Implementations**: Write full, working code only
> - **Input Disambiguation**: Ask for clarification when requests are ambiguous
> - **Cross-File Dependencies**: Analyze ripple effects before changes
> - **Self-Validation**: Check completeness, imports, and syntax before output

## Project Overview

- **Target SDK**: Android 16 (API 35)
- **Minimum SDK**: Android 9 (API 28)
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture principles
- **Build System**: Gradle 9.1.0 with Android Gradle Plugin 9.0.0
- **Language**: Kotlin 2.3.0 with Compose Compiler Plugin

## Code Standards

### Required Before Each Commit
- Format code: `./gradlew ktlintFormat` or `./gradlew spotlessApply` (if configured)
- Ensure all Compose previews work
- Verify API key handling is secure (never commit API keys)
- Test on both light and dark themes

### Development Flow
- Build: `./gradlew assembleDebug`
- Run tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Clean build: `./gradlew clean build`
- Install on device: `./gradlew installDebug`

## Repository Structure
```
app/src/main/java/com/novachat/app/
├── data/              # Data layer (repositories, models)
│   ├── repository/    # Repository implementations
│   ├── mapper/        # Data mappers
│   └── model/         # Data models
├── domain/            # Domain layer (use cases, business logic)
├── presentation/      # Presentation layer (UI state, events)
├── ui/                # Compose UI screens and theme
│   ├── theme/         # Material 3 theme
│   ├── ChatScreen.kt
│   └── SettingsScreen.kt
├── viewmodel/         # ViewModels
├── di/                # Dependency injection
└── MainActivity.kt
```

## Key Guidelines

1. **Jetpack Compose UI**
   - Use @Composable functions for all UI
   - Follow Material Design 3 guidelines
   - Use remember and rememberSaveable for state
   - Leverage ViewModel for business logic
   - Use LaunchedEffect for side effects
   - Prefer stateless Composables

2. **Kotlin Coding Standards**
   - Use Kotlin idiomatic patterns (data classes, sealed classes, extension functions)
   - Prefer immutability (val over var)
   - Use null-safety features properly
   - Use sealed classes/interfaces for UI state
   - Leverage Kotlin coroutines and Flow

3. **Architecture (MVVM + Clean Architecture)**
   - **Presentation Layer**: Composables observe ViewModel state
   - **ViewModel Layer**: Manages UI state with StateFlow, handles user events
   - **Domain Layer**: Use cases contain business logic
   - **Data Layer**: Repositories abstract data sources
   - Never reference Android UI components from ViewModels
   - Use dependency injection (AppContainer pattern or Hilt)

4. **AI Integration**
   - Support both online (Gemini API) and offline (AICore) modes
   - Handle API errors gracefully with user-friendly messages
   - Never hardcode API keys - use DataStore for storage
   - Implement proper loading states during AI processing
   - Respect user's AI mode preference

5. **State Management**
   - Use StateFlow in ViewModels for reactive UI updates
   - Define UI state as sealed classes or data classes
   - Handle loading, success, and error states
   - Use DataStore for persistent preferences
   - Implement proper error handling with Result<T>

6. **Testing**
   - Write unit tests for ViewModels and use cases
   - Test repositories with fake implementations
   - Use MockK for mocking in tests
   - Test state transitions and error handling
   - Aim for meaningful coverage on critical paths

7. **Dependencies**
   - Use Compose BOM for version management
   - Keep Kotlin and AGP versions in sync
   - Check security vulnerabilities before adding dependencies
   - Document why each dependency is needed

8. **Git Practices**
   - Never commit API keys or secrets
   - API keys should go in local.properties (gitignored)
   - Keep commits focused and atomic
   - Write descriptive commit messages
   - Don't commit build/ or .gradle/ directories

9. **Documentation**
   - Add KDoc for public APIs
   - Update README.md, API.md, or QUICKSTART.md when adding features
   - Document AI model limitations
   - Explain complex state transitions

## NovaChat-Specific Patterns

### UI State Pattern
```kotlin
sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(val messages: List<ChatMessage>) : ChatUiState
    data class Error(val message: String) : ChatUiState
}
```

### Repository Pattern
```kotlin
interface AiRepository {
    suspend fun sendMessage(message: String, useOnlineMode: Boolean): Result<String>
}
```

### ViewModel Pattern
```kotlin
class ChatViewModel(
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    fun sendMessage(message: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            try {
                val response = aiRepository.sendMessage(message, useOnlineMode = true)
                response.onSuccess { result ->
                    _uiState.value = ChatUiState.Success(listOf(ChatMessage(result, false)))
                }
                response.onFailure { error ->
                    _uiState.value = ChatUiState.Error(error.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error(e.message ?: "Error sending message")
            }
        }
    }
}
```

### Composable Pattern
```kotlin
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is ChatUiState.Loading -> CircularProgressIndicator()
            is ChatUiState.Success -> MessageList((uiState as ChatUiState.Success).messages)
            is ChatUiState.Error -> ErrorMessage((uiState as ChatUiState.Error).message)
        }
    }
}
```

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

1. **Check Current State**: Read existing files FIRST before implementing
2. **Identify Duplicates**: If feature exists, ask: "This exists in [File]. Modify or add new?"
3. **Clarify Ambiguity**: If unclear, STOP and ask specific questions
4. **Plan Dependencies**: List all files that will be affected
5. **Implement Atomically**: One complete file at a time

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

## Android-Specific Notes

- **Target SDK**: 35 (Android 16) - Use latest APIs
- **Minimum SDK**: 28 (Android 9) - Maintains 95%+ device compatibility
- **Compose**: All UI built with Jetpack Compose, no XML layouts
- **Material 3**: Use Material Design 3 components exclusively
- **Navigation**: Use Compose Navigation for screen transitions
- **Lifecycle**: Always use lifecycle-aware components
- **Configuration Changes**: Handled automatically by Compose
- **Accessibility**: Ensure all Composables have semantic content descriptions
- **Security**: 
  - Store API keys in DataStore (encrypted)
  - Never log sensitive information
  - Use HTTPS for all network calls

## Multi-Agent System

NovaChat uses a specialized multi-agent development system with **strict protocol enforcement**. 

See `.github/AGENTS.md` for details on:
- **Planner Agent**: Task breakdown and architecture decisions
- **UI Agent**: Jetpack Compose UI implementation  
- **Backend Agent**: ViewModels, repositories, and business logic
- **Testing Agent**: Unit and instrumentation tests
- **Build Agent**: Gradle configuration and dependencies
- **Reviewer Agent**: Code quality and security review

**All agents MUST follow DEVELOPMENT_PROTOCOL.md** including:
- Zero-elision policy (no placeholders)
- Complete implementations only
- Cross-file dependency analysis
- Input disambiguation protocols
- Self-validation before output

When working on NovaChat, use the appropriate agent for your task to maintain code quality and prevent scope drift.

## References

- **[DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md)**: Comprehensive development guidelines (MANDATORY)
- **[AGENTS.md](AGENTS.md)**: Multi-agent system documentation
- **Skills**: Reusable patterns in `.github/skills/`
  - `android-testing/`: Testing patterns and examples
  - `material-design/`: Compose UI patterns
  - `security-check/`: Security best practices
