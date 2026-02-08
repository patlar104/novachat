---
name: material-design
description: Complete Material 3 Compose patterns for NovaChat (NO placeholders, NO XML)
category: ui
applies_to:
  - "**/ui/**/*.kt"
  - "**/ui/theme/*.kt"
protocol_compliance: true
note: All examples are COMPLETE Jetpack Compose implementations - following DEVELOPMENT_PROTOCOL.md
---

# Material Design 3 with Jetpack Compose Skill

This skill provides **COMPLETE** Material Design 3 Compose patterns. All code examples are fully implemented with no placeholders.

> **PROTOCOL**: All examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - Complete @Composable functions (no `// ... UI code` placeholders)
> - All imports explicitly shown
> - Material 3 components only (no Material 2)
> - Theme integration shown
> - Accessibility semantics included

## Multi-Agent Coordination

### When the UI Agent Should Use Tools

**Use tools immediately for:**
- Reading existing Composable files → `read_file`
- Creating new screen Composables → `create_file`
- Modifying Composable implementations → `apply_patch`
- Searching for Material 3 patterns → `grep_search` or `semantic_search`
- Validating syntax and imports → `run_in_terminal` for build checks

**Do NOT describe; DO implement:**
- Don't say "create a screen Composable"; create it using `create_file`
- Don't say "update the theme colors"; update using `apply_patch`
- Don't say "add Material 3 buttons"; add them using `apply_patch`

### When to Hand Off to Other Agents

**Hand off to Backend Agent if:**
- ViewModel creation/modification is needed
- State management implementation needed
- Use case or repository integration needed
- Event handling logic needs implementation
- → **Action**: Provide Composable signature showing expected state/events

**Hand off to Preview Agent if:**
- @Preview annotations need creation
- Preview data providers need creation
- Preview state variants need setup
- → **Action**: Create Composable, then hand off for preview coverage

**Hand off to Testing Agent if:**
- UI behavior tests need creation
- Interaction testing needed
- Accessibility testing needed
- → **Action**: Implement Composable, then hand off for test coverage

**Hand off to Build Agent if:**
- Compose or Gradle dependencies missing
- Build configuration issues arise
- → **Action**: Report specific build/dependency issues

### UI Task Assessment

**Determine scope before acting:**

1. **Is this a UI task?**
   - Creating Composable screens → YES, use UI Agent tools
   - Modifying Material 3 components → YES, use UI Agent tools
   - Adjusting layout/spacing → YES, use UI Agent tools
   - Creating ViewModels → NO, hand off to Backend Agent
   - Creating tests → NO, hand off to Testing Agent
   - Creating previews → Maybe, see below

2. **Do I have all context needed?**
   - What state should this screen display? → Check UiState definition
   - What events should it emit? → Check UiEvent sealed interface
   - What Material 3 components apply? → Review patterns in this skill

3. **Is this within UI Agent scope?**
   - Creating screen Composables → YES ✓
   - Implementing Material 3 design → YES ✓
   - Building layouts and navigation → YES ✓
   - Creating ViewModels → NO, hand off to Backend Agent
   - Setting up state management → NO, hand off to Backend Agent
   - Writing tests → NO, hand off to Testing Agent

## Theme Setup (Complete)

### Color.kt - Material 3 Color System

Rules:

- Define Material 3 colors in [`ui/theme/Color.kt`](../../app/src/main/java/com/novachat/app/ui/theme/Color.kt).
- Use `md_theme_light_*` and `md_theme_dark_*` naming for all semantic colors.
- Define colors as `Color(0xFF...)` constants.

### Theme.kt - Complete Theme Configuration

Rules:

- Configure the app theme in [`ui/theme/Theme.kt`](../../app/src/main/java/com/novachat/app/ui/theme/Theme.kt).
- Provide light/dark color schemes and enable dynamic color on API 31+.
- Set status bar color with `SideEffect` and wrap content in `MaterialTheme`.

### Type.kt - Complete Typography

Rules:

- Define Typography in [`ui/theme/Type.kt`](../../app/src/main/java/com/novachat/app/ui/theme/Type.kt).
- Provide all Material 3 text styles (display, headline, title, body, label).
- Include `fontFamily`, `fontWeight`, `fontSize`, `lineHeight`, `letterSpacing`.

## Material 3 Components (Complete Examples)

### Buttons

Rules:

- Use Material 3 button variants (`Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`, `ElevatedButton`).
- Always wrap button content with `Text`.

### Text Fields

Rules:

- Use `TextField` or `OutlinedTextField` with `value` and `onValueChange`.
- Provide `label` and `supportingText` where needed.
- Provide `contentDescription` for icons.

### Cards

Rules:

- Use `Card`, `ElevatedCard`, and `OutlinedCard` for content grouping.
- Apply `Modifier.padding` and `fillMaxWidth` as needed.

### Top App Bar

Rules:

- Use `TopAppBar` with title, navigation icon, and actions as needed.
- Keep actions in `IconButton`s with `contentDescription`.
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme

### Dialogs

Rules:

- Use `AlertDialog` with title, text, confirm, and dismiss actions.
- Keep dialog actions as `TextButton`s.

### Lists with LazyColumn

Rules:

- Use `LazyColumn` for long lists and provide stable keys.
- Use `Card` or `ListItem` for list rows.

## Accessibility (Complete)

### Semantics for Screen Readers

Rules:

- Add `contentDescription` for interactive icons.
- Use `Modifier.semantics { contentDescription = "..." }` where needed.

### Minimum Touch Targets

Rules:

- Ensure interactive elements are at least 48dp by 48dp.
- Apply `Modifier.size(48.dp)` for icon-only buttons.

## Layout Best Practices

### ConstraintLayout in Compose

Rules:

- Use ConstraintLayout only for complex layouts that Row/Column cannot handle.
- Keep constraints readable and avoid deep nesting.

## Protocol Compliance Checklist

Before submitting Compose UI code, verify:

- [ ] **Complete @Composable functions** - No `// ... UI code` placeholders
- [ ] **All imports included** - Every Compose import explicitly listed
- [ ] **Material 3 components** - Using M3, not M2
- [ ] **Theme colors used** - From MaterialTheme.colorScheme, not hardcoded
- [ ] **Typography used** - From MaterialTheme.typography
- [ ] **Accessibility** - ContentDescription or semantics provided
- [ ] **Preview functions** - @Preview annotations for key Composables
- [ ] **State hoisting** - Stateless Composables with parameters
- [ ] **Modifiers** - Proper Modifier chaining and sizing

**Remember: DEVELOPMENT_PROTOCOL.md prohibits placeholder code in ALL files, including UI!**

---

**End of Material Design 3 with Jetpack Compose Skill**
