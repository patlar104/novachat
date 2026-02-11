# NovaChat Contributor Instructions (Portable)

This is the canonical contributor instruction file for this repository.
It is written to work across multiple environments (GitHub Copilot, Codex, VS Code, JetBrains, CLI-based agents).

## Scope

These instructions apply to the entire repository unless a path-specific instruction file adds stricter rules.

## Source of truth

1. Runtime behavior and architecture are defined by current code in:
   - `app/`
   - `feature-ai/`
   - `core-common/`
   - `core-network/`
   - `functions/`
2. Active human docs are:
   - `README.md`
   - `DEVELOPMENT.md`
   - `API.md`
   - `docs/ARCHITECTURE.md`
   - `functions/README.md`
3. Archived material in `docs/archive/` is historical context only.

## Required quality gates

Run these before finalizing substantial changes:

```bash
./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest
(cd functions && npm run build && npm run lint && npm test)
(cd /Users/patrick/dev/projects/novachat && npm run format:check)
```

## Architecture guardrails

1. `app/` is composition root and navigation host.
2. `feature-ai/` owns feature domain/presentation/data orchestration.
3. `core-network/` owns Firebase/network transport abstractions.
4. `core-common/` owns shared result/error primitives.
5. `functions/` is modularized backend source (validation, errors, client, analytics, exports).

## Editing rules

1. Prefer minimal, targeted changes.
2. Keep docs synchronized with current code paths and contracts.
3. Do not reintroduce archived legacy patterns (manual `AiContainer`, monolithic UI state files, deprecated API-key UI contracts).
4. Keep generated artifacts isolated under `generated/` and out of lint/quality ownership unless explicitly required.

## Path-scoped instruction files

Use these when touching corresponding areas:

- `.github/instructions/app.instructions.md`
- `.github/instructions/feature-ai.instructions.md`
- `.github/instructions/functions.instructions.md`
- `.github/instructions/build.instructions.md`
