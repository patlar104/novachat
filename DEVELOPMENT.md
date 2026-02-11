# Development Guide

## Branch and commit hygiene

1. Keep changes scoped by concern (guidance/docs/ci/hooks/repo hygiene).
2. Use conventional commits.
3. Avoid mixing runtime feature changes into repo-hygiene commits.

## Module responsibilities

### `app/`

- Hosts app entrypoints (`MainActivity`, `NovaChatApplication`).
- Owns navigation wiring and theme host.
- Should not contain feature business logic.

### `feature-ai/`

- Owns feature contracts, orchestration, and Compose UI.
- Domain contracts are split across dedicated files.
- UI state/events are split by feature area (`chat`, `settings`, `common`).

### `core-network/`

- Owns Firebase callable transport and request/response mapping helpers.
- Used by feature layer instead of direct transport wiring everywhere.

### `core-common/`

- Owns shared error/result primitives used across modules.

### `functions/`

- `src/index.ts` is export wiring.
- Request validation, Gemini client, analytics logging, and error mapping are modularized in `src/ai`, `src/analytics`, and `src/config`.

## Local verification commands

```bash
./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest
(cd functions && npm run build && npm run lint && npm test)
npm run format:check
```

## Debugger wait workflow (debug builds)

Use this when you want app startup to pause until a debugger attaches.

1. Run a debug build and open **Settings**.
2. Enable **Developer Options** -> **Wait for debugger on next launch**.
3. Close the app.
4. Relaunch with Android Studio **Debug**.
5. The app waits for debugger attach exactly once (up to ~10 seconds), then auto-resets.

## Documentation policy

1. Keep active docs aligned to current code paths and contracts.
2. Archive outdated docs under `docs/archive/` rather than deleting history.
3. Do not use archived docs as implementation source of truth.
