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

### Web Content & Verification Tools

When agents need to fetch web content, verify external docs, or automate browser flows:

- **MANDATORY: Ask which tool to use** before any web retrieval or verification. Do not choose a tool unilaterally; use the user-selected tool for the entire flow.
- If the user selects Cursor's built-in browser (cursor-ide-browser MCP), follow the skill reference and workflow.

- **Reference**: [`.github/skills/cursor-browser/SKILL.md`](skills/cursor-browser/SKILL.md)

- **Cursor core tools**: `browser_navigate`, `browser_snapshot`, `browser_click`, `browser_fill`, `browser_fill_form`, `browser_select_option`, `browser_evaluate`, `browser_take_screenshot`

- **Cursor workflow**: Navigate → Snapshot → Interact (using refs from snapshot) → Re-snapshot after DOM changes

### Git Context & Workflow (GitKraken MCP)

When agents need git context, history, or workflow operations:

- **Use GitKraken MCP** for:
  - `git_status` – Working tree status before/after work
  - `git_log_or_diff` – Commit history, diffs, changes in revision range
  - `git_blame` – Line-level authorship (reviewer)
  - `git_branch`, `git_checkout` – Branch management
  - `pull_request_get_detail`, `pull_request_get_comments` – PR context (reviewer)
  - `issues_assigned_to_me`, `issues_get_detail` – Issue context

- **Reference**: [`.github/skills/gitkraken-mcp/SKILL.md`](skills/gitkraken-mcp/SKILL.md)

- **Guardrails**: Reviewer and Planner use git tools read-only; `git_add_or_commit`, `git_push`, `git_stash` only when explicitly requested or approved

### Long-Term Memory (Pieces MCP)

When agents need to find older edits, changes, or context that may exist outside the current repo:

- **Use Pieces MCP** (`ask_pieces_ltm`) for:
  - Edits made in other IDEs (Android Studio, VS Code) that may not be committed
  - Previous implementations or decisions from past sessions
  - Cross-IDE context before reimplementing or duplicating work
  - Time-based recall: "What was I working on yesterday?"

- **Reference**: [`.github/skills/pieces-mcp/SKILL.md`](skills/pieces-mcp/SKILL.md)

- **Requirement**: PiecesOS running with LTM enabled; all data stored locally

---

## II. Zero-Elision Policy (Strictly Enforced)

### What Is Prohibited

NEVER use placeholders like:

```kotlin
// ... rest of implementation
// ... existing code
// ... other methods
```

NEVER summarize code with comments like:

```kotlin
// Add all the necessary imports here
// Implement the remaining methods
```

### What Is Required

ALWAYS write complete code:
No partial snippets, no omitted blocks, no elided sections.

### Enforcement

- **Before outputting code**: Self-check for completeness
- **If file is too large**: Stop and ask user to continue
- **No exceptions**: Even for "obvious" or "repetitive" code

---

## III. Agent Domain Boundaries & Access Control

### Agent Scope Purpose

Prevent agents from modifying files outside their responsibility, which causes confusion and breaking changes.

### Agent Modify-Access Zones (What They CAN Edit)

#### UI Agent - Modify Access

- [`ui/**/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui) - Composable functions, screens, layout
- [`ui/theme/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui/theme) - Colors, Typography, Theme configuration
- [`presentation/model/UiState.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model/UiState.kt) - UiState/UiEvent/UiEffect definitions
- [`MainActivity.kt`](../../app/src/main/java/com/novachat/app/MainActivity.kt) - **NavHost block only**, not other logic

#### Backend Agent - Modify Access

- [`presentation/viewmodel/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel) - ViewModels and state management
- [`domain/usecase/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain/usecase) - Business logic and use cases
- [`domain/model/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain/model) - Domain models (Android-agnostic)
- [`data/repository/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/repository) - Repository implementations
- [`data/model/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/model) - Data models and mappers
- [`data/mapper/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/mapper) - Domain ↔ Data conversion
- [`di/AiContainer.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/di/AiContainer.kt) - Dependency injection wiring

#### Testing Agent - Modify Access

- [`**/test/**/*Test.kt`](../../feature-ai/src/test/java) - Unit tests only
- [`**/androidTest/**/*Test.kt`](../../feature-ai/src/androidTest/java) - UI tests only
- [`**/test/**/Test*.kt`](../../feature-ai/src/test/java) - Test utilities and fixtures
- [`**/test/**/*Test.kt`](../../app/src/test/java) - App module tests only
- [`**/androidTest/**/*Test.kt`](../../app/src/androidTest/java) - App module UI tests

#### Preview Agent - Modify Access

- Add `@Preview` annotations to existing Composables (in [`ui/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui))
- Create `**/Preview.kt` files for preview helpers only (in [`ui/preview/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui/preview))
- **Modify Contents**: No - UI Agent owns Composable bodies

#### Build Agent - Modify Access

- [`build.gradle.kts`](../../build.gradle.kts) - Dependencies, versions, build config
- [`gradle.properties`](../../gradle.properties) - Gradle settings
- [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml) - Permissions, features, manifest
- [`res/xml/network_security_config.xml`](../../app/src/main/res/xml) - Security configuration
- [`app/proguard-rules.pro`](../../app/proguard-rules.pro) - Code optimization rules

### Agent Read-Access Zones (What They MUST Read Before Modifying)

