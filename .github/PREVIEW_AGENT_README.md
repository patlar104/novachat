# 🎬 Preview Agent - Complete Documentation Index

**Status**: ✅ Ready to Use  
**Version**: 1.0  
**Last Updated**: February 4, 2026

---

## Quick Navigation

### 👤 **For Developers**
Start here if you're a developer using the Preview Agent:
1. **[PREVIEW_AGENT_QUICK_START.md](./PREVIEW_AGENT_QUICK_START.md)** ← Start here! (5 min read)
2. **[.github/skills/compose-preview/SKILL.md](./.github/skills/compose-preview/SKILL.md)** - Copy-paste patterns
3. **[.github/agents/preview-agent.agent.md](./.github/agents/preview-agent.agent.md)** - Full agent spec

### 🤖 **For Agents/Copilot**
If you're invoking agents or implementing features:
1. **[.github/agents/preview-agent.agent.md](./.github/agents/preview-agent.agent.md)** - Complete specifications
2. **[.github/skills/compose-preview/SKILL.md](./.github/skills/compose-preview/SKILL.md)** - Reference patterns
3. **[.github/copilot-instructions.md](./.github/copilot-instructions.md)** - Integration context

### 📋 **For Team Leads**
If you're managing the multi-agent system:
1. **[PREVIEW_AGENT_INTEGRATION.md](./PREVIEW_AGENT_INTEGRATION.md)** - Implementation summary
2. **[.github/AGENTS.md](./.github/AGENTS.md)** - Full agent system overview
3. **[.github/DEVELOPMENT_PROTOCOL.md](./.github/DEVELOPMENT_PROTOCOL.md)** - Quality standards

---

## What is the Preview Agent?

The **Preview Agent** is a specialized development tool within NovaChat's multi-agent system that:

✅ **Creates @Preview annotations** for Jetpack Compose Composables  
✅ **Enables IDE debugging** of UI/UX through real-time preview composition  
✅ **Supports rapid iteration** on Android Studio without rebuilding APK  
✅ **Documents state handling** through comprehensive preview composition  
✅ **Coordinates with other agents** (UI, Backend, Testing) seamlessly

### The Problem It Solves

**Before Preview Agent**:
- UI developers manually create @Preview annotations
- Each screen needs 8-12 previews to cover states/devices/themes
- Previews are often incomplete or missing states
- No coordination with architecture changes
- IDE debugging is slow without proper preview setup

**After Preview Agent**:
- Systematic @Preview creation follows defined patterns
- All UI states automatically previewed
- Device variants and theme variants guaranteed
- Automatic updates when UI logic changes
- IDE provides instant feedback for UI/UX iteration

---

## Architecture Overview

### Where Preview Agent Fits

```
┌─────────────────────────────────────────────────────────────┐
│                    Multi-Agent System                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  🎯 Planner        → Analyzes requirements, creates plans   │
│  🎨 UI Agent       → Implements Composable screens          │
│  🎬 Preview Agent  → Creates @Preview annotations           │
│  ⚙️ Backend Agent  → Implements ViewModels & logic          │
│  🧪 Testing Agent  → Writes automated tests                 │
│  🔧 Build Agent    → Manages Gradle & dependencies          │
│  👁️ Reviewer      → Audits code quality & compliance        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Feature Development Workflow

```
Feature Request
    ↓
Planner Agent (breaks it down)
    ↓
UI Agent (creates Composable)
    ↓
Preview Agent (creates @Preview annotations) ← YOU ARE HERE
    ↓
Backend Agent (creates ViewModel)
    ↓
Preview Agent (updates mock ViewModels)
    ↓
Testing Agent (creates automated tests)
    ↓
Reviewer Agent (validates quality)
    ↓
