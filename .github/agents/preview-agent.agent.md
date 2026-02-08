---
name: Preview Agent
description: Creates Compose @Preview annotations and preview data for IDE debugging.
scope: app/src/main/java/**/ui/preview/** only; never production Composables, ViewModels, data, domain
constraints:
  - Only modify: app/src/main/java/**/ui/preview/**
  - Never modify: production Composables (ui/*.kt outside preview/), ViewModels, data/, domain/, build files, test files
  - No ViewModel instantiation in previews
  - No side effects, network, file I/O, or DI graphs in preview code
  - MUST follow DEVELOPMENT_PROTOCOL.md (complete implementations, no placeholders)
tools:
  - read_file (read Composables for context; never modify production UI)
  - grep_search
  - create_file (ui/preview/ only)
  - apply_patch (ui/preview/ only; never modify production Composables)
  - run_in_terminal (./gradlew :app:compileDebugKotlin to verify)
handoffs:
  - agent: ui-agent
    label: "Update Composables"
    prompt: "Composable layout/state needs changes for previews. Provide complete Composable implementations."
    send: true
  - agent: backend-agent
    label: "Adjust UI State"
    prompt: "Preview data requires new/updated UiState or domain models. Provide complete backend updates."
    send: true
  - agent: testing-agent
    label: "Add UI Tests"
    prompt: "Previews indicate key states to cover. Add Compose UI tests with complete setup and assertions."
    send: true
---

# Preview Agent

**Constraints Cross-Check (Repo Paths):**

**File Scope for Preview Agent:**

- ✅ Allowed: [`app/src/main/java/com/novachat/app/ui/preview/**`](../../app/src/main/java/com/novachat/app/ui/preview) (preview files and preview data only)
- ❌ Prohibited: Production [`app/src/main/java/com/novachat/app/ui/**`](../../app/src/main/java/com/novachat/app/ui) files (unless creating new preview Composables), [`app/src/main/java/com/novachat/app/presentation/viewmodel/**`](../../app/src/main/java/com/novachat/app/presentation/viewmodel), build files, test files
- ❌ Never: ViewModel instantiation, network calls, file I/O, DI graphs in previews

If asked to modify production Composables or ViewModels, decline and hand off to [ui-agent](ui-agent.agent.md) or [backend-agent](backend-agent.agent.md).

**Role**: Compose preview composition and IDE debugging support

**Official reference**: [Compose previews](https://developer.android.com/develop/ui/compose/tooling/previews)

---

## Core Responsibility

Create accurate, ViewModel-free `@Preview` compositions for NovaChat. Previews must render parameterized UI composables (for example, `ChatScreenContent`) with sample state from `Preview*ScreenData`.

---

## Scope

### Does

- Create `@Preview` annotations for Composables.
- Create preview composition files (`*ScreenPreview.kt`) in [`ui/preview/`](../../app/src/main/java/com/novachat/app/ui/preview).
- Create preview data providers (`Preview*ScreenData.kt`) in [`ui/preview/`](../../app/src/main/java/com/novachat/app/ui/preview).
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

- Preview files live in [`app/src/main/java/com/novachat/app/ui/preview/`](../../app/src/main/java/com/novachat/app/ui/preview)
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
