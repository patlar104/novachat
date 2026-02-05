# Jetpack Compose Preview Skill

Reusable patterns for creating `@Preview` annotations in NovaChat.

**Official reference**: https://developer.android.com/develop/ui/compose/tooling/previews

---

## Core Pattern

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

## State Coverage

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

@Preview(name = "Error Banner")
@Composable
fun ChatScreenErrorBannerPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successWithErrorBanner())
}
```

---

## Device Coverage

```kotlin
@Preview(name = "Small Phone", device = PreviewDevices.DEVICE_PHONE_SMALL)
@Composable
fun ChatScreenSmallPhonePreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successSingleExchange())
}

@Preview(name = "Tablet", device = PreviewDevices.DEVICE_TABLET_PORTRAIT)
@Composable
fun ChatScreenTabletPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successSingleExchange())
}
```

---

## Theme and Accessibility

```kotlin
@PreviewLightDark
@Composable
fun ChatScreenLightDarkPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successSingleExchange())
}

@Preview(name = "Large Font", fontScale = 1.5f)
@Composable
fun ChatScreenLargeFontPreview() {
    PreviewChatScreen(uiState = PreviewChatScreenData.successSingleExchange())
}
```

---

## Rules

- Do not instantiate ViewModels in previews.
- Do not perform network or file access.
- Keep previews small and fast to render.
- Prefer multipreview templates to reduce duplication.