#### UI Agent MUST READ

- ViewModel to understand state contract ([`presentation/viewmodel/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel))
- UiState/UiEvent definitions for all branches ([`presentation/model/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model))
- Existing Composable patterns for consistency ([`ui/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui))

#### Backend Agent MUST READ

- UI state definitions to match state values ([`presentation/model/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model))
- Existing ViewModel event patterns ([`presentation/viewmodel/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel))
- Repository interface contracts ([`domain/repository/`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain/repository))
- DI container to understand wiring ([`di/AiContainer.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/di/AiContainer.kt))

#### Testing Agent MUST READ

- Production code to understand actual behavior ([`feature-ai/src/main/java/`](../../feature-ai/src/main/java), [`app/src/main/java/`](../../app/src/main/java))
- Test patterns and helper functions ([`feature-ai/src/test/java/`](../../feature-ai/src/test/java), [`app/src/test/java/`](../../app/src/test/java))
- Mock/stub implementations (scan tests in [`feature-ai/src/test/java/`](../../feature-ai/src/test/java), [`app/src/test/java/`](../../app/src/test/java))

#### Build Agent MUST READ

- build.gradle.kts to understand current state ([`build.gradle.kts`](../../build.gradle.kts))
- AndroidManifest.xml structure ([`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml))
- Existing dependency versions ([`app/build.gradle.kts`](../../app/build.gradle.kts), [`feature-ai/build.gradle.kts`](../../feature-ai/build.gradle.kts), [`core-common/build.gradle.kts`](../../core-common/build.gradle.kts), [`core-network/build.gradle.kts`](../../core-network/build.gradle.kts))
- Impact on all layers (UI, Backend, Testing)

### Violation Detection Stop and Hand Off If Needed

| Situation                                       | Required Action       | Handoff Target                                                                         |
| ----------------------------------------------- | --------------------- | -------------------------------------------------------------------------------------- |
| **UI Agent editing ViewModel / domain / data**  | STOP work immediately | [Backend Agent](agents/backend-agent.agent.md)                                         |
| **UI Agent editing build/config**               | STOP work immediately | [Build Agent](agents/build-agent.agent.md)                                             |
| **Backend Agent editing UI / Compose / themes** | STOP work immediately | [UI Agent](agents/ui-agent.agent.md)                                                   |
| **Backend Agent editing build/config**          | STOP work immediately | [Build Agent](agents/build-agent.agent.md)                                             |
| **Testing Agent editing production code**       | STOP work immediately | [UI Agent](agents/ui-agent.agent.md) or [Backend Agent](agents/backend-agent.agent.md) |
| **Build Agent editing Java/Kotlin app logic**   | STOP work immediately | [UI Agent](agents/ui-agent.agent.md) or [Backend Agent](agents/backend-agent.agent.md) |
| **Any agent editing outside its zone**          | STOP work immediately | Primary owner per matrix above                                                         |

**How agents must use this table:**

- Before editing, confirm target files match the agent’s allowed scope.
- If a violation is detected, stop edits, report the file, and hand off using the target above.
- Never “fix it anyway” outside scope, even if the change is small.

### Cross-Boundary Files (Require Communication)

These files affect multiple agents and require approval before modification:

| File                                                                                                                    | Primary Owner                                  | Must Communicate With                                                                             | Approval Required      |
| ----------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------- | ---------------------- |
| [`presentation/model/UiState.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model/UiState.kt) | [UI Agent](agents/ui-agent.agent.md)           | [Backend Agent](agents/backend-agent.agent.md) (fills state/events/effects)                       | UI + Backend           |
| [`presentation/viewmodel/*.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel)          | [Backend Agent](agents/backend-agent.agent.md) | [UI Agent](agents/ui-agent.agent.md) (uses state), [Testing Agent](agents/testing-agent.agent.md) | Backend + UI + Testing |
| [`di/AiContainer.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/di/AiContainer.kt)                         | [Backend Agent](agents/backend-agent.agent.md) | **ALL agents** (affects everyone)                                                                 | Backend + All          |
| [`build.gradle.kts`](../../build.gradle.kts)                                                                            | [Build Agent](agents/build-agent.agent.md)     | **ALL agents** (dependencies affect everyone)                                                     | Build + All            |
| [`feature-ai/build.gradle.kts`](../../feature-ai/build.gradle.kts)                                                      | [Build Agent](agents/build-agent.agent.md)     | **ALL agents** (dependencies affect everyone)                                                     | Build + All            |
| [`core-common/build.gradle.kts`](../../core-common/build.gradle.kts)                                                    | [Build Agent](agents/build-agent.agent.md)     | **ALL agents** (dependencies affect everyone)                                                     | Build + All            |
| [`core-network/build.gradle.kts`](../../core-network/build.gradle.kts)                                                  | [Build Agent](agents/build-agent.agent.md)     | **ALL agents** (dependencies affect everyone)                                                     | Build + All            |
| [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml)                                                         | [Build Agent](agents/build-agent.agent.md)     | **ALL agents** (permissions affect everyone)                                                      | Build + All            |

**How agents must use this table:**

- Before changing any cross-boundary file, notify all listed parties.
- Do not edit until all required approvals are acknowledged.
- Include the impacted files and a short rationale in the handoff message.

---

## IV. Broader Context & Tunnel Vision Prevention

### The Problem: Tunnel Vision

Agents focusing only on their narrow task without understanding:

- What other files depend on their changes
- Whether their change breaks something downstream
- How multiple layers interact
- Cross-file impact

### The Solution: Broader Context Check

#### Before ANY modification ask (repo-wide)

**MANDATORY**: Check for BOTH explicit and implicit references!

1. **"What files depend on mine across the repo?"** (explicit imports/references)

   **AND**

   **"What files mention related concepts semantically?"** (implicit references)
   - Search for synonyms: "verify" vs "check" vs "validate"
   - Search for related tools: "browser" vs "web" vs "navigate"
   - Search for concepts: "external docs" vs "official sources" vs "release notes"

   ```text
   Example: Changing ChatViewModel.kt
   ├─ ui/ChatScreen.kt depends on it (observes state)
   ├─ test/ChatViewModelTest.kt depends on it (tests it)
   └─ di/AiContainer.kt depends on it (provides instance)
   ```

2. **"Will my change break anything downstream in other layers?"**

   ```text
   If I change UiState structure:
   ├─ UI must observe all branches
   ├─ ViewModel must emit valid states
   └─ Tests must verify all branches
   ```

3. **"Do I understand the complete flow end-to-end?"**

   ```text
   Not just: "I'll add a new ViewModel"
   But: "ViewModel → UI observes → Tests verify → DI provides"
   ```

4. **"What if another agent modifies a dependency in a different folder?"**

   ```text
   If I'm creating a state branch:
   ├─ What if Repository fails to implement needed method?
   ├─ What if UI can't handle this state?
   ├─ What if Tests can't verify this state?
   ```

**Quick cross-checks (entire workspace):**

- Search for usages in `feature-ai/src/main/java`, `feature-ai/src/test/java`, `feature-ai/src/androidTest/java`.
- Verify DI wiring in [`di/AiContainer.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/di/AiContainer.kt).
- Confirm UI observation in [`ui/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui).
- Confirm tests in [`feature-ai/src/test/java/`](../../feature-ai/src/test/java).

### Build Agent Specific: Avoid Narrow Focus

Build Agent responsibilities are **systemically broad**, not narrow, and must account for repo-wide impact.

#### ❌ NARROW (Tunnel Vision)

- "Just add this dependency"
- "Just update the Gradle version"
- "Just configure the manifest"
- "Just change a plugin version"

#### ✅ BROAD (Systemic Awareness)

- "Add dependency and verify no conflicts with:
  - Existing versions (including BOM and transitive)
  - UI layer needs
  - Backend layer compatibility
  - Test framework requirements
  - Build pipeline requirements
  - Security implications"

- "Update Gradle/AGP version and ensure:
  - Kotlin compatibility
  - Compose compatibility (BOM + compiler)
  - Plugin compatibility
  - CI and local build tooling still work"

- "Configure manifest and understand:
  - Permission implications for UI
  - Security implications for Backend
  - Build implications for testing
  - Impact on release signing and Play policy checks"

### Broader Context Checklist (Before ANY File Modification)

- **Identify dependencies**: What other files depend on mine across the repo?
- **Verify impact**: Will my change break any downstream code or tests?
- **Check ripple effects**: What else needs updating (Docs, CI, tests)?
- **Read related code**: Understand the full flow, not just my piece
- **Ask cross-layer questions**: How does this affect UI, backend, and testing?
- **Plan sequential updates**: What updates are needed in what order?
- **Verify completeness**: Have I updated ALL affected files?
- **Run verifications**: Update instructions or commands if build steps change

### Example: Broader Context in Action

#### Scenario: Change UiState Structure

❌ **Tunnel Vision Approach** (Wrong)

```text
Backend Agent thinks:
"I'll change ChatUiState from data class to sealed interface"
[Makes the change]
[Done - doesn't check what breaks]

Result: ChatScreen breaks, tests fail, inconsistency
```

✅ **Broader Context Approach** (Right)

```text
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

Repo links for this scenario:

- [`presentation/model/UiState.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model/UiState.kt)
- [`presentation/viewmodel/ChatViewModel.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/ChatViewModel.kt)
- [`ui/ChatScreen.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui/ChatScreen.kt)
- [`feature-ai/src/test/java/.../ChatViewModelTest.kt`](../../feature-ai/src/test/java/com/novachat/feature/ai/presentation/viewmodel/ChatViewModelTest.kt)

