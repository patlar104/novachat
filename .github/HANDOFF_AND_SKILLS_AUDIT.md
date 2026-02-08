# Agent System Audit: Handoffs & Skills Issues

**Date**: February 5, 2026  
**Status**: Issues Identified

---

## Executive Summary

The multi-agent system has **7 critical areas** where handoffs and skills need clarification. These issues create ambiguity that can lead to:

- **Agent scope creep** (agents working outside their boundaries)
- **Incomplete features** (features handed off but expectations unclear)
- **Duplicate/conflicting work** (agents working on same files)
- **Missing integrations** (cross-boundary files updated in isolation)

---

## 🔴 Critical Issues

### Issue 1: Missing Agent Definition Files

**Status**: ❌ **CRITICAL**

**Current State**:

- AGENTS.md references `planner.agent.md`, `ui-agent.agent.md`, `backend-agent.agent.md`, etc.
- These files do NOT exist in the repository
- Line in AGENTS.md (Section "Directory Structure"):

```text
.github/
├── agents/
│   ├── planner.agent.md
│   ├── ui-agent.agent.md
│   ├── preview-agent.agent.md
│   ├── backend-agent.agent.md
│   ├── testing-agent.agent.md
│   ├── build-agent.agent.md
│   └── reviewer-agent.agent.md
```

**Problem**:

- Agents cannot be invoked or referenced if their definition files don't exist
- Users cannot read these files to understand agent constraints
- No way to enforce agent boundaries

**Impact**:


- Agents are invoked but using what definition? (None exists)
- Users cannot reference `@copilot using ui-agent.agent.md` because file doesn't exist
- Agent scope enforcement relies entirely on users' memory

**Solution Required**:


1. ✅ Create `.github/agents/planner.agent.md` with Planner Agent definition
2. ✅ Create `.github/agents/ui-agent.agent.md` with UI Agent definition
3. ✅ Create `.github/agents/preview-agent.agent.md`
4. ✅ Create `.github/agents/backend-agent.agent.md`
5. ✅ Create `.github/agents/testing-agent.agent.md`
6. ✅ Create `.github/agents/build-agent.agent.md`
7. ✅ Create `.github/agents/reviewer-agent.agent.md`


---

### Issue 2: Handoff Matrix Not Documented

**Status**: ⚠️ **MAJOR**

**Current State**:

- AGENTS.md mentions handoffs in narrative form scattered throughout
- Example from UI Agent section: "Handoffs: To Backend (for ViewModel integration), Testing (for UI tests), or Reviewer"
- No centralized, complete handoff routing table

**Problem**:

- When an agent finishes work, how does it decide which agent to hand off to?
- Different agents have different valid handoff paths
- No clear documentation of which agent can hand off to which

**Example of Missing Structure**:

```text
❌ Current (scattered in text):
"Handoffs: To Backend (for ViewModel integration), Testing (for UI tests), or Reviewer"

✅ Needed (centralized matrix):
UI Agent Handoff Routes:
├─ New Composable needs ViewModel → Backend Agent
├─ Composable implementation complete → Preview Agent
├─ Needs theme/color adjustment → UI Agent (self-loop for shared resources)
├─ Needs testing → Testing Agent
└─ Critical review needed → Reviewer Agent
```

**Impact**:

- Agents guess which agent to hand off to
- Incomplete handoffs leave work dangling
- Testing may miss needed backend changes
- Preview coverage incomplete if code not handed to Preview Agent

**Solution Required**:

Create a comprehensive handoff matrix showing:

1. ✅ Valid handoff paths for each agent
2. ✅ Conditions triggering each handoff
3. ✅ Required state/context to pass during handoff

---

### Issue 3: Cross-Boundary File Coordination Not Implementable

**Status**: ⚠️ **MAJOR**

**Current State**:

- DEVELOPMENT_PROTOCOL.md Section VI lists cross-boundary files:

  - `presentation/model/*UiState.kt` - Modified by UI Agent, read by Backend Agent
  - `presentation/model/*UiEvent.kt` - Modified by UI Agent, read by Backend Agent
  - `presentation/viewmodel/*.kt` - Modified by Backend Agent, read by UI Agent & Testing Agent
  - `di/AppContainer.kt` - Modified by Backend Agent, synchronized across all agents

