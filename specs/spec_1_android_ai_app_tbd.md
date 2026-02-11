# SPEC-1-Android AI App (TBD)

## Change Log
- 2026-02-10: Set cloud-first hybrid architecture; streaming via Cloud Functions v2 streaming callable `aiProxy` with typed events.
- 2026-02-10: Auth = Firebase anonymous; App Check required; provider = Play Integrity (debug tokens in dev).
- 2026-02-10: Rate limiting = Firestore token bucket keyed by installId + uid (10/min burst 3).
- 2026-02-10: Storage = Room + DataStore; UI = Jetpack Compose; navigation = single-activity Navigation Compose.
- 2026-02-10: DI = Hilt; modules = core-common/core-network/core-data/feature-ai/app.
- 2026-02-10: Testing = JUnit 5 in test + androidTest via mannodermaus framework; CI = GitHub Actions + Gradle Managed Devices (API 30 & 35) + JaCoCo.
- 2026-02-10: Environments = separate staging + prod Firebase projects.
- 2026-02-10: Telemetry = Crashlytics + Analytics + Performance; Remote Config feature flags; privacy-first logging (no prompts/outputs).
- 2026-02-10: Server logs = Option A (minimal metadata only; no BigQuery export for now).
- 2026-02-10: Media roadmap = future image input/output support.
- 2026-02-10: Tools = enabled. MVP tool = calculator (basic arithmetic only), executed server-side in `aiProxy`. Tool calling mode = auto. `web_fetch` disabled for now (no allowed domains). Policy: deny-by-default allowlist + strict limits.

## Background

You’re building an Android application with AI-related features. Exact product details are intentionally undisclosed; the design will use assumption-driven iteration and be refined as constraints emerge.

## Requirements (Draft – to confirm)

**Must-haves**
- Android-only client for initial MVP using Kotlin.
- Modern declarative UI.
- Clear separation of concerns (presentation, domain, data layers).
- Local persistence for user/session/configuration data.
- Remote data access for AI inference and/or content retrieval.
- Automated unit and UI tests; basic end‑to‑end sanity checks.
- Baseline app security (secure storage, network hardening, anti-tamper basics).
- Telemetry: crash reporting and minimal analytics compliant with privacy laws.

**Should-haves**
- Offline-first UX for critical screens where possible.
- Feature flagging and remote config for controlled rollouts.
- CI/CD with device farm tests on prerelease builds.

**Could-haves**
- On-device AI inference for low-latency tasks where feasible.
- Modularization by feature for faster builds and team scaling.

**Won’t-haves (MVP)**
- Cross-platform targets.
- Complex multi-tenant admin console.

## Method

### Target Stack
- **Android**: Kotlin 2.2.21, AGP 9.0.0, minSdk 28, targetSdk 35, compileSdk 36
- **UI**: Jetpack Compose + Navigation Compose, single-activity
- **DI**: Hilt
- **Storage**: Room (chat/history) + DataStore (settings/installId)
- **Backend**: Cloud Functions for Firebase (2nd gen), streaming callable `aiProxy`
- **AI**: Gemini Developer API called **server-side** from `aiProxy` using a Functions **secret**
- **Security**: Firebase Auth (anonymous), App Check (Play Integrity; debug tokens in dev)
- **Telemetry**: Crashlytics + Analytics + Performance; Remote Config feature flags; privacy-first logging
- **Testing**: JUnit 5 for `test/` and `androidTest` via mannodermaus Android JUnit framework; CI with GitHub Actions + GMD (API 30 & 35)

### App Architecture
- MVVM + unidirectional state
- Modules:
  - `core-common`: Result/error types, utils
  - `core-network`: Functions client (`AiCallableClient`), event mapping
  - `core-data`: Room + repositories + mappers
  - `feature-ai`: conversation list + chat UI + VMs
  - `app`: composition root, nav graph, Hilt setup

### Navigation
Routes:
- `ConversationListRoute`
- `ChatRoute/{conversationId}`
- `SettingsRoute`

Flow:
- Start → Conversation List
- Tap conversation → Chat
- New chat → create conversation + navigate to Chat
- Settings reachable from list/chat top bar

### Local Storage
#### DataStore keys (MVP)
- `install_id` (UUID)
- `ai_model_name` (string)
- `temperature` (double)
- `max_output_tokens` (int)
- `telemetry_opt_in` (bool)

#### Room schema (MVP + future media)
- `conversations`
  - `id` TEXT PK (UUID)
  - `title` TEXT NULL
  - `summary` TEXT NULL
  - `createdAtMs` INTEGER
  - `updatedAtMs` INTEGER
  - `deletedAtMs` INTEGER NULL (unused for MVP; hard delete for now)
- `messages`
  - `id` TEXT PK (UUID)
  - `conversationId` TEXT FK → conversations.id ON DELETE CASCADE
  - `role` TEXT (user/model/system)
  - `content` TEXT
  - `mediaRef` TEXT NULL (future images: Storage path)
  - `createdAtMs` INTEGER
  - `tokenCount` INTEGER NULL

Indexes:
- `messages(conversationId, createdAtMs)`

