# NovaChat Development Guide

## Architecture Overview

NovaChat follows the MVVM (Model-View-ViewModel) architecture pattern with Jetpack Compose for the UI layer.

### Project Structure

```
app/src/main/java/com/novachat/app/
├── presentation/              # UI state + ViewModels
│   ├── model/                 # UiState, UiEvent, UiEffect
│   └── viewmodel/             # ChatViewModel, SettingsViewModel
├── domain/                    # Business logic interfaces + models
│   ├── model/                 # Message, AiConfiguration, etc.
│   └── repository/            # AiRepository, MessageRepository, PreferencesRepository
├── data/                      # Implementations + mappers
│   ├── repository/            # AiRepositoryImpl, MessageRepositoryImpl, PreferencesRepositoryImpl
│   ├── mapper/                # Domain <-> data mappers
│   └── model/                 # DataModels.kt
├── ui/                        # UI layer (Compose)
│   ├── ChatScreen.kt          # Main chat interface
│   ├── SettingsScreen.kt      # Settings screen
│   └── theme/                 # App theming
├── di/                        # Dependency injection
│   └── AppContainer.kt
├── MainActivity.kt            # App entry point
└── NovaChatApplication.kt     # Application class
```

### Key Components

#### 1. Data Layer

**Message (domain/model/Message.kt)**
- Represents individual chat messages
- Contains message content, sender info, timestamp, and status
- Used across layers via mappers

**AiRepository (domain/repository/AiRepository.kt)**
- Defines AI model interactions via `generateResponse()`
- Implemented in data layer (`AiRepositoryImpl`)
- Returns `Result<String>` for proper error handling

**PreferencesRepository (domain/repository/PreferencesRepository.kt)**
- Manages user preferences using DataStore
- Implemented in data layer (`PreferencesRepositoryImpl`)
- Persists AI configuration and API keys

#### 2. ViewModel Layer

**ChatViewModel (presentation/viewmodel/ChatViewModel.kt)**
- Manages chat state and business logic
- Exposes StateFlows for UI observation
- Handles message sending and error states
- Coordinates between UI and data layers

#### 3. UI Layer

**ChatScreen (ui/ChatScreen.kt)**
- Main chat interface using Jetpack Compose
- Shows message list with auto-scrolling
- Input field for typing messages
- Error banners and loading states

**SettingsScreen (ui/SettingsScreen.kt)**
- Configure AI mode (online/offline)
- Enter and save API key
- View app information

## Adding New Features

### Adding a New AI Provider

1. Add the new AI SDK dependency in `app/build.gradle.kts`
2. Implement a new path in `AiRepositoryImpl.kt`:
```kotlin
suspend fun sendMessageToNewProvider(message: String): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            // Your implementation
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```
3. Add a new `AiMode` entry in `domain/model/AiConfiguration.kt`
4. Update `AiRepositoryImpl.generateResponse()` to route the new mode
5. Update `SettingsScreen.kt` to add UI for the new option

### Adding Message Persistence

Currently, messages are stored in-memory. To add persistence:

1. Add Room database dependency to `app/build.gradle.kts`
2. Create a `MessageEntity` and `MessageDao`
3. Update `ChatViewModel` to save/load from database
4. Use `Flow` to observe database changes

### Adding User Authentication

1. Add Firebase Authentication dependency
2. Create an `AuthRepository` in the data layer
3. Add login/signup screens
4. Store user ID with each message

## Testing

### Unit Tests

Create unit tests in `app/src/test/`:

```kotlin
class ChatViewModelTest {
    @Test
    fun `sendMessage should add user message to list`() {
        // Test implementation
    }
}
```

### UI Tests

Create UI tests in `app/src/androidTest/`:

```kotlin
@Test
fun testChatScreenDisplaysMessages() {
    composeTestRule.setContent {
        ChatScreen(viewModel = testViewModel)
    }
    composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
}
```

## Code Style

This project follows the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

Key points:
- Use 4 spaces for indentation
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Use immutable data structures where possible

## Building for Release

1. Create a keystore for signing:
```bash
keytool -genkey -v -keystore novachat.keystore -alias novachat -keyalg RSA -keysize 2048 -validity 10000
```

2. Add signing config to `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("novachat.keystore")
            storePassword = "your-password"
            keyAlias = "novachat"
            keyPassword = "your-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

3. Build the release APK:
```bash
./gradlew assembleRelease
```

## Debugging

### Logcat Filtering

In Android Studio Logcat, filter by:
- Tag: `ChatViewModel`, `AiRepository`
- Package name: `com.novachat.app`

### Common Issues

**"API key not set"**
- Check that the API key is saved in Settings
- Verify DataStore is not being cleared

**"Network error"**
- Check internet connectivity
- Verify API key is valid
- Check Logcat for detailed error messages

**Compose recomposition issues**
- Use `remember` for state that should persist across recompositions
- Use `LaunchedEffect` for side effects
- Check if StateFlows are being collected properly

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Google Generative AI SDK](https://github.com/google/generative-ai-android)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Material Design 3](https://m3.material.io/)

## Getting Help

- Check existing GitHub Issues
- Read the Android Developer documentation
- Ask questions on Stack Overflow with tags: `android`, `jetpack-compose`, `kotlin`
