# NovaChat Development Protocol

**Version**: 2026.02
**Last Updated**: February 4, 2026
**Status**: Active

## Purpose

This document defines the comprehensive development protocol for NovaChat to ensure code quality, prevent context drift, and maintain 2026 best practices. These protocols apply to all development work, whether performed by agents or human developers.

---

## I. Context & State Management

### Current Project State
- **Language**: Kotlin 2.2.21 (CodeQL-compatible baseline)
- **Build System**: Gradle 9.1.0 with AGP 9.0.0
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **Target SDK**: Android 16 (API 35)
- **Minimum SDK**: Android 9 (API 28)

### State Awareness Rules
1. **Always verify current file state** before making changes
2. **Check git history** to understand previous decisions
3. **Review related files** for context dependencies
4. **Document assumptions** when state is uncertain

---

## II. Zero-Elision Policy (Strictly Enforced)

### What Is Prohibited
❌ **NEVER use placeholders** like:
```kotlin
// ... rest of implementation
// ... existing code
// ... other methods
```

❌ **NEVER summarize code** with comments like:
```kotlin
// Add all the necessary imports here
// Implement the remaining methods
```

### What Is Required
✅ **ALWAYS write complete code**:
```kotlin
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun MyButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Click Me")
    }
}
```

### Enforcement
- **Before outputting code**: Self-check for completeness
- **If file is too large**: Stop and ask user to continue
- **No exceptions**: Even for "obvious" or "repetitive" code

---

## III. Agent Domain Boundaries & Access Control

### Purpose
Prevent agents from modifying files outside their responsibility, which causes confusion and breaking changes.

### Agent Modify-Access Zones (What They CAN Edit)

#### UI Agent - Modify Access
- `ui/**/*.kt` - Composable functions, screens, layout
- `ui/theme/*.kt` - Colors, Typography, Theme configuration
- `presentation/model/*UiState.kt` - UI state definitions
- `presentation/model/*UiEvent.kt` - UI event definitions
- `MainActivity.kt` - **NavHost block only**, not other logic

#### Backend Agent - Modify Access
- `presentation/viewmodel/*.kt` - ViewModels and state management
- `domain/usecase/*.kt` - Business logic and use cases
- `domain/model/*.kt` - Domain models (Android-agnostic)
- `data/repository/*.kt` - Repository implementations
- `data/model/*.kt` - Data models and mappers
- `data/mapper/*.kt` - Domain ↔ Data conversion
- `di/AppContainer.kt` - Dependency injection wiring

#### Testing Agent - Modify Access
- `**/test/**/*Test.kt` - Unit tests only
- `**/androidTest/**/*Test.kt` - UI tests only
- `**/test/**/Test*.kt` - Test utilities and fixtures

#### Preview Agent - Modify Access
- Add `@Preview` annotations to existing Composables
- Create `**/Preview.kt` files for preview helpers only
- **Modify Contents**: No - UI Agent owns Composable bodies

#### Build Agent - Modify Access
- `build.gradle.kts` - Dependencies, versions, build config
- `gradle.properties` - Gradle settings
- `AndroidManifest.xml` - Permissions, features, manifest
- `res/xml/network_security_config.xml` - Security configuration
- `app/proguard-rules.pro` - Code optimization rules

### Agent Read-Access Zones (What They MUST Read Before Modifying)

#### UI Agent MUST READ
- ViewModel to understand state contract
- UiState/UiEvent definitions for all branches
- Existing Composable patterns for consistency

#### Backend Agent MUST READ
- UI state definitions to match state values
- Existing ViewModel event patterns
- Repository interface contracts
- DI container to understand wiring

#### Testing Agent MUST READ
- Production code to understand actual behavior
- Test patterns and helper functions
- Mock/stub implementations

#### Build Agent MUST READ
- build.gradle.kts to understand current state
- AndroidManifest.xml structure
- Existing dependency versions
- Impact on all layers (UI, Backend, Testing)

### Violation Detection: Stop & Hand Off If...

