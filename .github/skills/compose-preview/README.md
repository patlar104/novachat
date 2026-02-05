# Jetpack Compose Preview System - Documentation

**Version**: 1.1
**Date**: February 2026

**Official reference**: https://developer.android.com/develop/ui/compose/tooling/previews

---

## Documentation Map

1. COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md - Official-doc-aligned preview guidance.
2. PREVIEW_AGENT_ROLE.md - Preview Agent responsibilities and constraints.
3. examples/PreviewScreenData.kt - Sample preview state builders.
4. examples/ChatScreenPreview.kt - ViewModel-free preview patterns.
5. examples/SettingsScreenPreview.kt - ViewModel-free preview patterns.

---

## Key Rules (NovaChat)

- Do not pass ViewModels into previews.
- Render parameterized UI composables (for example, `ChatScreenContent`).
- Use `Preview*ScreenData` for state.
- Avoid side effects and real data calls.

---

## Quick Example

```kotlin
@Preview(name = "Initial")
@Composable
fun ChatScreenInitialPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.initialState())
}
```
