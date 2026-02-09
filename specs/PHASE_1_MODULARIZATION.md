# Phase 1 App Modularization (Core-Common, Core-Network, Feature-AI)

**Status**: Draft
**Date**: 2026-02-09
**Owner**: Copilot (with user approval)

## 1. Goal

Create minimal modules so refactors do not ripple through the app. Phase 1 establishes:

- core-common: shared result types, error mapping, and small utilities.
- core-network: Retrofit/OkHttp client and serializers.
- feature-ai: AI chat + settings feature (UI, ViewModels, domain, data).
- app: composition root only (navigation + DI wiring).

## 2. Scope

### In Scope

- Add Gradle modules: core-common, core-network, feature-ai.
- Move AI-related UI, presentation, domain, and data code to feature-ai.
- Create a minimal, real Retrofit/OkHttp setup in core-network (not placeholders).
- Introduce core-common error and result types with simple mapping utilities.
- Update app module to depend on feature-ai and act only as composition root.

### Out of Scope (Phase 2+)

- core-data and core-ui modules.
- Non-AI features beyond chat/settings.
- Refactoring Firebase Functions integration beyond module relocation.

## 3. Current State (Baseline)

AI feature is currently embedded under app:

- UI: app/src/main/java/com/novachat/app/ui/ChatScreen.kt, SettingsScreen.kt
- ViewModels: app/src/main/java/com/novachat/app/presentation/viewmodel/*
- Domain: app/src/main/java/com/novachat/app/domain/*
- Data: app/src/main/java/com/novachat/app/data/*
- DI: app/src/main/java/com/novachat/app/di/AppContainer.kt

## 4. Target Module Graph

```
:app
  -> :feature-ai
  -> :core-common
  -> :core-network

:feature-ai
  -> :core-common
  -> :core-network

:core-network
  -> :core-common
```

## 5. Module Responsibilities

### 5.1 core-common

**Purpose**: Shared primitives used by multiple modules.

**Planned APIs**:

- AppResult typealias for Kotlin Result.
- AppError sealed interface for error semantics.
- ErrorMapper for mapping Throwable -> AppError.
- Lightweight utilities (time, id helpers) as needed by feature-ai.

### 5.2 core-network

**Purpose**: Standardized network client setup.

**Planned APIs**:

- OkHttpClient factory with interceptors.
- Retrofit factory using kotlinx.serialization.
- Json serializer configuration.

Notes:
- core-network will be wired with Retrofit/OkHttp even if not used in Phase 1.
- Firebase Functions remains in feature-ai data layer (no behavior change).

### 5.3 feature-ai

**Purpose**: AI chat + settings feature, owning UI, ViewModels, domain, and data.

**Planned Contents**:

- UI: ChatScreen, SettingsScreen, previews, shared UI for AI feature.
- Presentation: UiState/UiEvent/UiEffect for AI screens.
- ViewModels: ChatViewModel, SettingsViewModel, ThemeViewModel, ViewModelFactory (feature scoped).
- Domain: AI models and use cases.
- Data: repositories, mappers, data models, DataStore bridges.
- DI: Feature container (feature-scoped) for wiring dependencies.

## 6. File Moves (Phase 1)

### 6.1 UI

Move from app to feature-ai:

- app/src/main/java/com/novachat/app/ui/ChatScreen.kt
- app/src/main/java/com/novachat/app/ui/SettingsScreen.kt
- app/src/main/java/com/novachat/app/ui/preview/ChatScreenPreview.kt
- app/src/main/java/com/novachat/app/ui/preview/PreviewChatScreenData.kt
- app/src/main/java/com/novachat/app/ui/preview/SharedPreviewComponents.kt

### 6.2 Presentation

Move from app to feature-ai:

- app/src/main/java/com/novachat/app/presentation/model/UiState.kt
- app/src/main/java/com/novachat/app/presentation/viewmodel/ChatViewModel.kt
- app/src/main/java/com/novachat/app/presentation/viewmodel/SettingsViewModel.kt
- app/src/main/java/com/novachat/app/presentation/viewmodel/ThemeViewModel.kt
- app/src/main/java/com/novachat/app/presentation/viewmodel/ViewModelFactory.kt

### 6.3 Domain

Move from app to feature-ai:

- app/src/main/java/com/novachat/app/domain/model/AiConfiguration.kt
- app/src/main/java/com/novachat/app/domain/model/Message.kt
- app/src/main/java/com/novachat/app/domain/model/ThemePreferences.kt
- app/src/main/java/com/novachat/app/domain/repository/Repositories.kt
- app/src/main/java/com/novachat/app/domain/usecase/MessageUseCases.kt

### 6.4 Data

Move from app to feature-ai:

- app/src/main/java/com/novachat/app/data/model/DataModels.kt
- app/src/main/java/com/novachat/app/data/mapper/Mappers.kt
- app/src/main/java/com/novachat/app/data/repository/AiRepositoryImpl.kt
- app/src/main/java/com/novachat/app/data/repository/MessageRepositoryImpl.kt
- app/src/main/java/com/novachat/app/data/repository/PreferencesRepositoryImpl.kt

### 6.5 App Composition Root

Remain in app, but refactor to only wire modules:

- app/src/main/java/com/novachat/app/MainActivity.kt
- app/src/main/java/com/novachat/app/NovaChatApplication.kt

DI wiring will move to feature-ai or a feature container; app will only create and pass it.

## 7. Dependency and Package Updates

- Update package declarations under feature-ai to use a new root, for example:
  - com.novachat.feature.ai.*
- Replace cross-module imports accordingly.
- App module references feature-ai public entry points only.

## 8. Build Changes

- settings.gradle.kts: include new modules.
- Create build.gradle.kts for core-common, core-network, feature-ai.
- Move dependencies from app/build.gradle.kts into feature-ai and core-network as appropriate.
- Keep app/build.gradle.kts minimal (Compose + navigation + module deps).

## 9. Tests

- Move AI-related unit tests to feature-ai module:
  - app/src/test/java/com/novachat/app/presentation/viewmodel/ChatViewModelTest.kt
- Update imports to new packages.

## 10. Migration Plan (Phase 1)

1. Add specs/PHASE_1_MODULARIZATION.md (this document).
2. Add new modules and Gradle wiring.
3. Create core-common and core-network implementations.
4. Move AI feature files into feature-ai with updated packages.
5. Update app composition root and DI wiring.
6. Fix imports and update tests.
7. Run tests and compile verification.

## 11. Acceptance Criteria

- App builds with :core-common, :core-network, :feature-ai modules.
- App module contains only composition root (navigation + DI creation).
- AI feature screens and ViewModels compile in feature-ai.
- Retrofit/OkHttp stack compiles in core-network.
- No placeholder code; all files compile.

## 12. Risks and Mitigations

- **Risk**: Large package move breaks imports and DI.
  - **Mitigation**: Update in dependency order and run compilation checks.

- **Risk**: Firebase Functions usage conflicts with Retrofit setup.
  - **Mitigation**: Keep Firebase logic unchanged; Retrofit is additive.

- **Risk**: Theme dependencies needed by feature-ai before core-ui exists.
  - **Mitigation**: Keep current theme inside feature-ai temporarily; move to core-ui in Phase 2.