| Situation | Action |
|-----------|--------|
| **UI Agent modifying ViewModel** | ❌ STOP - Hand off to Backend Agent |
| **UI Agent modifying UseCase/Repository** | ❌ STOP - Hand off to Backend Agent |
| **Backend Agent modifying Composable functions** | ❌ STOP - Hand off to UI Agent |
| **Backend Agent modifying Theme/colors** | ❌ STOP - Hand off to UI Agent |
| **Testing Agent modifying production code** | ❌ STOP - Hand off to appropriate agent |
| **Build Agent modifying Java/Kotlin logic** | ❌ STOP - Hand off to appropriate agent |
| **Any agent editing files outside their zone** | ❌ STOP - Check matrix above, hand off |

### Cross-Boundary Files (Require Communication)

These files affect multiple agents and require approval before modification:

| File | Primary Owner | Must Communicate With |
|------|---------------|----------------------|
| `presentation/model/*UiState.kt` | UI Agent | Backend Agent (fills state) |
| `presentation/model/*UiEvent.kt` | UI Agent | Backend Agent (handles events) |
| `presentation/viewmodel/*.kt` | Backend Agent | UI Agent (uses state), Testing Agent |
| `di/AppContainer.kt` | Backend Agent | **ALL agents** (affects everyone) |
| `build.gradle.kts` | Build Agent | **ALL agents** (dependencies affect everyone) |
| `AndroidManifest.xml` | Build Agent | **ALL agents** (permissions affect everyone) |

---

## IV. Broader Context & Tunnel Vision Prevention

### The Problem: Tunnel Vision
Agents focusing only on their narrow task without understanding:
- What other files depend on their changes
- Whether their change breaks something downstream
- How multiple layers interact
- Cross-file impact

### The Solution: Broader Context Check

#### Before ANY modification, ask:

1. **"What files depend on mine?"**
   ```
   Example: Changing ChatViewModel.kt
   ├─ ChatScreen.kt depends on it (observes state)
   ├─ ChatViewModelTest.kt depends on it (tests it)
   └─ AppContainer.kt depends on it (provides instance)
   ```

2. **"Will my change break anything downstream?"**
   ```
   If I change UiState structure:
   ├─ ChatScreen must observe all branches
   ├─ Tests must verify all branches
   └─ ViewModel must emit valid states
   ```

3. **"Do I understand the complete flow?"**
   ```
   Not just: "I'll add a new ViewModel"
   But: "I'll add ViewModel → UI will observe it → Tests will verify → DI will provide it"
   ```

4. **"What if another agent modifies a dependency?"**
   ```
   If I'm creating a state branch:
   ├─ What if Repository fails to implement needed method?
   ├─ What if UI can't handle this state?
   ├─ What if Tests can't verify this state?
   ```

### Build Agent Specific: Avoid Narrow Focus

Build Agent responsibilities are **systemically broad**, not narrow.

#### ❌ NARROW (Tunnel Vision)
- "Just add this dependency"
- "Just update the gradle version"
- "Just configure the manifest"

#### ✅ BROAD (Systemic Awareness)
- "Add dependency and verify no conflicts with:
  - Existing versions
  - UI layer needs
  - Backend layer compatibility
  - Test framework requirements
  - Security implications"

- "Update gradle version and ensure:
  - Kotlin compatibility
  - Compose compatibility
  - Plugin compatibility
  - All agents' tooling works"

- "Configure manifest and understand:
  - Permission implications for UI
  - Security implications for Backend
  - Build implications for testing"

### Broader Context Checklist (Before ANY File Modification)

- [ ] **Identify dependencies**: What other files depend on mine?
- [ ] **Verify impact**: Will my change break any downstream code?
- [ ] **Check ripple effects**: What else needs updating?
- [ ] **Read related code**: Understand the full flow, not just my piece
- [ ] **Ask cross-layer questions**: How does this affect other layers?
- [ ] **Plan sequential updates**: What updates are needed in what order?
- [ ] **Verify completeness**: Have I updated ALL affected files?

### Example: Broader Context in Action

#### Scenario: Change UiState Structure

❌ **Tunnel Vision Approach** (Wrong)
```
Backend Agent thinks:
"I'll change ChatUiState from data class to sealed interface"
[Makes the change]
[Done - doesn't check what breaks]

Result: ChatScreen breaks, tests fail, inconsistency
```

