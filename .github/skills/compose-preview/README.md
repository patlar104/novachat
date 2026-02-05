# Jetpack Compose Preview System - Complete Documentation

**Version**: 1.0
**Status**: Complete & Ready for Preview Agent Role Implementation
**Date**: February 2026

---

## 📚 Documentation Structure

This skill package provides everything needed to understand and implement Jetpack Compose `@Preview` annotations in the NovaChat project.

### Core Documentation

#### 1. **[COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md)** ⭐
**Comprehensive reference for all aspects of Compose previews**

- Overview & benefits of using previews
- Complete @Preview annotation parameter reference
- Preview configuration patterns (light/dark theme, devices, fonts, locales)
- How to structure preview composables
- Multiple device preview setup
- Comprehensive preview examples
- Mock ViewModel setup patterns
- Android Studio IDE integration details
- Preview organization best practices
- Performance optimization tips
- Testing previews
- Common pitfalls and solutions

**Read this for**: Understanding everything about Preview annotations

---

#### 2. **[PREVIEW_AGENT_ROLE.md](PREVIEW_AGENT_ROLE.md)** 👥
**Role definition for the new Preview Agent**

- What Preview Agent does
- Responsibilities and focus areas
- Distinction from other agents (UI, Backend, Testing)
- Preview coverage requirements
- Preview infrastructure setup
- Mock ViewModel strategy
- Test data organization
- Preview organization guidelines
- Performance optimization
- Collaboration with other agents
- Success criteria
- Getting started checklist

**Read this for**: Understanding the Preview Agent role on a multi-agent team

---

### Example Code Files

#### 3. **[examples/PreviewViewModels.kt](examples/PreviewViewModels.kt)**
**Factory functions for creating mock ViewModels**

Contains:
- `createPreviewChatViewModel()` - Mock ChatViewModel with proper StateFlow setup
- `createPreviewSettingsViewModel()` - Mock SettingsViewModel
- Helper functions for verifying events

**Use this for**: Creating mock ViewModels in your own previews

**Key Pattern:**
```kotlin
fun createPreviewChatViewModel(
    initialState: ChatUiState = ChatUiState.Success(...),
    draftMessage: String = ""
): ChatViewModel {
    val mockViewModel = mockk<ChatViewModel>(relaxed = true)
    // Configure StateFlows
    // Configure event handling
    // Return properly initialized mock
}
```

---

#### 4. **[examples/PreviewData.kt](examples/PreviewData.kt)**
**Test data builders and datasets**

Contains:
- `previewUserMessage()` - Create user message test data
- `previewAiMessage()` - Create AI message test data
- Pre-built test conversations:
  - `shortTestMessages` - 6 messages for quick testing
  - `testMessages` - 12 messages for standard previews
  - `longTestMessages` - 40+ messages for scroll testing
  - `longMessageTestMessages` - Multi-line text wrapping tests
  - `emojiTestMessages` - Emoji and special character tests
  - `shortMessageTestMessages` - Compact layouts
  - `mixedLengthMessages` - Realistic conversations

**Use this for**: Creating consistent test data across all previews

**Key Pattern:**
```kotlin
val messages = listOf(
    previewUserMessage("Hello"),
    previewAiMessage("Hi there!"),
    previewUserMessage("How are you?"),
    previewAiMessage("I'm doing great!")
)

// Or use pre-built datasets
val msgs = testMessages  // 12 message conversation
val msgs = longTestMessages  // 40+ messages for performance testing
```

---

#### 5. **[examples/ChatScreenPreview.kt](examples/ChatScreenPreview.kt)** 🎯
**Comprehensive preview implementation for ChatScreen**

**60+ previews demonstrating:**

**UI State Coverage** (7 previews):
- Initial (empty state)
- Loading (spinner)
- Success (empty conversation)
- Success (with messages)
- Success (processing - AI response)
- Success (with error banner)
- Error (recoverable)
- Error (non-recoverable)

**Device Coverage** (7 previews):
- Compact phone (320dp)
- Standard phone (412dp)
- Large phone (480dp)
- Tablet portrait (600dp)
- Tablet landscape (1000dp)
- Foldable

**Theme Coverage** (6 previews):
- Light mode (success state)
- Dark mode (success state)
- Light mode (error state)
- Dark mode (error state)

**Accessibility Coverage** (3 previews):
- Normal font (1x)
- Large font (1.5x)
- Extra large font (2x)

**Localization Coverage** (4 previews):
- English (US)
- Spanish
- Japanese
- Arabic (RTL)

**Component-Level Previews** (15+ previews):
- MessageBubble (user)
- MessageBubble (AI)
- MessageBubble (long text)
- MessageInputBar (empty)
- MessageInputBar (filled)
- MessageInputBar (multi-line)
- MessageInputBar (loading)
- ErrorBanner
- EmptyState

**Use as**:
- Template for creating previews for other screens
- Reference for all preview patterns
- Training material for new developers
- Example of organized preview structure

