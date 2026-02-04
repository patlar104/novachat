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

- **Language**: Kotlin 2.2.21 (via Compose Compiler Plugin)
- **UI Framework**: Jetpack Compose (BOM 2026.01.01)
- **Build System**: Gradle 9.1.0 with Android Gradle Plugin 9.0.0
- **Architecture**: MVVM with ViewModels
- **AI Libraries**:
  - Google Generative AI SDK 0.9.0 (Gemini)
  - Google AICore (Not yet available - offline mode disabled)
- **Data Storage**: DataStore Preferences
- **Async**: Kotlin Coroutines

## Requirements

- Android Studio Otter or newer (for AGP 9.0.0 support)
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
- **Note: Currently Unavailable** - Google AICore is not yet publicly available on Maven (as of January 2026)
- When available, will use Google AICore for on-device processing
- No internet required after model download
- Privacy-focused (data stays on device)
- Will require Android 15+ with AICore support
- Availability will vary by device

## Troubleshooting

### Build Issues

#### "Android Gradle Plugin 9.0.0 requires Gradle 9.1.0"
The project uses the latest Android Gradle Plugin (9.0.0) which requires Gradle 9.1.0. This is automatically configured in the gradle wrapper.

#### "Plugin was not found" or repository errors
This app requires access to Google's Maven repository. If you're building in a restricted network environment:
1. Ensure your firewall allows access to `dl.google.com`
2. Check your proxy settings if behind a corporate firewall
3. Try using a VPN if the repository is geo-blocked

#### "On-device AI is not available"
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
# NovaChat

An Android AI chatbot application built with Jetpack Compose, supporting both online (Google Gemini) and offline (on-device AICore) AI modes.

## 🤖 About NovaChat

NovaChat is a modern Android AI chatbot that demonstrates:
- **Jetpack Compose** with Material Design 3
- **Dual AI Integration**: Google Gemini API (online) and AICore (offline)
- **MVVM + Clean Architecture** with proper separation of concerns
- **Modern Android Development**: Kotlin 2.2.21, Coroutines, StateFlow, DataStore

**Current Status**: This branch contains the multi-agent development system configuration. See the `copilot/create-ai-chatbot-app` branch for the full application implementation.

## 🤖 Multi-Agent Development System

This repository uses a specialized multi-agent system with GitHub Copilot to maintain code quality and prevent agent drift. The system is tailored for NovaChat's Jetpack Compose and AI integration architecture.

See [.github/AGENTS.md](.github/AGENTS.md) for detailed documentation.

### Quick Start with Agents

**Planning a Feature:**
```
@copilot using planner.agent.md
Plan implementation for user authentication with biometric support
```

**Implementing Compose UI:**
```
@copilot using ui-agent.agent.md
Create a settings screen with Material 3 components for API key input
```

**Implementing ViewModels & Repositories:**
```
@copilot using backend-agent.agent.md
Implement ChatViewModel with Gemini AI integration and error handling
```

**Adding Tests:**
```
@copilot using testing-agent.agent.md
Add Compose UI tests for ChatScreen and unit tests for ChatViewModel
```

**Configuring Dependencies:**
```
@copilot using build-agent.agent.md
Add dependencies for Google Generative AI SDK and update Compose BOM
```

**Reviewing Code:**
```
@copilot using reviewer-agent.agent.md
Review the AI integration for security vulnerabilities and best practices
```

### Available Agents

- 🎯 **Planner** - Task breakdown and architecture planning
- 🎨 **UI Agent** - Jetpack Compose UI (Material Design 3, state hoisting)
- ⚙️ **Backend Agent** - ViewModels, repositories, AI integration, DataStore
- 🧪 **Testing Agent** - Unit tests and Compose UI tests
- 🔧 **Build Agent** - Gradle, Compose BOM, AI SDK dependencies
- 👁️ **Reviewer Agent** - Code quality and security review

### Reusable Skills

- 📱 **Android Testing** - Compose UI tests, ViewModel testing, coroutine testing
- 🎨 **Material Design 3** - Compose components, theme configuration
- 🔒 **Security** - API key storage, encryption, network security

## Project Structure

This branch focuses on the multi-agent system configuration in `.github/`:
- `copilot-instructions.md` - General NovaChat development guidelines
- `agents/` - Specialized agent configurations (6 agents)
- `skills/` - Reusable knowledge and patterns (3 skills)
- `AGENTS.md` - Complete multi-agent system documentation

For the full application implementation, see the `copilot/create-ai-chatbot-app` branch.

## Development Guidelines

See [.github/copilot-instructions.md](.github/copilot-instructions.md) for:
- NovaChat project overview and architecture
- Jetpack Compose best practices
- MVVM + Clean Architecture patterns  
- AI integration guidelines (Gemini + AICore)
- StateFlow and coroutine patterns
- Testing strategies

## License

See [LICENSE](LICENSE) file for details.