**Problem**:

- Protocol says these files "require communication" and "approval before modification"
- No mechanism exists for agents to communicate or coordinate
- No handoff protocol for shared ownership
- Risk of racing changes: UI Agent modifies state while Backend Agent updates observable

**Example Scenario** (What Can Go Wrong):

```kotlin
1. UI Agent creates new ChatUiState.Loading state
2. UI Agent doesn't hand off to Backend Agent
3. Backend Agent is unaware of new state
4. ViewModel never emits ChatUiState.Loading
5. ChatScreen observes loading state that never happens
6. Feature is broken
```


**Impact**:

- Race conditions on shared files
- Inconsistent state definitions and usage
- ChatScreen observes states ViewModel never emits
- Tests may test states UI never displays

**Solution Required**:

1. ✅ Create coordination protocol for cross-boundary files
2. ✅ Document explicit handoff when modifying shared files
3. ✅ Require review of all affected files before merging changes

---

### Issue 4: Skills Missing Coverage for Major Domains

**Status**: ⚠️ **MAJOR**

**Current State**:

Skills exist for:

- ✅ `android-testing/SKILL.md` - Complete ViewModel unit testing patterns
- ✅ `compose-preview/SKILL.md` - Preview annotations
- ✅ `material-design/SKILL.md` - Material 3 components & theme
- ✅ `security-check/SKILL.md` - Security & network config

Missing skills for:

- ❌ `backend-patterns/SKILL.md` - Domain modeling, use case patterns, repository patterns, error handling
- ❌ `clean-architecture/SKILL.md` - MVVM structure, state management, ViewModels, sealed interfaces
- ❌ `dependency-injection/SKILL.md` - Manual DI pattern, AppContainer, lazy singletons, factory functions


**Problem**:

- Backend Agent is the largest, most complex agent (6 file scopes)
- No reusable patterns for core backend work
- Testing Agent tests depend on patterns not documented
- Each Backend Agent task requires starting from scratch

**Example Impact**:

- Backend Agent creates a new ViewModel
- No documented pattern for ViewModel structure
- Testing Agent creates tests but no pattern reference
- Code might not follow project conventions

**Solution Required**:

1. ✅ Create `.github/skills/backend-patterns/SKILL.md`
   - ViewModel structure and StateFlow setup
   - UseCase patterns (input/output, `Result` handling)
   - Repository interface design
   - Error handling with `Result` and `fold()`
   - State transition examples

2. ✅ Create `.github/skills/clean-architecture/SKILL.md`
   - Layer separation (presentation/domain/data)
   - Sealed interface patterns (UiState/UiEvent/UiEffect)
   - Data flow between layers
   - Import policy (what can import what)

3. ✅ Create `.github/skills/dependency-injection/SKILL.md`
   - AppContainer structure
   - Lazy singleton pattern
   - Factory function pattern
   - Wiring repositories, use cases, ViewModels


---

### Issue 5: Preview Agent Scope Ambiguity

**Status**: ⚠️ **MEDIUM**

**Current State**:

- AGENTS.md says: "Add `@Preview` annotations to existing Composables"
- AGENTS.md constraints: "ONLY creates preview code (for IDE debugging, not production)"
- compose-preview/SKILL.md says: "Creating preview files → create_file"

**Problem**:



Unclear whether Preview Agent:

1. Only adds `@Preview` annotations to existing Composables (lightweight)
2. Creates separate `*Preview.kt` files with `@Composable` preview functions (heavyweight)
3. Creates preview data helper objects (e.g., `PreviewChatScreenData`)


**Example Confusion**:

```kotlin
// Option 1: Just annotation (minimal)
@Preview
@Composable
fun ChatScreenPreview() {
    ChatScreen(...)
}

// Option 2: Separate file with composition (current skill pattern)
// File: app/src/main/java/.../ui/preview/ChatScreenPreview.kt
@Composable
fun PreviewChatScreen(...) { ... }

// Which is Preview Agent responsible for?
```


**Current Skill Shows**:

```kotlin
@Composable
fun PreviewChatScreen(uiState: ChatUiState, ...) {
    NovaChatTheme {
        ChatScreenContent(...)
    }
}

@Preview
@Composable
fun ChatScreenInitialPreview() {
    PreviewChatScreen(...)
}
```

This is creating BOTH:

- A helper Composable (`PreviewChatScreen`)
- Multiple @Preview functions


**Impact**:

- Preview Agent scope unclear
- Whether Agent creates new files vs. modifies existing
- How much preview logic is Preview Agent responsible for

**Solution Required**:

1. ✅ Clarify in AGENTS.md whether Preview Agent creates helper Composables
2. ✅ Clarify whether Preview Agent creates separate files or adds annotations in-place
3. ✅ Update AGENTS.md constraints if needed (currently says "Add @Preview annotations" which seems incomplete)


---

### Issue 6: Testing Agent Handoff to Production Code Issues

**Status**: ⚠️ **MEDIUM**

**Current State**:

- Testing Agent scope says: "If tests fail, reports issues and hands off to appropriate agent"
- android-testing/SKILL.md says: "Hand off to Backend Agent if: Production code logic needs fixing"

**Problem**:

- What does "reports issues and hands off" mean in practice?
- Testing Agent creates a test file, runs it, it fails
- Root cause: ViewModel logic bug (Backend Agent scope)
- Testing Agent cannot fix it (violates Testing Agent scope)
- How does handoff happen?

**Example Scenario**:

```text
1. Testing Agent creates ChatViewModelTest
2. Test runs: sendMessage updates state incorrectly
3. Testing Agent reads ChatViewModel - finds logic bug
4. Testing Agent reports to Backend Agent... how?
5. What format? What information is passed?
```


**Impact**:

- Incomplete test implementations (tests fail, no handoff mechanism)
- Unclear handoff documentation in skill
- Testing Agent might attempt to fix production code

- Testing Agent might attempt to fix production code

**Solution Required**:

1. ✅ Document formal testing handoff protocol
2. ✅ Show example handoff message format
3. ✅ Update android-testing/SKILL.md with handoff examples


---

### Issue 7: Reviewer Agent Handoff Routing Not Specified


**Status**: ⚠️ **MEDIUM**

**Current State**:

- AGENTS.md Reviewer Agent: "Routes issues to appropriate agents for fixes"
- No documentation of HOW it routes
- No documented format for issue routing

**Problem**:

Complete review might find issues in:

- UI layer (hand to UI Agent)
- Backend layer (hand to Backend Agent)
- Tests (hand to Testing Agent)
- Build config (hand to Build Agent)
- Security (hand to Build Agent or specific domain)

**How does Reviewer route these systematically?**

**Example Reviewer Output**:

```text
❌ No current mechanism:
"There are issues in ChatScreen and ChatViewModel"
(Reviewer can't split these!)

✅ Needed:
Reviewer Agent identifies 3 issues:
1. [UI Agent] ChatScreen missing error state handling
2. [Backend Agent] ViewModel not emitting error state
3. [Testing Agent] Tests don't cover error states

Each issue routed with:

- Clear problem description
- Root cause analysis
- File(s) affected
- Suggested fix (if applicable)

```

**Impact**:

- Reviewer output unclear about which agent handles what
- Multiple issues mixed in one handoff
- Agents unclear about scope of required fixes

**Solution Required**:

1. ✅ Document Reviewer Agent routing protocol
2. ✅ Create issue categorization system (UI/Backend/Test/Build/Security)
3. ✅ Show example Reviewer Agent handoff with multiple categorized issues

---

## 🟡 Secondary Issues

### Issue 8: Skill Handoff Guidance Missing


**Status**: ⚠️ **MINOR**

Each skill should document when to hand off, but they don't consistently:

**Current State**:

- ✅ android-testing/SKILL.md has "When to Hand Off" section
- ✅ compose-preview/SKILL.md has "When to Hand Off to Other Agents" section
- ✅ material-design/SKILL.md has "When to Hand Off to Other Agents" section
- ✅ security-check/SKILL.md has "When Build Agent Should Hand Off" section

**Problem**:

Skills define handoffs, but don't show them in order of likelihood or priority.

Example from compose-preview/SKILL.md:

