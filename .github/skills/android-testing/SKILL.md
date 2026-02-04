---
name: Android Testing Best Practices
description: Reusable testing patterns and utilities for Android development
category: testing
applies_to:
  - "**/*Test.kt"
  - "**/*Tests.kt"
---

# Android Testing Best Practices Skill

This skill provides reusable testing patterns and best practices for Android development.

## Unit Testing ViewModels

### Setup with Coroutines

```kotlin
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

// Usage in test
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    // Tests...
}
```

### Testing StateFlow

```kotlin
@Test
fun `test state updates`() = runTest {
    // Arrange
    val repository = mockk<MyRepository>()
    coEvery { repository.getData() } returns flowOf(expectedData)
    val viewModel = MyViewModel(repository)
    
    // Collect states
    val states = mutableListOf<UiState>()
    val job = launch {
        viewModel.uiState.collect { states.add(it) }
    }
    
    // Act
    viewModel.loadData()
    
    // Assert
    assertThat(states).containsExactly(
        UiState.Loading,
        UiState.Success(expectedData)
    )
    
    job.cancel()
}
```

## Instrumentation Testing with Espresso

### Page Object Pattern

```kotlin
class LoginScreen {
    fun enterUsername(username: String) {
        onView(withId(R.id.usernameInput))
            .perform(typeText(username), closeSoftKeyboard())
    }
    
    fun enterPassword(password: String) {
        onView(withId(R.id.passwordInput))
            .perform(typeText(password), closeSoftKeyboard())
    }
    
    fun clickLogin() {
        onView(withId(R.id.loginButton))
            .perform(click())
    }
    
    fun verifyErrorMessage(message: String) {
        onView(withText(message))
            .check(matches(isDisplayed()))
    }
}

// Usage in test
@Test
fun login_withInvalidCredentials_showsError() {
    val loginScreen = LoginScreen()
    
    loginScreen.enterUsername("invalid")
    loginScreen.enterPassword("wrong")
    loginScreen.clickLogin()
    loginScreen.verifyErrorMessage("Invalid credentials")
}
```

### Testing RecyclerView

```kotlin
fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val viewHolder = view.findViewHolderForAdapterPosition(position)
                ?: return false
            return itemMatcher.matches(viewHolder.itemView)
        }
    }
}

// Usage
@Test
fun recyclerView_displaysCorrectData() {
    onView(withId(R.id.recyclerView))
        .check(matches(atPosition(0, hasDescendant(withText("Item 1")))))
}
```

## Mocking Best Practices

### MockK Setup

```kotlin
@Before
fun setup() {
    // Mock with relaxed mode for simple cases
    repository = mockk(relaxed = true)
    
    // Mock with specific behavior
    coEvery { repository.fetchData() } returns expectedData
    
    // Verify interactions
    coVerify { repository.fetchData() }
    
    // Verify order
    coVerifyOrder {
        repository.fetchData()
        repository.saveData(any())
    }
}
```

### Fakes vs Mocks

```kotlin
// Fake: Real implementation for testing
class FakeRepository : MyRepository {
    private val data = mutableListOf<Item>()
    
    override suspend fun getItems(): List<Item> = data
    override suspend fun addItem(item: Item) {
        data.add(item)
    }
}

// Use fake when you need stateful behavior across multiple calls
```

## Test Organization

### AAA Pattern

```kotlin
@Test
fun `descriptive test name using backticks`() {
    // Arrange - Set up test data and mocks
    val input = "test"
    val expected = "EXPECTED"
    
    // Act - Execute the code under test
    val result = systemUnderTest.process(input)
    
    // Assert - Verify the results
    assertThat(result).isEqualTo(expected)
}
```

### Parameterized Tests

```kotlin
@RunWith(Parameterized::class)
class ValidationTest(
    private val input: String,
    private val expectedResult: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf("valid@email.com", true),
            arrayOf("invalid", false),
            arrayOf("", false)
        )
    }
    
    @Test
    fun testEmailValidation() {
        assertThat(validator.isValid(input)).isEqualTo(expectedResult)
    }
}
```

## Common Testing Utilities

### InstantTaskExecutorRule for LiveData

```kotlin
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()

// Allows LiveData to post values immediately in tests
```

### Waiting for Async Operations in Espresso

```kotlin
fun waitForView(viewMatcher: Matcher<View>, timeout: Long = 5000): ViewInteraction {
    val startTime = System.currentTimeMillis()
    val endTime = startTime + timeout
    
    do {
        try {
            onView(viewMatcher).check(matches(isDisplayed()))
            return onView(viewMatcher)
        } catch (e: Exception) {
            Thread.sleep(100)
        }
    } while (System.currentTimeMillis() < endTime)
    
    throw AssertionError("View not found within timeout")
}
```

## Test Coverage Tips

1. **Test happy paths first**: Normal user flows
2. **Test error cases**: Network failures, validation errors
3. **Test edge cases**: Empty lists, null values, boundary conditions
4. **Test state changes**: UI state transitions
5. **Don't test framework code**: Don't test Android SDK or libraries
6. **Keep tests fast**: Mock slow dependencies
7. **Make tests deterministic**: No random values or timing dependencies
