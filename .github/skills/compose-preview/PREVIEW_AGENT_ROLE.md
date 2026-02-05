# Preview Agent Role Definition

**Version**: 1.1
**Date**: February 2026
**Project**: NovaChat
**Technology**: Jetpack Compose Preview

**Official reference**: https://developer.android.com/develop/ui/compose/tooling/previews

---

## Role Overview

Preview Agent creates ViewModel-free `@Preview` compositions that render parameterized UI composables with sample state (`Preview*ScreenData`).

---

## Responsibilities

1. Plan preview coverage for new screens/components.
2. Create state, device, theme, and accessibility previews.
3. Provide preview data providers (sample state builders).
4. Keep previews lightweight and side-effect free.
5. Document preview patterns for the team.

---

## Preview Coverage Checklist

- Initial, Loading, Success, Error states.
- Small phone, standard phone, tablet (portrait/landscape).
- Light + Dark themes.
- At least one large font scale.
- At least one RTL locale.

---

## Preview Infrastructure

```
.github/skills/compose-preview/
├── COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md
├── examples/
│   ├── PreviewScreenData.kt
│   ├── ChatScreenPreview.kt
│   └── SettingsScreenPreview.kt
└── PREVIEW_AGENT_ROLE.md
```

---

## Example Pattern

```kotlin
@Composable
fun PreviewChatScreen(
    uiState: ChatUiState,
    draftMessage: String = ""
) {
    val snackbarHostState = remember { SnackbarHostState() }

    NovaChatTheme {
        ChatScreenContent(
            uiState = uiState,
            draftMessage = draftMessage,
            snackbarHostState = snackbarHostState,
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}
```

---

## Constraints

- No ViewModel instantiation in previews.
- No network, file I/O, or DI graphs in previews.
- Use multipreview templates where possible.
- Keep IDE rendering fast.
