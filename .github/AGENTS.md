# Multi-Agent System for NovaChat

This repository uses a specialized multi-agent system with GitHub Copilot to prevent agent drift and maintain code quality across the Android project.

> **⚠️ MANDATORY PROTOCOL**: All agents MUST follow [DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md)
> 
> **Key Requirements for ALL Agents:**
> - ✅ **Zero-Elision Policy**: Never use placeholders (`// ... code`)
> - ✅ **Complete Implementations**: Write full, working code only
> - ✅ **Self-Validation**: Check completeness, imports, syntax before output
> - ✅ **Input Disambiguation**: Ask for clarification when requests are ambiguous
> - ✅ **Cross-File Dependencies**: Analyze ripple effects before changes
> - ✅ **Atomic Processing**: One complete file at a time
> - ✅ **2026 Standards**: Kotlin 2.3.0, AGP 9.0.0, Compose BOM 2026.01.01

## Agent Overview

We have seven specialized agents, each with specific responsibilities and constraints:

### 1. 🎯 Planner Agent (`planner.agent.md`)
**Role**: Analyzes requirements and creates implementation plans

**Responsibilities**:
- Break down features into actionable tasks
- Define architecture and component structure
- Create test strategies
- Set clear acceptance criteria

**Constraints**:
- Never implements code directly
- Only creates plans and specifications
- Always uses markdown checklists

**Handoffs**: Routes tasks to UI, Backend, or Build agents

---

### 2. 🎨 UI Agent (`ui-agent.agent.md`)
**Role**: Implements Jetpack Compose user interfaces

**Scope**:
- Jetpack Compose UI (Composables)
- Material Design 3 components
- Theme configuration
- Resource files (strings only)
- UI state management

**Constraints**:
- ONLY modifies UI-related files
- Never implements business logic
- All UI in Compose (no XML layouts)
- All strings must be in resources
- **MUST provide complete Composable implementations**

**Protocol Requirements**:
- ✅ Complete `@Composable` functions (no `// ... UI implementation` placeholders)
- ✅ All imports explicitly included
- ✅ Full theme integration shown
- ✅ Check existing Composables before creating new ones

**Handoffs**: To Backend (for ViewModel integration), Testing (for UI tests), or Reviewer

---

### 3. 🎬 Preview Agent (`preview-agent.agent.md`)
**Role**: Creates comprehensive @Preview annotations and preview composables for IDE debugging

**Scope**:
- @Preview annotations on Composables
- Preview composition files (*ScreenPreview.kt)
- Preview data providers (Preview*ScreenData)
- Preview utilities and device specifications
- Multi-state/device/theme preview composition

**Constraints**:
- ONLY creates preview code (for IDE debugging, not production)
- Never calls production repositories
- No side effects in preview Composables
- Previews are driven by parameterized UI composables (no ViewModel usage)
- **MUST provide complete preview implementations**
- Lightweight theme variants for fast IDE compilation

**Protocol Requirements**:
- ✅ Complete @Preview functions (all states shown)
- ✅ Multiple device variants for each preview
- ✅ Light and dark theme previews
- ✅ Mock data only (no API calls)
- ✅ All imports explicitly included
- ✅ Preview data helpers (Preview*ScreenData) for state coverage

**Handoffs**: From UI (for new Composables) and Backend (for state changes), to Testing (for automated tests)

---

### 4. ⚙️ Backend Agent (`backend-agent.agent.md`)
**Role**: Implements business logic and data layer

**Scope**:
- ViewModels with StateFlow
- Repositories (AI, Preferences, Data)
- Data sources (DataStore, Gemini API, AICore)
- Use cases
- Dependency injection (AppContainer)

**Constraints**:
- ONLY modifies backend/logic files
- Never modifies UI files
- ViewModels must not have UI references
- All logic must be unit testable
- **MUST provide complete implementations**

**Protocol Requirements**:
- ✅ Complete ViewModels with all state handling
- ✅ Complete Repository implementations (no `// ... implement` placeholders)
- ✅ All coroutine scopes and error handling included
- ✅ Verify existing implementations before adding new ones

**Handoffs**: To UI (for integration), Testing (for unit tests), or Build (for dependencies)

---

### 5. 🧪 Testing Agent (`testing-agent.agent.md`)
**Role**: Writes comprehensive tests

**Scope**:
- Unit tests (ViewModels, repositories) with coroutines
- Compose UI tests (not Espresso)
- Test utilities and helpers

**Constraints**:
- ONLY creates or modifies test files
- Never modifies production code
- If tests fail, reports issues and hands off to appropriate agent
- **MUST provide complete test implementations**

