# Agent Handoff Matrix

**Purpose**: Document all valid agent-to-agent handoffs and when they occur.

**Status**: Complete reference for all agent workflows

---

## Quick Reference: Who Can Hand Off to Whom?

```
┌─────────────────────────────────────────────────────────────────┐
│ PLANNER AGENT                                                   │
│ └─ Hands off to: UI | Backend | Build | Testing | Reviewer     │
├─────────────────────────────────────────────────────────────────┤
│ UI AGENT                                                        │
│ └─ Hands off to: Backend | Preview | Testing | Reviewer        │
├─────────────────────────────────────────────────────────────────┤
│ BACKEND AGENT                                                   │
│ └─ Hands off to: UI | Testing | Build | Reviewer               │
├─────────────────────────────────────────────────────────────────┤
│ PREVIEW AGENT                                                   │
│ └─ Hands off to: UI | Testing | Reviewer                       │
├─────────────────────────────────────────────────────────────────┤
│ TESTING AGENT                                                   │
│ └─ Hands off to: Backend | UI | Build | Reviewer               │
├─────────────────────────────────────────────────────────────────┤
│ BUILD AGENT                                                     │
│ └─ Hands off to: Testing | Backend | Reviewer                  │
├─────────────────────────────────────────────────────────────────┤
│ REVIEWER AGENT                                                  │
│ └─ Hands off to: UI | Backend | Testing | Build | Planner      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed Handoff Scenarios

### 1. PLANNER AGENT Handoffs

#### 1.1 → UI Agent (Create UI Implementation)
**When**: Specification complete, UI components needed
**Trigger**: "Implement composables according to spec"
**Information Passed**:
- Specification file reference (specs/PHASE_X_SPEC.md)
- UI components list (screens, composables needed)
- Design requirements (Material 3, theme, accessibility)
- ViewModel state contract (UiState/UiEvent definitions)

**Example**:
```
PLANNER → UI AGENT

Task: Implement chat screen UI

Reference: specs/PHASE_005_CHAT_SCREEN.md

Design Requirements:
□ Material 3 Compose components only (no XML)
□ Light and dark theme support
□ Accessible (semantics for screen readers)
□ Show message bubbles for user/AI
□ Input field with send button

ViewModel State Contract:
- ChatUiState (sealed interface) with states: Initial, Loading, Success, Error
- ChatUiEvent (sealed interface) with events: SendMessage, Clear
- UiEffect - ShowSnackbar effect

Acceptance Criteria:
✓ All screens from spec implemented
✓ State observation working
✓ Events trigger correctly
```

#### 1.2 → Backend Agent (Implement Business Logic)
**When**: Domain/data layer needed for feature
**Trigger**: "Implement ViewModels and repositories"
**Information Passed**:
- Specification file reference
- Domain model definitions needed
- Repository interfaces required
- ViewModel event handling contract
- DI container updates needed

**Example**:
```
PLANNER → BACKEND AGENT

Task: Implement chat business logic

Reference: specs/PHASE_005_CHAT_SCREEN.md

Domain Layer Needed:
□ Message domain model (id, content, isFromUser, timestamp)
□ AiConfiguration model (mode, apiKey)
□ SendMessageUseCase
□ ObserveMessagesUseCase

Repository Layer Needed:
□ MessageRepository interface + impl
□ AiRepository interface + impl

ViewModel Implementation:
□ ChatViewModel with StateFlow<ChatUiState>
□ Single onEvent() handler for ChatUiEvent
□ Error handling with Result<T>

DI Wiring:
□ Add to AppContainer: repositories, usecases, viewmodel factory

Files to Reference:
- presentation/model/ChatUiState.kt (state contract from UI Agent)
- presentation/model/ChatUiEvent.kt (event contract from UI Agent)

Acceptance Criteria:
✓ ViewModel correctly observes and emits state
✓ Events handled in single onEvent() method
✓ All error paths use Result<T>.fold()
✓ SavedStateHandle used for draft messages
```

#### 1.3 → Build Agent (Configure Dependencies)
**When**: New dependencies needed for feature
**Trigger**: "Add/verify dependencies"
**Information Passed**:
- Specification file reference
- List of required dependencies
- Dependency versions (must be 2026 standards)
- Build configuration changes needed

**Example**:
```
PLANNER → BUILD AGENT