```text
Hand off to UI Agent if:
- Composable function doesn't exist ...
- Composable layout/logic needs changes
- Material Design implementation needs adjustment
- State management in Composable needs fixes

Hand off to Backend Agent if:
- ViewModel or state management needs changes
- Preview data providers need production logic
```

Second one ("Preview data providers need production logic") is vague.

**Impact**:

- Minor: agents follow handoff guidance, but could be more precise
- Low priority since structure exists, just needs tightening

**Solution Required**:

Document more precisely when "preview data providers need production logic"

---

### Issue 9: Atomic File Processing Not Enforced for Handoffs

**Status**: ⚠️ **MINOR**

**Current State**:

- DEVELOPMENT_PROTOCOL.md enforces "one complete file at a time"
- But no handoff protocol says WHEN an agent should hand off
- Example: UI Agent creates ChatScreen, should it hand off after 1 file? Or wait for theme files?

**Problem**:

Unclear when handoff should occur:

```text
Scenario: UI Agent creating login screen
├─ Create LoginScreen.kt - hand off after this?
├─ Update theme/colors - hand off after this?
├─ Create preview file - hand off after this?
└─ When does the handoff actually happen?
```

**Impact**:


- Low impact: usually agents complete a logical feature before handing off
- But could cause unnecessary rework if handoff timing unclear

---

### Issue 10: No Cross-Reference Index

**Status**: ℹ️ **INFORMATIONAL**

**Current State**:

- AGENTS.md has 7 agent descriptions
- Each agent has file scopes listed
- But no index showing: "If I need to modify ChatViewModel.kt, which agent(s) can touch it?"

**Needed**:

File-centric index:

```text
ChatViewModel.kt:
├─ Primary Owner: Backend Agent
├─ Readers: UI Agent, Testing Agent
├─ Modifiers Allowed: Backend Agent only
└─ Related Files: ChatUiState.kt, ChatViewModelTest.kt, AppContainer.kt
```

**Impact**:

- Users must read all agent descriptions to find the right agent
- Could cause agents to work outside their scope unknowingly


---

## Summary Table

| Issue | Severity | Category | Impact | Status |
| --- | --- | --- | --- | --- |
| 1. Missing Agent Files | 🔴 CRITICAL | Documentation | Can't invoke agents | NOT STARTED |
| 2. No Handoff Matrix | 🔴 CRITICAL | Process | Incomplete features | NOT STARTED |
| 3. Cross-Boundary Coordination | 🟠 MAJOR | Process | Race conditions | NOT STARTED |
| 4. Missing Skill Files | 🟠 MAJOR | Documentation | Incomplete patterns | NOT STARTED |
| 5. Preview Agent Scope | 🟡 MEDIUM | Clarity | Scope creep | NOT STARTED |
| 6. Testing Handoff Protocol | 🟡 MEDIUM | Documentation | Incomplete tests | NOT STARTED |
| 7. Reviewer Routing | 🟡 MEDIUM | Documentation | Unclear issues | NOT STARTED |
| 8. Skill Handoff Guidance | 🟡 MINOR | Documentation | Minor ambiguity | READY TO FIX |
| 9. Atomic File Handoff Timing | 🟡 MINOR | Process | Re-work possible | READY TO FIX |
| 10. No Cross-Reference Index | ℹ️ INFO | Usability | Navigation harder | READY TO FIX |

---

## Recommended Action Plan

### Phase 1: Critical (BLOCKING)

1. Create all 7 agent definition files
2. Create handoff matrix documentation
3. Document cross-boundary file coordination

### Phase 2: Major

1. Create missing skill files
2. Clarify Preview Agent scope

### Phase 3: Medium

1. Create testing handoff protocol
2. Create reviewer routing documentation

### Phase 4: Polish

1. Tighten skill handoff guidance
2. Document atomic file handoff timing
3. Create cross-reference file index


---

## Notes for Implementation

- **Skills should be complete**: All examples must follow DEVELOPMENT_PROTOCOL.md (no placeholders)
- **Agent files should be detailed**: Include examples of handoff messages
- **Matrix should be visual**: Use mermaid diagrams or clear tables
- **Coordination protocol**: Show example of how agents communicate about cross-boundary files