---

## V. Search & Locate Workflow

When the location of a change is unknown, use this workflow to avoid editing the wrong block.

Preferred search scope (start narrow, expand only if needed):

- [`feature-ai/src/main/java`](../../feature-ai/src/main/java)
- [`feature-ai/src/test/java`](../../feature-ai/src/test/java)
- [`feature-ai/src/androidTest/java`](../../feature-ai/src/androidTest/java)
- [`feature-ai/src/main/res`](../../feature-ai/src/main/res)

### Step 1: Semantic Discovery

- Ask: "Where is X handled?" or "Where does Y get created?"
- Use semantic search to find likely files and entry points
- Start in [`feature-ai/src/main/java`](../../feature-ai/src/main/java) and narrow to [`presentation/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation) or [`ui/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui) when possible

### Step 2: Symbol Search

- Use exact symbol search to jump to the definition
- Confirm the file path and scope match the intended layer
- Prefer jumping to definitions in [`presentation/viewmodel/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel), [`domain/`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain), or [`data/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data)

### Step 3: Find References

- Enumerate all usages of the symbol
- Identify alternative implementations or similarly named blocks
- Check references across [`feature-ai/src/main/java`](../../feature-ai/src/main/java) and [`feature-ai/src/test/java`](../../feature-ai/src/test/java)

