# NovaChat — Architecture & Product Spec

> **Single source of truth** for product, backend contracts, and app architecture. Use this when designing or implementing features so existing behavior is preserved and new work stays consistent.

**Last updated:** 2026-02-17  
**Status:** Living document; current implementation and target roadmap are both described below.

---

## 1. Product summary

- **What:** Android-only AI chat app (NovaChat). Online mode via Firebase/Gemini; offline/on-device is a future roadmap item.
- **Stack:** Kotlin, Jetpack Compose, Hilt, Firebase Auth + Functions, Room + DataStore, Cloud Functions for Firebase (TypeScript, Node 20+).
- **Security:** API keys only in backend; App Check and rate limiting as described in backend section.

---

## 2. Repository layout (canonical)

| Path            | Purpose                                                        |
| --------------- | -------------------------------------------------------------- |
| `app/`          | Composition root, navigation host, `@HiltAndroidApp` bootstrap |
| `feature-ai/`   | Chat feature: UI, ViewModels, domain, data                     |
| `core-common/`  | Shared result/error types, utilities                           |
| `core-network/` | Firebase / backend transport (callable client, event mapping)  |
| `core-data/`    | Not present; Room lives in `feature-ai/` (see §9).             |
| `functions/`    | Cloud Functions backend (TypeScript)                           |
| `generated/`    | Generated artifacts (e.g. Data Connect client)                 |
| `specs/`        | This spec and any future product/arch docs                     |
| `docs/`         | Active docs; `docs/plans/` for implementation plans            |

Active docs (see README "Active documentation"): `AGENTS.md`, `docs/CONTRIBUTING.md`, `docs/ARCHITECTURE.md`, `DEVELOPMENT.md`, `API.md`, `docs/plans/`, `docs/archive/` (historical).

Build/verify (from repo root):

```bash
./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest
(cd functions && npm run build && npm run lint && npm test)
npm run format:check
```

---

## 3. Backend — current vs target (no breaking changes)

The backend has **two supported paths**. Changing or removing one must be an explicit decision and reflected here and in READMEs.

### 3.1 Path A — Callable `aiProxy` (current)

- **Type:** Firebase Functions v2 **callable** (non-streaming).
- **File:** `functions/src/functions/aiProxy.ts`
- **Auth:** Required. `request.auth` must be present; otherwise `unauthenticated` is thrown.
- **App Check:** Not enforced in code today. Target: add `enforceAppCheck: true` in options when ready.
- **Contract:**
  - **Input:** `{ message: string; modelParameters?: { temperature?, topK?, topP?, maxOutputTokens? } }`
  - **Output:** `{ response: string; model: string }`
- **Rate limiting:** None in this handler. If you add it, key by `installId` + `uid` and document in this spec.
- **Use case:** Simple request/response from the app; used by clients that call the callable directly.

### 3.2 Path B — HTTP submit / status / worker (current)

- **Type:** HTTP endpoints + Cloud Tasks. Submit → enqueue → worker calls Gemini; client polls status.
- **Files:** `functions/src/functions/chatSubmit.ts`, `chatStatus.ts`, `chatWorker.ts`; plus `functions/src/chat/*` (rate limit, policy, Firestore, circuit breaker, etc.).
- **Auth:** Bearer token (Firebase ID token) on submit; worker is invoked by Cloud Tasks (internal).
- **Rate limiting:** Per user, per device, per IP (token-bucket style); daily quota; in-flight cap. See `functions/src/chat/rateLimit.ts` and `types.ts` for limits.
- **Use case:** Resilient async flow with retries, circuit breaker, and content policy (block/redact patterns in `policy.ts`).

When adding features (e.g. streaming, tools, App Check), implement in a way that does not break Path A or Path B unless a deliberate deprecation is decided and documented.

### 3.3 Target — streaming callable (roadmap)