✅ **Broader Context Approach** (Right)
```
Backend Agent thinks:
"I want to change ChatUiState structure. Let me check impact:
- ChatScreen observes this state → Need to update when/branches
- ChatViewModelTest tests this state → Need to update assertions
- ChatViewModel emits this state → Need to update emission points
- Is there documentation? → Yes, update examples

I'll update in this order:
1. ChatUiState (definition)
2. ChatViewModel (emission)
3. ChatScreen (observation)
4. ChatViewModelTest (assertions)
"
[Makes all changes together]
[Result: Consistent, nothing breaks]
```

---

## V. Atomic File Processing

### The Rule
Generate code **one complete file at a time**.

### Implementation
1. **Start with file header**:
   ```kotlin
   // File: app/src/main/java/com/novachat/app/ui/ChatScreen.kt
   package com.novachat.app.ui
   
   import ...
   ```

2. **Write the complete file** from top to bottom

3. **If file exceeds reasonable size** (>500 lines):
   - Stop after a logical section
   - Say: "This file is large. The implementation continues. Should I proceed with the next section?"
   - Wait for user confirmation
   - Continue from exactly where you stopped

4. **End with clear marker**:
   ```kotlin
   // End of ChatScreen.kt
   ```

### What Counts as "One File"
- ✅ A single `.kt` source file
- ✅ A single `build.gradle.kts` file
- ✅ A single XML resource file
- ❌ NOT multiple files in one response

---

## VI. Cross-File Dependency Protocol

### Before Changing Any File

1. **Identify all dependent files**:
   ```
   Changing: ChatViewModel.kt
   
   Dependencies:
   - ChatScreen.kt (observes ChatViewModel state)
   - ChatRepository.kt (injected into ChatViewModel)
   - ChatViewModelTest.kt (tests ChatViewModel)
   - AppContainer.kt (provides ChatViewModel instance)
   ```

2. **Determine ripple effects**:
   - What breaks if I change this?
   - What needs to be updated together?
   - Are there any interface contracts?

3. **Create update plan**:
   ```
   1. Update ChatViewModel.kt (change state structure)
   2. Update ChatScreen.kt (observe new state)
   3. Update ChatViewModelTest.kt (test new state)
   4. Update AppContainer.kt (if DI changes)
   ```

4. **Execute updates in dependency order**:
   - Core/base files first
   - Dependent files second
   - Test files last

### Ripple Effect Checklist
- [ ] Are there any interfaces this file implements?
- [ ] Are there any classes that extend this class?
- [ ] Are there any files that import this file?
- [ ] Are there any tests for this file?
- [ ] Are there any configuration files that reference this?

---

## VII. Self-Validation Protocol

### Before Outputting Any Code

Run these checks internally:

#### 1. Completeness Check
```
Question: "Did I write the FULL file?"
✅ Yes - File is complete from package declaration to last brace
❌ No - I used placeholders or summaries → FIX IMMEDIATELY
```

#### 2. Import Check
```
Question: "Did I include EVERY required import?"
✅ Yes - All classes, functions, and annotations are imported
❌ No - Missing imports → ADD THEM NOW
```

Example:
```kotlin
// ❌ WRONG - Missing imports
@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = viewModel()
    // ... implementation
}

// ✅ CORRECT - All imports present
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novachat.app.viewmodel.ChatViewModel

@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = viewModel()
    // ... implementation
}
```

#### 3. Syntax Check
```
Question: "Are all brackets { } closed? All parentheses balanced?"
✅ Yes - Code compiles without syntax errors
❌ No - Unbalanced brackets/parens → FIX IMMEDIATELY
```

#### 4. Logic Check
```
Question: "Does this implementation make sense?"
- Are variables used before being declared?
- Are there obvious logic errors?
- Does it follow the architecture pattern?
```

### Validation Failure Response
If ANY check fails:
1. **STOP immediately**
2. **Fix the issue**
3. **Re-run all checks**
4. **Only then output the code**

---

## VIII. Spec-First Mandate

### The Rule
**No code is written until a `specs/PHASE_X_SPEC.md` is committed.**

