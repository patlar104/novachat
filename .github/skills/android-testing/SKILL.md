---
name: Android Testing Best Practices
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

> **⚠️ PROTOCOL**: All test examples follow [DEVELOPMENT_PROTOCOL.md](../../DEVELOPMENT_PROTOCOL.md)
> - ✅ Complete test functions (no `// ... test code` placeholders)
> - ✅ All imports explicitly shown
> - ✅ Complete MockK setup
> - ✅ All assertions included
> - ✅ AAA pattern (Arrange, Act, Assert)

## Unit Testing ViewModels

### Setup with Coroutines

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

// Complete usage example in test class
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var repository: MyRepository
    private lateinit var viewModel: MyViewModel
    
    @Before
    fun setup() {
        repository = mockk()
        viewModel = MyViewModel(repository)
    }
    
    @Test
    fun `loadData updates state to Success when repository succeeds`() = runTest {
        // Arrange
        val expectedData = listOf("Item1", "Item2")
        coEvery { repository.getData() } returns expectedData
        
        // Act
        viewModel.loadData()
        
        // Assert
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(UiState.Success::class.java)
        assertThat((state as UiState.Success).data).isEqualTo(expectedData)
    }
}
```

### Testing StateFlow Emissions

```kotlin
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@Test
fun `test state transitions through multiple emissions`() = runTest {
    // Arrange
    val repository = mockk<MyRepository>()
    val expectedData = "Test Data"
    coEvery { repository.getData() } returns flowOf(expectedData)
    val viewModel = MyViewModel(repository)
    
    // Collect all state emissions
    val states = mutableListOf<UiState>()
    val collectJob = launch {
        viewModel.uiState.collect { state ->
            states.add(state)
        }
    }
    
    // Act
    viewModel.loadData()
    advanceUntilIdle() // Wait for all coroutines
    
    // Assert - verify complete state transition
    assertThat(states).hasSize(2)
    assertThat(states[0]).isEqualTo(UiState.Loading)
    assertThat(states[1]).isInstanceOf(UiState.Success::class.java)
    assertThat((states[1] as UiState.Success).data).isEqualTo(expectedData)
    
    collectJob.cancel()
}
```

## Compose UI Testing

### Complete ComposeTestRule Setup

```kotlin
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var viewModel: ChatViewModel
    private lateinit var repository: AiRepository
    
    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = ChatViewModel(repository)
    }
    
    @Test
    fun `chatScreen displays welcome message when messages are empty`() {
        // Arrange
        composeTestRule.setContent {
            ChatScreen(
                viewModel = viewModel,
                onNavigateToSettings = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Welcome to NovaChat")
            .assertIsDisplayed()
    }
    
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
        
        composeTestRule
            .onNodeWithContentDescription("Send message")
            .performClick()
        
        composeTestRule.waitForIdle()
        
        // Assert
        composeTestRule
            .onNodeWithText(errorMessage, substring = true)
            .assertIsDisplayed()
    }
}
```

## MockK Best Practices

### Complete MockK Setup Example

```kotlin
import io.mockk.mockk
import io.mockk.every
import io.mockk.coEvery
import io.mockk.verify
import io.mockk.coVerify
import io.mockk.slot
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class RepositoryTest {
    private lateinit var dataSource: DataSource
    private lateinit var repository: RepositoryImpl
    
    @Before
    fun setup() {
        dataSource = mockk()
        repository = RepositoryImpl(dataSource)
    }
    
    @Test
    fun `getData returns success when dataSource succeeds`() {
        // Arrange
        val expectedData = "Test Data"
        every { dataSource.fetchData() } returns expectedData
        
        // Act
        val result = repository.getData()
        
        // Assert
        assertThat(result).isEqualTo(expectedData)
        verify(exactly = 1) { dataSource.fetchData() }
    }
    
    @Test
    fun `saveData calls dataSource with correct parameters`() {
        // Arrange
        val dataSlot = slot<String>()
        every { dataSource.saveData(capture(dataSlot)) } returns Unit
        
        // Act
        repository.saveData("New Data")
        
        // Assert
        assertThat(dataSlot.captured).isEqualTo("New Data")
        verify { dataSource.saveData("New Data") }
    }
    
    @Test
    fun `async operation with coEvery and coVerify`() = runTest {
        // Arrange
        val expectedResult = Result.success("Success")
        coEvery { dataSource.fetchDataAsync() } returns expectedResult
        
        // Act
        val result = repository.getDataAsync()
        
        // Assert
        assertThat(result).isEqualTo(expectedResult)
        coVerify(exactly = 1) { dataSource.fetchDataAsync() }
    }
}
```

## Testing Best Practices Summary

### AAA Pattern (Arrange, Act, Assert)

**ALWAYS structure tests this way:**

```kotlin
@Test
fun `descriptive test name describing behavior`() = runTest {
    // Arrange - Set up test data and mocks
    val repository = mockk<Repository>()
    val expectedData = "Test"
    coEvery { repository.getData() } returns expectedData
    val viewModel = MyViewModel(repository)
    
    // Act - Perform the action being tested
    viewModel.loadData()
    advanceUntilIdle()
    
    // Assert - Verify the expected outcome
    val state = viewModel.uiState.value
    assertThat(state).isInstanceOf(UiState.Success::class.java)
    assertThat((state as UiState.Success).data).isEqualTo(expectedData)
}
```

### Test Naming Convention

Use backticks for descriptive names:
- `methodName_condition_expectedResult`
- Or natural language: `when user clicks send button then message is sent`

### Common Assertions (Truth library)

```kotlin
// Equality
assertThat(actual).isEqualTo(expected)
assertThat(actual).isNotEqualTo(notExpected)

// Nullability
assertThat(value).isNull()
assertThat(value).isNotNull()

// Booleans
assertThat(condition).isTrue()
assertThat(condition).isFalse()

// Collections
assertThat(list).hasSize(3)
assertThat(list).contains("item")
assertThat(list).containsExactly("item1", "item2")
assertThat(list).isEmpty()

// Types
assertThat(object).isInstanceOf(MyClass::class.java)

// Exceptions
assertThrows<IllegalArgumentException> {
    functionThatThrows()
}
```

### Fake vs Mock

**Use Fakes for complex dependencies:**

```kotlin
// Fake implementation for testing
class FakeAiRepository : AiRepository {
    private val responses = mutableMapOf<String, String>()
    var shouldFail = false
    
    fun setResponse(message: String, response: String) {
        responses[message] = response
    }
    
    override suspend fun sendMessage(
        message: String,
        useOnlineMode: Boolean
    ): Result<String> {
        delay(50) // Simulate network delay
        
        return if (shouldFail) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(responses[message] ?: "Default response")
        }
    }
}

// Usage in test
@Test
fun `test with fake repository`() = runTest {
    // Arrange
    val fakeRepository = FakeAiRepository()
    fakeRepository.setResponse("Hello", "Hi there!")
    val viewModel = ChatViewModel(fakeRepository)
    
    // Act
    viewModel.sendMessage("Hello")
    advanceUntilIdle()
    
    // Assert
    val messages = viewModel.messages.value
    assertThat(messages).hasSize(2) // User + AI message
    assertThat(messages[1].content).isEqualTo("Hi there!")
}
```

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