🚀 Production Ready!
```

---

## File Organization

### Main Documentation

| File | Purpose | Audience |
|------|---------|----------|
| [PREVIEW_AGENT_QUICK_START.md](./PREVIEW_AGENT_QUICK_START.md) | 5-min overview + quick reference | Developers |
| [PREVIEW_AGENT_INTEGRATION.md](./PREVIEW_AGENT_INTEGRATION.md) | Complete feature description | Team leads |
| [Preview Agent (README - this file)] | Navigation & context | Everyone |

### Agent & Skill Files

| File | Purpose | Size |
|------|---------|------|
| [.github/agents/preview-agent.agent.md](./.github/agents/preview-agent.agent.md) | Full agent specifications & protocol | ~1,300 lines |
| [.github/skills/compose-preview/SKILL.md](./.github/skills/compose-preview/SKILL.md) | Reusable @Preview patterns & examples | ~800 lines |

### Updated System Files

| File | Change | Impact |
|------|--------|--------|
| [.github/AGENTS.md](./.github/AGENTS.md) | Added Preview Agent overview | +80 lines |
| [.github/copilot-instructions.md](./.github/copilot-instructions.md) | Added Preview Agent section | +50 lines |

---

## Core Concepts

### What Preview Agent Creates

#### 1. **@Preview Annotations**
```kotlin
@Preview(name = "Chat - Empty")
@Composable
fun ChatScreenEmptyPreview() { ... }
```

#### 2. **Preview Files**
Location: `app/src/main/java/com/novachat/app/ui/preview/`

Examples:
- `ChatScreenPreview.kt` - All chat screen preview variants
- `SettingsScreenPreview.kt` - All settings screen variants
- `SharedPreviewComponents.kt` - Shared utilities & constants
- `Preview*ScreenData.kt` - Sample state providers for previews

#### 3. **Device Constants**
```kotlin
const val DEVICE_PHONE = "spec:width=411dp,height=891dp,dpi=420"
const val DEVICE_TABLET = "spec:width=1280dp,height=800dp,dpi=240"
const val DEVICE_LANDSCAPE = "spec:width=854dp,height=480dp,dpi=420"
```

#### 4. **Preview Data Providers**
```kotlin
object PreviewChatScreenData {
    fun initialState(): ChatUiState = ChatUiState.Initial
    fun successSingleExchange(): ChatUiState = ChatUiState.Success(messages = SAMPLE_DATA)
    fun successWithErrorBanner(): ChatUiState = ChatUiState.Success(error = "Network error")
}
```

### Preview Coverage Pattern

Every new screen gets previews for:

**States**:
- Empty/Initial state
- Loading state
- Success state (with sample data)
- Error states (network, API, etc.)

**Devices**:
- Standard phone (411x891)
- Small phone (360x740)
- Large phone (480x854)
- Tablet (1280x800)
- Landscape orientation

**Themes**:
- Light theme
- Dark theme

**Result**: 12-15 comprehensive previews per screen

---

## How to Use

### For a New Screen

```
Step 1: UI Agent creates Composable
Step 2: Ask Preview Agent to create previews
    
@copilot using preview-agent.agent.md

Create previews for ChatScreen:
- Empty/loading/success/error states
- Light and dark themes  
- Phone and tablet sizes
- Device constants in SharedPreviewComponents.kt

Step 3: Use Android Studio IDE preview panel
Step 4: See live composition as you edit Composable
Step 5: Iterate on UI/UX instantly
```

### For an Existing Screen

```
Step 1: UI Agent modifies Composable
Step 2: Ask Preview Agent to update previews
    
@copilot using preview-agent.agent.md

Update previews for updated ChatScreen to show:
- New state field 'isArchived'
- Adjusted layout spacing
- Updated error message display

Step 3: IDE previews update automatically
Step 4: Verify all states look correct
```

### Working with Previews in Android Studio

1. Open any `*Preview.kt` file
2. Click **Preview** tab on right side
3. See live composition of all @Preview functions
4. Edit Composable → Preview updates in real-time
5. Try different states with sample state providers

---

## Quality Standards

All Preview Agent code MUST comply with [DEVELOPMENT_PROTOCOL.md](./.github/DEVELOPMENT_PROTOCOL.md):

### ✅ Preview Code Quality Checklist

**Completeness**:
- [ ] All @Preview functions are complete (no `// ... code` placeholders)
- [ ] All state variants have previews
- [ ] Multiple device sizes shown
- [ ] Light and dark themes shown

**Correctness**:
- [ ] All imports are explicit
- [ ] All brackets and braces balanced
- [ ] All @Preview annotations are valid
- [ ] Compilable without errors

**Composition**:
- [ ] Wrapped in `NovaChatTheme { }`
- [ ] Uses `Preview*ScreenData` (never real repositories or ViewModels)
- [ ] No side effects (LaunchedEffect, etc.)
- [ ] No production API calls

**Performance**:
- [ ] IDE preview compiles in < 3 seconds
- [ ] ≤ 12 previews per file (split if needed)
- [ ] Lightweight mock data

**Documentation**:
- [ ] Each preview has descriptive `name` parameter
- [ ] Complex previews have explanatory comments
- [ ] Preview*ScreenData builders are documented

---

## Integration with Other Agents

### ← Receives From

**UI Agent**:
```
"Created ChatScreen.kt composable. 
Ready for preview composition."
```
→ Preview Agent creates `ChatScreenPreview.kt`

**Backend Agent**:
```
"Added ChatUiState.Archived state.
Preview Agent should create preview for this."
```
→ Preview Agent adds archived state preview

### → Hands Off To

**Testing Agent**:
```
"ChatScreenPreview.kt complete with 12 state variants.
Ready for ComposeTestRule automated tests."
```
→ Testing Agent creates automated UI tests

**UI Agent** (if preview reveals issues):
```
"Preview shows error banner overlaps message list.
Suggest adjusting padding in ChatScreen line 82."
```
→ UI Agent fixes layout issue

---

## File Locations

### Agent Files
```
.github/agents/
├── planner.agent.md            (Strategic planning)
├── ui-agent.agent.md           (UI implementation)
├── preview-agent.agent.md      ← NEW!
├── backend-agent.agent.md      (Business logic)
├── testing-agent.agent.md      (Automated tests)
├── build-agent.agent.md        (Build config)
└── reviewer-agent.agent.md     (Code review)
```