### Implementation
1. **Write specification first**: Create detailed spec file describing the feature, architecture, and acceptance criteria
2. **Commit the spec**: Push the spec to the repository
3. **Only then code**: Implement based on the committed spec
4. **Reference the spec**: Code comments reference section numbers of the spec

### Why This Matters
- Prevents post-hoc rationalization (writing code then documenting it)
- Forces design thinking before implementation
- Allows stakeholder feedback before coding begins
- Maintains audit trail of decisions

### Violation
❌ **NEVER** create "Summary" or "Completion" markdown files after coding
✅ **ALWAYS** spec the feature before any code is written

---

## VIII-B. Commit Hygiene Rule

### The Rule
**One type per commit. NEVER mix feature and documentation commits.**

### Implementation
1. Feature work (code changes): `feat(ui): Add chat screen` or `feat(backend): Add SendMessageUseCase`
2. Documentation (updates to `.md` files): `docs: Update DEVELOPMENT_PROTOCOL.md`
3. Tests: `test: Add ChatViewModelTest`
4. Build/Config: `build: Update gradle dependencies`
5. Refactor: `refactor: Extract KEY_DRAFT_MESSAGE constant`

### Invalid Patterns
❌ `feat(ui,docs): Add screen and document it` (mixed types)
❌ `feat: Update implementation AND create summary file` (code + doc in one commit)
❌ `docs: Add summary of previous 5 commits` (retroactive documentation)

### Why This Matters
- Atomic commits make git history readable
- Separate concerns = easier to revert if needed
- Enforces spec-first workflow (spec commit comes before code commit)

---

## IX. Input Disambiguation Protocol

### The "Confusion Guard"

When a user request is:
- ❓ Ambiguous
- 🔄 Repetitive
- 🤔 Conflicting with current state

**DO NOT GUESS. DO NOT ASSUME.**

### Response Protocol

#### Step 1: Interpretative Stop
```
STOP and say:
"I want to make sure I understand correctly. It sounds like you want to [X], 
but previously we implemented [Y]. 

Could you clarify:
1. Do you want to replace Y with X?
2. Do you want X as an additional feature alongside Y?
3. Do you want to modify Y to include aspects of X?"
```

#### Step 2: Reflection
```
Before implementing, summarize understanding:

"Based on your clarification, I will:
1. [Action 1]
2. [Action 2]
3. Files affected: [List]

Is this correct?"
```

#### Step 3: Source of Truth
```
ALWAYS check current files first:

"Let me check the current state of [File]..."
[Read file]
"I see that [Feature] is already implemented in [File]. 
Do you want to modify this existing implementation?"
```

### Examples

#### Example 1: Repetitive Request
```
User: "Add a settings screen"

Agent checks: SettingsScreen.kt already exists

Response:
"I checked and found that SettingsScreen.kt already exists with the following features:
- API key input
- AI mode selection (Online/Offline)
- Save button

Do you want to:
1. Add new features to the existing SettingsScreen?
2. Modify the existing features?
3. Something else?
```

#### Example 2: Ambiguous Request
```
User: "Update the chat feature"

Response:
"I want to make sure I implement exactly what you need. 
'Update the chat feature' could mean several things:

1. Update the ChatScreen UI?
2. Update the ChatViewModel logic?
3. Update the AI integration in ChatRepository?
4. Update the message display format?
5. Something else?

Which specific aspect would you like me to update?"
```

#### Example 3: Conflicting Request
```
User: "Use LiveData for state management"

Agent checks: Project currently uses StateFlow

Response:
"I notice that the project currently uses StateFlow for state management 
(as seen in ChatViewModel.kt and SettingsViewModel.kt). 

LiveData and StateFlow are different approaches. Switching would require 
updating multiple files and changing the architecture.

Did you mean to:
1. Convert the entire project from StateFlow to LiveData?
2. Keep StateFlow but add LiveData for a specific use case?
3. I may have misunderstood - could you clarify?"
```

---

## X. 2026 Standards Compliance

### Technology Standards

#### Kotlin
- **Version**: 2.2.21 (project baseline for CodeQL compatibility)
- **Compiler Plugin**: Compose Compiler Plugin (not separate plugin)
- **Coroutines**: 1.10.2+
- **Features**: Use context receivers, value classes where appropriate