### Step 4: Trace the Flow

- UI → ViewModel → UseCase → Repository → Data Source
- Verify the block is in the active path, not legacy or example code
- Confirm each step exists in [`ui/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui), [`presentation/viewmodel/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel), [`domain/usecase/`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain/usecase), and [`data/repository/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/repository)

### Step 5: Verify with Tests

- Check for tests that mention the symbol or behavior
- Update tests when behavior or contracts change
- Confirm test coverage in [`feature-ai/src/test/java`](../../feature-ai/src/test/java) and [`feature-ai/src/androidTest/java`](../../feature-ai/src/androidTest/java)

### Common Symbol Search Patterns

Use these repo-wide searches from the workspace root to quickly locate common contracts, flows, and anti-patterns.

```text
Result and error handling:
rg "Result<|runCatching|fold|onSuccess|onFailure"

Flow and StateFlow:
rg "\bStateFlow\b|\bMutableStateFlow\b|\bSharedFlow\b|\bStateIn\b|\bshareIn\b"
rg "\bFlow\b|\bChannel\b|\bReceiveChannel\b|\bsend\("

Lifecycle and scope:
rg "\bSavedStateHandle\b|\bviewModelScope\b|\bDispatchers\.\w+\b"

Sealed and value types:
rg "\bsealed (class|interface)\b|\bdata class\b|\bvalue class\b"

UI state/events/effects:
rg "UiState|UiEvent|UiEffect"

Use cases and repositories:
rg "\bUseCase\b|\bRepository\b|\bDataSource\b"

DataStore:
rg "DataStore|preferencesDataStore|protoDataStore"

Compose annotations:
rg "@Composable|@Preview|@Immutable|@Stable"

Navigation:
rg "NavHost|navController|navigate\("

DI patterns:
rg "AiContainer|provide[A-Za-z0-9]+|Factory|@Inject|@Singleton"

Coroutines:
rg "suspend\b|coroutineScope|withContext|async|await|launch\b"

Synchronization:
rg "\bMutex\b|\bwithLock\b|\bsynchronized\b"

Anti-patterns:
rg "AndroidView|Fragment|LiveData|MutableLiveData"

TODO markers:
rg "TODO:|FIXME:|HACK:"
```

---

## VI. Atomic File Processing

### The Rule

Generate code **one complete file at a time**.

### Implementation

1. **Start with file header**:

2. **Write the complete file** from top to bottom

3. **If file exceeds reasonable size** (>500 lines):
   - Stop after a logical section
   - Say: "This file is large. The implementation continues. Should I proceed with the next section?"
   - Wait for user confirmation
   - Continue from exactly where you stopped

Example file:

- [`feature-ai/src/main/java/com/novachat/feature/ai/ui/ChatScreen.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui/ChatScreen.kt)

### What Counts as "One File"

- ✅ A single `.kt` source file
- ✅ A single `build.gradle.kts` file
- ✅ A single XML resource file
- ❌ NOT multiple files in one response

---

## VII. Cross-File Dependency Protocol

### Before Changing Any File

**CRITICAL**: This protocol applies to ALL file types:

- **Code files** (Kotlin, Java, XML, etc.)
- **Agent files** (`.github/agents/*.agent.md`)
- **Skill files** (`.github/skills/*/SKILL.md`)
- **Documentation files** (`.github/*.md`, `README.md`, etc.)

1. **Identify all dependent files (repo-wide)** - BOTH explicit AND implicit:

   ```text
   Changing: ChatViewModel.kt

   Dependencies:
   - ui/ChatScreen.kt (observes state)
   - domain/usecase/MessageUseCases.kt (used by ViewModel)
   - data/repository/AiRepositoryImpl.kt (dependency chain)
   - test/ChatViewModelTest.kt (verifies behavior)
   - di/AiContainer.kt (provides instance)
   ```

2. **Determine ripple effects**:
   - What breaks if I change this?
   - What needs to be updated together?
   - Are there any interface contracts?
   - Are there any DI providers or factories that must be updated?

3. **Create update plan**:

   ```text
   1. Update ChatUiState (definition)
   2. Update ChatViewModel (emission)
   3. Update UI observers (when/branches)
   4. Update tests (assertions)
   5. Update DI wiring if constructor signature changed
   ```

4. **Execute updates in dependency order**:
   - Core/base files first
   - Dependent files second
   - Test files last

### Ripple Effect Checklist

**Code Dependencies:**

- [ ] Are there any interfaces this file implements?
- [ ] Are there any classes that extend this class?
- [ ] Are there any files that import this file?
- [ ] Are there any tests for this file?
- [ ] Are there any configuration files that reference this?
- [ ] Are there any DI providers or factories that reference this?
- [ ] Are there any docs/examples that mention this behavior?

**Documentation/Agent/Skill Cross-References (CRITICAL):**

