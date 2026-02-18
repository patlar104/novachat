# Streaming Callable & App Check — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a streaming callable and enforce App Check on the existing callable without breaking Path A (current `aiProxy`) or Path B (HTTP submit/worker). See `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §3.3 (Target — streaming callable).

**Architecture:** Introduce a new export `aiProxyStream` (streaming callable) alongside existing `aiProxy`. Reuse validation, error mapping, and env from `functions/src/ai/`. Add `enforceAppCheck: true` to both callables when App Check is configured in the Firebase project. Rate limiting for the streaming callable can key on `installId` + `uid` (from request.data or auth) and reuse Firestore patterns from `functions/src/chat/rateLimit.ts` if desired in a later task.

**Tech Stack:** Firebase Functions v2 (TypeScript), Node 20+, `onCall` with optional `(request, response)` for streaming; Gemini API (REST streaming or `@google/genai`); Firebase App Check.

---

## Prerequisites

- Firebase project has App Check configured (Play Integrity for prod, debug token for dev). If not, implement App Check setup first or add `enforceAppCheck: true` only after configuration.
- Existing `aiProxy` and HTTP pipeline remain unchanged in behavior; new code is additive.

---

## Task 1: Align Node version in functions (good to have)

**Files:**

- Modify: `functions/package.json` — set `"engines": { "node": "20" }` **or** keep `"24"` and update `functions/README.md` to match (e.g. "Node.js 24").
- Modify: `functions/README.md` — ensure the "Runtime" section matches `package.json` engines exactly.

**Step 1: Check current values**

```bash
grep -A1 '"engines"' functions/package.json
grep -i node functions/README.md | head -3
```

**Step 2: Make them consistent**

Choose one Node major version (20 or 24) and set both files to that version. Example if choosing 20:

In `functions/package.json`: `"engines": { "node": "20" }`  
In `functions/README.md`: `- Node.js 20`

**Step 3: Commit**

```bash
git add functions/package.json functions/README.md
git commit -m "chore(functions): align Node version in package.json and README"
```

---

## Task 2: Add enforceAppCheck to existing aiProxy (optional, gated)

**Files:**

- Modify: `functions/src/functions/aiProxy.ts` — add `enforceAppCheck: true` to the `onCall` options object **only if** the Firebase project already has App Check enabled for the app. Otherwise skip this task or add a comment that it is disabled until App Check is configured.

**Step 1: Add option**

In `aiProxy.ts`, change the `onCall` first argument from:

```ts
{
  region: "us-central1",
  cors: true,
}
```

to:

```ts
{
  region: "us-central1",
  cors: true,
  enforceAppCheck: true,  // Requires App Check configured in Firebase project
}
```

If you are not enabling App Check yet, add a comment and leave `enforceAppCheck` out:

```ts
// enforceAppCheck: true,  // Uncomment when App Check is configured (Play Integrity / debug token)
```

**Step 2: Build and test**

```bash
cd functions && npm run build && npm run lint && npm test
```

Expected: all pass.

**Step 3: Commit**

```bash
git add functions/src/functions/aiProxy.ts
git commit -m "feat(functions): add App Check enforcement to aiProxy (or comment for later)"
```

---

## Task 3: Define stream event types and shared validation

**Files:**

- Create: `functions/src/ai/streamTypes.ts` — export a type `StreamEvent` (discriminated union: `token`, `tool_call`, `tool_result`, `tool_error`, `safety_block`, `usage`, `done`, `error`) and a helper `sendStreamEvent(event: StreamEvent)` usage note in a comment (actual send will be in the handler that has `response`).
- Modify: `functions/src/ai/validateRequest.ts` — ensure it can accept optional `installId` in the payload for rate limiting; or add a small helper in `streamTypes.ts` that reads `installId` from request.data. No change to existing `validateAiProxyRequest` signature unless you add an optional second parameter for streaming-specific fields.

**Step 1: Create stream event type**