Task: Add Google Generative AI SDK dependency

Reference: specs/PHASE_010_GEMINI_INTEGRATION.md

Dependencies Needed:
□ Google AI Client Library (com.google.ai.client.generativeai)
   Version: 0.9.0+ (2026 standard)
□ Kotlin Serialization (for API models)
   Version: 1.6.0+

Dependency Compatibility:
- Must work with Kotlin 2.2.21
- Must not conflict with existing versions
- Check for security vulnerabilities

ProGuard/R8 Rules Needed:
- Keep Google AI classes from obfuscation
- (Specifics in specification)

Acceptance Criteria:
✓ All specified versions added
✓ No version conflicts
✓ Project compiles cleanly
✓ No new security issues introduced
```

#### 1.4 → Testing Agent (Create Test Strategy)
**When**: Test coverage plan needed
**Trigger**: "Create tests according to strategy"
**Information Passed**:
- Specification file reference
- Test coverage requirements
- Unit test focus areas
- Integration test paths
- Test data patterns

**Example**:
```
PLANNER → TESTING AGENT

Task: Create tests for chat feature

Reference: specs/PHASE_005_CHAT_SCREEN.md

Unit Tests Needed:
□ ChatViewModel:
  • sendMessage success case
  • sendMessage network error case
  • sendMessage empty message case
  • clearConversation
□ SendMessageUseCase:
  • successful send and save
  • AI failure handling
  • input validation
□ AiRepositoryImpl:
  • online mode (Gemini) success
  • offline mode (AICore) not supported yet

UI Tests Needed:
□ ChatScreen:
  • Display initial empty state
  • Send message flow
  • Error state display
  • Message persistence on rotation

Mock/Fake Pattern:
- Use FakeAiRepository returning "Test response"
- Use FakeMessageRepository with in-memory storage

Acceptance Criteria:
✓ All unit tests created with complete setup
✓ Compose UI tests for critical paths
✓ Edge cases covered (empty input, network error, etc.)
✓ All tests pass
```

#### 1.5 → Reviewer Agent (Final Review)
**When**: Feature implementation complete
**Trigger**: "Review all implementation"
**Information Passed**:
- Specification file reference
- File list to review
- Acceptance criteria
- Known limitations

**Example**:
```
PLANNER → REVIEWER AGENT

Task: Review chat feature implementation

Reference: specs/PHASE_005_CHAT_SCREEN.md

Files to Review:
□ UI Layer
  - ui/ChatScreen.kt
  - ui/preview/ChatScreenPreview.kt
□ Backend Layer
  - presentation/viewmodel/ChatViewModel.kt
  - domain/usecase/SendMessageUseCase.kt
  - domain/model/Message.kt
  - data/repository/AiRepositoryImpl.kt
□ Tests
  - ChatViewModelTest.kt
  - ChatScreenTest.kt

Review Against:
✓ DEVELOPMENT_PROTOCOL.md (no placeholders, complete)
✓ Clean Architecture (layer separation)
✓ MVVM pattern (state management)
✓ Test coverage (all paths tested)
✓ Security (no hardcoded secrets, input validated)
✓ Accessibility (screen reader support)
✓ Performance (no unnecessary recompositions)

Specification Compliance:
✓ All acceptance criteria met
✓ Known limitations documented
✓ Future work identified
```

---

### 2. UI AGENT Handoffs

#### 2.1 → Backend Agent (Missing ViewModel)
**When**: Composable needs ViewModel that doesn't exist
**Trigger**: "ViewModel not found, implementation needed"
**Information Passed**:
- Screen that needs ViewModel
- UiState/UiEvent interfaces required
- Expected behavior/state transitions

**Example**:
```
UI AGENT → BACKEND AGENT

Issue: SettingsScreen needs SettingsViewModel

Required ViewModel:
- Location: presentation/viewmodel/SettingsViewModel.kt
- State: SettingsUiState (sealed interface) with states:
  • Initial, Loading, Success, Error
- Events: SettingsUiEvent (sealed interface):
  • ToggleDarkMode
  • ChangeApiKey
  • SaveSettings