- [ ] Are there any **implicit references** to this file/feature in agent files (`.github/agents/*.agent.md`)?
- [ ] Are there any **implicit references** in skill files (`.github/skills/*/SKILL.md`)?
- [ ] Are there any **implicit references** in documentation (`.github/*.md`, `README.md`, etc.)?
- [ ] When updating agents/skills/docs, did I check for **semantic references** (not just exact name matches)?
- [ ] Did I search for **related concepts** (e.g., "verify", "check", "validate" when updating verification tools)?
- [ ] Did I check for **cross-boundary references** (e.g., tool mentions, workflow references, protocol references)?

**Implicit Reference Detection:**
When updating any file, search for:

- **Semantic synonyms**: "verify" vs "check" vs "validate" vs "ensure"
- **Related concepts**: "fetch" vs "retrieve" vs "access" vs "get" vs "lookup"
- **Tool references**: "browser" vs "web" vs "navigate" vs "automation"
- **Workflow mentions**: References to processes, protocols, or patterns
- **Cross-file patterns**: Similar patterns in other files that might need updates

**Example**: When updating a tool name (e.g., "OldTool" → "NewTool"):

- ✅ Explicit: "OldTool" → Updated
- ✅ Explicit: "old-tool" → Updated
- ❌ **MISSED**: "verify external docs" (implicit reference) → **MUST UPDATE**
- ❌ **MISSED**: "check dependencies" (implicit reference) → **MUST UPDATE**
- ❌ **MISSED**: "validate versions" (implicit reference) → **MUST UPDATE**

### Implicit Reference Detection Protocol

**CRITICAL**: When updating ANY file (code, agent, skill, or documentation), you MUST check for implicit references, not just explicit name matches.

#### Step 1: Identify Semantic Concepts

Before updating, identify the semantic concepts involved:

- **Tool names**: "OldTool" → Also search for related terms like "browser", "web", "navigate", "fetch", "automation"
- **Actions**: "verify" → Also search for "check", "validate", "ensure", "confirm"
- **Concepts**: "external docs" → Also search for "official sources", "release notes", "documentation"

#### Step 2: Search for Implicit References

Use semantic search across the entire `.github/` directory for:

- **Synonyms and related terms** (not just exact matches)
- **Conceptual references** (e.g., "check dependencies" when updating verification tools)
- **Cross-file patterns** (similar patterns in other files)

#### Step 3: Update All Found References

Update BOTH:

- **Explicit references**: Exact name matches (e.g., "OldTool", "old-tool")
- **Implicit references**: Semantic matches (e.g., "verify external docs", "check dependencies")

#### Step 4: Verify No References Were Missed

After updating, search again with:

- Different synonyms
- Related concepts
- Cross-boundary terms

**Example Workflow**:

```text
Updating: "OldTool" → "NewTool"

Step 1 - Identify concepts:
- Tool: browser automation (or whatever the tool does)
- Action: verify/check/validate external sources
- Purpose: web content retrieval (or tool's purpose)

Step 2 - Search for implicit references:
- "verify" → Found in build-agent.agent.md (line 48)
- "check" → Found in AGENTS.md (line 207)
- "validate" → Found in planner.agent.md (line 69)
- "external docs" → Found in reviewer-agent.agent.md (line 72)
- "dependencies" → Found in security-check/SKILL.md (line 204)

Step 3 - Update all found references:
✅ Updated explicit: "OldTool" → "NewTool"
✅ Updated implicit: "verify external docs" → "verify external docs using NewTool"
✅ Updated implicit: "check dependencies" → "check dependencies using NewTool"

Step 4 - Verify:
- Search again for "verify", "check", "validate" → All updated
- Search for related terms → All updated to mention NewTool
```

#### Mandatory Checklist for Any Update

- [ ] Searched for explicit name matches (exact strings)
- [ ] Searched for semantic synonyms (verify/check/validate)
- [ ] Searched for related concepts (browser/web/navigate)
- [ ] Searched for cross-boundary references (agents/skills/docs)
- [ ] Updated ALL found references (explicit + implicit)
- [ ] Verified no references were missed (re-search with different terms)

---

## VIII. Self-Validation Protocol

### Before Outputting Any Code

Run these checks internally (every file, every time):

#### 1. Completeness Check

```text
Question: "Did I write the FULL file?"
Yes - File is complete from package declaration to last brace
No - I used placeholders or summaries → FIX IMMEDIATELY
```

#### 2. Import Check

```text
Question: "Did I include EVERY required import?"
Yes - All classes, functions, and annotations are imported
No - Missing imports → ADD THEM NOW
```

Example:

```kotlin
// WRONG - Missing imports
@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = viewModel()
    // ... implementation
}

// CORRECT - All imports present
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.novachat.feature.ai.presentation.viewmodel.ChatViewModel

@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = viewModel()
    // ... implementation
}
```

#### 3. Syntax Check

```text
Question: "Are all brackets { } closed? All parentheses balanced?"
Yes - Code compiles without syntax errors
No - Unbalanced brackets/parens → FIX IMMEDIATELY
```

#### 4. Logic Check

```text
Question: "Does this implementation make sense?"
- Are variables used before being declared?
- Are there obvious logic errors?
- Does it follow the architecture pattern?
- Does it match existing contracts (interfaces, sealed states, DI)?
- Did I update all dependent files (UI, tests, DI)?
```

### Quick Self-Validation Checklist

