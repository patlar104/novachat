# Jetpack Compose @Preview Annotations - Comprehensive Guide

**Document Version**: 1.1
**Last Updated**: February 2026
**Compose BOM**: 2026.01.01
**Target**: Android 28+ (API 28)

---

**NovaChat rule (Feb 2026)**: Do not pass a `ViewModel` into previews. Preview a parameterized UI composable (for example, `ChatScreenContent`) with sample `ChatUiState` from `Preview*ScreenData`.

**Official reference**: https://developer.android.com/develop/ui/compose/tooling/previews

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

```kotlin
object PreviewChatScreenData {
    fun initialState(): ChatUiState = ChatUiState.Initial

    fun successSingleExchange(): ChatUiState {
        val now = Instant.now()
        return ChatUiState.Success(
            messages = listOf(
                Message(
                    id = MessageId("user-1"),
                    content = "Hello!",
                    sender = MessageSender.USER,
                    timestamp = now
                ),
                Message(
                    id = MessageId("ai-1"),
                    content = "Hi there!",
                    sender = MessageSender.ASSISTANT,
                    timestamp = now.plusSeconds(2)
                )
            )
        )
    }

    fun successWithErrorBanner(): ChatUiState {
        val now = Instant.now()
        return ChatUiState.Success(
            messages = listOf(
                Message(
                    id = MessageId("user-2"),
                    content = "This message failed to send.",
                    sender = MessageSender.USER,
                    timestamp = now
                )
            ),
            error = "Network error. Tap to retry."
        )
    }
}
```

### Preview Helper

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

## Example: ChatScreen Previews

```kotlin
@Preview(name = "Initial")
@Composable
fun ChatScreenInitialPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.initialState())
}

@Preview(name = "Loading")
@Composable
fun ChatScreenLoadingPreview() {
    PreviewChatScreen(uiState = ChatUiState.Loading)
}

@Preview(name = "Success - Error Banner")
@Composable
fun ChatScreenErrorBannerPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successWithErrorBanner())
}

@Preview(name = "Tablet", device = "spec:width=600dp,height=800dp,dpi=160")
@Composable
fun ChatScreenTabletPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successSingleExchange())
}

@Preview(name = "Large Font", fontScale = 1.5f)
@Composable
fun ChatScreenLargeFontPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successSingleExchange())
}

@Preview(name = "Arabic (RTL)", locale = "ar-SA")
@Composable
fun ChatScreenArabicPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successSingleExchange())
}
```

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

```kotlin
@get:Rule
val composeRule = createComposeRule()

@Test
fun chatScreenInitialPreviewRenders() {
    composeRule.setContent {
        PreviewChatScreen(uiState = PreviewChatScreenData.initialState())
    }
    composeRule.onNodeWithText("Start a conversation!").assertExists()
}
```
