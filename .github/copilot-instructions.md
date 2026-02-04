# NovaChat - Copilot Instructions

NovaChat is an Android AI chatbot application built with Jetpack Compose, supporting both online (Google Gemini) and offline (on-device AICore) AI modes. Please follow these guidelines when contributing:

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
            // Implementation
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
    // UI implementation
}
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

NovaChat uses a specialized multi-agent development system. See `.github/AGENTS.md` for details on:
- **Planner Agent**: Task breakdown and architecture decisions
- **UI Agent**: Jetpack Compose UI implementation  
- **Backend Agent**: ViewModels, repositories, and business logic
- **Testing Agent**: Unit and instrumentation tests
- **Build Agent**: Gradle configuration and dependencies
- **Reviewer Agent**: Code quality and security review

When working on NovaChat, use the appropriate agent for your task to maintain code quality and prevent scope drift.
