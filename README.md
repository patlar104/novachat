# NovaChat - Android AI Chatbot Assistant

NovaChat is a modern Android AI chatbot application that supports online (cloud-based) AI models. Offline (on-device) mode is planned but currently unavailable because AICore is not yet published to Maven. Built with the latest Android technologies targeting Android 16.

## Features

- **Dual AI Mode Support**
  - Online mode using Firebase Functions proxy (Gemini 2.5 Flash via server)
  - Offline mode planned with Google AICore for on-device AI (Android 15+), currently unavailable
- Modern chat interface built with Jetpack Compose
- Settings management for AI mode and theme preferences
- Material Design 3 with dynamic theming support
- Targeting Android 16 (API 35) with backward compatibility to Android 9 (API 28)

## Technologies Used

- **Language**: Kotlin 2.2.21 (via Compose Compiler Plugin)
- **UI Framework**: Jetpack Compose (BOM 2026.01.01; Google Maven only, mapping: [BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping))
- **Build System**: Gradle 9.1.0 with Android Gradle Plugin 9.0.0
- **Architecture**: MVVM with ViewModels
- **AI Libraries**:
  - Firebase Functions (proxy for Gemini API)
  - Firebase Authentication (anonymous sign-in)
  - Google AICore (not yet available on Maven; offline mode disabled)
- **Data Storage**: DataStore Preferences
- **Async**: Kotlin Coroutines

## Requirements

- Android Studio (latest stable, for AGP 9.0.0 support)
- JDK 21
- Android SDK 36 (compileSdk) and Android SDK 35 (targetSdk)
- Minimum device: Android 9 (API 28)
- Google Maven repository enabled (`google()`) for Compose BOM and AndroidX

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/patlar104/novachat.git
cd novachat
```

### 2. Firebase Setup

The app uses Firebase Cloud Functions as a proxy for AI requests. No API key setup required - the app automatically signs in anonymously and uses the Firebase proxy.

### 3. Build and Run

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click the "Run" button or press Shift + F10

### 4. Configure the App

1. Launch the app on your device
2. The app automatically signs in anonymously with Firebase
3. Tap the Settings icon (gear icon) in the top-right corner
4. Choose your AI mode:
   - **Online (Gemini)**: Cloud-based via Firebase Functions proxy, requires internet
   - **Offline (On-device)**: Planned; requires Android 15+ with AICore support (currently unavailable)
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

NovaChat uses a layered architecture, following **Clean Architecture** principles (Domain, Data, Presentation) with **MVVM** pattern. Dependencies are managed manually via the `di` (Dependency Injection) package using `AiContainer`.

```
feature-ai/
├── src/
│   └── main/
│       ├── java/com/novachat/feature/ai/
│       │   ├── di/                # Dependency Injection (AiContainer)
│       │   ├── data/              # Data Layer: Repository implementations and data sources
│       │   ├── domain/            # Domain Layer: Core models, Repository interfaces, and Use Cases
│       │   ├── presentation/      # Presentation Layer: UI State models, UI Events, and ViewModels
│       │   │   └── viewmodel/     # ViewModels (e.g., ChatViewModel.kt, SettingsViewModel.kt)
│       │   ├── ui/                # Compose UI screens and theming
│       │   └── ui/preview/         # Compose previews and preview data
│       ├── res/                   # Resources
│       └── AndroidManifest.xml
├── build.gradle.kts

app/
├── src/
│   └── main/
│       ├── java/com/novachat/app/
│       │   ├── MainActivity.kt
│       │   └── NovaChatApplication.kt
│       ├── res/
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
- View app information
- Manage theme mode and dynamic color preferences

### AI Modes

#### Online Mode (Firebase Functions Proxy)

- Uses Firebase Cloud Functions proxy to access Gemini 2.5 Flash model
- Requires internet connection
- No API key required - Firebase handles authentication server-side
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

- Offline mode is currently disabled because AICore is not yet available on Maven
- Use Online mode instead

### "Authentication required" error

- Check that Anonymous Authentication is enabled in Firebase Console
- Verify Firebase project is properly configured
- Check app logs for sign-in errors

### Build errors

- Ensure you have JDK 21 installed
- Run `./gradlew clean` and rebuild
- Check that Android SDK 35 (target) and 36 (compile) are installed

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Acknowledgments

- Firebase Functions
- Gemini 2.5 Flash model
- Google AICore (planned)
- Jetpack Compose team
- Android community

---

Built with ❤️ for novice developers learning Android AI integration

**Current Status**: This repository includes the app code and the multi-agent development system configuration.

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
Create a settings screen with Material 3 components for AI mode and theme preferences
```

**Implementing ViewModels & Repositories:**

```
@copilot using backend-agent.agent.md
Implement ChatViewModel with Firebase Functions proxy integration and error handling
```

**Adding Tests:**

```
@copilot using testing-agent.agent.md
Add Compose UI tests for ChatScreen and unit tests for ChatViewModel
```

**Configuring Dependencies:**

```
@copilot using build-agent.agent.md
Add dependencies for DataStore Preferences and update the version catalog
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
- 🔒 **Security** - secure storage patterns, network security, secret handling

## Project Structure

This repository includes both the app implementation and the multi-agent system configuration in `.github/`:

- `copilot-instructions.md` - General NovaChat development guidelines
- `agents/` - Specialized agent configurations
- `skills/` - Reusable knowledge and patterns
- `AGENTS.md` - Complete multi-agent system documentation

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