#### Android Gradle Plugin
- **Version**: 9.0.0+
- **Gradle**: 9.1.0+
- **Build Features**: Compose, BuildConfig
- **Kotlin Options**: JVM target 17

#### Jetpack Compose
- **BOM**: 2026.01.01 or later
- **Compiler**: Integrated with Kotlin 2.2.21
- **Material 3**: Exclusive (no Material 2)
- **Navigation**: Compose Navigation 2.9.0+

#### Dependencies
```kotlin
// ✅ 2026 Standard
dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.01.01")
    implementation(composeBom)
    
    // Kotlin
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    
    // Lifecycle
   implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    
    // Google AI
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}
```

### Architecture Standards

#### MVVM + Clean Architecture
```
presentation/     → UI State, Events
ui/              → Composables
viewmodel/       → ViewModels (StateFlow)
domain/          → Use Cases
data/repository/ → Repository Implementations
data/model/      → Data Models
di/              → Dependency Injection
```

#### State Management
- **UI State**: Sealed interfaces or data classes
- **ViewModel State**: StateFlow (not LiveData)
- **Repository Results**: Result<T> or Flow<T>
- **Preferences**: DataStore (not SharedPreferences)

---

## XI. File Generation Workflow

### Standard Workflow

1. **Understand Request**
   - Read current files
   - Check for existing implementation
   - Identify dependencies

2. **Plan Changes**
   ```
   Files to modify:
   1. ChatViewModel.kt (add new state)
   2. ChatScreen.kt (observe new state)
   3. ChatViewModelTest.kt (test new state)
   
   Dependencies:
   - ChatViewModel.kt → ChatScreen.kt
   - ChatViewModel.kt → ChatViewModelTest.kt
   ```

3. **Generate Files (One at a Time)**
   ```
   File 1: ChatViewModel.kt
   [Complete implementation]
   
   File 2: ChatScreen.kt
   [Complete implementation]
   
   File 3: ChatViewModelTest.kt
   [Complete implementation]
   ```

4. **Self-Validate**
   - Completeness ✓
   - Imports ✓
   - Syntax ✓
   - Logic ✓

5. **Output**
   - Provide complete code
   - Explain changes
   - List affected files

---

## XII. Error Handling & Recovery

### When Things Go Wrong

#### Scenario 1: Code Doesn't Compile
```
1. Identify the error from compiler output
2. Check imports
3. Check syntax (brackets, parentheses)
4. Check type mismatches
5. Fix and provide complete corrected file
```

#### Scenario 2: User Reports Bug
```
1. Ask for specific error message or behavior
2. Check the relevant files
3. Identify root cause
4. Plan fix with ripple effect analysis
5. Implement fix with full files
6. Provide test case to verify fix
```

#### Scenario 3: Context Confusion
```
1. Acknowledge confusion
2. Ask user to clarify current state
3. Read relevant files to verify
4. Proceed only after clarity achieved
```

---

## XIII. Communication Standards

### When Responding to User

#### Good Response Pattern
```
"I understand you want to [X]. Let me check the current implementation.

[Checks files]

Current state:
- Feature [Y] exists in [File]
- Dependencies: [List]

To implement [X], I will:
1. [Action 1] in [File 1]
2. [Action 2] in [File 2]

Proceeding with File 1: [FileName]
[Complete implementation]

Should I continue with File 2?"
```

#### Bad Response Pattern
```
❌ "I'll add that feature."
   (Too vague - what feature? where? how?)

❌ "Here's the code... // rest of implementation"
   (Violates zero-elision policy)

❌ "Just update the ViewModel"
   (Not atomic - which ViewModel? show the code!)
```

### When Asking for Clarification

#### Good Clarification
```
"I need to clarify your request because:
1. [Specific reason for confusion]
2. [What I found in current code]
3. [What seems to conflict]

Could you specify:
- [Specific question 1]
- [Specific question 2]"
```

#### Bad Clarification
```
❌ "What do you want?"
   (Too general)

❌ "I don't understand"
   (Not helpful - be specific about what's unclear)
```

---

## XIV. Special Cases

### Large Files (>500 lines)