State Transitions Needed:
- User toggles dark mode → state updated
- Submit settings → Loading → Success/Error

Acceptance Criteria:
✓ ViewModel created and observable in Composable
✓ Dark mode toggle persists preference
✓ Error handling for preference save failures
```

#### 2.2 → Preview Agent (Preview Coverage)
**When**: Composable implementation complete
**Trigger**: "Ready for preview coverage"
**Information Passed**:
- Composable file path and function name
- All possible UI states from UiState
- Device variants to preview (phone, tablet)
- Theme variants needed (light, dark)

**Example**:
```
UI AGENT → PREVIEW AGENT

Task: Add preview coverage for ChatScreen

Composable Location:
- File: app/src/main/java/com/novachat/app/ui/ChatScreen.kt
- Functions: ChatScreen() - Main composable to preview

States to Preview:
□ ChatUiState.Initial (empty screen, no messages)
□ ChatUiState.Loading (spinner visible)
□ ChatUiState.Success (messages displayed, single exchange)
□ ChatUiState.Error (error message shown)

Device Variants:
□ Phone (small: 4.5", medium: 5.5", large: 6.5")
□ Tablet (portrait)

Theme Variants:
□ Light theme
□ Dark theme (use @PreviewLightDark)

Preview Data Helpers Needed:
- PreviewChatScreenData.initialState()
- PreviewChatScreenData.successWithMessages()
- PreviewChatScreenData.errorState()

Acceptance Criteria:
✓ At least 2 device variants
✓ Light and dark theme shown
✓ All major UI states previewed
✓ Preview Composables have no ViewModel imports
```

#### 2.3 → Testing Agent (UI Test Cases)
**When**: UI implementation ready for testing
**Trigger**: "Composable ready for UI testing"
**Information Passed**:
- Screen name and file path
- Key interactions to test
- Expected state changes
- Error scenarios

**Example**:
```
UI AGENT → TESTING AGENT

Task: Create UI tests for ChatScreen

Composable: app/src/main/java/com/novachat/app/ui/ChatScreen.kt

Key Interactions to Test:
□ User types message and taps send
  Expected: onEvent(ChatUiEvent.SendMessage) called
            Message appears in list
□ Error banner appears when message send fails
  Expected: Error text displayed
            Dismiss button available
□ Clear conversation button clears message list
  Expected: All messages removed
            Back to initial state

State Assertions:
□ Initial state: "Start a conversation!" message shown
□ Loading state: Spinner visible, input disabled
□ Success state: Messages displayed in correct order
□ Error state: Error toast/snackbar shown

ViewModel Mocks Needed:
- Mock ChatViewModel
- Return states from uiState.value
- Track onEvent() calls

Acceptance Criteria:
✓ All user interactions tested
✓ State transitions verified
✓ Error handling tested
✓ No missing interactions
```

#### 2.4 → Reviewer Agent (UI Code Review)
**When**: UI implementation complete and tested
**Trigger**: "UI ready for architecture review"
**Information Passed**:
- All UI files created/modified
- Test files created
- Preview coverage
- Design system compliance

---

### 3. BACKEND AGENT Handoffs

#### 3.1 → UI Agent (Update UI Needed)
**When**: ViewModel state changed, UI must update
**Trigger**: "ViewModel state structure changed"
**Information Passed**:
- ViewModel file path
- State changes (new branches, parameters)
- Event changes (new event types)
- Required UI updates

**Example**:
```
BACKEND AGENT → UI AGENT

Issue: ChatViewModel state structure changed

Changes:
- ChatUiState.Success now includes:
  • messages: List<Message>
  • isLoadingMore: Boolean
  • selectedMessage: Message?

Required UI Updates:
□ ChatScreen must observe isLoadingMore for loading indicator
□ Show message selection visual (highlight/background)
□ Handle selectedMessage state (show detail panel)

Event Addition:
- ChatUiEvent.SelectMessage(messageId: Long)

UI Screens Affected:
- ChatScreen (main display)
- ChatDetailPanel (new, shows selected message)

Acceptance Criteria:
✓ ChatScreen observes all state changes
✓ New loading indicator works
✓ Message selection visually distinct
✓ Detail panel displays selected message
```

#### 3.2 → Testing Agent (ViewModel Tests)
**When**: ViewModel implementation complete
**Trigger**: "ViewModel ready for unit testing"
**Information Passed**:
- ViewModel file path and class name
- State structure (UiState/UiEvent)
- Major event paths to test
- Error scenarios

**Example**:
```
BACKEND AGENT → TESTING AGENT

Task: Create unit tests for ChatViewModel

ViewModel: presentation/viewmodel/ChatViewModel.kt

Constructor:
- Requires: SavedStateHandle, SendMessageUseCase, ObserveMessagesUseCase
- Uses mock/fake implementations

State Observations Needed:
□ uiState: StateFlow<ChatUiState>
□ uiEffect: receiveAsFlow() → ChatUiEffect

Event Paths to Test:
□ SendMessage event → loading → success
□ SendMessage event → loading → error
□ SendMessage with empty text → ShowSnackbar effect
□ Clear conversation → success
□ Retry message → success

Mock Setup Required:
- FakeSendMessageUseCase returning Result.success("response")
- FakeSendMessageUseCase throwing exception
- FakeObserveMessagesUseCase with test data

State Assertions:
- Initial state is ChatUiState.Initial
- After sendMessage: Loading visible
- After success: Success state with messages
- Draft message in SavedStateHandle

Acceptance Criteria:
✓ All event paths tested
✓ State transitions verified
✓ Error handling tested
✓ Draft message persistence tested
```

#### 3.3 → Build Agent (Dependency Addition)
**When**: New dependencies required for backend
**Trigger**: "New dependencies needed"
**Information Passed**:
- Required library/package
- Version constraints
- Compatibility requirements
- Build configuration changes

**Example**:
```
BACKEND AGENT → BUILD AGENT

Issue: Need DataStore for preference persistence

Required Dependency:
- androidx.datastore:datastore-preferences:1.1.0+
- Must support Kotlin 2.2.21
- No conflicts with existing androidx versions

Build Configuration:
- Add to build.gradle.kts dependencies
- Verify version catalog (libs.versions.toml) if used

ProGuard Rules:
- No special rules needed for public API

Compatibility Check:
- Current AGP 9.0.0: ✓ Compatible
- Current Compose BOM 2026.01.01: ✓ Compatible (Google Maven; mapping: [BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping))
- Current Kotlin 2.2.21: ✓ Compatible

Acceptance Criteria:
✓ Dependency added with correct version
✓ Project compiles without conflicts
✓ No new security vulnerabilities
✓ Works on target SDK 35
```

#### 3.4 → Reviewer Agent (Backend Review)
**When**: UseCase, Repository, ViewModel all complete
**Trigger**: "Backend implementation ready for review"
**Information Passed**:
- Backend files to review
- Architecture compliance checklist
- Test coverage status

---

### 4. PREVIEW AGENT Handoffs

#### 4.1 → UI Agent (Composable Issues)
**When**: Creating preview, Composable not working
**Trigger**: "Composable doesn't compile or preview"
**Information Passed**:
- Error details
- Composable location
- What needs fixing

**Example**:
```
PREVIEW AGENT → UI AGENT

Issue: ChatScreen doesn't preview correctly

Problem:
- Compilation error: ChatUiState not found
- or: ViewModel being used in preview (not allowed)

Location:
- File: app/src/main/java/com/novachat/app/ui/ChatScreen.kt

Fix Needed:
□ Ensure all state parameters have default values
□ Remove any @Composable that uses ViewModel
□ Create parameterized preview helper function

Example Pattern:
```kotlin
// Composable with state parameters (previewable)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState = ChatUiState.Initial,
    onEvent: (ChatUiEvent) -> Unit = {}
) { ... }

// Preview it
@Preview
@Composable
fun ChatScreenInitialPreview() {
    ChatScreenContent(
        uiState = ChatUiState.Initial
    )
}
```

Acceptance Criteria:
✓ Composable has state parameters with defaults
✓ No ViewModel usage in Composable
✓ PreviewData helpers available
```

#### 4.2 → Backend Agent (State Definition Missing)
**When**: UiState/UiEvent interfaces missing or incomplete
**Trigger**: "Cannot preview, need state definitions"
**Information Passed**:
- Composable needing preview
- What state branches needed
- What parameters each state needs

---

### 5. TESTING AGENT Handoffs

#### 5.1 → Backend Agent (Production Code Bug)
**When**: Test finds bug in ViewModel/Repository
**Trigger**: "Production code logic incorrect"
**Information Passed**:
- Test file that failed
- What the bug is
- Expected behavior
- Root cause analysis if possible

**Example**:
```
TESTING AGENT → BACKEND AGENT

Issue: SendMessageUseCase doesn't save messages correctly

Test File: app/src/test/java/com/novachat/app/domain/usecase/SendMessageUseCaseTest.kt

Failing Test:
- sendMessage_success_savesUserMessage()

Issue:
- UseCase calls messageRepository.addMessage() with wrong parameters
- Timestamp not set correctly
- isFromUser flag incorrect

Expected Behavior:
- User message saved with isFromUser=true
- AI message saved with isFromUser=false
- Both messages appear in list afterward

Root Cause (if known):
- Possibly mapping issue in SendMessageUseCase
- Or MessageRepository not storing correctly

Files to Check:
- SendMessageUseCase.kt
- MessageRepository interface + impl

Acceptance Criteria:
✓ Test passes after fix
✓ Both messages save correctly
✓ No regression in other tests
```

#### 5.2 → UI Agent (UI Component Bug)
**When**: Compose UI test finds bug in Composable
**Trigger**: "UI not behaving as expected"
**Information Passed**:
- Which UI test failed
- What behavior is wrong
- What's expected

**Example**:
```
TESTING AGENT → UI AGENT

Issue: ChatScreen not displaying error state

Test File: app/src/androidTest/java/com/novachat/app/ui/ChatScreenTest.kt

Failing Test:
- chatScreen_showsErrorBannerWhenSendFails()

Issue:
- When uiState is ChatUiState.Error, error banner not shown
- or: Error text not displayed

Expected UI Behavior:
- ChatUiState.Error shows error banner with message
- Perhaps with dismiss button

File to Check:
- ChatScreen.kt (when branch for Error state)

Possible Fixes:
□ Missing when branch for ChatUiState.Error
□ Error message parameter named differently
□ Error banner Composable not visible

Acceptance Criteria:
✓ Error banner displays when state is Error
✓ Error message text visible
✓ User can dismiss banner
✓ Test passes
```

#### 5.3 → Build Agent (Test Setup Issue)
**When**: Tests fail due to missing dependencies/config
**Trigger**: "Test infrastructure missing"
**Information Passed**:
- What test framework is missing
- What error message appears
- Which tests are affected

**Example**:
```
TESTING AGENT → BUILD AGENT

Issue: Compose UI tests fail to compile

Error: Cannot find: androidx.compose.ui.test.junit4.createComposeRule

Problem:
- Test dependency missing or wrong version
- or: Plugin not configured

Required:
- androidx.compose.ui:ui-test-junit4 (in testImplementation)
- androidx.compose.ui:ui-test-manifest (in androidTestImplementation)

Current versions: Need to verify in build.gradle.kts

Affected Tests:
- All files in app/src/androidTest/java/com/novachat/app/ui/*

Acceptance Criteria:
✓ Dependencies added with correct versions
✓ Tests compile without errors
✓ Tests run successfully
```

#### 5.4 → Reviewer Agent (Coverage Review)
**When**: Testing complete, ready for quality check
**Trigger**: "Tests ready for coverage review"
**Information Passed**:
- Test files created
- Coverage summary
- Edge cases tested

---

### 6. BUILD AGENT Handoffs

#### 6.1 → Testing Agent (Dependency Added)
**When**: New dependency added, test setup may be affected
**Trigger**: "New dependency added, verify test config"
**Information Passed**:
- Dependency added
- Potential test impacts
- What to verify

**Example**:
```
BUILD AGENT → TESTING AGENT

Dependency Added: Google Generative AI SDK 0.9.0

Impact on Tests:
□ May need mock objects for GeminiAPI
□ May need test API key handling
□ Network mocking framework might be helpful

Verification Needed:
□ GeminiDataSource tests created
□ Mock API responses working
□ Error cases handled

Consider Adding:
- OkHttp MockWebServer for API mocking
- Or direct mocking with MockK

Acceptance Criteria:
✓ Tests still pass with new dependency
✓ No new test failures
✓ DataSource integration tests work
```

#### 6.2 → Backend Agent (Dependency Available)
**When**: Dependency added successfully, ready to use
**Trigger**: "Dependency available for implementation"
**Information Passed**:
- Dependency added
- Version confirmed
- Ready to implement

**Example**:
```
BUILD AGENT → BACKEND AGENT

Dependency Available: DataStore Preferences 1.1.0

Ready to Implement:
□ PreferencesRepository using DataStore
□ Secure preference storage for API keys
□ AI mode preference (Online/Offline)

Examples Available in Skill:
- Refer to dependency-injection/SKILL.md for complete examples
- PreferencesRepository implementation pattern

Acceptance Criteria:
✓ PreferencesRepository created
✓ Preferences persist across app restarts
✓ Encryption used for sensitive data
```

---

### 7. REVIEWER AGENT Handoffs

#### 7.1 → UI Agent (UI Issues Found)
**When**: Code review identifies UI problems
**Trigger**: "Issues found in UI layer"
**Information Passed**:
- Specific files and lines
- Issue description and severity
- Suggested fix (if applicable)

**Example**:
```
REVIEWER AGENT → UI AGENT

Issue Category: Design System Non-Compliance

Files Affected:
- ui/ChatScreen.kt (line 45-50)
- ui/SettingsScreen.kt (line 30)

Issues Found:
□ CRITICAL: Text color hardcoded instead of MaterialTheme.colorScheme.onSurface
□ MAJOR: Missing accessibility semantics on icon buttons
□ MINOR: Inconsistent padding (12.dp vs 16.dp)

Details:
1. Text color: "#3C3C3C" hardcoded
   → Should use: MaterialTheme.colorScheme.onSurfaceVariant

2. Icon button missing contentDescription
   → Add semantics for screen readers

3. Padding inconsistency
   → Use consistent theme spacing values

Acceptance Criteria:
✓ All colors from MaterialTheme
✓ All interactive elements have contentDescription
✓ Spacing consistent throughout
```

#### 7.2 → Backend Agent (Backend Issues Found)
**When**: Code review identifies architecture/logic problems
**Trigger**: "Issues found in backend layer"
**Information Passed**:
- Files affected
- Issue description
- Severity level

**Example**:
```
REVIEWER AGENT → BACKEND AGENT

Issue Category: Error Handling Missing

Files Affected:
- domain/usecase/SendMessageUseCase.kt (line 30-40)

Issues Found:
□ CRITICAL: No error handling when messageRepository.addMessage() fails
□ MAJOR: UseCase doesn't validate empty message before sending
□ MEDIUM: Dispatcher not injected (hardcoded)

Details:
1. Missing error handling:
   → Wrap in try-catch or use repository Result<T>

2. Input validation:
   → Check userMessage.isNotBlank() before processing

3. Hardcoded Dispatcher:
   → Inject Dispatchers.IO, don't hardcode

Acceptance Criteria:
✓ All error paths return Result.Failure
✓ Input validation before business logic
✓ Dependencies injected (no hardcoding)
```

#### 7.3 → Testing Agent (Test Coverage Issues)
**When**: Code review identifies missing tests
**Trigger**: "Test coverage gaps found"
**Information Passed**:
- What's not tested
- Why it's important
- Suggested test cases

**Example**:
```
REVIEWER AGENT → TESTING AGENT

Issue Category: Missing Test Coverage

Code Gap:
- ChatViewModel.onEvent(ChatUiEvent.SendMessage(...))
  Only tests happy path, no error cases

Missing Tests:
□ sendMessage_networkError_updatesErrorState
□ sendMessage_emptyMessage_showsSnackbar
□ sendMessage_saveDraftBefore releasingLoading
□ sendMessage_clearDraftOnSuccess

Impact:
- Error handling untested
- Edge cases not verified
- Potential bugs in production

Suggested Test Cases:
1. sendMessage with empty text
   → Should show snackbar, not call usecase

2. sendMessage with network error
   → Should show error state and snackbar

3. Draft persistence
   → Should be in SavedStateHandle before and after send

Acceptance Criteria:
✓ All event paths tested
✓ Error cases covered
✓ Edge cases verified
```

---

## Handoff Best Practices

### ✅ DO: Clear Handoff Messages
```
AGENT_A → AGENT_B

Task: [Clear action title]

Context:
[What this is part of, why it matters]

Requirements:
□ Specific requirement 1
□ Specific requirement 2
□ Specific requirement 3

Files Affected:
- [File path] ([What changes])

Acceptance Criteria:
✓ Verifiable criteria 1
✓ Verifiable criteria 2
```

### ❌ DON'T: Vague Handoffs
```
AGENT_A → AGENT_B

"Fix the backend"
(Unclear, no context, no acceptance criteria)
```

### ✅ DO: Include File References
```
Implementation: Read presentation/viewmodel/ChatViewModel.kt (pattern)
Test Pattern: Reference android-testing/SKILL.md section "ViewModel Unit Testing"
Architecture: Follow clean-architecture/SKILL.md layer separation
```

### ✅ DO: Provide Acceptance Criteria
```
Acceptance Criteria:
✓ ViewModel correctly transitions through Loading → Success/Error
✓ All error cases return Result.Failure
✓ SavedStateHandle survives configuration change
✓ No memory leaks (verified with profiler)
```

---

## Common Handoff Chains

### Feature Implementation Chain (Typical Flow)

```
PLANNER
├─ Creates: specs/PHASE_X_SPEC.md
│
├→ BACKEND AGENT
│  ├─ Creates: domain/usecase/*, data/repository/*, viewmodel/*
│  ├→ TESTING AGENT (ViewModel tests)
│  └→ UI AGENT (hand off with state contract)
│
├→ UI AGENT
│  ├─ Creates: ui/XScreen.kt, ui/components/*
│  ├→ PREVIEW AGENT (preview coverage)
│  └→ TESTING AGENT (UI tests)
│
├→ BUILD AGENT
│  ├─ Adds: Required dependencies
│  └→ TESTING AGENT (test setup verification)
│
└→ REVIEWER AGENT
   └─ Reviews: All files, checks acceptance criteria
```

### Bug Fix Chain (Shorter Flow)

```
USER: "SendMessage not working"

REVIEWER AGENT: Identifies root cause
│
├→ BACKEND AGENT: Fix SendMessageUseCase logic
│  └→ TESTING AGENT: Verify fix with test
│
└→ REVIEWER AGENT: Final verification
```

### Dependency Addition Chain

```
USER: "Need encryption for preferences"

PLANNER: Creates plan

BUILD AGENT: Add androidx.security:security-crypto
│
├→ BACKEND AGENT: Use in PreferencesRepository
│  └→ TESTING AGENT: Add encryption tests
│
└→ REVIEWER AGENT: Verify implementation
```

---

## Cross-Agent Communication Template

Use this format for clearest handoffs:

```markdown
## Handoff: [Agent A] → [Agent B]

**Task**: [Action to complete]

**Context**: [Why this is needed, where it fits in larger feature]

**Dependency On**: [What must exist before this is started]

**Files to Create**:
- [ ] path/to/file1.kt ([purpose])
- [ ] path/to/file2.kt ([purpose])

**Files to Modify**:
- [ ] path/to/file3.kt ([what changes])
- [ ] path/to/file4.kt ([what changes])

**Interfaces/Contracts to Implement**:
- [ ] InterfaceA with methods: x(), y(), z()
- [ ] InterfaceB with properties: a, b, c

**Key Constraints**:
- [ ] Constraint 1
- [ ] Constraint 2

**Related Skill References**:
- [Skill Name](path/to/skill.md) - Section on [topic]

**Reference Existing Patterns**:
- See ChatViewModel.kt for ViewModel structure
- See SendMessageUseCase.kt for UseCase pattern

**Acceptance Criteria** (How we know this is done):
- [ ] Criterion 1 (measurable/verifiable)
- [ ] Criterion 2 (measurable/verifiable)

**Blocked By**: [Any blockers preventing start]

**Next Handoff**: [Which agent gets this after completion]
```

---

**End of Handoff Matrix**
