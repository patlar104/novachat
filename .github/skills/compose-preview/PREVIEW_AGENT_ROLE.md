# Preview Agent Role Definition

**Version**: 1.0
**Date**: February 2026
**Project**: NovaChat
**Technology**: Jetpack Compose, Android Studio Preview System

---

## Role Overview

**Preview Agent** is a specialized agent role within the NovaChat multi-agent development system responsible for designing, implementing, creating, and validating Jetpack Compose `@Preview` composables. Preview Agents ensure that all screens and components have comprehensive preview coverage for rapid iteration during development.

### Agent Responsibilities

1. **Design Preview Strategy** - Plan preview coverage for new screens/components
2. **Implement Preview Composables** - Create `@Preview` annotations and helper functions
3. **Test Preview Rendering** - Validate previews render correctly in IDE
4. **Create Preview Utilities** - Develop mock ViewModels and test data builders
5. **Optimize Preview Performance** - Keep compilation times fast
6. **Maintain Preview Organization** - Keep previews organized and discoverable
7. **Document Preview Patterns** - Write guides for other agents on preview best practices

### Key Distinction from Other Agents

| Agent | Preview Focus | Who Creates Previews? |
|-------|---------------|----------------------|
| **UI Agent** | Implements composables | Creates component previews |
| **Backend Agent** | Implements ViewModels | Provides mock ViewModels |
| **Preview Agent** | Creates all previews | Creates comprehensive preview coverage |
| **Testing Agent** | Tests composables | Validates preview rendering works |
| **Build Agent** | Manages build system | Monitors preview compilation times |

---

## Preview Agent Focus Areas

### 1. **Preview Coverage** [CRITICAL]

Ensure every screen and significant component has:

- [x] **State Previews**: All UI states (Loading, Success, Error, Empty)
- [x] **Device Previews**: Compact phone, standard phone, large phone, tablet, foldable
- [x] **Theme Previews**: Light mode, Dark mode
- [x] **Accessibility Previews**: Normal, Large (1.5x), Extra Large (2x) font scales
- [x] **Localization Previews**: At least English and 1 RTL language (Arabic)
- [x] **Component Previews**: Individual components in isolation with multiple states

**Example Coverage Checklist for New Screen:**
```
✅ Initial state preview
✅ Loading state preview
✅ Success state preview(s) - at least 2 variants
✅ Error state preview(s) - recoverable + non-recoverable
✅ Compact phone (320dp) preview
✅ Standard phone (412dp) preview  
✅ Large phone (480dp) preview
✅ Tablet portrait (600dp) preview
✅ Light theme preview
✅ Dark theme preview
✅ Normal font (1x) preview
✅ Large font (1.5x) preview
✅ Extra large font (2x) preview
✅ English locale preview
✅ RTL locale (Arabic) preview

= 16 previews minimum for a typical screen
```

### 2. **Preview Infrastructure** [CRITICAL]

Establish and maintain:

```
.github/skills/compose-preview/
├── COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md
├── examples/
│   ├── PreviewViewModels.kt      ← Mock ViewModel factories
│   ├── PreviewData.kt            ← Test data builders
│   ├── ChatScreenPreview.kt      ← Full screen previews
│   ├── SettingsScreenPreview.kt  ← Another screen previews
│   └── PreviewDevices.kt         ← Device specs (optional)
└── PREVIEW_AGENT_ROLE.md         ← This file
```

**Required Files for Each Composable:**
- `XxxScreenPreview.kt` - All screen previews
- `PreviewXxxViewModel.kt` - Mock ViewModels (in `previews/` package)
- `PreviewData.kt` - Test data (shared, in `previews/` package)
- Components can have previews in same file or separate `XxxComponentPreview.kt`

### 3. **Mock ViewModel Strategy** [CRITICAL]

Create proper mock ViewModels in `ui/previews/PreviewViewModels.kt`:

