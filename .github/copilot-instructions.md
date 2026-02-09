# NovaChat - Copilot Instructions

NovaChat is a modern Android AI chatbot built with **Jetpack Compose + MVVM + Clean Architecture**. It demonstrates 2026 Android best practices with dual AI mode support (online Gemini API + planned offline AICore).

> **CRITICAL**: All development MUST follow [DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md):
>
> - **Zero-Elision Policy**: Never use placeholders (`// ... rest of code`)
> - **Complete Implementations**: Write full, working code only
> - **Input Disambiguation**: Ask when requests are ambiguous
> - **Cross-File Dependencies**: Analyze ripple effects before changes
> - **Self-Validation**: Check completeness, imports, syntax before output

## Quick Facts

- **Versions**: Kotlin 2.2.21, AGP 9.0.0, Gradle 9.1.0, Compose BOM 2026.01.01
- **SDK**: Target 35, Compile 36, Min 28 (Android 9+)
- **Architecture**: MVVM + Clean Architecture (presentation/domain/data layers)
- **DI**: Manual `AppContainer` (no Hilt/Koin) - lazy singletons pattern
- **State**: Sealed interfaces + StateFlow (persistent) + Channel (one-time effects)
- **AI Mode**: ONLINE only (Gemini 1.5 Flash) - AICore commented out in [build.gradle.kts](../app/build.gradle.kts)
- **Compose BOM source**: Google Maven only; mapping at [BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping)

## Multi-Agent System

This project uses specialized agents with clear boundaries. See [AGENTS.md](AGENTS.md) for complete documentation.

| Agent | Scope | Key Files |
| --- | --- | --- |
| **Planner** | Task breakdown, architecture planning | Creates implementation plans |
| **UI** | Composables, Material 3, layouts | `ui/**/*.kt`, `ui/theme/*.kt` |
| **Preview** | @Preview annotations, preview data | `ui/preview/*.kt` (no ViewModels) |
| **Backend** | ViewModels, use cases, repositories, DI | `presentation/viewmodel/*.kt`, `domain/**`, `data/**`, `di/*.kt` |
| **Testing** | Unit tests, Compose UI tests | `**/*Test.kt` (test dirs only) |
| **Build** | Gradle, dependencies, manifest | `build.gradle.kts`, `AndroidManifest.xml` |
| **Reviewer** | Code quality, security, architecture | Reviews all layers |

**Critical Rule**: Agents NEVER work outside their scope. Hand off when boundaries are crossed.

---

## Core Architecture Patterns

### 1. State & Event-Driven UI (Sealed Interfaces)

NovaChat uses sealed interfaces for type-safe state management. See [presentation/model/UiState.kt](../app/src/main/java/com/novachat/app/presentation/model/UiState.kt) for complete implementation.

Instructions:

- Define `UiState`, `UiEvent`, and `UiEffect` as sealed interfaces under `presentation/model/`.
- Expose `StateFlow` for persistent state and `Channel`/`Flow` for one‑time effects in `presentation/viewmodel/`.
- Use a single `onEvent(event: UiEvent)` entry point for ViewModels.
- In Composables, collect state with `collectAsStateWithLifecycle()` and effects with `LaunchedEffect(Unit)` using an exhaustive `when` on state.

**Key Pattern**: StateFlow for persistent state, Channel for one-time effects. Always use `LaunchedEffect(Unit)` for effect collection.

### 2. Clean Architecture Layers

Instructions:

- Keep ViewModels and UI contracts in [`presentation/`](../app/src/main/java/com/novachat/app/presentation/).
- Keep business logic in [`domain/`](../app/src/main/java/com/novachat/app/domain/) with no Android imports.
- Keep data implementations in [`data/`](../app/src/main/java/com/novachat/app/data/) and use mappers for model conversion.
- Keep Compose screens in [`ui/`](../app/src/main/java/com/novachat/app/ui/) and themes in [`ui/theme/`](../app/src/main/java/com/novachat/app/ui/theme/).
- Keep DI wiring in [`di/AppContainer.kt`](../app/src/main/java/com/novachat/app/di/AppContainer.kt).