---

#### 6. **[examples/SettingsScreenPreview.kt](examples/SettingsScreenPreview.kt)**
**Preview implementation for SettingsScreen**

Demonstrates:
- Different screen type previews
- Form state handling (loading, error, success)
- Configuration state variations
- API key input states
- Mode selection (Online vs Offline)
- Save success messages

**Use as**: Reference for form/settings screen previews

---

## 🗺️ Quick Navigation Map

### I want to...

**...understand what @Preview does**
→ Start with [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Overview section

**...see detailed @Preview parameters**
→ [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - @Preview Annotation Reference

**...see theme/dark mode preview examples**
→ [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Theme & Dark Mode Previews section
OR [examples/ChatScreenPreview.kt](examples/ChatScreenPreview.kt) - CHAT SCREEN - THEME PREVIEWS section

**...see multiple device previews**
→ [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Multiple Device Previews section
OR [examples/ChatScreenPreview.kt](examples/ChatScreenPreview.kt) - CHAT SCREEN - DEVICE PREVIEWS section

**...see how to mock a ViewModel**
→ [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Mock ViewModel Setup section
OR [examples/PreviewViewModels.kt](examples/PreviewViewModels.kt) - Full implementation

**...see test data builders**
→ [examples/PreviewData.kt](examples/PreviewData.kt) - All functions and datasets

**...see a complete preview file**
→ [examples/ChatScreenPreview.kt](examples/ChatScreenPreview.kt) - 60+ real previews

**...understand Android Studio integration**
→ [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Android Studio IDE Integration section

**...optimize preview compilation**
→ [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Performance Optimization section

**...learn about the Preview Agent role**
→ [PREVIEW_AGENT_ROLE.md](PREVIEW_AGENT_ROLE.md)

**...understand how previews fit in multi-agent development**
→ [PREVIEW_AGENT_ROLE.md](PREVIEW_AGENT_ROLE.md) - Collaboration with Other Agents section

**...see best practices and common pitfalls**
→ [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Common Pitfalls & Solutions section

---

## 🎯 Preview Coverage Checklist

### For Every New Screen You Create:

- [ ] **State Previews** (≥3 states)
  - [ ] Initial/Empty state
  - [ ] Loading state
  - [ ] Success state (at least 1-2 variants)
  - [ ] Error state (at least 1 variant)

- [ ] **Device Previews** (≥4 devices)
  - [ ] Compact phone (320dp)
  - [ ] Standard phone (412dp)
  - [ ] Large phone (480dp)
  - [ ] Tablet (600dp or larger)

- [ ] **Theme Previews** (2 themes)
  - [ ] Light mode
  - [ ] Dark mode

- [ ] **Accessibility Previews** (3 scales)
  - [ ] Normal font (1x)
  - [ ] Large font (1.5x)
  - [ ] Extra large font (2x)

- [ ] **Localization Previews** (≥2 locales)
  - [ ] English (or primary language)
  - [ ] RTL language (Arabic recommended)

- [ ] **Component Previews** (for significant components)
  - [ ] Each key component
  - [ ] Multiple states per component
  - [ ] Dark/light theme variants

**Total**: Minimum **16 previews per full screen**, **3+ previews per component**

---

## 🚀 Getting Started

### Step 1: Read the Core Documentation
1. Read [COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md](COMPOSE_PREVIEW_COMPREHENSIVE_GUIDE.md) - Overview & Benefits
2. Skim the @Preview Annotation Reference
3. Look at Theme & Dark Mode examples

### Step 2: Review Example Implementations
1. Study [examples/ChatScreenPreview.kt](examples/ChatScreenPreview.kt)
2. Look at [examples/PreviewViewModels.kt](examples/PreviewViewModels.kt)
3. Review [examples/PreviewData.kt](examples/PreviewData.kt)

### Step 3: Create Your First Previews
1. Pick a composable without previews
2. Create `*Preview.kt` or add to existing file
3. Start with state previews (5-7)
4. Add device previews (4-5)
5. Add theme previews (2)
6. Test rendering in Android Studio

### Step 4: Expand Coverage
1. Add accessibility previews (3)
2. Add localization previews (2)
3. Add component previews (5+)
4. Verify all previews render
5. Check compilation time < 5s

### Step 5: Optimize
1. Extract mock ViewModels to `PreviewViewModels.kt`
2. Extract test data to `PreviewData.kt`
3. Organize with `group` parameter
4. Add documentation if patterns are new
5. Submit for review

---

## 📊 File Organization Reference

### Where Previews Go

```
app/src/main/java/com/novachat/app/
├── ui/
│   ├── ChatScreen.kt              ← Composable only
│   ├── ChatScreenPreview.kt       ← All previews (separate file recommended)
│   │
│   ├── SettingsScreen.kt          ← Composable only
│   ├── SettingsScreenPreview.kt   ← All previews
│   │
│   ├── components/
│   │   ├── MessageBubble.kt       ← Composable + local previews OK
│   │   ├── MessageInputBar.kt     ← Composable + local previews OK
│   │   └── ErrorBanner.kt         ← Composable + local previews OK
│   │
│   ├── previews/                  ← Shared preview utilities
│   │   ├── PreviewViewModels.kt   ← All mock ViewModel factories
│   │   └── PreviewData.kt         ← All test data builders
│   │
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
```

### Naming Convention

```
<Component><State><Device><Theme><Feature>Preview

Examples:
ChatScreenInitialPreview
ChatScreenSuccessPreview
ChatScreenPhonePreview
ChatScreenLightThemePreview
ChatScreenNormalFontPreview
MessageBubbleUserPreview
```

---

## 🎨 Common Preview Patterns

### Pattern 1: Full Screen with State Variants

```kotlin
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
```

### Pattern 2: Device-Specific

```kotlin
@Preview(device = Devices.PIXEL_6, group = "ChatScreen/Devices")
@Composable
fun ChatScreenPhonePreview() {
    NovaChatTheme {
        ChatScreen(
            viewModel = createPreviewChatViewModel(),
            onNavigateToSettings = {}
        )
    }
}
```

### Pattern 3: Theme Variants

```kotlin
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, group = "ChatScreen/Themes")
@Composable
fun ChatScreenLightPreview() {
    NovaChatTheme(darkTheme = false) {
        ChatScreen(
            viewModel = createPreviewChatViewModel(),
            onNavigateToSettings = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, group = "ChatScreen/Themes")
@Composable
fun ChatScreenDarkPreview() {
    NovaChatTheme(darkTheme = true) {
        ChatScreen(
            viewModel = createPreviewChatViewModel(),
            onNavigateToSettings = {}
        )
    }
}
```

### Pattern 4: Accessibility

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

### Pattern 5: Component Isolation

```kotlin
@Preview(showBackground = true)
@Composable
fun MessageBubbleUserPreview() {
    NovaChatTheme {
        MessageBubble(message = previewUserMessage("Hello"))
    }
}
```

---

## ❓ FAQ

**Q: How many previews do I need?**
A: Minimum 16 per screen (states + devices + themes + accessibility). More is better for comprehensive coverage.

**Q: Can I put previews in the same file as the composable?**
A: For components, yes. For full screens, create separate `*Preview.kt` file to keep files manageable.

**Q: How do I mock a ViewModel?**
A: Use `createPreviewChatViewModel()` from `PreviewViewModels.kt`. Never instantiate real ViewModels.

**Q: Where do I put test data?**
A: In `PreviewData.kt`. Create reusable builders like `previewUserMessage()` and datasets like `testMessages`.

**Q: Why are my previews slow?**
A: Too many previews in one file, real ViewModel instantiation, or network calls. See Performance section.

**Q: How do I test RTL languages?**
A: Use `locale = "ar-SA"` (Arabic) or `locale = "he-IL"` (Hebrew) parameter in @Preview.

**Q: Can previews execute side effects?**
A: No. They shouldn't perform network requests, database writes, or launch background tasks.

**Q: How do I organize lots of previews?**
A: Use the `group` parameter hierarchically: `"ChatScreen/States"`, `"ChatScreen/Devices"`, etc.

---

## 📖 Related Resources

### Within NovaChat Project

- [copilot-instructions.md](../copilot-instructions.md) - Main architecture guide
- [DEVELOPMENT_PROTOCOL.md](../DEVELOPMENT_PROTOCOL.md) - Development standards
- [AGENTS.md](../AGENTS.md) - Multi-agent system overview
- [Material Design Skill](../material-design/SKILL.md) - Design system reference
- [Android Testing Skill](../android-testing/SKILL.md) - Testing patterns

### External Resources

- [Official Jetpack Compose Preview Docs](https://developer.android.com/jetpack/compose/tooling/previews)
- [Material Design 3 for Compose](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Android Accessibility Testing](https://developer.android.com/develop/ui/views/accessibility/testing)
- [Compose Tooling Documentation](https://developer.android.com/jetpack/compose/tooling)

---

## 💡 Key Takeaways

1. **@Preview is powerful** - See changes instantly without running on device
2. **Mock ViewModels properly** - Never use real dependencies in previews
3. **Organize hierarchically** - Use `group` parameter for discoverability
4. **Cover all states** - Test every UI state your screen can be in
5. **Test accessibility** - Always include font scale previews
6. **Monitor performance** - Keep compilation times fast
7. **Reuse test data** - Extract to `PreviewData.kt`
8. **Document patterns** - Help future developers understand patterns

---

## 📋 Revision History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Feb 2026 | Initial comprehensive guide, role definition, and examples |

---

**Last Updated**: February 2026
**Status**: Complete & Production Ready ✅

For updates or questions about Compose previews in NovaChat, refer to the specific documentation files linked above.