- [ ] Full file written, no placeholders or ellipses
- [ ] Imports complete and minimal
- [ ] Build compiles in principle (no unresolved symbols)
- [ ] Architecture boundaries respected (UI/VM/domain/data)
- [ ] Tests updated when behavior changed
- [ ] Documentation/examples updated if referenced

### Validation Failure Response

If ANY check fails:

1. **STOP immediately**
2. **Fix the issue**
3. **Re-run all checks**
4. **Only then output the code**
5. **Document the fix briefly** (what failed, what changed)

---

## IX. Spec-First Mandate

### Spec-First Rule

**No production code is written until a spec in `specs/` is committed.**
Name it clearly for the feature (for example: `specs/PHASE_X_SPEC.md`).

### Spec-First Implementation

1. **Write specification first**: Describe scope, architecture, data flow, and acceptance criteria
2. **Review for completeness**: Confirm impacts on UI, ViewModel, domain, data, tests, and DI
3. **Commit the spec**: Commit the spec before any production code changes
4. **Only then code**: Implement strictly against the committed spec
5. **Reference the spec**: Use section numbers in code comments when relevant

### Why This Matters

- Prevents post-hoc rationalization (writing code then documenting it)
- Forces design thinking before implementation
- Allows stakeholder feedback before coding begins
- Maintains audit trail of decisions

### Violation

❌ **NEVER** create "Summary" or "Completion" markdown files after coding
❌ **NEVER** backfill a spec after code is already written
✅ **ALWAYS** spec the feature before any production code is written

---

## VIII-B. Commit Hygiene Rule

### Commit Hygiene Rule

**One type per commit. NEVER mix feature and documentation commits.**

### Commit Hygiene Implementation

1. Feature work (code changes): `feat(ui): Add chat screen` or `feat(backend): Add SendMessageUseCase`
2. Documentation (updates to `.md` files): `docs: Update DEVELOPMENT_PROTOCOL.md`
3. Tests: `test: Add ChatViewModelTest`
4. Build/Config: `build: Update gradle dependencies`
5. Refactor: `refactor: Extract KEY_DRAFT_MESSAGE constant`
6. Chore (non-functional changes): `chore: Update repo metadata`

### Invalid Patterns

❌ `feat(ui,docs): Add screen and document it` (mixed types)
❌ `feat: Update implementation AND create summary file` (code + doc in one commit)
❌ `docs: Add summary of previous 5 commits` (retroactive documentation)
❌ `test: Update tests and production code` (mixed scope)

### Commit Hygiene Benefits

- Atomic commits make git history readable
- Separate concerns = easier to revert if needed
- Enforces spec-first workflow (spec commit comes before code commit)
- Improves review focus (each PR commit has a single intent)

---

## X. Input Disambiguation Protocol

### Best Practices

Trigger this protocol when a user request is ambiguous, repetitive, or conflicts with current state.
**DO NOT GUESS. DO NOT ASSUME.**

### Tone Requirements

- Keep responses concise and neutral.
- Ask only what is needed to disambiguate.
- Always cite the file or folder you checked.

### Response Protocol

#### Step 1: Interpretative Stop

```text
STOP and say:
"I want to make sure I understand correctly. It sounds like you want to [X],
but previously we implemented [Y].

Could you clarify:
1. Do you want to replace Y with X?
2. Do you want X as an additional feature alongside Y?
3. Do you want to modify Y to include aspects of X?"
```

#### Step 2: Reflection

```text
Before implementing, summarize understanding:

"Based on your clarification, I will:
1. [Action 1]
2. [Action 2]
3. Files affected: [List]

Is this correct?"
```

#### Step 3: Source of Truth

```text
ALWAYS check current files first:

"Let me check the current state of [File]..."
"I checked: [File A], [File B]"
"I see that [Feature] is already implemented in [File].
Do you want to modify this existing implementation?"
```

### Examples

#### Example 1: Repetitive Request

```text
User: "Add a settings screen"

Agent checks: [`ui/SettingsScreen.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui/SettingsScreen.kt) already exists

Response:
"I checked and found that SettingsScreen.kt already exists with the following features:
- AI mode selection (Online/Offline; offline disabled)
- Theme mode selection (Light/Dark/System)
- Dynamic color toggle

Do you want to:
1. Add new features to the existing SettingsScreen?
2. Modify the existing features?
3. Something else?
```

#### Example 2: Ambiguous Request

```text
User: "Update the chat feature"

Response:
"I want to make sure I implement exactly what you need.
'Update the chat feature' could mean several things:

1. Update the ChatScreen UI? ([`ui/ChatScreen.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui/ChatScreen.kt))
2. Update the ChatViewModel logic? ([`presentation/viewmodel/ChatViewModel.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/ChatViewModel.kt))
3. Update the AI integration in `AiRepositoryImpl`? ([`data/repository/AiRepositoryImpl.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/repository/AiRepositoryImpl.kt))
4. Update the message display format? ([`presentation/model/UiState.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model/UiState.kt))
5. Something else?

Which specific aspect would you like me to update?"
```

#### Example 3: Conflicting Request

```text
User: "Use LiveData for state management"