- **Goal:** One streaming callable that supports token-by-token and optional tool events, with App Check and rate limiting.
- **Firebase (as of 2026):** Use `onCall` with `enforceAppCheck: true`; for streaming, use `request.acceptsStreaming` and `response.sendChunk()` (see [Firebase Callable](https://firebase.google.com/docs/functions/callable)).
- **Suggested event schema (when implemented):**
  - `token`, `tool_call`, `tool_result`, `tool_error`, `safety_block`, `usage`, `done`, `error`
- **Auth (target):** Anonymous allowed (`request.auth` may be null); rate limit key: `installId` + `uid` (or fallback).
- **App Check (target):** Enforce in callable options so `request.app` is present for valid requests.

Implementing this target must not remove or break Path A or B until clients are migrated and a deprecation is announced.

---

## 4. App architecture (Android)

- **Pattern:** MVVM + unidirectional data flow.
- **Navigation:** Single-activity; Navigation Compose. Routes: conversation list, chat by `conversationId`, settings.
- **DI:** Hilt. Modules: `app`, `feature-ai`, `core-common`, `core-network` (and `core-data` when used).
- **Storage (when used):** Room for conversations/messages; DataStore for installId and settings (e.g. model name, temperature, telemetry opt-in).
- **Remote:** Callable client in `core-network`; optionally HTTP client for submit/status if using Path B from the app.

---

## 5. Tech stack (pinned for consistency)

- **Android:** Versions in `gradle/libs.versions.toml`. Kotlin 2.3.x, AGP 9.x, Gradle 9.x, Compose BOM 2026.01.01, minSdk 28, targetSdk 35.
- **Backend:** Node 24 (see `functions/package.json` engines and `functions/.nvmrc`), TypeScript, Firebase Functions v2.
- **Security:** Firebase Auth (anonymous supported in target); App Check (Play Integrity in prod, debug tokens in dev when enabled); secrets via Firebase/params only.

---

## 6. Spec history and migration

- **2026-02-17:** This document created as the single architecture/product spec. It supersedes:
  - `spec_1_android_ai_app_tbd.md` (content folded into “target” and “current” sections above)
  - `PHASE_1_MODULARIZATION.md`, `KSP_MIGRATION.md`, `HILT_TEST_MIGRATION.md` (task-specific; completed or tracked in `docs/plans/` instead)
- Older spec files have been removed; any remaining references should point to this file.

---

## 7. Where to put new work

- **New backend behavior (streaming, tools, App Check):** Implement in `functions/` and document in §3 (Current vs Target). Keep existing callable and HTTP paths working unless deprecating.
- **New app features:** Follow module layout in §2; keep `app/` as composition root only.
- **Implementation plans:** Add under `docs/plans/YYYY-MM-DD-<feature>.md` and reference this spec where relevant. Current plans: `docs/plans/2026-02-17-consolidate-specs-and-architecture.md`, `docs/plans/2026-02-17-streaming-and-app-check.md`, `docs/plans/2026-02-17-docs-baseline-practice.md`.
- **Documentation:** New active docs go in `docs/` or root and must be listed in README; architecture changes stay in this spec.

---

## 8. Good to have (non-breaking)

These improve consistency and maintainability without changing behavior. Tackle when convenient.

- **Node version:** Keep `functions/package.json` `engines.node` and `functions/README.md` Runtime in sync (e.g. both "20" or both "24"). See plan `docs/plans/2026-02-17-streaming-and-app-check.md` Task 1.
- **Single Gemini client:** Consider one shared module (e.g. `functions/src/ai/geminiClient.ts`) for both non-streaming and streaming calls, with a shared model constant and config, to avoid drift between `geminiClient.ts` and `chat/callGeminiWithTimeout.ts`.
- **Tests for chat pipeline:** Add unit tests for `functions/src/chat/rateLimit.ts` and `functions/src/chat/policy.ts` (e.g. allowed/blocked inputs, quota logic) so changes don’t regress.
- **Worker security note:** In `functions/src/functions/chatWorker.ts` (or README), add a one-line comment that the handler is intended to be invoked only by Cloud Tasks (enforced via IAM or queue config), not by arbitrary clients.
- **Streaming + App Check:** Use the plan `docs/plans/2026-02-17-streaming-and-app-check.md` to add `aiProxyStream` and optional App Check enforcement; it references §3.3 above.

---

## 9. Codebase areas the old specs didn’t mention

These parts of the repo exist in code but were not described in the superseded specs. Use this section when navigating or changing behavior.

### 9.1 Firebase Data Connect (example / unused by chat)

- **What:** A full Data Connect setup with an **example** connector (movies, users, reviews), not used by the chat feature.
- **Where:**
  - **Source:** `dataconnect/` — `dataconnect.yaml` (service `novachat`, Cloud SQL), `schema/schema.gql` (User, Movie, MovieMetadata, Review), `example/` (connector, queries, mutations), `seed_data.gql`.
  - **Generated:** `generated/dataconnect/` — JS client (root `package.json` depends on it), Android Kotlin (ExampleConnector, ListMoviesQuery, etc.), and `functions-admin` (used as functions dependency but **not imported** in any function).
- **App:** `app/build.gradle.kts` adds `implementation(libs.firebase.dataconnect)` and `sourceSets` include `../generated/dataconnect/android`. The chat UI and feature-ai do **not** reference Data Connect; this is either template/boilerplate or reserved for a future feature.
- **Action:** Treat as optional. Removing or repurposing it (e.g. for chat-related data) is a separate decision; document in this spec if you change it.

### 9.2 Room and persistence (in feature-ai, no core-data module)

- **What:** Room is implemented **inside `feature-ai`**, not in a separate `core-data` module. `settings.gradle.kts` includes only `:app`, `:core-common`, `:core-network`, `:feature-ai`.
- **Where:**
  - **Database:** `feature-ai/.../data/local/NovaChatDatabase.kt` (Room DB).
  - **Entities:** `MessageRoomEntity`, `ConversationEntity`, `OutboundRequestEntity`, `SettingsBackupEntity`.
  - **DAOs:** `MessageDao`, `ConversationDao`, `OutboundRequestDao`, `SettingsBackupDao`.
  - **Repos:** `MessageRepositoryRoomImpl`, `OutboundRequestRepositoryRoomImpl`; used when async path is enabled (e.g. `ff_async_ack` and Room-backed message repo).
- **Implication:** “core-data” in §2 and §4 means “when you introduce a separate module”; today, persistence is feature-ai’s responsibility.

### 9.3 WorkManager and async-path reconciliation

- **What:** A periodic worker reconciles pending outbound chat requests (Path B) with Firestore.
- **Where:**
  - **Worker:** `feature-ai/.../data/worker/ChatReconciliationWorker.kt` — `@HiltWorker`, enqueued from `NovaChatApplication` as periodic work (e.g. every 30 min).
  - **Logic:** Loads pending requests in states `QUEUED_LOCAL`, `QUEUED`, `PROCESSING`; updates backoff/retry; comment notes that full re-submit would call submit API (not fully wired).
- **Old specs:** Did not mention WorkManager or reconciliation. Keep in mind when changing the async flow or Firestore `chat_requests` usage.

### 9.4 Firestore usage (Android + functions)

- **Android:**
  - **Collection:** `chat_requests` (same as backend).
  - **Usage:** `feature-ai/.../data/chat/ChatStatusObserver.kt` — Firestore listener on `chat_requests/{requestId}` to observe COMPLETED/FAILED for the async path.
  - **Libraries:** `firebase-firestore` in both `app` and `feature-ai`.
- **Functions (Path B):**
  - **Collections:** `chat_requests`, `dedupe_keys`, `rate_buckets`, `quota_daily` (see `functions/src/chat/firestore.ts`).
  - **Secret:** `CHAT_UID_HMAC_SECRET` (or default) used for `uidHash`; should be set and rotated in production.

### 9.5 Root-level Node tooling and Playwright

- **What:** Repo root has a Node/package.json for tooling; Playwright is configured but not used by chat.
- **Where:**
  - **Scripts:** `package.json` — `format`, `format:check`, `browser:research` (points to `scripts/browser-research.js`, which is **missing**).
  - **Playwright:** `playwright.config.js` — testDir `./tests`; root has no `tests/` directory (or it’s empty).
  - **Dependencies:** `@dataconnect/generated` (js-client), Playwright, Prettier.
- **Action:** Fix or remove `browser:research` if you rely on it; add Playwright tests under `tests/` if you adopt them for E2E.

### 9.6 GitHub hooks and contributor tooling

- **What:** Local git hooks for quality gates and commit format.
- **Where:** `.github/hooks/` — `pre-commit` (scoped: Android tests, functions build/lint, format check), `pre-push` (full build + tests + functions build/lint/test + format), `commit-msg` (conventional commits). Setup: `./.github/hooks/setup-hooks.sh` or `git config core.hooksPath .github/hooks`.
- **Not in old specs:** Hooks were not documented. Keep in mind for CI/local parity and contributor docs.

### 9.7 IDE and environment config

- **What:** Cursor and VS Code config that affects dev experience only.
- **Where:**
  - **.cursor:** e.g. `environment.json` (Cloud Runtime, Dockerfile, install/start, emulators, persisted dirs).
  - **.vscode:** e.g. `mcp.json` (Playwright MCP, Context7 MCP), `tasks.json`, `launch.json`, `settings.json`.
- **Not in old specs:** Purely environmental; no product impact.

### 9.8 Firebase config and emulators

- **What:** `firebase.json` config beyond “deploy functions”.
- **Where:**
  - **Emulators:** `functions` on port 5002 (host 0.0.0.0); `dataconnect` with `dataDir`: `dataconnect/.dataconnect/pgliteData`.
  - **Data Connect:** `dataconnect` source folder for schema/connectors.
- **Old specs:** Did not spell out emulator ports or Data Connect emulator. Useful for local backend and Data Connect development.
