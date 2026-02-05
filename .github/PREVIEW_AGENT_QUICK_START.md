# 🎬 Preview Agent Quick Reference

**TL;DR**: The Preview Agent creates @Preview annotations and preview files for IDE debugging UI/UX.

---

## 30-Second Overview

| Aspect | Details |
|--------|---------|
| **Role** | Create Jetpack Compose @Preview annotations and preview Composables |
| **Input** | New Composable screen from UI Agent |
| **Output** | `*ScreenPreview.kt` file with comprehensive state/device/theme previews |
| **Location** | `app/src/main/java/com/novachat/app/ui/preview/` |
| **When to Use** | After UI Agent creates a new screen |
| **Time to Complete** | 15-30 min per screen (depends on state complexity) |

---

## What Gets Created

For each new screen, Preview Agent creates files in `ui/preview/`:

```
ChatScreenPreview.kt             # 12 @Preview functions
├── Empty State (light + dark)
├── Loading State
├── Success State (multiple messages)
├── Error States (network, API)
├── Light/Dark Theme Variants
└── Device Variants (phone, tablet, landscape)

PreviewChatScreenData.kt         # Sample state/data providers
├── initialState() factory
├── loadingState() factory
├── successSingleExchange() factory
└── successWithErrorBanner() factory
```

---

## Quick Invocation

```bash
@copilot using preview-agent.agent.md

Create previews for ChatScreen including:
- Initial/empty state
- Messages conversation
- Loading while AI processes
- Network error state
- API error state
- Light and dark themes
- Phone and tablet sizes
```

---

## Agent Scope (Simple Version)

### ✅ Preview Agent DOES THIS

- Write `@Preview` annotations
- Create `*ScreenPreview.kt` files
- Create `Preview*ScreenData.kt` sample state providers
- Define device constants  
- Compose light/dark themes
- Document preview purpose

### ❌ Preview Agent DOES NOT DO THIS

- Modify production code
- Implement real ViewModels
- Instantiate ViewModels in preview composables
- Write test files
- Change business logic
- Create layouts/Composables

---

## File Organization

```
ui/
├── ChatScreen.kt              ← UI Agent creates this
├── SettingsScreen.kt          ← UI Agent creates this
├── theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── preview/                   ← Preview Agent creates this folder
    ├── ChatScreenPreview.kt              (12 previews)
    ├── SettingsScreenPreview.kt          (8 previews)
    ├── SharedPreviewComponents.kt        (device constants)
    └── Preview*ScreenData.kt             (sample state providers)
```

---

## Minimal Preview Example

```kotlin
// ChatScreenPreview.kt
@Preview(name = "Empty")
@Composable
fun ChatScreenEmptyPreview() {
    NovaChatTheme {
        Surface {
            ChatScreenContent(
                uiState = PreviewChatScreenData.initialState(),
                draftMessage = "",
                snackbarHostState = remember { SnackbarHostState() },
                onEvent = {},
                onDraftMessageChange = {}
            )
        }
    }
}

// PreviewChatScreenData.kt
object PreviewChatScreenData {
    fun initialState(): ChatUiState = ChatUiState.Initial
    fun successSingleExchange(): ChatUiState = ChatUiState.Success(messages = SAMPLE_DATA)
}
```

---

## Preview Checklist (Copy-Paste)

```
Preview Deliverables:
☐ Empty/Initial state (light + dark)
☐ Loading state
☐ Success state with data
☐ Error state
☐ @PreviewLightDark theme variants
☐ Multiple device sizes (@PreviewScreenSizes)
☐ Specific device previews (phone, tablet, landscape)
☐ All imports explicit
☐ All @Preview functions complete
☐ No production repository calls or ViewModel instantiation
☐ Preview data providers with sample states
☐ Performance: < 3s IDE preview time
☐ Documentation comments
```

---

## Common Device Constants

```kotlin
// Use these in @Preview(device = ...)

DEVICE_PHONE_SMALL     // 360x740 (small phones)
DEVICE_PHONE           // 411x891 (standard)
DEVICE_PHONE_LARGE     // 480x854 (large phones)
DEVICE_TABLET          // 1280x800 (tablets)
DEVICE_PHONE_LANDSCAPE // 854x480 (landscape)
DEVICE_FOLDABLE        // 412x915 (folding phones)
```

---

## State Preview Pattern

Every major UI state gets its own preview:

```kotlin
@Preview fun ChatScreenInitialPreview() { }      // Empty
@Preview fun ChatScreenLoadingPreview() { }      // Loading
@Preview fun ChatScreenSuccessPreview() { }      // With data
@Preview fun ChatScreenErrorPreview() { }        // Error
```

---

## Theme Preview Pattern

```kotlin
// Pattern 1: Separate light + dark previews
@Preview(name = "Light") @Composable
fun ChatScreenLightPreview() {
    NovaChatTheme(darkTheme = false) { ... }
}

@Preview(name = "Dark") @Composable
fun ChatScreenDarkPreview() {
    NovaChatTheme(darkTheme = true) { ... }
}

// Pattern 2: Auto light + dark (preferred)
@PreviewLightDark  @Composable
fun ChatScreenThemePreview() {
    NovaChatTheme { ... }
}
```