Agent checks: Project currently uses StateFlow in [`presentation/viewmodel/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel)

Response:
"I notice that the project currently uses StateFlow for state management
(as seen in [`ChatViewModel.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/ChatViewModel.kt) and
[`SettingsViewModel.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/SettingsViewModel.kt)).

LiveData and StateFlow are different approaches. Switching would require
updating multiple files and changing the architecture.

Did you mean to:
1. Convert the entire project from StateFlow to LiveData?
2. Keep StateFlow but add LiveData for a specific use case?
3. I may have misunderstood - could you clarify?"
```

#### Example 4: Build/Config Request

```text
User: "Add Retrofit and OkHttp"

Agent checks: [`feature-ai/build.gradle.kts`](../../feature-ai/build.gradle.kts), [`app/build.gradle.kts`](../../app/build.gradle.kts), and [`build.gradle.kts`](../../build.gradle.kts)

Response:
"I checked the current build files and didn’t see Retrofit/OkHttp listed.
Do you want:
1. Add dependencies only?
2. Add dependencies + minimal usage example?
3. Add dependencies + update network layer?"
```

---

## XI. 2026 Standards Compliance

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
- **Kotlin Options**: JVM target 21

#### Jetpack Compose

- **BOM**: 2026.01.01 or later
- **Repository**: Google Maven only (requires `google()` in repositories)
- **Source of truth**: [BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping)
- **Raw metadata**: [maven-metadata.xml](https://dl.google.com/android/maven2/androidx/compose/compose-bom/maven-metadata.xml)
- **Compiler**: Integrated with Kotlin 2.2.21
- **Material 3**: Exclusive (no Material 2)
- **Navigation**: Compose Navigation 2.9.0+
- **Where defined**: [`feature-ai/build.gradle.kts`](../../feature-ai/build.gradle.kts), [`app/build.gradle.kts`](../../app/build.gradle.kts), and [`settings.gradle.kts`](../../settings.gradle.kts)

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

    // Firebase (for AI proxy)
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    // Note: All KTX modules removed - KTX functionality now in main modules (BOM v34.0.0+)
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-auth")
}
```

### Architecture Standards

#### MVVM + Clean Architecture

```text
presentation/     → UI State, Events
ui/               → Composables
viewmodel/        → ViewModels (StateFlow)
domain/           → Use Cases
data/repository/  → Repository Implementations
data/model/       → Data Models
di/               → Dependency Injection
```

Repo paths:

- [`presentation/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation)
- [`ui/`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui)
- [`domain/`](../../feature-ai/src/main/java/com/novachat/feature/ai/domain)
- [`data/repository/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/repository)
- [`data/model/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/model)
- [`di/`](../../feature-ai/src/main/java/com/novachat/feature/ai/di)

#### State Management

- **UI State**: Sealed interfaces or data classes in [`presentation/model/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model)
- **ViewModel State**: `StateFlow` (not LiveData) in [`presentation/viewmodel/`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel)
- **Repository Results**: `Result<T>` or `Flow<T>` in [`data/repository/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/repository)
- **Preferences**: DataStore (not SharedPreferences) in [`data/repository/`](../../feature-ai/src/main/java/com/novachat/feature/ai/data/repository)

---

## XII. File Generation Workflow

### Standard Workflow

1. **Understand Request**
   - Read current files
   - Check for existing implementation
   - Identify dependencies

2. **Plan Changes**

   ```text
   Files to modify:
   1. ChatViewModel.kt (add new state)
   2. ChatScreen.kt (observe new state)
   3. ChatViewModelTest.kt (test new state)

   Dependencies:
   - ChatViewModel.kt → ChatScreen.kt
   - ChatViewModel.kt → ChatViewModelTest.kt
   ```

3. **Generate Files (One at a Time)**

   ```text
   File 1: ChatViewModel.kt
   [Complete implementation]

   File 2: ChatScreen.kt
   [Complete implementation]

   File 3: ChatViewModelTest.kt
   [Complete implementation]
   ```

   If any file is out of scope for the current agent, stop and hand off before proceeding.

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

## XIII. Error Handling & Recovery

### When Things Go Wrong

#### Scenario 1: Code Doesn't Compile

```text
1. Identify the error from compiler output
2. Check imports
3. Check syntax (brackets, parentheses)
4. Check type mismatches
5. Trace the error to the exact file/symbol
6. Fix and provide complete corrected file
7. Re-check dependent files and tests
```

#### Scenario 2: User Reports Bug

```text
1. Ask for specific error message or behavior
2. Check the relevant files
3. Identify root cause
4. Plan fix with ripple effect analysis
5. Implement fix with full files
6. Provide test case to verify fix
```

#### Scenario 3: Context Confusion

```text
1. Acknowledge confusion
2. Ask user to clarify current state
3. Read relevant files to verify
4. Proceed only after clarity achieved
```

---

## XIV. Communication Standards

### When Responding to User

#### Good Response Pattern

```text
"I understand you want to [X]. Let me check the current implementation.

[Checked files]: [File A], [File B]

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

```text
WRONG - "I'll add that feature."
   (Too vague - what feature? where? how?)

WRONG - "Here's the code... // rest of implementation"
   (Violates zero-elision policy)

WRONG - "Just update the ViewModel"
   (Not atomic - which ViewModel? show the code!)

WRONG - "I changed it without checking existing files"
   (Skips source-of-truth verification)
```

### When Asking for Clarification

#### Good Clarification

```text
"I need to clarify your request because:
1. [Specific reason for confusion]
2. [What I found in current code]
3. [What seems to conflict]

