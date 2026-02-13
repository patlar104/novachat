````skill
---
name: clean-architecture
description: Complete Clean Architecture + MVVM patterns for NovaChat (NO placeholders)
category: architecture
applies_to:
  - "**/*.kt"
  - "**/build.gradle.kts"
protocol_compliance: true
note: All examples are COMPLETE and runnable - following DEVELOPMENT_PROTOCOL.md zero-elision policy
---

# Clean Architecture & MVVM Skill for NovaChat

This skill documents NovaChat's architecture layer separation, data flow, and import policies. All code examples are fully implemented with no placeholders.

> **PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - Complete layer separation with clear boundaries
> - All import policies explicitly documented
> - Full examples of data flow between layers
> - No Android imports in domain/data layers

---

## Architecture Overview

NovaChat uses **Clean Architecture** with **MVVM** for state management:

Layer map (repo paths):

- Presentation: [`ui/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui), [`presentation/viewmodel/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel), [`presentation/model/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model)
- Domain: [`domain/usecase/`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain/usecase), [`domain/repository/`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain/repository), [`domain/model/`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain/model)
- Data: [`data/repository/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/repository), [`data/model/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/model), [`data/mapper/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/mapper)
- DI: [`di/AiContainer.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/di/AiContainer.kt)

---

## Layer Responsibilities

### Presentation Layer

**Files**: `ui/`, `presentation/viewmodel/`, `presentation/model/`

**Responsibilities**:
- Display UI to user (Jetpack Compose only, NO XML)
- Observe ViewModel state
- Send user events to ViewModel
- Handle one-time effects (navigate, toast)

**Constraints**:
- ✅ Can import from Domain
- ✅ Can import from Presentation
- ❌ Cannot import from Data
- ❌ No business logic

**Example: ChatScreen observing ViewModel**:

Rules:

- Observe `uiState` with `collectAsStateWithLifecycle()` from the ViewModel.
- Handle `UiEffect` in `LaunchedEffect(Unit)`.
- Render UI with an exhaustive `when` across all `UiState` branches.
- Route user events via `viewModel.onEvent(...)`.

### Domain Layer

**Files**: `domain/usecase/`, `domain/repository/`, `domain/model/`

**Responsibilities**:
- Pure business logic (independent of Android)
- Define repository contracts (interfaces only)
- Provide use cases for specific features
- Handle domain-level errors

**Constraints**:
- ✅ Can import from Domain only
- ❌ Cannot import Android classes
- ❌ Cannot import from Data
- ❌ Cannot import from Presentation

**Example: Pure domain model**:

Rules:

- Domain models are pure Kotlin data classes with no Android imports.
- Use enums or value classes to represent fixed modes (e.g., AI mode).
- Keep domain models small and platform‑agnostic.

**Example: Domain repository interface**:

Rules:

- Repository interfaces live in `domain/repository/`.
- Use `Result<T>` for operations that can fail.
- No Android imports in domain interfaces.

**Example: UseCase (business logic)**:

Rules:

- Use cases live in `domain/usecase/` and contain business logic only.
- Validate inputs and return `Result<T>` on failure.
- Use repositories for data access; do not call data sources directly.
- Use injected dispatchers (default to `Dispatchers.IO`) for background work.

### Data Layer

**Files**: `data/repository/`, `data/model/`, `data/mapper/`, `data/source/`

**Responsibilities**:
- Implement repository interfaces from Domain
- Manage data sources (API, Database, DataStore)
- Convert between data and domain models
- Handle persistence and loading

**Constraints**:
- ✅ Can import from Domain
- ✅ Can import from Data
- ✅ Can import Android libraries
- ❌ Cannot import from Presentation

**Example: Data model (different from domain)**:

Rules:

- Data models live in `data/model/` and can use serialization annotations.
- Data models may differ from domain models to match API/storage formats.
- Keep data models Android‑agnostic unless platform APIs are required.

**Example: Mapper (Data ↔ Domain)**:

Rules:

- Mapper functions live in `data/mapper/` and translate between data and domain.
- Keep mapping logic deterministic and side‑effect free.
- Handle nulls and format conversions explicitly.

**Example: Repository implementation**:

Rules:

- Implement domain repository interfaces in `data/repository/`.
- Convert data source responses into `Result<T>`.
- Handle mode switches (online/offline) explicitly.
- Catch exceptions and return `Result.failure`.
- **Firebase Functions Integration**: AiRepositoryImpl uses Firebase Functions callable (`aiProxy`) as the data source - never direct API calls. Function handles authentication, API key management, and external service communication.

### DI Layer (AiContainer)

**Files**: `di/AiContainer.kt`

**Responsibilities**:
- Create and provide singletons (repositories, use cases, ViewModels)
- Manage dependencies (who depends on whom)
- Lazy initialization for performance

**Example**:

Rules:

- Keep DI wiring in `di/AiContainer.kt`.
- Provide repositories and use cases as lazy singletons.
- Provide ViewModel factories with required dependencies.

---

## Data Flow Between Layers

### Complete Flow: User Sends Message

Rules:

- UI triggers `onEvent` on the ViewModel.
- ViewModel delegates to a domain UseCase.
- UseCase validates input and calls repositories.
- Repository calls data sources and returns `Result`.
- ViewModel updates `UiState`, and UI renders from state.

### Import Rules (Enforce with Code Review)

Rules:

- Presentation can import presentation + domain + Android/AndroidX.
- Domain can import domain only.
- Data can import data + domain + Android/AndroidX.
- DI can import all layers for assembly.

---

## Anti-Patterns (What NOT to Do)

### ❌ Don't: Import Data in Presentation

Rule:

- Presentation layer MUST NOT import from `com.novachat.feature.ai.data.*`.

### ✅ Do: Use Domain Models in Presentation

Rule:

- Presentation layer MUST use domain models (`com.novachat.feature.ai.domain.model.*`) for UI state.

### ❌ Don't: Android Imports in Domain

Rule:

- Domain layer MUST NOT import `android.*` or `androidx.*`.

### ✅ Do: Keep Domain Pure

Rule:

- Domain code must be pure Kotlin and depend only on domain interfaces/models.

### ❌ Don't: Business Logic in Composables

Rule:

- Composables MUST NOT perform business logic or data access.

### ✅ Do: Business Logic in ViewModel

Rule:

- ViewModels own business logic and call UseCases; Composables only render and dispatch events.

---

## File Organization Checklist

Before committing backend/architecture code, verify:

- [ ] **Layer separation correct** - Presentation, Domain, Data, DI in proper directories
- [ ] **No layer violations** - Data not imported by Presentation, etc.
- [ ] **Domain pure** - No Android imports in domain/
- [ ] **Repository interfaces** - domain/repository/ has only interfaces
- [ ] **Repository implementations** - data/repository/ has implementations
- [ ] **MapperFunctions** - Converting between Data and Domain models
- [ ] **AiContainer complete** - All dependencies wired
- [ ] **ViewModel uses UseCase** - Not calling Repository directly
- [ ] **Presentation uses ViewModel** - Not using repository or use case

**Remember: Clean Architecture enforces layer boundaries. Violations decrease testability and increase coupling!**

---

**End of Clean Architecture Skill**

````
