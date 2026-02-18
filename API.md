# NovaChat Backend API (summary)

Full contracts and behavior are in **`specs/NOVACHAT_ARCHITECTURE_SPEC.md`** §3. This file is a short reference.

- **Path A — Callable `aiProxy`:** Input `{ message: string; modelParameters?: { temperature?, topK?, topP?, maxOutputTokens? } }`. Output `{ response: string; model: string }`. Auth required.
- **Path B — HTTP submit/status/worker:** Submit POST with Bearer token and body (requestId, conversationId, messageText, etc.); returns 202 with status. Poll status endpoint or observe Firestore `chat_requests/{requestId}` for COMPLETED/FAILED. Worker is internal (Cloud Tasks).
- **Streaming (roadmap):** See spec §3.3 and plan `docs/plans/2026-02-17-streaming-and-app-check.md`.