Could you specify:
- [Specific question 1]
- [Specific question 2]"
```

#### Bad Clarification

```text
WRONG - "What do you want?"
   (Too general)

WRONG - "I don't understand"
   (Not helpful - be specific about what's unclear)

WRONG - "I'll just pick one approach"
   (Assumes intent instead of clarifying)
```

---

## XV. Special Cases

### Large Files (>500 lines)

```text
"I'm implementing [FileName]. This is a large file, so I'll provide it in sections.

Section 1: Package, Imports, and File-Level Types
[Complete section]

Section 2: Primary implementation block (core logic)
[Complete section]

Should I continue with Section 3?"
```

Rules:

- Always finish a logical block before stopping (no half-written functions).
- Clearly label where the next section starts.
- Do not skip imports or class headers in Section 1.
- Prefer large file edits in ViewModels or complex UI files only (for example: [`presentation/viewmodel/ChatViewModel.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/ChatViewModel.kt)).

### Multiple Related Files

```text
"This change affects 4 files. I'll provide them one at a time in dependency order:

1. Domain model (foundation)
2. Repository interface/impl (uses model)
3. ViewModel (uses repository)
4. UI screen (observes ViewModel)

Starting with File 1: [FileName]
[Complete implementation]

Ready for File 2?"
```

Rules:

- State the dependency order up front.
- Confirm all files are in scope before starting.
- If any file is out of scope, stop and hand off.
- Example dependency chain (NovaChat):
  - [`presentation/model/UiState.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/model/UiState.kt)
  - [`presentation/viewmodel/ChatViewModel.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/presentation/viewmodel/ChatViewModel.kt)
  - [`ui/ChatScreen.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/ui/ChatScreen.kt)
  - [`feature-ai/src/test/java/.../ChatViewModelTest.kt`](../../feature-ai/src/test/java/com/novachat/feature/ai/presentation/viewmodel/ChatViewModelTest.kt)

### Refactoring

```text
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

Rules:

- List all symbol renames and file moves before editing.
- Update references in tests and DI wiring.
- Avoid partial refactors; complete all references before stopping.
- If a refactor touches DI, update [`di/AiContainer.kt`](../../feature-ai/src/main/java/com/novachat/feature/ai/di/AiContainer.kt) and re-check impacted tests in [`feature-ai/src/test/java`](../../feature-ai/src/test/java).

---

## XVI. Quality Checklist

Before any code output, verify:

- [ ] Completeness: Full file, no placeholders
- [ ] Imports: All required imports included
- [ ] Syntax: All brackets/parens balanced
- [ ] Standards: Uses 2026 best practices
- [ ] Architecture: Follows MVVM + Clean Architecture
- [ ] Dependencies: Cross-file dependencies handled (code + documentation)
- [ ] Implicit References: Checked for semantic references in agents/skills/docs
- [ ] Testing: Test files updated if needed
- [ ] Documentation: KDoc for public APIs + cross-references updated
- [ ] Security: No hardcoded secrets
- [ ] Naming: Follows Kotlin conventions
- [ ] Abstraction Check: No hardcoded strings/numbers that should be constants

Notes:

- Prefer shared constants in the owning layer (`presentation/`, `domain/`, `data/`) rather than re‑declaring literals.
- If API behavior changes, update tests and any referenced docs.

---

## XVII. Abstraction & Constant Extraction

### Constant Extraction Rule

**Never hardcode magic strings or numbers. Extract to named constants.**

### Constant Extraction Examples

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

#### CORRECT - Named Constant

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

- Are there any quoted strings repeated more than once in the same file?
- Are there any algorithm parameters (temperatures, timeouts, retry counts) hardcoded?
- Are there any resource names or keys hardcoded in multiple places?
- Can a magic value be extracted to a named constant in the appropriate class?

---

## XVIII. Version Control

### Commit Guidelines

Each commit must:

1. Have a clear, descriptive message
2. Contain related changes only
3. Pass all checks (if configured)
4. Not include commented-out code
5. Not include debug statements
6. Respect commit hygiene rules (one change type per commit)
7. Avoid committing generated artifacts unless required

See also: [Commit Hygiene Rule](#viii-b-commit-hygiene-rule)

### Branch Strategy

- `main`: Stable, production-ready code
- `copilot/create-ai-chatbot-app`: Full application implementation
- `copilot/setup-copilot-instructions`: Multi-agent system configuration
- Feature branches: For new features in development
- Short-lived fix branches: `fix/<area>-<short-desc>` (for targeted bug fixes)
- Worktree/branch examples: [`scripts/setup-worktree.sh`](../../scripts/setup-worktree.sh)

---

## XIX. Enforcement

This protocol is **mandatory** for all development work on NovaChat.

### Violations

- Using placeholders → Must rewrite with complete code
- Skipping validation → Must validate before proceeding
- Ignoring dependencies → Must analyze ripple effects
- Missing implicit references → Must search for semantic synonyms and related concepts
- Updating only explicit references → Must check for implicit references in agents/skills/docs
- Skipping required handoffs/approvals → Must stop and notify owners

### Updates

This protocol may be updated as:

- New standards emerge
- Better practices are discovered
- Issues are identified
- Tooling or CI changes require process updates

**Current Version**: 2026.02
**Effective Date**: February 4, 2026

---

End of Development Protocol