**Protocol Requirements**:
- ✅ Complete test functions (no `// ... test implementation` placeholders)
- ✅ All test setup and teardown included
- ✅ MockK setup fully shown
- ✅ ComposeTestRule usage complete
- ✅ Check for existing tests before creating duplicates

**Handoffs**: To Backend or UI (for bug fixes), Reviewer (for coverage review)

---

### 6. 🔧 Build Agent (`build-agent.agent.md`)
**Role**: Manages build configuration and dependencies

**Scope**:
- Gradle build files (Kotlin DSL)
- Dependency management (Compose BOM, AI SDKs)
- Version catalogs (libs.versions.toml)
- ProGuard/R8 rules
- Build variants

**Constraints**:
- ONLY modifies build configuration files
- Never modifies application code
- Must check dependencies for security vulnerabilities
- No secrets in build files
- **MUST provide complete build configurations**

**Protocol Requirements**:
- ✅ Complete build.gradle.kts files (no `// ... dependencies` placeholders)
- ✅ All plugin configurations shown
- ✅ Version catalog entries complete
- ✅ Verify 2026 dependency versions (Compose BOM 2026.01.01, Kotlin 2.3.0)

**Handoffs**: To Backend (after adding dependencies), Testing (for test setup), or Reviewer

---

### 7. 👁️ Reviewer Agent (`reviewer-agent.agent.md`)
**Role**: Reviews code quality and security

**Responsibilities**:
- Code quality review
- Architecture compliance (MVVM + Clean Architecture)
- Security auditing
- Accessibility checking
- Performance analysis
- Test coverage review
- **DEVELOPMENT_PROTOCOL.md compliance**

**Constraints**:
- ONLY reviews - never implements fixes
- Must categorize issues by severity
- Provides specific, actionable feedback
- **MUST check for protocol violations**

**Protocol Requirements**:
- ✅ Verify zero-elision policy compliance (no placeholders in code)
- ✅ Check complete implementations
- ✅ Verify all imports present
- ✅ Check syntax correctness
- ✅ Validate 2026 standards usage
- ✅ Identify cross-file dependency issues

**Handoffs**: Routes issues to appropriate agents for fixes

---

## Reusable Skills

Skills are shared knowledge that agents can reference. **All skills MUST follow DEVELOPMENT_PROTOCOL.md** (no placeholder code).

### 📱 Android Testing Skill
Location: `.github/skills/android-testing/`

Provides:
- ViewModel unit testing with coroutines (complete examples)
- Compose UI testing with ComposeTestRule
- MockK best practices (full setup shown)
- Test organization (AAA pattern with complete tests)

**Protocol**: All test examples are complete and runnable

### 🎨 Material Design 3 Skill
Location: `.github/skills/material-design/`

Provides:
- Material Design 3 Compose components (complete implementations)
- Theme configuration (full theme files)
- Layout best practices (complete Composables)
- Accessibility guidelines

**Protocol**: All Compose examples are complete and functional

### 🔒 Security Best Practices Skill
Location: `.github/skills/security-check/`

Provides:
- Secure data storage patterns (complete DataStore implementations)
- Network security configuration (full XML configs)
- Input validation (complete validation functions)
- Permission handling (complete runtime permission code)
- Security checklist

**Protocol**: All security examples are complete implementations

---

## How to Use the Multi-Agent System

### Starting a New Feature

1. **Assign to Planner Agent**
   ```
   @copilot using planner.agent.md
   
   Create a plan for implementing [feature description]
   ```

2. **Planner creates detailed plan** with tasks assigned to specific agents

3. **Execute tasks** by invoking the assigned agents:
   ```
   @copilot using ui-agent.agent.md
   
   Implement the login screen layout according to the plan
   ```

4. **Agent completes work** and suggests handoff to next agent

5. **Continue the chain** until all tasks are complete

6. **Final review**:
   ```
   @copilot using reviewer-agent.agent.md
   
   Review the login feature implementation
   ```

### Example Workflow

```mermaid
graph TD
    A[User Request] --> B[Planner Agent]
    B --> C[Create Implementation Plan]
    C --> D{Task Type?}
    
    D -->|UI Task| E[UI Agent]
    D -->|Logic Task| F[Backend Agent]
    D -->|Dependencies| G[Build Agent]
    
    E --> H[Preview Agent]
    H --> I[Testing Agent]
    F --> I
    G --> I
    
    I --> J[Reviewer Agent]
    J --> K{Issues Found?}
    
    K -->|Yes| L[Route to Appropriate Agent]
    K -->|No| M[Complete]
    
    L --> J
```