```
"I'm implementing [FileName]. This is a large file, so I'll provide it in sections.

Section 1: Package, Imports, and Data Classes
[Complete section]

Section 2: ViewModel Implementation
[Complete section]

Should I continue with Section 3?"
```

### Multiple Related Files

```
"This change affects 4 files. I'll provide them one at a time in dependency order:

1. Data model (foundation)
2. Repository (uses model)
3. ViewModel (uses repository)
4. Screen (uses ViewModel)

Starting with File 1: [FileName]
[Complete implementation]

Ready for File 2?"
```

### Refactoring

```
"This refactor will:
1. Rename [X] to [Y]
2. Move [A] to [B]
3. Update all references

Files affected:
- [File 1]
- [File 2]
- [File 3]

Proceeding with File 1: [FileName]
[Complete implementation]"
```

---

## XV. Quality Checklist

Before any code output, verify:

- [ ] **Completeness**: Full file, no placeholders
- [ ] **Imports**: All required imports included
- [ ] **Syntax**: All brackets/parens balanced
- [ ] **Standards**: Uses 2026 best practices
- [ ] **Architecture**: Follows MVVM + Clean Architecture
- [ ] **Dependencies**: Cross-file dependencies handled
- [ ] **Testing**: Test files updated if needed
- [ ] **Documentation**: KDoc for public APIs
- [ ] **Security**: No hardcoded secrets
- [ ] **Naming**: Follows Kotlin conventions
- [ ] **Abstraction Check**: No hardcoded strings/numbers that should be constants

---

## XVI. Abstraction & Constant Extraction

### The Rule
**Never hardcode magic strings or numbers. Extract to named constants.**

### Examples

#### ❌ WRONG - Hardcoded String
```kotlin
class ChatViewModel(...) : ViewModel() {
    fun updateDraftMessage(text: String) {
        savedStateHandle["draft_message"] = text  // Magic string!
    }
}

class ChatViewModelTest {
    @Test
    fun draftMessage_survives() {
        val key = savedStateHandle.get<String>("draft_message")  // Hardcoded everywhere!
    }
}
```

#### ✅ CORRECT - Named Constant
```kotlin
class ChatViewModel(...) : ViewModel() {
    companion object {
        internal const val KEY_DRAFT_MESSAGE = "draft_message"
    }
    
    fun updateDraftMessage(text: String) {
        savedStateHandle[KEY_DRAFT_MESSAGE] = text
    }
}

class ChatViewModelTest {
    @Test
    fun draftMessage_survives() {
        val key = savedStateHandle.get<String>(ChatViewModel.KEY_DRAFT_MESSAGE)
    }
}
```

### Pattern: Constant Location
- **View Models**: `internal const val` in companion object at top level of ViewModel class
- **Domain Models**: `const val` in companion object or top-level object
- **Repositories**: `const val` in companion object if private to repo, or in configuration class if shared
- **Tests**: Reference constants from production code, never hardcode

### Checklist Before Committing
- [ ] Are there any quoted strings repeated more than once in the same file?
- [ ] Are there any algorithm parameters (temperatures, timeouts, retry counts) hardcoded?
- [ ] Are there any resource names or keys hardcoded in multiple places?
- [ ] Can a magic value be extracted to a named constant in the appropriate class?

---

## XVII. Version Control

### Commit Guidelines

Each commit must:
1. Have a clear, descriptive message
2. Contain related changes only
3. Pass all checks (if configured)
4. Not include commented-out code
5. Not include debug statements

### Branch Strategy

- `main`: Stable, production-ready code
- `copilot/create-ai-chatbot-app`: Full application implementation
- `copilot/setup-copilot-instructions`: Multi-agent system configuration
- Feature branches: For new features in development

---

## XVIII. Enforcement

This protocol is **mandatory** for all development work on NovaChat.

### Violations
- Using placeholders → Must rewrite with complete code
- Skipping validation → Must validate before proceeding
- Ignoring dependencies → Must analyze ripple effects

### Updates
This protocol may be updated as:
- New standards emerge
- Better practices are discovered
- Issues are identified

**Current Version**: 2026.02
**Effective Date**: February 4, 2026

---

**End of Development Protocol**
