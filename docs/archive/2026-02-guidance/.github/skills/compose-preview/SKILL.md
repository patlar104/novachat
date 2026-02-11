---
name: compose-preview
description: Complete preview patterns for NovaChat (NO placeholders)
category: preview
applies_to:
    - "**/ui/preview/**/*.kt"
protocol_compliance: true
note: All examples are COMPLETE and runnable - following DEVELOPMENT_PROTOCOL.md
---

# Jetpack Compose Preview Skill

Reusable patterns for creating `@Preview` annotations in NovaChat.

**Official reference**: https://developer.android.com/develop/ui/compose/tooling/previews

## Multi-Agent Coordination

### When the Preview Agent Should Use Tools

**Use tools immediately for:**
- Reading Composable files to understand structure → `read_file`
- Creating preview files → `create_file`
- Adding @Preview annotations to existing files → `apply_patch`
- Searching for Composable patterns → `grep_search` or `semantic_search`
- Running preview validation → `run_in_terminal`

**Do NOT describe; DO implement:**
- Don't say "add @Preview annotations"; add them using `apply_patch`
- Don't say "create a preview file"; create it using `create_file`
- Don't say "check what preview functions exist"; search using `grep_search`

### When to Hand Off to Other Agents

**Hand off to UI Agent if:**
- Composable function doesn't exist and needs creation
- Composable layout/logic needs changes
- Material Design implementation needs adjustment
- State management in Composable needs fixes
- → **Action**: Report which Composables need creation/fixes

**Hand off to Backend Agent if:**
- ViewModel or state management needs changes
- Preview data providers need production logic
- Use case integration is needed
- → **Action**: Report what state/data is needed in previews

**Hand off to Build Agent if:**
- Preview dependencies are missing
- Gradle configuration affects preview compilation
- → **Action**: Report missing dependencies or build issues

### Preview Task Assessment

**Determine scope before acting:**

1. **Is this a preview task?**
   - Creating @Preview annotations → YES, use Preview Agent tools
   - Creating PreviewData objects → YES, use Preview Agent tools
   - Creating Composable functions → NO, hand off to UI Agent
   - Modifying component behavior → NO, hand off to UI Agent

2. **Do I have all context needed?**
   - Does the Composable exist? → Check with `grep_search` or `read_file`
   - What states should be previewed? → Review UiState sealed interface
   - What devices/themes to show? → Reference existing preview patterns

3. **Is this within Preview Agent scope?**
   - Adding @Preview annotations → YES ✓
   - Creating preview data providers → YES ✓
   - Creating preview helper Composables → YES ✓
   - Fixing Composable logic → NO, hand off to UI Agent
   - Creating ViewModels → NO, hand off to Backend Agent
   - Creating use cases → NO, hand off to Backend Agent

---

## Core Pattern

Rules:

- Use a preview helper composable that accepts `UiState` and optional params.
- Wrap preview content with `NovaChatTheme`.
- Provide empty lambdas for event handlers.

---

## State Coverage

Rules:

- Provide previews for Initial, Loading, Success, and Error states.
- Use descriptive `@Preview(name = "...")` labels.

---

## Device Coverage

Rules:

- Cover small phone, standard phone, and tablet device previews.
- Use device constants from `PreviewDevices`.

---

## Theme and Accessibility

Rules:

- Include `@PreviewLightDark` for theme coverage.
- Include large font scale and RTL locale previews.

---

## Rules

- Do not instantiate ViewModels in previews.
- Do not perform network or file access.
- Keep previews small and fast to render.
- Prefer multipreview templates to reduce duplication.