In `functions/src/ai/streamTypes.ts`:

```ts
/**
 * Event schema for aiProxyStream. See specs/NOVACHAT_ARCHITECTURE_SPEC.md §3.3.
 */
export type StreamEvent =
  | { type: "token"; text: string }
  | { type: "tool_call"; name: string; argsJson: string }
  | { type: "tool_result"; name: string; resultJson: string }
  | { type: "tool_error"; name: string; message: string }
  | { type: "safety_block"; reason: string }
  | { type: "usage"; inputTokens?: number; outputTokens?: number }
  | { type: "done" }
  | { type: "error"; message: string };
```

**Step 2: Export from ai layer (optional)**

Ensure `functions/src/ai/types.ts` or an index re-exports `StreamEvent` if you want a single import path; otherwise import from `streamTypes.ts` in the streaming handler.

**Step 3: Run build**

```bash
cd functions && npm run build
```

Expected: success.

**Step 4: Commit**

```bash
git add functions/src/ai/streamTypes.ts
git commit -m "feat(functions): add StreamEvent types for streaming callable"
```

---

## Task 4: Add Gemini streaming client (non-streaming fallback)

**Files:**

- Create: `functions/src/ai/geminiStream.ts` — export `streamGemini(apiKey, request, onChunk)` that calls the Gemini REST API with streaming (e.g. `generateContent` with `stream: true` if using fetch, or use the streaming endpoint). For each text delta, invoke `onChunk(text)`. On safety block or error, throw or call onChunk with an error event. Reuse `GEMINI_MODEL` and generation config from `geminiClient.ts` (or move model name to `functions/src/config/constants.ts` to avoid duplication).
- Do **not** change `geminiClient.ts` or `callGeminiWithTimeout.ts` in this task; both remain for Path A and Path B.

**Step 1: Implement streamGemini**

- Use `https://generativelanguage.googleapis.com/v1beta/models/${model}:streamGenerateContent` (or the exact Gemini streaming endpoint from current docs) with `fetch` and read the stream, or use `@google/genai` if the team agrees to add the dependency.
- Parse server-sent chunks and extract text deltas; call `onChunk(deltaText)`.
- On completion, you may call `onChunk(null)` or a sentinel to indicate done.
- Map API errors to a single Error or to the `StreamEvent` error shape.

**Step 2: Add unit test (optional but recommended)**

Create `functions/src/ai/geminiStream.test.ts` that mocks fetch and asserts that `onChunk` is called with expected text (or that errors are propagated). Run:

```bash
cd functions && npm run build && npm test
```

**Step 3: Commit**

```bash
git add functions/src/ai/geminiStream.ts functions/src/ai/geminiStream.test.ts
git commit -m "feat(functions): add Gemini streaming client"
```

---

## Task 5: Implement aiProxyStream callable handler

**Files:**

- Create: `functions/src/functions/aiProxyStream.ts` — export `aiProxyStream` as `onCall` with `region: "us-central1"`, `cors: true`, and `enforceAppCheck: true` (or commented as in Task 2). Handler signature `(request, response)` so that when `request.acceptsStreaming === true`, you call `streamGemini` and for each chunk send `response.sendChunk({ type: "token", text: chunk })`, then `response.sendChunk({ type: "done" })`. When `request.acceptsStreaming === false`, fall back to existing non-streaming behavior (e.g. call `callGemini` from geminiClient and return the full result). Use `validateAiProxyRequest(request.data)` and `mapToHttpsError` for errors; allow `request.auth` to be null (anonymous) and use `installId` from request.data for logging/rate-limit key if present.
- Modify: `functions/src/index.ts` — export `aiProxyStream` and ensure `initializeFunctionsAdmin()` is still called.

**Step 1: Implement handler**

