````skill
---
name: backend-patterns
description: Complete backend patterns for NovaChat ViewModels, UseCase, Repository, and error handling (NO placeholders)
category: backend
applies_to:
  - "**/*ViewModel.kt"
  - "**/usecase/**/*.kt"
  - "**/repository/**/*.kt"
  - "**/model/*.kt"
protocol_compliance: true
note: All examples are COMPLETE and runnable - following DEVELOPMENT_PROTOCOL.md zero-elision policy
---

# Backend Patterns Skill for NovaChat

This skill provides **COMPLETE** backend implementation patterns for NovaChat. All code examples are fully implemented with no placeholders.

> **PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - Complete ViewModel implementations (no `// ... onEvent logic` placeholders)
> - Complete UseCase implementations (not just interfaces)
> - Complete error handling with Result<T> and fold()
> - All coroutine scopes properly defined
> - All imports explicitly included
> - SavedStateHandle usage for UI state

## Multi-Agent Coordination

### When the Backend Agent Should Use Tools

**Use tools immediately for:**
- Reading existing ViewModels to understand patterns → `read_file`
- Creating new ViewModel files → `create_file`
- Creating UseCase implementations → `create_file`
- Creating Repository implementations → `create_file`
- Modifying AiContainer for DI → `apply_patch`
- Searching for patterns in codebase → `grep_search` or `semantic_search`
- Checking repo state or recent changes → **GitKraken MCP** (git_status, git_log_or_diff)
- Finding older ViewModel/repository implementations from other IDEs → **Pieces MCP** (ask_pieces_ltm)

**Do NOT describe; DO implement:**
- Don't say "create a ViewModel"; create it using `create_file`
- Don't say "add error handling"; implement the complete try-catch/Result<T>
- Don't say "wire into DI"; update AiContainer using `apply_patch`

### When to Hand Off to Other Agents

**Hand off to UI Agent if:**
- ViewModel state needs Composable to observe it
- UI event handling needs Composable implementation
- Screen layout needs creation for the ViewModel
- → **Action**: Provide ViewModel interface and state definition

**Hand off to Testing Agent if:**
- ViewModel unit tests needed
- UseCase tests need implementation
- Repository tests needed
- → **Action**: Provide complete, testable ViewModel/UseCase/Repository

**Hand off to Build Agent if:**
- New dependencies needed (Retrofit, Room, DataStore, etc.)
- Build configuration affects backend code
- → **Action**: Report missing dependencies

---

## ViewModel Pattern (Complete)

### Basic Structure with StateFlow

Rules:

- Define `UiState`, `UiEvent`, and `UiEffect` as sealed interfaces.
- Expose `StateFlow` for state and `Channel`/`Flow` for effects.
- Use a single `onEvent` dispatcher and keep handlers private.
- Use UseCases for business logic; ViewModel coordinates state changes.
- Store draft UI state in `SavedStateHandle`.

---

## UseCase Pattern (Complete)

### Single Responsibility UseCase

Rules:

- UseCases do one job and live in `domain/usecase/`.
- Inject repositories and dispatcher (default `Dispatchers.IO`).
- Validate input early; return `Result.failure` for invalid input.
- Use repositories for data access and return `Result<T>`.
- Log failures with context before returning `Result.failure`.

---

## Repository Pattern (Complete)

### Interface Definition

Rules:

- Repository interfaces live in `domain/repository/`.
- Use `Result<T>` for operations that can fail.
- Keep domain repositories free of Android imports.

### Repository Implementation

Rules:

- Implement repositories in `data/repository/`.
- Wrap data source calls in `try/catch` and return `Result<T>`.
- Map online/offline behavior explicitly in AI repository.
- Use DataStore/Preferences repositories for persisted config.

### Firebase Functions Proxy Pattern (AiRepository)

Rules:

- **MUST use Firebase Functions callable** - Never call Gemini API directly from Android app.
- Get instance: `FirebaseFunctions.getInstance("us-central1")` (KTX extensions deprecated in BOM v34.0.0+)
- Use `functions.getHttpsCallable("aiProxy")` to get the callable function.
- Function name: `aiProxy` (deployed at us-central1 region).
- Check `auth.currentUser` before making function calls - authentication is required.
- Handle FirebaseFunctionsException with proper error code mapping:
  - UNAUTHENTICATED → SecurityException with user-friendly message
  - PERMISSION_DENIED → Non-recoverable error
  - INVALID_ARGUMENT → IllegalArgumentException
  - INTERNAL/UNAVAILABLE → Recoverable IOException
- Request format: Map with `message` (String) and `modelParameters` (Map).
- Response format: Extract `response` (String) from result data.
- Always validate response is not null/blank before returning success.
- See `AiRepositoryImpl.generateOnlineResponse()` for reference implementation.

---

## Error Handling Pattern

### Using Result<T> with fold()

Rules:

- Handle `Result<T>` with `fold()` for success/failure paths.
- Never ignore a `Result` return value.
- Avoid `if`/`else` type checks for `Result`.

### Try-Catch Pattern in UseCases

Rules:

- UseCases wrap work in `try/catch` and return `Result<T>`.
- Handle expected exception types with clear messages.
- Log unexpected errors and return `Result.failure`.

---

## State Management Best Practices

### ✅ DO: Atomic Updates with update()

Rules:

- Use `StateFlow.update {}` for atomic updates.
- Avoid intermediate inconsistent state changes.

### ❌ DON'T: Multiple Updates

Rule:

- Do not perform multiple sequential `_uiState.value = ...` assignments for a single change.

### ✅ DO: Save Draft Messages in SavedStateHandle

Rules:

- Use `SavedStateHandle` for non‑sensitive UI state (drafts, filters).
- Keep keys in `companion object` constants.

### ❌ DON'T: Store Sensitive Data in SavedStateHandle

Rules:

- Never store passwords, tokens, or secrets in `SavedStateHandle`.
- Use encrypted storage (DataStore/EncryptedSharedPreferences) for sensitive data.

---

## Protocol Compliance Checklist

Before submitting backend code, verify:

- [ ] **Complete ViewModel** - All onEvent branches handled, no `// ... logic` placeholders
- [ ] **Complete UseCase** - Full invoke() implementation with error handling
- [ ] **Complete Repository** - All interface methods implemented
- [ ] **All imports included** - No missing imports for coroutines, Flow, Result, etc.
- [ ] **Error handling complete** - try-catch, Result<T>, fold() all implemented
- [ ] **Coroutines scoped** - All launches in viewModelScope
- [ ] **SavedStateHandle used** - Draft messages saved (configuration change safe)
- [ ] **Logging included** - Error cases logged with context
- [ ] **No concurrent state updates** - use update() not direct assignment

**Remember: DEVELOPMENT_PROTOCOL.md prohibits placeholder code in ALL files, including backend!**

---

**End of Backend Patterns Skill**

````