---

## Integration with Other Agents

```
UI Agent Creates Screen
        ↓
Preview Agent Creates Previews
        ↓ (gives feedback to UI if preview constraints reveal issues)
Backend Agent Creates ViewModel
        ↓
Preview Agent Updates Preview*ScreenData
        ↓
Testing Agent Creates Automated Tests
        ↓
Preview Agent can be refreshed to test new states
```

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Preview won't compile | Check Theme wrapping: `NovaChatTheme { }` |
| Preview shows blank | Preview `ChatScreenContent` with sample state |
| Slow IDE preview | Use `@Preview(showSystemUi = false)` |
| Missing state | Add `@Preview` for all `ChatUiState` variants |
| Preview data looks wrong | Check `Preview*ScreenData` sample builders |

---

## Performance Tips

```kotlin
// ✅ Fast compilation (< 1s)
@Preview
@Composable
fun ChatScreenFastPreview() {
    NovaChatTheme(useDynamicColor = false) {
        ChatScreenContent(
            uiState = PreviewChatScreenData.initialState(),
            draftMessage = "",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}

// ❌ Slow compilation (> 5s)
@Preview
@Composable
fun ChatScreenSlowPreview() {
    // Heavy data, animations, complex logic
    val data = List(1000) { generateExpensiveData() }
    ...
}
```

---

## When Preview Agent Hands Off

**To Testing Agent**:
```
"Preview file complete. Ready for automated UI tests
using ComposeTestRule in AndroidTest."
```

**To UI Agent** (if preview reveals issues):
```
"Preview ChatScreenPreview shows error banner overlaps message list.
Adjust padding in ChatScreen at line 82."
```

---

## Files Modified/Created

### New Agent
- ✅ `.github/agents/preview-agent.agent.md` (full documentation)

### New Skill
- ✅ `.github/skills/compose-preview/SKILL.md` (reusable patterns)

### Updated Documentation
- ✅ `.github/AGENTS.md` (agent overview updated)
- ✅ `.github/copilot-instructions.md` (Preview Agent added)
- ✅ `.github/PREVIEW_AGENT_INTEGRATION.md` (this guide)

---

## Resources

| Resource | Purpose |
|----------|---------|
| [preview-agent.agent.md] | Complete Preview Agent specifications |
| [compose-preview/SKILL.md] | Reusable preview patterns and examples |
| [AGENTS.md] | Multi-agent system overview |
| [copilot-instructions.md] | All agent guidance (includes Preview) |

---

## Pro Tips

1. **Group previews by section**: Use comments to separate empty/loading/success/error
2. **Use factory builders**: `PreviewChatScreenData.initialState()`, `.successSingleExchange()`, `.successWithErrorBanner()`
3. **Show multiple states**: Don't just show success state - show errors & loading too
4. **Test on devices**: Use `@PreviewScreenSizes` and device constants
5. **Document why**: Add comments explaining what each preview tests
6. **Keep it light**: Previews should compile fast (< 3 seconds)

---

## Example: Complete Screen Preview File

```kotlin
// ChatScreenPreview.kt - Full example
package com.novachat.app.ui.preview

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import com.novachat.app.ui.ChatScreenContent
import com.novachat.app.ui.theme.NovaChatTheme

// === EMPTY STATE ===
@Preview(name = "Empty")
@Composable
fun ChatScreenEmptyPreview() {
    NovaChatTheme {
        ChatScreenContent(
            uiState = PreviewChatScreenData.initialState(),
            draftMessage = "",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}

// === LOADING STATE ===
@Preview(name = "Loading")
@Composable
fun ChatScreenLoadingPreview() {
    NovaChatTheme {
        ChatScreenContent(
            uiState = PreviewChatScreenData.loadingState(),
            draftMessage = "",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}

// === SUCCESS STATE ===
@Preview(name = "With Messages")
@Composable
fun ChatScreenSuccessPreview() {
    NovaChatTheme {
        ChatScreenContent(
            uiState = PreviewChatScreenData.successSingleExchange(),
            draftMessage = "",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}

// === ERROR STATE ===
@Preview(name = "Network Error")
@Composable
fun ChatScreenErrorPreview() {
    NovaChatTheme {
        ChatScreenContent(
            uiState = PreviewChatScreenData.successWithErrorBanner(),
            draftMessage = "",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}

// === MULTI DEVICE & THEME ===
@PreviewLightDark
@Composable
fun ChatScreenThemePreview() {
    NovaChatTheme {
        ChatScreenContent(
            uiState = PreviewChatScreenData.successSingleExchange(),
            draftMessage = "",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}

@PreviewScreenSizes
@Composable
fun ChatScreenDevicesPreview() {
    NovaChatTheme {
        ChatScreenContent(
            uiState = PreviewChatScreenData.successSingleExchange(),
            draftMessage = "",
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onDraftMessageChange = {}
        )
    }
}
```

---

## Ready to Use!

The Preview Agent is now part of your NovaChat development system.

**Next**: Invoke it for your next screen! 🚀

```bash
@copilot using preview-agent.agent.md

Create comprehensive previews for [ScreenName]
```