**Critical**: ViewModels never import from `ui/` package (testable without Android UI).

### 3. Manual Dependency Injection

See [di/AppContainer.kt](../app/src/main/java/com/novachat/app/di/AppContainer.kt) - lightweight pattern without Hilt/Koin.

Instructions:

- Initialize repositories and use cases in `AppContainer`, using lazy singletons where appropriate.
- Create ViewModels via `ViewModelFactory` using `LocalContext.current.appContainer` in Composables.

### 4. Error Handling with `Result<T>`

All async operations return `Result<T>`. Use `.fold()` for handling:

Instructions:

- Wrap repository calls in `try/catch` and return `Result.success` or `Result.failure`.
- In ViewModels, handle `Result` with `fold()` and update state/effects accordingly.

---

## Development Commands

Instructions:

- Build debug APK: `./gradlew assembleDebug`
- Build and install to device: `./gradlew installDebug`
- Run unit tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Clean build: `./gradlew clean build`
- Run lint: `./gradlew lint`

---

## Project Structure

Instructions:

- Presentation layer: [`presentation/`](../app/src/main/java/com/novachat/app/presentation/) for `viewmodel/` and `model/`.
- Domain layer: [`domain/`](../app/src/main/java/com/novachat/app/domain/) for `usecase/`, `model/`, and `repository/` interfaces.
- Data layer: [`data/`](../app/src/main/java/com/novachat/app/data/) for `repository/`, `mapper/`, and `model/`.
- UI layer: [`ui/`](../app/src/main/java/com/novachat/app/ui/) for screens, `preview/`, and `theme/`.
- DI: [`di/AppContainer.kt`](../app/src/main/java/com/novachat/app/di/AppContainer.kt).
- App entry: [`MainActivity.kt`](../app/src/main/java/com/novachat/app/MainActivity.kt) and [`NovaChatApplication.kt`](../app/src/main/java/com/novachat/app/NovaChatApplication.kt).

**Key References**:

- [ChatViewModel.kt](../app/src/main/java/com/novachat/app/presentation/viewmodel/ChatViewModel.kt) - Complete ViewModel pattern
- [UiState.kt](../app/src/main/java/com/novachat/app/presentation/model/UiState.kt) - State/Event/Effect definitions
- [AppContainer.kt](../app/src/main/java/com/novachat/app/di/AppContainer.kt) - DI wiring pattern
- [ChatScreen.kt](../app/src/main/java/com/novachat/app/ui/ChatScreen.kt) - Compose UI patterns

---

## NovaChat-Specific Patterns

### ViewModel Event Handling Pattern

All ViewModels use a single `onEvent(event: UiEvent)` entry point:

Instructions:

- Route all UI actions through `onEvent(event: UiEvent)` with an exhaustive `when`.
- Use `fold()` on use case results to update state and emit effects.

### UI State Transitions Pattern

Instructions:

- Use `_uiState.update {}` for atomic state transitions.
- Return the existing state for branches that do not change.

### Key File Patterns

1. **ChatViewModel** - Copy pattern for: event handling, state updates, effect emission
2. **ChatScreen** - Copy pattern for: state collection, effect handling, Compose layout
3. **AppContainer** - Copy pattern for: use case wiring, lazy-loaded singletons
4. **UiState.kt** - Copy pattern for: sealed interfaces, helper methods, exhaustive handling

### Common Imports to Include

Instructions:

- Include StateFlow, Channel, and `receiveAsFlow` for state/effects.
- Use `ViewModel`, `viewModelScope`, and a `CoroutineDispatcher` where needed.
- Collect state in Composables via `collectAsStateWithLifecycle`.

---

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
- [ ] **Standards**: Uses 2026 best practices (Kotlin 2.2.21, Compose BOM 2026.01.01)
- [ ] **Dependencies**: Cross-file impacts analyzed and addressed
- [ ] **Architecture**: Follows MVVM + Clean Architecture patterns