- If `!request.acceptsStreaming`: validate, call `callGemini`, return `{ response, model }` (same as current aiProxy).
- If `request.acceptsStreaming`: validate, then call `streamGemini` with an `onChunk` that does `response.sendChunk({ type: "token", text })`. After stream ends, send `{ type: "done" }`. On error, send `{ type: "error", message }` and/or throw after sending.

**Step 2: Wire in index**

In `functions/src/index.ts`:

```ts
import { aiProxyStream } from "./functions/aiProxyStream";
// ...
export { aiProxy, aiProxyStream };
```

**Step 3: Build, lint, test**

```bash
cd functions && npm run build && npm run lint && npm test
```

**Step 4: Commit**

```bash
git add functions/src/functions/aiProxyStream.ts functions/src/index.ts
git commit -m "feat(functions): add aiProxyStream callable with streaming and non-streaming fallback"
```

---

## Task 6: Rate limiting for streaming callable (optional)

**Files:**

- Modify: `functions/src/functions/aiProxyStream.ts` — before starting the stream, check a rate limit keyed by `(installId ?? request.auth?.uid ?? "anonymous")` (or similar). Reuse logic from `functions/src/chat/rateLimit.ts` (e.g. a single Firestore bucket per key, 10/min cap) or add a thin wrapper that reads from request and calls a shared `checkRateLimitForKey(key)`. If over limit, throw `HttpsError("resource-exhausted", "Rate limit exceeded")` and do not stream.
- Document in `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §3.3 that the streaming callable is rate-limited (e.g. installId + uid, 10/min).

**Step 1: Add rate limit check**

Implement or reuse a function that checks Firestore (or in-memory for dev) for the key and returns allowed/not. Call it at the start of the streaming handler.

**Step 2: Update spec**

In `NOVACHAT_ARCHITECTURE_SPEC.md`, under §3.3, add one line: "Rate limiting: Firestore token bucket keyed by installId + uid (10/min); implemented in aiProxyStream."

**Step 3: Commit**

```bash
git add functions/src/functions/aiProxyStream.ts specs/NOVACHAT_ARCHITECTURE_SPEC.md
git commit -m "feat(functions): rate limit aiProxyStream; document in spec"
```

---

## Task 7: Update functions README and architecture spec

**Files:**

- Modify: `functions/README.md` — add a "Streaming callable" section: describe `aiProxyStream`, same input contract as `aiProxy`, and that when the client uses streaming, events are `{ type, ... }` (token, done, error). Link to `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §3.3.
- Modify: `specs/NOVACHAT_ARCHITECTURE_SPEC.md` — under §3.3, add a line that the streaming callable is implemented as `aiProxyStream` and exported from `functions`; Path A remains `aiProxy`.

**Step 1: Edit README**

Add under "Callable contract summary" or a new section:

```markdown
## Streaming callable (aiProxyStream)

Same input as aiProxy. When the client requests streaming, the function sends events via sendChunk: `{ type: "token", text }`, `{ type: "done" }`, `{ type: "error", message }`. See specs/NOVACHAT_ARCHITECTURE_SPEC.md §3.3.
```

**Step 2: Edit spec**

In §3.3, add: "Implemented as `aiProxyStream` in `functions/src/functions/aiProxyStream.ts`; existing `aiProxy` (Path A) unchanged."

**Step 3: Commit**

```bash
git add functions/README.md specs/NOVACHAT_ARCHITECTURE_SPEC.md
git commit -m "docs: document aiProxyStream in README and architecture spec"
```

---

## Execution handoff

Plan saved to `docs/plans/2026-02-17-streaming-and-app-check.md`.

**Execution options:**

1. **Subagent-Driven (this session)** — Use superpowers:subagent-driven-development; one subagent per task with code review between tasks.
2. **Parallel Session** — New session in worktree with superpowers:executing-plans; run tasks in order with checkpoints.

**Notes:** Task 2 (App Check) can be skipped until the Firebase project has App Check configured. Task 6 (rate limiting) can be deferred and done after the stream works end-to-end. Task 1 (Node version) is independent and can be done first.