### Skills Files
```
.github/skills/
├── android-testing/SKILL.md    (Testing patterns)
├── material-design/SKILL.md    (UI components)
├── compose-preview/SKILL.md    ← NEW!
└── security-check/SKILL.md     (Security best practices)
```

### Preview Files (Created by Preview Agent)
```
app/src/main/java/com/novachat/app/ui/preview/
├── ChatScreenPreview.kt        (Chat screen previews)
├── SettingsScreenPreview.kt    (Settings screen previews)
├── SharedPreviewComponents.kt  (Constants & utilities)
└── Preview*ScreenData.kt       (Sample state providers)
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Preview won't render | Check `NovaChatTheme { }` wrapping |
| Can't find preview constants | Are they in `SharedPreviewComponents.kt`? |
| Preview compiles slowly | Reduce mock data size or split previews |
| IDE shows outdated preview | Clean project: Build → Clean Project |
| Preview data provider not found | Check it's in `Preview*ScreenData.kt` and imported |
| States missing previews | Ask Preview Agent to add missing state previews |

---

## Quick Links

### 📚 Must Read
1. **[PREVIEW_AGENT_QUICK_START.md](./PREVIEW_AGENT_QUICK_START.md)** - Get started in 5 minutes
2. **[.github/agents/preview-agent.agent.md](./.github/agents/preview-agent.agent.md)** - Full specifications

### 📖 Reference
1. **[.github/skills/compose-preview/SKILL.md](./.github/skills/compose-preview/SKILL.md)** - Copy-paste patterns
2. **[.github/AGENTS.md](./.github/AGENTS.md)** - Multi-agent system overview
3. **[.github/copilot-instructions.md](./.github/copilot-instructions.md)** - Agent guidance

### ⚙️ System
1. **[.github/DEVELOPMENT_PROTOCOL.md](./.github/DEVELOPMENT_PROTOCOL.md)** - Mandatory development standards
2. **[PREVIEW_AGENT_INTEGRATION.md](./PREVIEW_AGENT_INTEGRATION.md)** - Integration details

---

## Key Takeaways

### Preview Agent In One Sentence
**The Preview Agent automatically creates comprehensive @Preview compositions for all UI states, devices, and themes to enable rapid IDE-based UI/UX iteration.**

### What It Enables
- 🚀 **Fast iteration**: See UI changes in IDE instantly (no APK rebuild)
- 🎨 **Complete coverage**: All states, devices, themes automatically handled
- 🤝 **Coordination**: Works seamlessly with UI, Backend, and Testing agents
- 📋 **Documentation**: Preview composition serves as living UI documentation
- 🔄 **Maintenance**: Updates automatically when ViewModel states change

### When to Use It
- ✅ Creating new Composable screens
- ✅ Updating existing Composable layouts
- ✅ Adding new UI states to ViewModels
- ✅ Testing responsive design
- ✅ Designing light/dark theme variants

### When to NOT Use It
- ❌ Creating test files (use Testing Agent)
- ❌ Implementing business logic (use Backend Agent)
- ❌ Modifying build configs (use Build Agent)

---

## Getting Started

### 1. **First Time?** (5 minutes)
Read: [PREVIEW_AGENT_QUICK_START.md](./PREVIEW_AGENT_QUICK_START.md)

### 2. **Want Details?** (30 minutes)
Read: [.github/agents/preview-agent.agent.md](./.github/agents/preview-agent.agent.md)

### 3. **Need Examples?** (Ongoing)
Reference: [.github/skills/compose-preview/SKILL.md](./.github/skills/compose-preview/SKILL.md)

### 4. **Ready to Use?**
```bash
@copilot using preview-agent.agent.md

Create comprehensive previews for [ScreenName]:
- All UI states
- Multiple devices
- Light and dark themes
```

---

## Support & Questions

If you have questions:

1. **Quick question?** Check [PREVIEW_AGENT_QUICK_START.md](./PREVIEW_AGENT_QUICK_START.md)
2. **Need examples?** Check [.github/skills/compose-preview/SKILL.md](./.github/skills/compose-preview/SKILL.md)
3. **Want specs?** Read [.github/agents/preview-agent.agent.md](./.github/agents/preview-agent.agent.md)
4. **System design?** Check [PREVIEW_AGENT_INTEGRATION.md](./PREVIEW_AGENT_INTEGRATION.md)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Feb 4, 2026 | Initial release |

---

## Summary

The Preview Agent is now fully integrated into NovaChat's development system, enabling you to:

✅ Create comprehensive @Preview annotations automatically  
✅ Debug UI/UX directly in Android Studio IDE  
✅ Iterate rapidly without rebuilding APKs  
✅ Cover all states, devices, and themes systematically  
✅ Maintain consistency across the codebase  

**Ready to create amazing UI previews?** 🚀

Start with: [PREVIEW_AGENT_QUICK_START.md](./PREVIEW_AGENT_QUICK_START.md)
