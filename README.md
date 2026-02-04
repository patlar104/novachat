# NovaChat

An Android AI chatbot application built with Jetpack Compose, supporting both online (Google Gemini) and offline (on-device AICore) AI modes.

## 🤖 About NovaChat

NovaChat is a modern Android AI chatbot that demonstrates:
- **Jetpack Compose** with Material Design 3
- **Dual AI Integration**: Google Gemini API (online) and AICore (offline)
- **MVVM + Clean Architecture** with proper separation of concerns
- **Modern Android Development**: Kotlin 2.3.0, Coroutines, StateFlow, DataStore

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