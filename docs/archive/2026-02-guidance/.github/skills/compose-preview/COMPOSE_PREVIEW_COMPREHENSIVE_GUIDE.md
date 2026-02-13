# Jetpack Compose @Preview Annotations - Comprehensive Guide

**Document Version**: 1.1
**Last Updated**: February 2026
**Compose BOM**: 2026.01.01 (Google Maven; mapping: [BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping))
**Target**: Android 28+ (API 28)

---

**NovaChat rule (Feb 2026)**: Do not pass a `ViewModel` into previews. Preview a parameterized UI composable (for example, `ChatScreenContent`) with sample `ChatUiState` from `Preview*ScreenData`.

**Official reference**: [Compose previews](https://developer.android.com/develop/ui/compose/tooling/previews)

## Overview

`@Preview` renders composables in Android Studio without running the app. It supports fast iteration, multi-state checks, and device/accessibility coverage. Official guidance emphasizes that previews work best when UI is driven by state and events, not by ViewModel construction.

---

## @Preview Parameters (Summary)

- `name`: Label shown in the Preview pane.
- `group`: Groups related previews together.
- `widthDp` / `heightDp`: Custom dimensions in dp.
- `device`: Predefined device id or a custom spec string (for example, `spec:width=411dp,height=891dp,dpi=420`).
- `showBackground` / `backgroundColor`: Background rendering and ARGB color (Long).
- `showSystemUi`: Show status and nav bars.
- `uiMode`: Night mode and other UI mode flags.
- `fontScale`: Accessibility font scaling.
- `locale`: Locale tag (BCP 47).
- `apiLevel`: Preview API level (compile-time only).

---

## Multipreview Templates

Android Studio supports multipreview templates like `@PreviewScreenSizes`, `@PreviewFontScales`, and `@PreviewLightDark` so you can render multiple variants without repeating each `@Preview`.

---

## Preview Data and Helpers (NovaChat Pattern)

### Preview Data Provider

Rules:

- Use a `Preview*ScreenData` object with functions per state.
- Return complete `UiState` objects with realistic sample data.

### Preview Helper

Rules:

- Use a preview helper composable that accepts `UiState`.
- Wrap with `NovaChatTheme` and pass no‑op callbacks.

---

## Example: ChatScreen Previews

Rules:

- Provide previews for Initial, Loading, Success, Error Banner, Tablet, Large Font, and RTL.

---

## LocalInspectionMode

Use `LocalInspectionMode.current` to substitute placeholder data in previews without triggering real work. This avoids network calls or file access when running in preview.

---

## Limitations (From Official Guidance)

- Preview runs in a lightweight environment (Layoutlib), not a full device runtime.
- Network and file access are not available.
- Many `Context` APIs are limited.
- ViewModel construction often fails because DI graphs and dependencies are unavailable.

---

## Performance Tips

- Keep previews lightweight and focused.
- Limit previews per file (8-12 is a good target).
- Use `@PreviewScreenSizes` or `@PreviewLightDark` to reduce duplication.
- Use small sample datasets unless testing scroll behavior.

---

## Testing Previews

Previews are not tests, but you can mirror preview states in Compose UI tests to validate key UI paths.

Rules:

- Mirror preview states in Compose UI tests when needed.
- Use `createComposeRule()` and assert key UI text.