```kotlin
fun createPreviewChatViewModel(
    initialState: ChatUiState = /* default state */,
    draftMessage: String = ""
): ChatViewModel {
    // Create properly configured mockk<ChatViewModel>
    // All StateFlows must be initialized
    // All flows must respond to events appropriately
    // NO actual use cases/repositories
}
```

**Key Constraints:**
- Never instantiate real ViewModels with actual dependencies
- Mock all uses cases with `mockk<UseCase>(relaxed = true)`
- Initialize StateFlow properties explicitly
- Implement realistic event handling (state updates on events)
- Return empty Flow for effects (no side effects in preview)

### 4. **Test Data Organization** [IMPORTANT]

Create reusable test data sets in `ui/previews/PreviewData.kt`:

```kotlin
// Short conversation for quick testing
val shortTestMessages = listOf(...)

// Standard conversation for typical previews  
val testMessages = listOf(...)

// Long conversation for scroll/performance testing
val longTestMessages = listOf(...)

// Messages with specific characteristics
val longMessageTestMessages = listOf(...)
val emojiTestMessages = listOf(...)
```

**Data Builder Functions:**
```kotlin
fun previewUserMessage(content: String, id: String = ...): Message
fun previewAiMessage(content: String, id: String = ...): Message
fun conversationFrom(vararg messages: Message): List<Message>
```

### 5. **Preview Organization** [IMPORTANT]

Organize previews hierarchically using `group` parameter:

```kotlin
@Preview(group = "ChatScreen/States", name = "Initial")
@Preview(group = "ChatScreen/States", name = "Loading")
@Preview(group = "ChatScreen/Devices", name = "Compact Phone")
@Preview(group = "ChatScreen/Themes", name = "Light Mode")
@Preview(group = "Components/MessageBubble", name = "User Message")
```

**Resulting IDE hierarchy:**
```
Previews
├── ChatScreen/
│   ├── States/
│   │   ├── Initial
│   │   ├── Loading
│   │   └── ...
│   ├── Devices/
│   │   ├── Compact Phone
│   │   ├── Standard Phone
│   │   └── ...
│   └── Themes/
│       ├── Light Mode
│       └── Dark Mode
└── Components/
    ├── MessageBubble/
    └── ...
```

### 6. **Preview Performance** [IMPORTANT]

Optimize preview compilation:

