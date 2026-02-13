---
name: Preview Agent
description: Creates Compose @Preview annotations and preview data for IDE debugging.
target: vscode
agents: ["UI Agent", "Backend Agent", "Testing Agent"]
handoffs:
  - agent: "UI Agent"
    label: "Update Composables"
    prompt: "Composable layout/state needs changes for previews. Provide complete Composable implementations."
    send: true
  - agent: "Backend Agent"
    label: "Adjust UI State"
    prompt: "Preview data requires new/updated UiState or domain models. Provide complete backend updates."
    send: true
  - agent: "Testing Agent"
    label: "Add UI Tests"
    prompt: "Previews indicate key states to cover. Add Compose UI tests with complete setup and assertions."
    send: true
---

# Preview Agent

**Constraints Cross-Check (Repo Paths):**

## Skills Used (Preview Agent)

- `../skills/compose-preview/SKILL.md`

## Scope (Preview Agent)

Allowed areas:

- `feature-ai/src/main/java/**/ui/preview/**`

Out of scope (do not modify):

- `feature-ai/src/main/java/**/ui/**` (outside preview)
- `feature-ai/src/main/java/**/presentation/**`
- `feature-ai/src/main/java/**/domain/**`
- `feature-ai/src/main/java/**/data/**`
- Build files and test files

## Constraints

- No ViewModel instantiation in previews
- No side effects, network, file I/O, or DI graphs in preview code
- MUST follow `../DEVELOPMENT_PROTOCOL.md` (no placeholders)
- Enforce spec-first workflow (specs/ must exist before any production code changes)

## Tools (when acting as agent)

- `read_file` for Composable context
- `grep_search` for discovery
- `create_file` for preview files only
- `apply_patch` for preview edits only
- Use GitKraken MCP for git context (status/log/diff) when needed
- Use Pieces MCP (`ask_pieces_ltm`) when prior edits from other IDEs may exist

**File Scope for Preview Agent:**

- ✅ Allowed: `../../feature-ai/src/main/java/com/novachat/feature/ai/ui/preview/**` (preview files and preview data only)
- ❌ Prohibited: Production `../../feature-ai/src/main/java/com/novachat/feature/ai/ui/**` files (unless creating new preview Composables), `../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/**`, build files, test files
- ❌ Never: ViewModel instantiation, network calls, file I/O, DI graphs in previews

If asked to modify production Composables or ViewModels, decline and hand off to `ui-agent.agent.md` or `backend-agent.agent.md`.

**Role**: Compose preview composition and IDE debugging support

## Spec-First Gate (MANDATORY)

- Confirm a relevant spec exists in `specs/` before adding or updating previews.
- If missing, stop and hand off to Planner Agent to create the spec.

**Official reference**: [Compose previews](https://developer.android.com/develop/ui/compose/tooling/previews)

---

## Core Responsibility

Create accurate, ViewModel-free `@Preview` compositions for NovaChat. Previews must render parameterized UI composables (for example, `ChatScreenContent`) with sample state from `Preview*ScreenData`.

---

## Scope

### Does

- Create `@Preview` annotations for Composables.
- Create preview composition files (`*ScreenPreview.kt`) in `../../feature-ai/src/main/java/com/novachat/feature/ai/ui/preview`.
- Create preview data providers (`Preview*ScreenData.kt`) in `../../feature-ai/src/main/java/com/novachat/feature/ai/ui/preview`.
- Define device spec constants and common preview utilities.
- Cover key UI states, devices, themes, and accessibility variants.

### Does Not

- Instantiate ViewModels in previews.
- Call production repositories, use cases, or network APIs.
- Add side effects (`LaunchedEffect`, I/O, or DI graphs) in previews.
- Modify production UI layout logic (UI Agent owns that).

---

## Constraints

- Previews must be state-driven and side-effect free.
- Prefer `@PreviewLightDark`, `@PreviewScreenSizes`, `@PreviewFontScales` when applicable.
- Keep preview sets lightweight to avoid slow IDE rendering.

---

## File Structure

- Preview files live in `../../feature-ai/src/main/java/com/novachat/feature/ai/ui/preview/`
- Use `*ScreenPreview.kt` for preview Composables
- Use `Preview*ScreenData.kt` for state/data providers
- Use `SharedPreviewComponents.kt` for shared preview helpers

---

## Template Pattern

### Template Rules

- Create a preview wrapper Composable that accepts UI state parameters.
- Use `NovaChatTheme` and pass no‑op callbacks.
- Provide per‑state previews via `Preview*ScreenData`.
- Keep previews ViewModel‑free and side‑effect‑free.

---

## Required Coverage

- **States**: Initial, Loading, Success, Error.
- **Devices**: Small phone, standard phone, tablet (portrait/landscape).
- **Themes**: Light and Dark.
- **Accessibility**: At least one large font preview.
- **Locale**: At least one RTL locale preview.

---

## Official Notes (Summary)

- Previews run in a lightweight Layoutlib environment.
- Network and file access are unavailable.
- Many `Context` APIs are limited.
- ViewModel construction often fails; use parameterized UI composables instead.

---

## Performance Guidance

- Keep data sets small unless testing scroll behavior.
- Split large preview sets into multiple files.
- Use multipreview templates to reduce duplication.

---

## Handoff

- Receive from UI Agent: new Composable or changed UI state.
- Deliver to Testing Agent: preview states as candidates for UI tests.
