# NovaChat

NovaChat is a modular Android chat application that uses a Firebase Functions proxy for Gemini responses.

## Current status

1. Online mode is fully supported through Firebase callable function `aiProxy`.
2. Offline mode remains in the product surface as a scaffold with explicit capability states.
3. Deprecated API-key client surface has been removed from app contracts.

## Tech stack

- Android versions: see `gradle/libs.versions.toml` (Kotlin 2.3.x, AGP 9.x, Compose BOM 2026.01.01).
- Hilt DI
- Firebase Auth + Firebase Functions
- Cloud Functions for Firebase (TypeScript, Node 24)

## Repository layout

- `app/`: composition root, navigation host, application bootstrap.
- `feature-ai/`: chat feature domain/data/presentation and UI.
- `core-common/`: shared error/result primitives.
- `core-network/`: Firebase transport abstractions.
- `functions/`: backend callable function implementation.
- `generated/`: generated artifacts (do not edit by hand; Data Connect and other codegen).
- `docs/`: active documentation and archived history.

## Build and verification

Run **`npm run verify`** to run Android build, unit tests, lint, Detekt, functions build/lint/test, and format check (matches CI).

Or run per stack:

```bash
./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest :app:lintDebug :app:detekt :feature-ai:detekt
(cd functions && npm run build && npm run lint && npm test)
npm run format:check
```

## Firebase setup (high level)

1. Configure an Android Firebase app and include `app/google-services.json` locally.
2. Enable anonymous authentication.
3. Deploy functions from `functions/`.
4. Set `GEMINI_API_KEY` for functions runtime.

## Active documentation

- `README.md` (this file)
- `specs/NOVACHAT_ARCHITECTURE_SPEC.md` — architecture and product spec (single source of truth)
- `AGENTS.md` — canonical rules for agents and contributors
- `docs/CONTRIBUTING.md` — how to contribute, build, hooks, commits
- `docs/ARCHITECTURE.md` — short pointer to the spec
- `DEVELOPMENT.md` — dev setup, emulators, env
- `API.md` — backend API summary (full contracts in spec §3)
- `functions/README.md` — Functions layout and callable contract
- `docs/plans/` — implementation plans
- `docs/archive/` — historical material only (not active guidance)

## Contributor instructions

- Canonical rules: `AGENTS.md`
- Contributing: `docs/CONTRIBUTING.md`
- Copilot adapter: `.github/copilot-instructions.md`
- Path-scoped rules: `.github/instructions/*.instructions.md`