### Streaming Proxy (`aiProxy`) and Typed Events
- Cloud Functions v2 **streaming callable**:
  - Require App Check: `if (!request.app) throw failed-precondition`
  - Anonymous allowed: `request.auth` may be null
  - Rate-limit (Firestore token bucket): `installId + uid` (10/min, burst 3)
  - Gemini key from secret: `GEMINI_API_KEY`

#### Stream event schema
```ts
type StreamEvent =
  | { type: "token"; text: string }
  | { type: "tool_call"; name: string; argsJson: string }
  | { type: "tool_result"; name: string; resultJson: string }
  | { type: "tool_error"; name: string; message: string }
  | { type: "safety_block"; reason: string }
  | { type: "usage"; inputTokens?: number; outputTokens?: number }
  | { type: "done" }
  | { type: "error"; message: string };
```

### Tools (MVP)
- Enabled: `calculator` only
- Mode: tool calling **auto**
- Execution: **server-side** inside `aiProxy`
- Policy: deny-by-default
- Calculator constraints:
  - Allowed chars: digits, whitespace, `.`, `+ - * / ( )`
  - Max length: 128
  - Timeout: 50ms
  - Result: `{ "value": number }`
- `web_fetch`: disabled (no allowed domains)

### Chat Streaming Algorithm (Client)
Goal: one growing assistant bubble.

Algorithm:
1. User sends message → insert user `messages` row.
2. Insert placeholder assistant `messages` row with empty content and a new `messageId`.
3. Start `aiProxy.stream()` and collect events:
   - `token`: append to in-memory buffer; periodically persist updates to the placeholder message (e.g., every 250ms or every N chars).
   - `tool_call`: show a small inline “tool running…” status (no user content logged).
   - `tool_result`: append (optional) tool summary to buffer or treat as internal (your choice via Remote Config).
   - `tool_error`: append a user-friendly tool error.
   - `safety_block`: replace placeholder with a blocked message.
   - `usage`: store token counts in DB (optional).
   - `done`: finalize message; update conversation `updatedAtMs`.
4. If stream fails: mark placeholder as error and allow retry.

### Security
- App Check Play Integrity in prod; debug tokens in dev.
- Secrets in Functions; no keys in APK.
- Privacy-first logging: no prompts/outputs stored server-side; only metadata (errors, rate-limit, block reason, latency, token counts).

### Testing & CI
- JUnit 5 for unit + instrumentation via mannodermaus framework.
- Compose UI tests for list/chat/settings.
- CI:
  - PR: unit tests + lint
  - main: unit tests + lint + GMD (API 30 & 35)
  - JaCoCo coverage (unit tests)

## Implementation

### 1) Project structure + Hilt
- Create modules: `core-common`, `core-network`, `core-data`, `feature-ai`.
- Enable Hilt in `app` + relevant modules; add `@HiltAndroidApp` Application.

### 2) Storage
- Add DataStore keys (installId + settings).
- Add Room entities/DAOs for conversations/messages (cascade delete).

### 3) Backend proxy
- Implement `aiProxy` streaming callable in Functions v2.
- Add Secrets: `GEMINI_API_KEY`.
- Add Firestore rate limiter (installId + uid).
- Add calculator tool execution.

### 4) Android streaming client
- Implement `AiCallableClient` in `core-network`.
- Implement repositories in `core-data`:
  - `ConversationRepository`
  - `ChatRepository` (handles placeholder message + token append)
- Build VMs + Compose screens in `feature-ai`.

### 5) Security hardening
- Enable App Check (Play Integrity) in prod; debug tokens in staging/dev.
- R8/Proguard for release; basic tamper signals recorded in telemetry.

### 6) Telemetry + feature flags
- Crashlytics + Analytics + Performance.
- Remote Config flags: `proxy_enabled`, `rate_limit_enabled`, `ai_model_name`, `temperature`, `max_output_tokens`, kill switch.

### 7) Testing + CI
- Configure JUnit 5 instrumentation using mannodermaus Android JUnit framework.
- Add unit tests for repositories + rate-limit key logic.
- Add Compose UI tests for list/chat/settings.
- GitHub Actions:
  - PR: `test` + `lint`
  - main: `test` + `lint` + GMD `managedDeviceCheck`
  - GMD APIs: 30 and 35

## Milestones

1. **Foundation**: modules, Hilt, Compose navigation skeleton.
2. **Persistence**: Room schema + DataStore installId.
3. **Streaming AI**: `aiProxy` streaming callable + Android Flow UI.
4. **Security**: Play Integrity App Check enforced + secrets + rate limiting.
5. **Quality**: JUnit 5 instrumentation, CI with GMD, coverage + crash monitoring.

## Gathering Results

- **Correctness**: token streaming works; safety blocks handled; tool calls parsed.
- **Reliability**: crash-free sessions, ANR rate, function error rate.
- **Performance**: time-to-first-token, tokens/sec, end-to-end latency.
- **Cost**: average input/output tokens per turn; rate-limit trigger frequency.

## Need Professional Help in Developing Your Architecture?

Please contact me at [sammuti.com](https://sammuti.com) :)

