# NovaChat

NovaChat is a modular Android chat application that uses a Firebase Functions proxy for Gemini responses.

## Current status

1. Online mode is fully supported through Firebase callable function `aiProxy`.
2. Offline mode remains in the product surface as a scaffold with explicit capability states.
3. Deprecated API-key client surface has been removed from app contracts.

## Tech stack

- Kotlin 2.2.21
- Android Gradle Plugin 9.0.0
- Gradle 9.1.0
- Jetpack Compose BOM 2026.01.01
- Hilt DI
- Firebase Auth + Firebase Functions
- Cloud Functions for Firebase (TypeScript)

## Repository layout

- `app/`: composition root, navigation host, application bootstrap.
- `feature-ai/`: chat feature domain/data/presentation and UI.
- `core-common/`: shared error/result primitives.
- `core-network/`: Firebase transport abstractions.
- `functions/`: backend callable function implementation.
- `generated/`: isolated generated artifacts.
- `docs/`: active documentation and archived history.

## Build and verification

```bash
./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest
(cd functions && npm run build && npm run lint && npm test)
npm run format:check
```

## Firebase setup (high level)

1. Configure an Android Firebase app and include `app/google-services.json` locally.
2. Enable anonymous authentication.
3. Deploy functions from `functions/`.
4. Set `GEMINI_API_KEY` for functions runtime.

## Active documentation

- `README.md`
- `DEVELOPMENT.md`
- `API.md`
- `docs/ARCHITECTURE.md`
- `functions/README.md`

Historical material is archived under `docs/archive/`.

## Contributor instructions

- Canonical contributor rules: `AGENTS.md`
- Copilot adapter: `.github/copilot-instructions.md`
- Path-scoped rules: `.github/instructions/*.instructions.md`
