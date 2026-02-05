# 🎬 Preview Agent Integration Guide

**Status**: ✅ Complete  
**Date**: February 4, 2026  
**Agent Role**: Specialized UI preview composition for Android Studio IDE debugging

---

## What Has Been Created

A comprehensive new **Preview Agent** system for NovaChat to enable rapid UI/UX iteration and debugging through Jetpack Compose @Preview annotations.

### Files Added/Modified

#### 1. **Agent Definition** (NEW)
- **Location**: [`.github/agents/preview-agent.agent.md`](./.github/agents/preview-agent.agent.md)
- **Size**: ~1,300 lines
- **Contains**:
  - Core responsibility and scope definition
  - Preview file structure and location conventions
  - Key preview patterns (device specs, themes, states)
  - Integration with other agents (UI, Backend, Testing)
  - Quality gates and checklist

#### 2. **Skills Library** (NEW)
- **Location**: [`.github/skills/compose-preview/SKILL.md`](./.github/skills/compose-preview/SKILL.md)
- **Size**: ~800 lines
- **Contains**:
  - Reusable @Preview patterns
  - Device specification constants
  - Mock ViewModel factory patterns
  - Light/dark theme composition examples
  - Advanced techniques (parameterized previews, interactive previews)
  - Common patterns for different component types
  - Performance optimization tips

#### 3. **AGENTS.md Updates**
- Added Preview Agent to agent overview (now 7 agents total)
- Updated workflow diagram to include Preview Agent
- Updated directory structure references
- Clarified agent handoff protocols

#### 4. **copilot-instructions.md Updates**
- Added Preview Agent section to agent-specific guidance
- Updated agent focus quick reference table
- Added [PREVIEW-FOCUS] tags to relevant sections

---

## Preview Agent Overview

### Core Responsibilities

The Preview Agent specializes in:

```
UI Composable → Preview Agent → @Preview Annotations + Preview Files
                                ↓
                        Android Studio IDE Debugging
                        (Real-time composition feedback)
```

### Scope

**✅ What Preview Agent Does**:
- Create `@Preview` annotations on Composables
- Create `*ScreenPreview.kt` files with comprehensive previews
- Create `Preview*ScreenData.kt` sample state providers
- Configure device specifications and variants
- Compose light/dark theme previews
- Document preview purpose and performance notes

**❌ What Preview Agent Does NOT Do**:
- Modify business logic or repositories
- Change UI layouts (that's UI Agent's job)
- Implement production ViewModels (Backend Agent handles that)
- Create test files (Testing Agent handles that)

### Key Files to Create

When adding a new screen, Preview Agent creates:

```
app/src/main/java/com/novachat/app/ui/preview/
├── ChatScreenPreview.kt           # All preview variants of ChatScreen
├── SettingsScreenPreview.kt       # All preview variants of SettingsScreen
├── SharedPreviewComponents.kt     # Shared preview utilities, device constants
└── Preview*ScreenData.kt          # Sample state providers for previews
```

---

## Preview Composition Pattern

### Basic Preview (Minimum)

```kotlin
@Preview(name = "Chat - Empty")
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
```

### Comprehensive Preview File

Every new screen should have:

1. **Empty/Initial State** (light + dark)
2. **Success State** with sample data
3. **Loading State**
4. **At least one Error State**
5. **Multi-device preview** using `@PreviewScreenSizes`
6. **Theme variants** using `@PreviewLightDark`

### Preview Data Provider Example

```kotlin
object PreviewChatScreenData {
    fun initialState(): ChatUiState = ChatUiState.Initial
    fun successSingleExchange(): ChatUiState = ChatUiState.Success(messages = SAMPLE_DATA)
    fun successWithErrorBanner(): ChatUiState = ChatUiState.Success(error = "Network error")
}
```

---

## How to Use the Preview Agent

### Workflow: Creating a New Screen

```
1. UI Agent creates ChatScreen.kt
           ↓
2. Preview Agent creates ChatScreenPreview.kt
           ↓
3. Backend Agent creates ChatViewModel.kt
           ↓
4. Preview Agent updates PreviewChatScreenData.kt
           ↓
5. Testing Agent creates ChatScreenTest.kt
           ↓
6. Reviewer Agent validates all layers
```

### Invoking the Preview Agent

```bash
@copilot using preview-agent.agent.md

Create comprehensive previews for the ChatScreen with:
- Empty conversation state
- Single and multiple message exchanges
- Loading while AI processes
- Error states (network, API)
- Device variants (phone, tablet, landscape)
- Theme variants (light/dark)
```

### Expected Output

The Preview Agent will deliver:

```kotlin
// ChatScreenPreview.kt
@Preview(name = "Empty Chat")
@Composable
fun ChatScreenEmptyPreview() { ... }

@Preview(name = "With Messages")
@Composable
fun ChatScreenWithMessagesPreview() { ... }

@PreviewLightDark
@Composable
fun ChatScreenThemePreview() { ... }

@PreviewScreenSizes
@Composable
fun ChatScreenMultiDevicePreview() { ... }

// And more state variants...
```

---

## Agent Integration Points

### Handoff FROM UI Agent
```
"Created ChatScreen.kt - ready for preview composition.
Please create ChatScreenPreview.kt with empty/loading/success/error states."
```
→ Preview Agent creates comprehensive preview file

### Handoff FROM Backend Agent
```
"Updated ChatUiState with new 'archived' field.
Preview Agent should add preview for archived message list."
```
→ Preview Agent adds new state preview

### Handoff TO Testing Agent
```
"Preview composition complete in ChatScreenPreview.kt.
Ready for automated UI tests using ComposeTestRule."
```
→ Testing Agent creates automated tests