### Web Content Verification

- **Use Cursor Browser** (not fetch) when verifying external docs, BOM mapping, AGP release notes, or security references. See [cursor-browser skill](skills/cursor-browser/SKILL.md).

### Git Context

- **Use GitKraken MCP** for git_status, git_log_or_diff, git_blame, PR details. See [gitkraken-mcp skill](skills/gitkraken-mcp/SKILL.md).

### Long-Term Memory (Older Edits)

- **Use Pieces MCP** (`ask_pieces_ltm`) when edits or context might exist from other IDEs or past sessions. See [pieces-mcp skill](skills/pieces-mcp/SKILL.md).

### Verification & Duplication Guard

- **No guessing**: Read the current file(s) before editing; never infer content.
- **Find duplicates**: If adding sections, confirm the topic does not already exist elsewhere in the file.
- **Edits may be wrong**: After edits/searches, re-read the target area to confirm correctness.
- **If missing**: When you cannot find something, assume it may be in a different file or location and broaden the search (do not invent).

### Input Handling Protocol

When receiving a request:

1. **Check Current State**: Use `grep_search` or `semantic_search` to find existing implementations
2. **Identify Duplicates**: If feature exists, clarify: "This exists in [File]. Modify or create new?"
3. **Plan Dependencies**: List all files that will be created/modified
4. **Implement Atomically**: One complete file at a time
5. **Verify Imports**: Ensure all cross-layer dependencies are correct

### Prohibited Practices

❌ **NEVER** use these patterns:
Instructions:

- Do not use placeholders such as `// ...` or `TODO: implement`.
- Do not defer missing logic or imports.

✅ **ALWAYS** write complete implementations:
Instructions:

- Provide fully implemented functions with all required imports.
- Ensure all braces and parentheses are balanced.

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

---

## Key Development Conventions

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

---

## Common Anti-Patterns (What NOT to Do)

These patterns violate NovaChat's architecture and will cause problems:

### ❌ UI Layer Anti-Patterns

1. **Calling ViewModels from Composables directly**
   Instructions:
   - Do not instantiate ViewModels directly in Composables.
   - Use `viewModel<...>(factory = viewModelFactory)` so DI remains intact.
   Why: Violates DI, makes testing impossible

2. **Storing UI state in Composables**
   Instructions:
   - Keep UI state in the ViewModel and expose it via `StateFlow`.
   - Collect state in Composables with `collectAsStateWithLifecycle()`.
   Why: Lost on configuration changes; ViewModels handle this

3. **Using LaunchedEffect with State parameters**
   Instructions:
   - Use `LaunchedEffect(Unit)` for one‑time effect collection.
   - Avoid using mutable state as the LaunchedEffect key for effects.
   Why: Causes repeated executions; use Unit or stable keys

4. **Emit state AND effect for same action**
   Instructions:
   - Use state for persistent UI and effects for one‑time actions.
   - Do not emit both state and effect for the same UI action.

### ❌ Backend Layer Anti-Patterns

1. **Not using `Result<T>` for error handling**
   Instructions:
   - Return `Result<T>` from async operations.
   - Wrap failures in `Result.failure` rather than throwing.
   Why: Uncaught exceptions crash; `Result<T>` makes errors explicit

2. **Silent error handling without logging**
   Instructions:
   - Never swallow exceptions silently.
   - Log failures with context and emit a safe fallback value.
   Why: Impossible to debug silent failures

3. **Use cases calling other use cases**
   Instructions:
   - Keep UseCases independent; do not chain UseCases.
   - Use repositories as the dependency boundary for UseCases.
   Why: Violates single responsibility; use case should not depend on other use cases

4. **Storing Critical Data in SavedStateHandle**
   Instructions:
   - Use `SavedStateHandle` only for non‑sensitive UI drafts.
   - Persist secrets and config via repositories (DataStore/Encrypted prefs).
   Why: SavedStateHandle ≠ persistent storage; only use for draft messages