**DO:**
- ✅ Keep state previews focused (one state per preview)
- ✅ Reuse test data objects (don't recreate)
- ✅ Use mockk for dependencies
- ✅ Split large preview files (>20 previews per file)
- ✅ Profile compilation times regularly

**DON'T:**
- ❌ Create >30 previews in single file
- ❌ Perform network/database operations
- ❌ Create real ViewModels
- ❌ Launch heavy background tasks
- ❌ Include animations that run continuously

**Monitoring:**
```bash
# Measure preview compilation time
./gradlew assembleDebug --profile
cat build/reports/profile/profile-*.html
# Look for preview files taking > 5 seconds
```

### 7. **Preview Validation** [IMPORTANT]

Before considering a preview complete:

- [ ] Renders without errors in IDE
- [ ] Displays correct state/content
- [ ] Text is readable (no truncation at normal size)
- [ ] Colors appropriate for theme (light/dark)
- [ ] Layout responsive (no cutoff at screen edges)
- [ ] Accessible text sizes visible (1.5x, 2x scales)
- [ ] RTL languages properly mirrored
- [ ] System UI renders correctly when `showSystemUi=true`
- [ ] No unwanted spacing/alignment issues
- [ ] Loading indicators show meaningful progress

---

## Preview Agent Operations

### Creating Previews for a New Screen

**Step 1: Understand the Screen's States**
```kotlin
// Review the sealed interface
sealed interface ChatUiState {
    data object Initial : ChatUiState
    data object Loading : ChatUiState  
    data class Success(...) : ChatUiState
    data class Error(...) : ChatUiState
}

// Plan previews for each state
```

**Step 2: Create State Previews**
```kotlin
// ChatScreenPreview.kt
@Preview(name = "Initial", group = "ChatScreen/States")
@Composable
fun ChatScreenInitialPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Initial
            ),
            onNavigateToSettings = {}
        )
    }
}
// ... repeat for each state
```

**Step 3: Add Device Previews**
```kotlin
@Preview(device = "spec:width=320dp,height=640dp", group = "ChatScreen/Devices")
@Composable
fun ChatScreenCompactPhonePreview() { /* ... */ }

@Preview(device = Devices.PIXEL_6, group = "ChatScreen/Devices")
@Composable
fun ChatScreenStandardPhonePreview() { /* ... */ }
// ... etc
```

**Step 4: Add Theme Previews**
```kotlin
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, group = "ChatScreen/Themes")
@Composable
fun ChatScreenLightPreview() { /* ... */ }

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, group = "ChatScreen/Themes")
@Composable
fun ChatScreenDarkPreview() { /* ... */ }
```

**Step 5: Add Accessibility Previews**
```kotlin
@Preview(fontScale = 1f, group = "ChatScreen/Accessibility")
@Composable
fun ChatScreenNormalFontPreview() { /* ... */ }

@Preview(fontScale = 1.5f, group = "ChatScreen/Accessibility")
@Composable
fun ChatScreenLargeFontPreview() { /* ... */ }

@Preview(fontScale = 2f, group = "ChatScreen/Accessibility")
@Composable
fun ChatScreenExtraLargeFontPreview() { /* ... */ }
```

**Step 6: Create Component Previews (if applicable)**
```kotlin
// In same file or separate file
@Preview(group = "Components/MessageBubble")
@Composable
fun MessageBubbleUserPreview() { /* ... */ }

@Preview(group = "Components/MessageBubble")
@Composable
fun MessageBubbleAiPreview() { /* ... */ }
```

**Step 7: Test Rendering**
- Open Preview pane in Android Studio
- Verify all previews render without errors
- Check visual appearance matches expectations
- Test preview interactions if applicable

### Updating Previews When UI Changes

**When UI State Changes:**
```kotlin
// Before: Old state structure
sealed interface ChatUiState {
    data class Success(val messages: List<Message>)  // Old
}

// After: New state structure
sealed interface ChatUiState {
    data class Success(
        val messages: List<Message>,
        val isProcessing: Boolean,  // New field
        val error: String?
    )
}

// Update all Success previews to use new parameters
@Preview
@Composable
fun ChatScreenSuccessPreview() {
    ChatScreen(
        viewModel = createPreviewChatViewModel(
            initialState = ChatUiState.Success(
                messages = testMessages,
                isProcessing = false,        // New
                error = null                 // New
            )
        ),
        onNavigateToSettings = {}
    )
}
```

**When ViewModel Events Change:**
```kotlin
// Update event handling in mock ViewModel
every { mockViewModel.onEvent(any()) } answers {
    val event = firstArg<ChatUiEvent>()
    when (event) {
        is ChatUiEvent.SendMessage -> { /* update */ }
        is ChatUiEvent.NewEvent -> { /* new handler */ }  // New
        // ...
    }
}
```

### Review Checklist for Preview Pull Requests

When reviewing PR with preview changes:

- [ ] All new states/screens have preview coverage
- [ ] At least 16 previews per full screen (minimum)
- [ ] Previews organized with meaningful group names
- [ ] Mock ViewModels in `ui/previews/` package
- [ ] Test data in `PreviewData.kt` (reusable)
- [ ] No real ViewModel instantiation
- [ ] No network/database calls in preview code
- [ ] Component previews in separate logical groups
- [ ] All previews render without errors
- [ ] Device/theme/accessibility coverage complete
- [ ] Performance acceptable (preview compilation < 5s per file)
- [ ] Documentation updated if preview patterns changed

---

## Collaboration with Other Agents

### With UI Agent

**When UI Agent creates composable:**
1. UI Agent implements screen/component
2. Preview Agent creates comprehensive previews
3. Previews become reference for UI Agent iteration

**Communication:**
- UI Agent: "Here's the ChatScreen composable"
- Preview Agent: "I'll create preview coverage for it"
- Both iterate: Previews help validate UI, UI code provides states for previews

### With Backend Agent

**When Backend Agent changes ViewModel:**
1. Backend Agent updates state/event definitions
2. Preview Agent updates mock ViewModels and previews to match
3. Previews validate state transitions work visually

**Communication:**
- Backend Agent: "ChatUiState now has isProcessing flag"
- Preview Agent: "Updating preview utilities to support new state"
- Testing Agent: "New state renders correctly in previews"

### With Testing Agent

**When Testing Agent writes UI tests:**
1. Testing Agent uses preview functions as reference
2. Preview functions provide mock ViewModels for testing
3. Preview data builders create test data

**Communication:**
- Testing Agent: "Need test data for 5 user messages"
- Preview Agent: "Using previewUserMessage() from PreviewData.kt"
- Testing Agent: "Preview setup helps ensure test data matches production patterns"

### With Build Agent

**For monitoring preview compilation:**
1. Build Agent tracks preview compilation times
2. Preview Agent optimizes if times exceed threshold
3. Alerts if large previews added

**Communication:**
- Build Agent: "Preview compilation taking 7s, above 5s threshold"
- Preview Agent: "Splitting ChatScreenPreview.kt into ComponentPreview.kt"
- Build Agent: "Compilation back to 3s ✅"

---

## Tools & Commands

### useful Tools for Preview Agent

```bash
# View all previews in a file
grep -n "@Preview" ChatScreen.kt

# Check for preview-related compilation issues
./gradlew assembleDebug 2>&1 | grep -i preview

# Profile preview compilation time
./gradlew assembleDebug --profile
open build/reports/profile/profile-*.html

# Search for @Preview annotations
find app/src/main -name "*Preview.kt" | wc -l

# Validate preview code compiles
./gradlew compileDebugKotlin
```

### Android Studio Keyboard Shortcuts for Preview Work

| Shortcut | Action |
|----------|--------|
| `Cmd+Alt+P` (Mac) | Toggle Preview pane |
| `Cmd+B` (Mac) | Jump to definition from preview |
| `Cmd+E` (Mac) | Recent files (navigate preview files) |
| Click preview name | Jump to preview function |
| Hover over preview | Quick preview tooltip |

---

## Common Preview Patterns

### Pattern 1: Full Screen Preview with Multiple States

```kotlin
@Preview(name = "State: Success", group = "ChatScreen/States")
@Composable
fun ChatScreenSuccessPreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(
                initialState = ChatUiState.Success(messages = testMessages)
            ),
            onNavigateToSettings = {}
        )
    }
}
```

### Pattern 2: Component Preview Variants

```kotlin
@Preview(name = "User Message", group = "Components/MessageBubble", showBackground = true)
@Composable
fun MessageBubbleUserPreview() {
    NovaChatTheme {
        MessageBubble(message = previewUserMessage("Hello"))
    }
}

@Preview(name = "AI Message", group = "Components/MessageBubble", showBackground = true)
@Composable
fun MessageBubbleAiPreview() {
    NovaChatTheme {
        MessageBubble(message = previewAiMessage("Response"))
    }
}
```

### Pattern 3: Device & Theme Coverage

```kotlin
@Preview(device = Devices.PIXEL_6, group = "ChatScreen/Devices/Light")
@Composable
fun ChatScreenPixel6LightPreview() { /* ... */ }

@Preview(
    device = Devices.PIXEL_6, 
    uiMode = Configuration.UI_MODE_NIGHT_YES, 
    group = "ChatScreen/Devices/Dark"
)
@Composable
fun ChatScreenPixel6DarkPreview() { /* ... */ }
```

### Pattern 4: Accessibility Testing

```kotlin
@Preview(fontScale = 1f, group = "ChatScreen/Accessibility")
@Composable
fun ChatScreenNormalFontPreview() { /* ... */ }

@Preview(fontScale = 2f, group = "ChatScreen/Accessibility")
@Composable
fun ChatScreenExtraLargeFontPreview() { /* ... */ }
```

---

## Success Criteria

### Preview Agent Deliverables

✅ **Comprehensive Coverage**
- Every screen has ≥16 previews
- Every significant component has ≥3 state variants
- Device coverage: compact, standard, large phones + tablet
- Theme coverage: light and dark
- Accessibility: 1x, 1.5x, 2x font scales
- Localization: English + RTL (Arabic)

✅ **Quality**
- All previews render without errors
- Visual appearance matches design
- Text readable at all scales
- Colors correct for each theme
- Components properly sized/spaced
- No cutoff or truncation

✅ **Performance**
- Preview compilation < 5s per file
- < 20 previews per single file
- Mock ViewModels initialize quickly
- No network/database calls
- Hot reload working (< 2s)

✅ **Organization**
- Previews in logical `*Preview.kt` files
- Component previews grouped
- Meaningful preview names
- Hierarchical group organization
- Documentation updated

✅ **Maintainability**
- Reusable test data builders
- Centralized mock ViewModel creation
- Clear naming conventions
- Easy to add new previews
- Easy to update when UI changes

---

## References

### Essential Reading for Preview Agents

1. **[COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md)** - Complete reference
2. **[Jetpack Compose Preview Docs](https://developer.android.com/jetpack/compose/tooling/previews)** - Official documentation
3. **[Material Design 3 Guide](../material-design/SKILL.md)** - Design system reference
4. **[Android Testing Guide](../android-testing/SKILL.md)** - Testing preview rendering

### Related DocumentsWithin NovaChat

- [copilot-instructions.md](../copilot-instructions.md) - Main project architecture
- [DEVELOPMENT_PROTOCOL.md](../DEVELOPMENT_PROTOCOL.md) - Development standards
- [AGENTS.md](../AGENTS.md) - Multi-agent guidelines

### Example Implementation Files

- `examples/ChatScreenPreview.kt` - Full screen preview example
- `examples/SettingsScreenPreview.kt` - Another screen example
- `examples/PreviewViewModels.kt` - Mock ViewModel factories
- `examples/PreviewData.kt` - Test data builders

---

## Getting Started as a Preview Agent

### First Task Checklist

1. **Read documentation**
   - [ ] Study COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md
   - [ ] Review example files (ChatScreenPreview.kt, etc.)
   - [ ] Understand mock ViewModel patterns

2. **Understand project structure**
   - [ ] Navigate `app/src/main/java/com/novachat/app/ui/`
   - [ ] Review existing composables (ChatScreen, SettingsScreen)
   - [ ] Check `ui/previews/` for existing utilities

3. **Create your first previews**
   - [ ] Pick an existing composable without previews
   - [ ] Create `*Preview.kt` file
   - [ ] Create ≥5 previews covering different states
   - [ ] Test rendering in Android Studio

4. **Optimize and document**
   - [ ] Verify all previews render
   - [ ] Check compilation times
   - [ ] Document any new patterns used

### Common First Tasks

- Add previews to ChatScreen.kt
- Add previews to SettingsScreen.kt
- Create PreviewViewModels.kt utilities
- Create PreviewData.kt test data
- Add component previews (MessageBubble, MessageInputBar, etc.)
- Document preview strategy for new screens

---

**Document Status**: Complete & Ready for Implementation

**Next Steps**:
1. Establish Preview Agent on team
2. Assign first preview creation tasks
3. Review initial preview PRs with this checklist
4. Iterate and refine patterns based on team experience
