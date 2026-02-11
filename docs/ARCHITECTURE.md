# NovaChat Architecture

Last updated: 2026-02-11

## High-level modules

- `app`: application bootstrap + navigation composition.
- `feature-ai`: feature domain/data/presentation/ui.
- `core-network`: Firebase callable transport abstractions.
- `core-common`: shared result/error primitives.
- `functions`: backend callable implementation.

## Android architecture

### Composition root (`app`)

- `NovaChatApplication` initializes Firebase and startup auth guard behavior.
- `MainActivity` wires theme, navigation destinations, and screen ViewModels.

### Feature layer (`feature-ai`)

#### Domain

- Models: `Message`, `AiConfiguration`, `ThemePreferences`, `OfflineCapability`.
- Repositories: `AiRepository`, `MessageRepository`, `PreferencesRepository`.
- Use cases are split one-per-file.

#### Data

- Repository implementations are separate files.
- OFFLINE handling is delegated via `OfflineAiEngine` abstraction.
- Network calls go through `core-network` transport types.

#### Presentation

- Chat and settings models are split:
  - `presentation/model/chat/*`
  - `presentation/model/settings/*`
  - `presentation/model/common/*`
- ViewModels are feature-specific and Hilt-injected.

#### UI

- Chat UI split into route/content/components/previews.
- Settings UI split into route/content components.

## Core modules

### `core-common`

- Canonical app error types and mapping utilities.
- Shared result aliasing for cross-module consistency.

### `core-network`

- `FirebaseCallableClient`
- `AiProxyRemoteDataSource`
- `AuthSessionProvider`
- `PlayServicesChecker`

These components isolate Firebase transport concerns from feature orchestration.

## Backend architecture (`functions`)

- `src/index.ts`: exports and initialization only.
- `src/functions/aiProxy.ts`: callable entrypoint.
- `src/ai/validateRequest.ts`: input validation.
- `src/ai/geminiClient.ts`: Gemini API call.
- `src/ai/errors.ts`: error normalization to `HttpsError`.
- `src/analytics/usageLogger.ts`: usage telemetry logging.
- `src/config/env.ts`: runtime configuration access.

## Data flow

1. UI emits event.
2. ViewModel invokes use case.
3. Use case calls repository.
4. Repository delegates transport to `core-network`.
5. Firebase callable `aiProxy` validates, calls Gemini, logs usage, returns typed response.
6. Result propagates back to ViewModel state/effect.
