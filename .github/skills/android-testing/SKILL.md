---
name: android-testing
description: Complete testing patterns and examples for NovaChat (NO placeholders)
category: testing
applies_to:
  - "**/*Test.kt"
  - "**/*Tests.kt"
protocol_compliance: true
note: All examples are COMPLETE and runnable - following DEVELOPMENT_PROTOCOL.md zero-elision policy
---

# Android Testing Best Practices Skill

This skill provides **COMPLETE** testing patterns for Android development. All code examples are fully implemented with no placeholders.

> **PROTOCOL**: All test examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - Complete test functions (no `// ... test code` placeholders)
> - All imports explicitly shown
> - Complete MockK setup
> - All assertions included
> - AAA pattern (Arrange, Act, Assert)

## Multi-Agent Coordination

### When the Testing Agent Should Use Tools

**Use tools immediately for:**
- Reading test files or implementation files → `read_file`
- Creating new test files → `create_file`
- Modifying existing test files → `apply_patch`
- Running tests to validate → `run_in_terminal`
- Searching for test patterns in codebase → `grep_search` or `semantic_search`

**Do NOT describe; DO implement:**
- Don't say "you should write tests"; write them using `create_file`
- Don't say "you should update the test file"; update it using `apply_patch`
- Don't say "you should run tests"; run them using `run_in_terminal`

### When to Hand Off to Other Agents

**Hand off to Backend Agent if:**
- Production code logic needs fixing (implementation issue, not test issue)
- Use case or repository implementations are incomplete
- State management logic needs correction
- Error handling patterns need implementation
- → **Action**: Report findings, suggest fixes needed in production code

**Hand off to UI Agent if:**
- Composable @Composable functions need creation/modification
- UI state transitions need implementation
- UI event handling needs fixes
- Material Design components need adjustment
- → **Action**: Report findings, suggest UI fixes needed

**Hand off to Build Agent if:**
- Test dependencies are missing or incorrect versions
- Gradle test configuration issues arise
- Test runner setup problems occur
- → **Action**: Report specific dependency/build issues

### Testing Task Assessment

**Determine scope before acting:**

1. **Is this a testing task?**
   - Creating/modifying test files → YES, use Testing Agent tools
   - Fixing production code → NO, hand off to appropriate agent
   - Setting up test infrastructure → Maybe, see below

2. **Do I have all context needed?**
   - Can I see the code being tested? → Read it first with `read_file`
   - Do I understand the expected behavior? → Check implementation or ask user
   - Do I know what's being tested? → Search codebase with `grep_search`

3. **Is this within Testing Agent scope?**
   - Writing unit tests → YES ✓
   - Writing integration tests → YES ✓
   - Writing Compose UI tests → YES ✓
   - Fixing production code logic → NO, hand off
   - Creating ViewModels → NO, hand off to Backend Agent
   - Creating Composables → NO, hand off to UI Agent

## Unit Testing ViewModels

### Setup with Coroutines

Rules:

- Use `MainDispatcherRule` with `UnconfinedTestDispatcher` in ViewModel tests.
- Wrap coroutine tests in `runTest`.
- Call `advanceUntilIdle()` to flush state emissions.

### Testing StateFlow Emissions

Rules:

- Collect StateFlow in a `launch` and store emissions.
- Call `advanceUntilIdle()` after actions.
- Cancel collection job after assertions.
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import io.mockk.coEvery

## Compose UI Testing

### Complete ComposeTestRule Setup

Rules:

- Use `@get:Rule val composeTestRule = createComposeRule()`.
- Follow Arrange/Act/Assert with `setContent`, actions, then assertions.
- Call `waitForIdle()` after interactions.
    @Test
    fun `chatScreen displays welcome message when messages are empty`() {
    @Test
    fun `chatScreen sends message when send button is clicked`() {
        // Arrange
        val testMessage = "Hello AI"
        coEvery { repository.sendMessage(any(), any()) } returns Result.success("AI Response")

        composeTestRule.setContent {
            ChatScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Act
        composeTestRule
            .onNodeWithContentDescription("Message input")
            .performTextInput(testMessage)

        composeTestRule
            .onNodeWithContentDescription("Send message")
            .performClick()

        composeTestRule.waitForIdle()

        // Assert
        coVerify { repository.sendMessage(testMessage, any()) }

        composeTestRule
            .onNodeWithText(testMessage)
            .assertIsDisplayed()
    }

    @Test
    fun `chatScreen displays error when message sending fails`() {
        // Arrange
        val errorMessage = "Network error"
        coEvery { repository.sendMessage(any(), any()) } returns
            Result.failure(Exception(errorMessage))

        composeTestRule.setContent {
            ChatScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }

        // Act
        composeTestRule
            .onNodeWithContentDescription("Message input")
            .performTextInput("Test")


## MockK Best Practices

### Complete MockK Setup Example

Rules:

- Use `mockk()`/`mockk(relaxed = true)` for mocks.
- Use `every`/`coEvery` for stubbing, `verify`/`coVerify` for expectations.
- Use `slot<T>()` with `capture()` for argument checks.

## Testing Best Practices Summary

### AAA Pattern (Arrange, Act, Assert)

**ALWAYS structure tests this way:**

Rules:

- Structure tests with explicit Arrange/Act/Assert sections.
- Use descriptive test names (backticks for readability).

### Test Naming Convention

Use backticks for descriptive names:
- `methodName_condition_expectedResult`
- Or natural language: `when user clicks send button then message is sent`

### Common Assertions (Truth library)

Rules:

- Use Truth assertions (`assertThat`) for all checks.
- Prefer type-specific assertions (`isEqualTo`, `hasSize`, `contains`).
- Use `assertThrows<T>` for exception testing.

### Fake vs Mock

**Use Fakes for complex dependencies:**

Rules:

- Use fakes for complex or stateful dependencies.
- Use mocks for simple interfaces and call verification.

## Protocol Compliance Checklist

Before submitting test code, verify:

- [ ] **Complete test functions** - No `// ... test code` placeholders
- [ ] **All imports included** - Every import explicitly listed
- [ ] **AAA pattern followed** - Clear Arrange, Act, Assert sections
- [ ] **MockK setup complete** - All `every`/`coEvery` calls shown
- [ ] **All assertions present** - Every expected outcome verified
- [ ] **Test names descriptive** - Clear what's being tested
- [ ] **Coroutine tests use runTest** - Proper coroutine test scope
- [ ] **ComposeTestRule complete** - Full Compose UI test setup shown

**Remember: DEVELOPMENT_PROTOCOL.md prohibits placeholder code in ALL files, including tests!**

---

**End of Android Testing Best Practices Skill**