### Handoff TO Reviewer
```
"Preview file complete with 12 state variants, 3 device sizes,
light/dark themes, and comprehensive documentation."
```
→ Reviewer validates coverage and quality

---

## Key Features

### Device Specification Constants

```kotlin
const val DEVICE_PHONE_SMALL = "spec:width=360dp,height=740dp,dpi=420"
const val DEVICE_PHONE = "spec:width=411dp,height=891dp,dpi=420"
const val DEVICE_PHONE_LARGE = "spec:width=480dp,height=854dp,dpi=420"
const val DEVICE_FOLDABLE = "spec:width=412dp,height=915dp,dpi=420"
const val DEVICE_TABLET = "spec:width=1280dp,height=800dp,dpi=240"
```

### Preview Annotation Options

```kotlin
@Preview(
    name = "Descriptive Name",              // Required: preview identifier
    device = DEVICE_PHONE,                  // Optional: device specification
    showSystemUi = false,                   // Optional: show system bars
    backgroundColor = 0xFF000000            // Optional: background color
)
@Composable
fun ScreenPreview() { ... }
```

### Advanced Annotations

```kotlin
@PreviewLightDark        // Automatic light and dark variants
@PreviewScreenSizes       // Multiple device sizes
@PreviewScreenSizes  
@Preview(device = ...)    // Specific device size
```

---

## Quality Requirements

All Preview Agent code MUST comply with:

✅ **Zero-Elision Policy**: Complete preview implementations (no `// ... code`)  
✅ **Compilability**: All previews compile without errors  
✅ **Imports**: All imports explicitly listed  
✅ **Theme Wrapping**: All previews wrapped in `NovaChatTheme`  
✅ **State Coverage**: All major states have previews  
✅ **Device Coverage**: Multiple device sizes shown  
✅ **Performance**: Fast IDE compilation (< 3s per preview)  
✅ **Documentation**: Comments explain preview purpose  

---

## Directory Structure

The Preview Agent operates within this structure:

```
app/src/main/java/com/novachat/app/ui/
├── ChatScreen.kt
├── SettingsScreen.kt
├── theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── preview/  📁 Preview Agent Files
    ├── ChatScreenPreview.kt
    ├── SettingsScreenPreview.kt
    ├── SharedPreviewComponents.kt
    └── Preview*ScreenData.kt
```

---

## Performance Considerations

### Recommended Limits

- **Previews per file**: 8-12 (split into multiple files if exceeding)
- **IDE preview compilation**: < 3 seconds per preview
- **Build time impact**: Minimal, use lightweight theme variants

### Optimization Tips

```kotlin
// ✅ Good: Lightweight preview
@Preview
@Composable
fun ChatScreenPreview() {
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

// ❌ Avoid: Complex setup in preview
@Preview
@Composable
fun ChatScreenPreview() {
    // Multiple heavy compositions
    // large data sets
    // animations
}
```

---

## Next Steps

### Immediate Actions

1. **Understand the Pattern**: Review `.github/agents/preview-agent.agent.md`
2. **Study Skills Library**: Reference `.github/skills/compose-preview/SKILL.md`
3. **Check Examples**: Look at existing preview patterns in `ui/preview/` (when created)

### Using the Preview Agent

Invoke when:
- A new UI screen is created by UI Agent
- UI screen states change (need new state previews)
- You want to iterate on UI/UX in Android Studio IDE
- You need to test responsive design across devices

Example invocation:
```
@copilot using preview-agent.agent.md

Create comprehensive previews for [ScreenName]:
- States: [list states needed]
- Devices: phone, tablet, landscape
- Themes: light, dark
```

### Handoff Chain

```
new feature request
    ↓
Planner Agent (planning)
    ↓
UI Agent (creates Composable)
    ↓
Preview Agent (creates @Preview annotations)
    ↓
Backend Agent (creates ViewModel)
    ↓
Preview Agent (updates mock ViewModels)
    ↓
Testing Agent (creates automated tests)
    ↓
Reviewer Agent (validates everything)
    ↓
Ready for production!
```

---

## Documentation References

### Agent-Focused Files
- **Preview Agent**: [`.github/agents/preview-agent.agent.md`]
- **UI Agent**: [`.github/agents/ui-agent.agent.md`]
- **Backend Agent**: [`.github/agents/backend-agent.agent.md`]
- **Testing Agent**: [`.github/agents/testing-agent.agent.md`]

### Skills
- **Compose Preview Skill**: [`.github/skills/compose-preview/SKILL.md`]
- **Material Design Skill**: [`.github/skills/material-design/SKILL.md`]
- **Android Testing Skill**: [`.github/skills/android-testing/SKILL.md`]

### Guidelines
- **Development Protocol**: [`DEVELOPMENT_PROTOCOL.md`]
- **Multi-Agent System**: [`AGENTS.md`]
- **Copilot Instructions**: [`copilot-instructions.md`]

---

## Summary

The Preview Agent has been successfully integrated into the NovaChat multi-agent system. It provides:

✅ **Specialized Role**: Focused on @Preview annotations and IDE debugging  
✅ **Clear Scope**: Defined boundaries with other agents  
✅ **Reusable Patterns**: Comprehensive skills library with examples  
✅ **Integration Points**: Clear handoff protocols with UI, Backend, Testing agents  
✅ **Quality Standards**: Adherence to DEVELOPMENT_PROTOCOL.md  
✅ **Documentation**: Complete guides and patterns for team usage  

The system is ready to use for creating rich, debuggable UI previews that enable rapid iteration on NovaChat's user interface!
