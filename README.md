# NovaChat - Android AI Chatbot Assistant

NovaChat is a modern Android AI chatbot application that supports both online (cloud-based) and offline (on-device) AI models. Built with the latest Android technologies targeting Android 16.

## Features

- 🤖 **Dual AI Mode Support**
  - Online mode using Google Gemini API (gemini-1.5-flash)
  - Offline mode using Google AICore for on-device AI (Android 15+)
- 💬 Modern chat interface built with Jetpack Compose
- ⚙️ Easy settings management for API keys and AI mode selection
- 🎨 Material Design 3 with dynamic theming support
- 📱 Targeting Android 16 (API 35) with backward compatibility to Android 9 (API 28)

## Technologies Used

- **Language**: Kotlin 2.1.0
- **UI Framework**: Jetpack Compose (BOM 2024.12.01)
- **Build System**: Gradle 8.7.3
- **Architecture**: MVVM with ViewModels
- **AI Libraries**:
  - Google Generative AI SDK 0.9.0 (Gemini)
  - Google AICore 1.0.0-alpha01 (On-device AI)
- **Data Storage**: DataStore Preferences
- **Async**: Kotlin Coroutines

## Requirements

- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35 (Android 16)
- Minimum device: Android 9 (API 28)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/patlar104/novachat.git
cd novachat
```

### 2. Get a Google AI API Key (for Online Mode)

1. Visit [Google AI Studio](https://ai.google.dev/)
2. Sign in with your Google account
3. Create a new API key
4. Copy the API key for use in the app

### 3. Build and Run

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click the "Run" button or press `Shift + F10`

### 4. Configure the App

1. Launch the app on your device
2. Tap the Settings icon (gear icon) in the top-right corner
3. Choose your AI mode:
   - **Online (Gemini)**: Cloud-based, requires internet and API key
   - **Offline (On-device)**: On-device AI, requires Android 15+ with AICore support
4. If using Online mode, enter your API key and tap "Save"
5. Return to the chat screen and start chatting!

## Building from Command Line

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing configuration)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/novachat/app/
│       │   ├── data/              # Data models and repositories
│       │   │   ├── AiRepository.kt
│       │   │   ├── ChatMessage.kt
│       │   │   └── PreferencesRepository.kt
│       │   ├── ui/                # Compose UI screens
│       │   │   ├── ChatScreen.kt
│       │   │   ├── SettingsScreen.kt
│       │   │   └── theme/         # App theme
│       │   ├── viewmodel/         # ViewModels
│       │   │   └── ChatViewModel.kt
│       │   └── MainActivity.kt
│       ├── res/                   # Resources
│       └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Features Explanation

### Chat Screen
- Send and receive messages in a clean, modern interface
- Messages are displayed in chat bubbles
- User messages appear on the right (blue)
- AI responses appear on the left (gray)
- Loading indicator shows when AI is processing
- Clear chat history with a single tap

### Settings Screen
- Toggle between Online and Offline AI modes
- Configure your Google AI API key for online mode
- View app information

### AI Modes

#### Online Mode (Google Gemini)
- Uses Google's Gemini 1.5 Flash model
- Requires internet connection
- Requires API key from Google AI Studio
- Advanced capabilities and up-to-date knowledge

#### Offline Mode (On-device)
- Uses Google AICore for on-device processing
- No internet required after model download
- Privacy-focused (data stays on device)
- Requires Android 15+ with AICore support
- Note: AICore availability varies by device

## Troubleshooting

### "On-device AI is not available"
- Ensure your device runs Android 15 or later
- Not all devices support Google AICore
- Try using Online mode instead

### "Please set your API key in Settings"
- Open Settings and enter a valid Google AI API key
- Get a key from https://ai.google.dev/

### Build errors
- Ensure you have JDK 17 installed
- Run `./gradlew clean` and rebuild
- Check that Android SDK 35 is installed

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- Google Generative AI SDK
- Google AICore
- Jetpack Compose team
- Android community

---

Built with ❤️ for novice developers learning Android AI integration