5. **Changing AI mode without validation**
   Instructions:
   - Validate AI mode availability before applying changes.
   - Emit a user‑visible effect when a mode is unavailable.
   Why: OFFLINE mode is unavailable; silent failures confuse users

### ❌ Testing Anti-Patterns

1. **Testing sealed interface instantiation**
   Instructions:
   - Test ViewModel behavior that produces the state rather than instantiating it directly.
   - Assert on emitted state values from the ViewModel.
   Why: Sealed interfaces are implementation; test through behavior

2. **Using real repositories in unit tests**
   Instructions:
   - Use mocks/fakes for repositories and use cases in unit tests.
   - Avoid real dependencies in ViewModel unit tests.
   Why: Unit tests should isolate the component; use fakes/mocks for dependencies

3. **Not handling cancellation in tests**
   Instructions:
   - Await asynchronous state changes in coroutine tests.
   - Use Turbine or equivalent to collect and assert state updates.
   Why: Coroutines are async; tests must wait for state updates

### ❌ Multi-Agent Anti-Patterns

1. **UI Agent modifying repositories**
   Instructions:
   - UI agent must call ViewModel events, not repositories.
   - Repository access belongs to Backend agent scope.
   Why: Violates layer separation and agent scope

2. **Backend Agent implementing Compose UI logic**
   Instructions:
   - Backend agent exposes state only; UI decides rendering.
   - Avoid embedding UI decisions in ViewModels.
   Why: ViewModels are testable without UI; don't leak UI concerns

3. **Testing Agent modifying production code**
   Instructions:
   - Testing agent edits test files only.
   - Hand off production changes to the responsible agent.
   Why: Testing Agent scope is tests only; maintains isolation

---

## Testing Patterns

### ViewModel Unit Testing
Test ViewModel behavior by triggering events and asserting state changes:
Instructions:

- Stub use cases, trigger `onEvent`, and assert state transitions.
- Use `runTest` with coroutine test utilities.

### Use Case Testing
Test use case logic with mocked repositories:
Instructions:

- Mock repositories, execute the UseCase, and verify repository calls.
- Assert success/failure via `Result`.

### Compose UI Testing
Test critical UI paths with ComposeTestRule:
Instructions:

- Use `createComposeRule()` to render UI and assert key text/behaviors.
- Provide a mocked ViewModel state for UI tests.

### Common Test Dependencies
Instructions:

- Add unit test deps in `build.gradle.kts`: JUnit, MockK, Coroutines Test, Turbine, Kotest assertions.
- Add Compose UI test deps under `androidTestImplementation` using the Compose BOM.

---


## References

- **[DEVELOPMENT_PROTOCOL.md](DEVELOPMENT_PROTOCOL.md)**: Comprehensive development guidelines (MANDATORY)
- **[AGENTS.md](AGENTS.md)**: Multi-agent system documentation
- **[HANDOFF_MATRIX.md](HANDOFF_MATRIX.md)**: Complete agent-to-agent handoff routing
- **Skills**: Reusable patterns in `.github/skills/`
  - `backend-patterns/`: ViewModel, UseCase, Repository, error handling patterns
  - `clean-architecture/`: MVVM + Clean Architecture layer separation and data flow
  - `dependency-injection/`: Manual DI pattern with AppContainer and lazy singletons
  - `android-testing/`: Unit tests, Compose UI tests, MockK patterns
  - `compose-preview/`: @Preview annotations and preview data providers
  - `material-design/`: Material 3 Compose components and theme configuration
  - `security-check/`: Security best practices and secure storage patterns
  - `cursor-browser/`: **Use Cursor Browser instead of fetch** for web content verification, browser automation, docs lookup
  - `gitkraken-mcp/`: **Git context** – git_status, git_log_or_diff, git_blame, PR/issue tools
  - `pieces-mcp/`: **Long-Term Memory** – ask_pieces_ltm to find older edits from other IDEs
