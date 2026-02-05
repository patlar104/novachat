# Jetpack Compose Preview Skill

Reusable patterns for creating `@Preview` annotations in NovaChat.

**Official reference**: https://developer.android.com/develop/ui/compose/tooling/previews

## Multi-Agent Coordination

### When the Preview Agent Should Use Tools

**Use tools immediately for:**
- Reading Composable files to understand structure → `read_file`
- Creating preview files → `create_file`
- Adding @Preview annotations to existing files → `replace_string_in_file`
- Searching for Composable patterns → `grep_search` or `semantic_search`
- Running preview validation → `run_in_terminal`

**Do NOT describe; DO implement:**
- Don't say "add @Preview annotations"; add them using `replace_string_in_file`
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