## Anti-Drift Mechanisms

### 1. **Strict Scope Enforcement**
Each agent has explicit file scope constraints. Agents will refuse to work on files outside their scope.

### 2. **Handoff Protocols**
Agents clearly specify when to hand off to another agent, preventing scope creep.

### 3. **Boundary Checks**
Built-in checks prevent common mistakes:
- UI Agent refuses business logic
- Backend Agent refuses UI modifications
- Testing Agent never modifies production code

### 4. **Skills as Reference**
Shared skills provide consistent patterns, reducing variation in implementation.

### 5. **Reviewer Oversight**
Reviewer Agent catches drift before code is merged.

## Best Practices

### ✅ Do's

- Always start with Planner Agent for new features
- Use the appropriate specialized agent for each task
- Reference skills when implementing patterns
- Run Reviewer Agent before finalizing
- Follow handoff recommendations

### ❌ Don'ts

- Don't ask UI Agent to implement ViewModels
- Don't ask Backend Agent to create layouts
- Don't skip the planning phase
- Don't ignore handoff suggestions
- Don't bypass the review process

## Agent Invocation Examples

### Planning
```
@copilot using planner.agent.md

Plan implementation for:
- User authentication with email/password
- Remember me functionality
- Password reset flow
```

### UI Implementation
```
@copilot using ui-agent.agent.md

Create the login screen with:
- Email input field
- Password input field (masked)
- Login button
- Forgot password link
```

### Backend Implementation
```
@copilot using backend-agent.agent.md

Implement LoginViewModel with:
- Email and password validation
- Authentication state management
- Error handling
```

### Testing
```
@copilot using testing-agent.agent.md

Create tests for LoginViewModel covering:
- Successful login
- Invalid credentials
- Network errors
```

### Build Configuration
```
@copilot using build-agent.agent.md

Add dependencies for:
- Retrofit for networking
- Room for local storage
- Hilt for dependency injection
```

### Code Review
```
@copilot using reviewer-agent.agent.md

Review the authentication feature for:
- Security vulnerabilities
- Architecture compliance
- Test coverage
```

## Troubleshooting

### Agent Not Following Constraints?

1. Re-invoke with explicit scope reminder
2. Reference the agent file directly
3. Use Reviewer Agent to catch violations

### Confused About Which Agent to Use?

1. Start with Planner Agent
2. Let Planner recommend the right agent
3. Follow handoff suggestions

### Tests Failing After Implementation?

1. Invoke Testing Agent to analyze failures
2. Testing Agent will hand off to appropriate agent
3. Fix and re-test

## Directory Structure

```
.github/
├── agents/
│   ├── planner.agent.md
│   ├── ui-agent.agent.md
│   ├── preview-agent.agent.md
│   ├── backend-agent.agent.md
│   ├── testing-agent.agent.md
│   ├── build-agent.agent.md
│   └── reviewer-agent.agent.md
├── skills/
│   ├── android-testing/
│   │   └── SKILL.md
│   ├── material-design/
│   │   └── SKILL.md
│   ├── compose-preview/
│   │   └── SKILL.md
│   └── security-check/
│       └── SKILL.md
└── copilot-instructions.md
```

## Contributing

When contributing to this project:

1. **Read DEVELOPMENT_PROTOCOL.md FIRST** - Mandatory for all development
2. **Use the agent system** - don't bypass it
3. **Follow handoff protocols** - respect agent boundaries
4. **Update skills** - if you find better patterns
5. **Review before merge** - always use Reviewer Agent

### Protocol Compliance

All contributions MUST comply with [DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md):

- ✅ **No placeholders**: Complete implementations only
- ✅ **Self-validation**: Check completeness, imports, syntax
- ✅ **Input disambiguation**: Ask when requests are unclear
- ✅ **Cross-file analysis**: Identify and update dependencies
- ✅ **Atomic processing**: One complete file at a time
- ✅ **2026 standards**: Latest Kotlin, AGP, Compose versions

### Enforcement

**All agents automatically enforce protocol compliance.** Violations will be rejected:

❌ Code with `// ... rest of implementation` → **REJECTED**
❌ Missing imports → **REJECTED**
❌ Incomplete implementations → **REJECTED**
❌ Outdated dependencies (pre-2026) → **REJECTED**

## Additional Resources

- **[DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md)** - **MANDATORY READING**
- [GitHub Copilot Agent Documentation](https://docs.github.com/en/copilot/how-tos/use-copilot-agents)
- [Android Developer Guide](https://developer.android.com)
- [Material Design 3](https://m3.material.io)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security-testing-guide/)
