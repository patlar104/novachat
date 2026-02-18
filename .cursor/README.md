# Cursor Cloud Runtime

Secrets for cloud agents are **not** stored in this repo. Add them in Cursor:

- **Cursor Desktop:** Settings (Cmd/Ctrl + ,) → **Cloud Agents** → **Secrets**
- **Web:** [Cursor Dashboard](https://cursor.com/dashboard?tab=cloud-agents) → **Cloud Agents** → **Secrets**

## Secrets

| Name               | Required | Description |
| ------------------ | -------- | ----------- |
| `GEMINI_API_KEY`   | Yes      | Gemini API key for the functions AI proxy (Firebase/emulators). |
| `CONTEXT7_API_KEY` | No       | Enables Context7 MCP lookups from `.cursor/mcp.json`. |

Add `GEMINI_API_KEY` as a key-value pair in the Secrets tab; Cursor injects it as an environment variable in the cloud agent.

## Worktrees

`./.cursor/worktrees.json` is configured to bootstrap each new worktree with:

1. stale git lock cleanup
2. `npm ci` at repo root
3. `npm ci` in `functions/`
4. `scripts/ensure-android-sdk.sh` auto-detects SDK path and writes `local.properties`
5. `scripts/ensure-google-services.sh` attempts to copy `app/google-services.json` from known local paths
6. `scripts/ensure-editor-extensions.sh` installs missing recommended editor extensions when supported

This keeps worktrees ready for the repo verification flow from `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §2.

## Docker MCP fallback

- Cursor MCP servers are routed through:
  - `./scripts/mcp-playwright.sh`
  - `./scripts/mcp-context7.sh`
- These wrappers try Docker first and auto-build `novachat-cursor-mcp:2026-02-18` from `docker/cursor-mcp.Dockerfile` if missing.
- If Docker is unavailable, they fall back to local `npx`.
- Optional env vars:
  - `NOVACHAT_MCP_DOCKER_IMAGE` to override image tag
  - `NOVACHAT_MCP_DISABLE_DOCKER=1` to force local mode

## Search and sandbox behavior

1. `@Web` search is available in Cursor but is disabled by default in many environments; enable it in Cursor Settings under **Features > Web Search**.
2. Project MCP servers are defined in `./.cursor/mcp.json` (Playwright + Context7).
3. Terminal actions remain guardrailed by default in Agent settings; keep approval/sandbox mode enabled unless you explicitly need broader execution.
4. Stability profile: keep high-risk/experimental agent features off in workspace settings unless you are actively testing them.

## Validated AI settings sources

- VS Code agent/MCP settings are validated against the official Copilot settings reference:
  - [VS Code Copilot settings reference](https://code.visualstudio.com/docs/copilot/reference/copilot-settings)
- Cursor MCP schema/fields are validated against official Cursor docs:
  - [Cursor MCP docs](https://docs.cursor.com/context/model-context-protocol)
