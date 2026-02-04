# NovaChat

An Android chat application built with modern Android development practices.

## 🤖 Multi-Agent Development System

This repository uses a specialized multi-agent system with GitHub Copilot to maintain code quality and prevent agent drift. See [.github/AGENTS.md](.github/AGENTS.md) for detailed documentation.

### Quick Start with Agents

**Planning a Feature:**
```
@copilot using planner.agent.md
Plan implementation for [feature description]
```

**Implementing UI:**
```
@copilot using ui-agent.agent.md
Create [screen/component] with [requirements]
```

**Implementing Business Logic:**
```
@copilot using backend-agent.agent.md
Implement [ViewModel/Repository] for [feature]
```

**Adding Tests:**
```
@copilot using testing-agent.agent.md
Add tests for [component]
```

**Configuring Build:**
```
@copilot using build-agent.agent.md
Add dependencies for [feature]
```

**Reviewing Code:**
```
@copilot using reviewer-agent.agent.md
Review [feature/component] implementation
```

### Available Agents

- 🎯 **Planner** - Task breakdown and architecture planning
- 🎨 **UI Agent** - Android UI implementation (Material Design 3)
- ⚙️ **Backend Agent** - Business logic and data layer
- 🧪 **Testing Agent** - Unit and instrumentation tests
- 🔧 **Build Agent** - Gradle and dependency management
- 👁️ **Reviewer Agent** - Code quality and security review

### Reusable Skills

- 📱 **Android Testing** - Test patterns and best practices
- 🎨 **Material Design 3** - UI component guidelines
- 🔒 **Security** - Security best practices and checks

## Development

See [.github/copilot-instructions.md](.github/copilot-instructions.md) for Android development guidelines and build instructions.

## License

See [LICENSE](LICENSE) file for details.