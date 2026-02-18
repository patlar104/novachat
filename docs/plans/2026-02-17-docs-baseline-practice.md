# Docs and Specs Baseline Practice — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Update every doc and spec so the project follows a single baseline: one architecture spec, no broken links, contributor and agent rules clear, and a consistent place for each type of content (specs, plans, contributing, API, dev setup).

**Architecture:** Root README stays the entry point; `specs/NOVACHAT_ARCHITECTURE_SPEC.md` remains the single source of truth for product/backend/app layout; `docs/` holds active prose (CONTRIBUTING, ARCHITECTURE, plans); `.github/` holds path-scoped instructions and hooks; generated docs stay as-is with a root-level note not to edit them.

**Tech Stack:** Markdown; paths: `README.md`, `specs/`, `docs/`, `.github/`, `functions/README.md`, optional `AGENTS.md`, `DEVELOPMENT.md`, `API.md`.

---

## Task 1: Create AGENTS.md (canonical contributor rules)

**Files:**

- Create: `AGENTS.md`

**Step 1: Create file**

Add at repo root:

```markdown
# NovaChat — Agent and Contributor Rules

1. **Architecture and product:** Use `specs/NOVACHAT_ARCHITECTURE_SPEC.md` as the single source of truth. Do not add product or backend contracts elsewhere without updating that spec.
2. **Path-scoped rules:** Follow `.github/instructions/*.instructions.md` for the paths you are editing (app, feature-ai, functions, build).
3. **Verification:** Before claiming work complete, run the commands in the spec §2 (Build and verification) and fix any failures.
4. **Plans:** New multi-step work goes in `docs/plans/YYYY-MM-DD-<name>.md` and should reference the architecture spec where relevant.
5. **Docs:** Do not add new top-level doc files (e.g. API.md, DEVELOPMENT.md) without listing them in README "Active documentation" and, if architectural, noting them in the spec.
```

**Step 2: Commit**

```bash
git add AGENTS.md
git commit -m "docs: add AGENTS.md as canonical agent/contributor rules"
```

---

## Task 2: Create docs/CONTRIBUTING.md

**Files:**

- Create: `docs/CONTRIBUTING.md`

**Step 1: Create file**

Content:

```markdown
# Contributing to NovaChat

- **Architecture and contracts:** See `specs/NOVACHAT_ARCHITECTURE_SPEC.md`.
- **Agent/AI rules:** See `AGENTS.md`.
- **Build and verify:** From repo root:
  `./gradlew :app:assembleDebug :feature-ai:testDebugUnitTest`
  `(cd functions && npm run build && npm run lint && npm test)`
  `npm run format:check`
- **Git hooks (optional):** Install with `./.github/hooks/setup-hooks.sh` or `git config core.hooksPath .github/hooks`. Hooks run tests and format checks; see `.github/hooks/README.md`.
- **Commits:** Prefer conventional commits: `type(scope): subject` (e.g. `feat(functions): add rate limit to aiProxy`).
```

**Step 2: Commit**

```bash
git add docs/CONTRIBUTING.md
git commit -m "docs: add CONTRIBUTING.md with build, hooks, and commit guidance"
```

---

## Task 3: Create docs/ARCHITECTURE.md (defer to spec)

**Files:**

- Create: `docs/ARCHITECTURE.md`

**Step 1: Create file**

Content:

```markdown
# NovaChat Architecture

The canonical architecture and product spec is:

**`specs/NOVACHAT_ARCHITECTURE_SPEC.md`**

It covers repository layout, backend paths (callable and HTTP), app structure, tech stack, and codebase areas not covered by older specs. Use it for any design or implementation decisions.
```

**Step 2: Commit**

```bash
git add docs/ARCHITECTURE.md
git commit -m "docs: add ARCHITECTURE.md pointing to spec"
```

---

## Task 4: Create DEVELOPMENT.md (dev setup)

**Files:**

- Create: `DEVELOPMENT.md`

**Step 1: Create file**

Content:

```markdown
# Development Setup

- **Android:** Android Studio or CLI; Kotlin 2.2.x, AGP 9.x, JDK 21. Add `app/google-services.json` for Firebase.
- **Functions:** Node 20+ (see `functions/package.json` engines). Run `cd functions && npm install`. Set `GEMINI_API_KEY` (env or Firebase params) for local run. Emulator: `cd functions && npm run serve` (functions on port 5002; see `firebase.json`).
- **Data Connect (optional):** Emulator config in `firebase.json`; schema and connectors in `dataconnect/`. Generated clients in `generated/dataconnect/` — do not edit by hand.
- **Format/lint:** `npm run format:check` (root); functions: `npm run lint` and `npm test`.
- **Cursor/VS Code:** `.cursor/` and `.vscode/` hold environment and MCP config; see `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §9.7.
```

**Step 2: Commit**

```bash
git add DEVELOPMENT.md
git commit -m "docs: add DEVELOPMENT.md for dev setup"
```

---

## Task 5: Create API.md (backend contracts from spec)

**Files:**

- Create: `API.md`

**Step 1: Create file**

Content:

```markdown
# NovaChat Backend API (summary)

Full contracts and behavior are in **`specs/NOVACHAT_ARCHITECTURE_SPEC.md`** §3. This file is a short reference.

- **Path A — Callable `aiProxy`:** Input `{ message: string; modelParameters?: { temperature?, topK?, topP?, maxOutputTokens? } }`. Output `{ response: string; model: string }`. Auth required.
- **Path B — HTTP submit/status/worker:** Submit POST with Bearer token and body (requestId, conversationId, messageText, etc.); returns 202 with status. Poll status endpoint or observe Firestore `chat_requests/{requestId}` for COMPLETED/FAILED. Worker is internal (Cloud Tasks).
- **Streaming (roadmap):** See spec §3.3 and plan `docs/plans/2026-02-17-streaming-and-app-check.md`.
```

**Step 2: Commit**

```bash
git add API.md
git commit -m "docs: add API.md summarizing backend contracts"
```

---

## Task 6: Create docs/archive placeholder and README

**Files:**

- Create: `docs/archive/README.md`

**Step 1: Create file**

Content:

```markdown
# Archive

Historical material only. Not active implementation guidance. See `specs/NOVACHAT_ARCHITECTURE_SPEC.md` and `docs/plans/` for current work.
```

**Step 2: Commit**

```bash
git add docs/archive/README.md
git commit -m "docs: add docs/archive placeholder"
```

---

## Task 7: Update README.md (active documentation list and generated note)

**Files:**

- Modify: `README.md`

**Step 1: Update "Repository layout"**

Ensure the row for `generated/` says: "Generated artifacts (do not edit by hand). Data Connect and other codegen live here."

**Step 2: Update "Active documentation"**

Replace the list with this (all files must exist after Tasks 1–6):

```markdown
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
```

**Step 3: Update "Contributor instructions"**

Replace with:

```markdown
## Contributor instructions

- Canonical rules: `AGENTS.md`
- Contributing: `docs/CONTRIBUTING.md`
- Copilot adapter: `.github/copilot-instructions.md`
- Path-scoped rules: `.github/instructions/*.instructions.md`
```

**Step 4: Commit**

```bash
git add README.md
git commit -m "docs: align README with docs baseline and existing files"
```

---

## Task 8: Update .github/copilot-instructions.md

**Files:**

- Modify: `.github/copilot-instructions.md`

**Step 1: Replace content**

Ensure it references only existing paths and aligns with baseline:

```markdown
# Copilot Adapter Instructions

- **Canonical rules:** `AGENTS.md`
- **Architecture and product:** `specs/NOVACHAT_ARCHITECTURE_SPEC.md`
- **Path-specific instructions:**
  - `.github/instructions/app.instructions.md`
  - `.github/instructions/feature-ai.instructions.md`
  - `.github/instructions/functions.instructions.md`
  - `.github/instructions/build.instructions.md`
- Treat `docs/archive/` as historical only, not active implementation guidance.
```

**Step 2: Commit**

```bash
git add .github/copilot-instructions.md
git commit -m "docs: align copilot-instructions with docs baseline"
```

---

## Task 9: Update .github/instructions to reference spec

**Files:**

- Modify: `.github/instructions/app.instructions.md`
- Modify: `.github/instructions/feature-ai.instructions.md`
- Modify: `.github/instructions/functions.instructions.md`
- Modify: `.github/instructions/build.instructions.md`

**Step 1: Add one line to each**

In each file, add at the top (after "Applies to:") a line: "See `specs/NOVACHAT_ARCHITECTURE_SPEC.md` for architecture and layout."

(Or a short "Architecture: see specs/NOVACHAT_ARCHITECTURE_SPEC.md" under the Applies to block.)

**Step 2: Commit**

```bash
git add .github/instructions/
git commit -m "docs: point path instructions to architecture spec"
```

---

## Task 10: Update .github/hooks/README.md

**Files:**

- Modify: `.github/hooks/README.md`

**Step 1: Add reference to CONTRIBUTING**

In the Install section or top, add: "See also `docs/CONTRIBUTING.md` for full contributor workflow."

**Step 2: Commit**

```bash
git add .github/hooks/README.md
git commit -m "docs: link hooks README to CONTRIBUTING"
```

---

## Task 11: Update specs/NOVACHAT_ARCHITECTURE_SPEC.md (doc layout and baseline)

**Files:**

- Modify: `specs/NOVACHAT_ARCHITECTURE_SPEC.md`

**Step 1: Add to §2 (Repository layout) a row or note**

In the table or right after it, add a note: "Active docs: `AGENTS.md`, `docs/CONTRIBUTING.md`, `docs/ARCHITECTURE.md`, `DEVELOPMENT.md`, `API.md`, `docs/plans/`, `docs/archive/` (historical). See README 'Active documentation'."

**Step 2: In §7 (Where to put new work) add one bullet**

"Documentation: New active docs go in `docs/` or root and must be listed in README; architecture changes stay in this spec."

**Step 3: Commit**

```bash
git add specs/NOVACHAT_ARCHITECTURE_SPEC.md
git commit -m "docs: spec references doc baseline and README"
```

---

## Task 12: Update functions/README.md (link to spec)

**Files:**

- Modify: `functions/README.md`

**Step 1: Add at top or in "Callable contract" section**

"Full backend contracts and Path A/B behavior: `specs/NOVACHAT_ARCHITECTURE_SPEC.md` §3."

**Step 2: Commit**

```bash
git add functions/README.md
git commit -m "docs: functions README links to architecture spec"
```

---

## Task 13: Fix package.json script if browser-research is missing

**Files:**

- Modify: `package.json`

**Step 1: If `scripts/browser-research.js` does not exist**

Remove the `"browser:research": "node scripts/browser-research.js"` script line, or add a stub `scripts/browser-research.js` that logs "Not implemented". Prefer removing the script to avoid broken commands.

**Step 2: Commit**

```bash
git add package.json
# If you created a stub: git add scripts/browser-research.js
git commit -m "chore: remove or stub broken browser:research script"
```

---

## Execution handoff

Plan complete and saved to `docs/plans/2026-02-17-docs-baseline-practice.md`.

**Execution options:**

1. **Subagent-Driven (this session)** — Use superpowers:subagent-driven-development; one subagent per task with code review.
2. **Parallel Session** — New session with superpowers:executing-plans; run tasks in order with checkpoints.

**Which approach?**

If Subagent-Driven: use subagent-driven-development in this session.  
If Parallel Session: open a new session in the worktree and use executing-plans there.
