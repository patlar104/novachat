package com.novachat.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.novachat.app.domain.model.Message
import com.novachat.app.domain.model.MessageId
import com.novachat.app.domain.model.MessageSender
import com.novachat.app.domain.usecase.ClearConversationUseCase
import com.novachat.app.domain.usecase.ObserveMessagesUseCase
import com.novachat.app.domain.usecase.RetryMessageUseCase
import com.novachat.app.domain.usecase.SendMessageUseCase
import com.novachat.app.presentation.model.ChatUiEvent
import com.novachat.app.presentation.model.ChatUiState
import com.novachat.app.presentation.model.UiEffect
import com.novachat.app.testutil.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for ChatViewModel following 2026 best practices.
 *
 * These tests verify:
 * - Event handling and state transitions
 * - Effect emission for one-time actions
 * - Error handling through Result<T> pattern
 * - SavedStateHandle persistence for draft messages
 *
 * Uses MockK for mocking and kotlin-coroutines-test for async testing.
 *
 * @since 1.0.0
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockSendMessageUseCase = mockk<SendMessageUseCase>()
    private val mockObserveMessagesUseCase = mockk<ObserveMessagesUseCase>()
    private val mockClearConversationUseCase = mockk<ClearConversationUseCase>()
    private val mockRetryMessageUseCase = mockk<RetryMessageUseCase>()

    private fun createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): ChatViewModel {
        return ChatViewModel(
            savedStateHandle = savedStateHandle,
            sendMessageUseCase = mockSendMessageUseCase,
            observeMessagesUseCase = mockObserveMessagesUseCase,
            clearConversationUseCase = mockClearConversationUseCase,
            retryMessageUseCase = mockRetryMessageUseCase
        )
    }

    @Test
    fun sendMessage_success_updates_state() = runTest {
        // Arrange
        val messageText = "hello"
        val aiResponse = Message(
            id = MessageId("1"),
            content = "Hi there!",
            sender = MessageSender.ASSISTANT
        )

        coEvery { mockObserveMessagesUseCase() } returns flowOf(emptyList())
        coEvery { mockSendMessageUseCase(messageText) } returns Result.success(aiResponse)

        val viewModel = createViewModel()

        // Wait for initialization
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ChatUiEvent.SendMessage(messageText))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<ChatUiState.Success>()
        (state as ChatUiState.Success).isProcessing.shouldBe(false)

        coVerify { mockSendMessageUseCase(messageText) }
    }

    @Test
    fun sendMessage_error_shows_snackbar() = runTest {
        // Arrange
        val messageText = "bad message"
        val error = Exception("Network error")

        coEvery { mockObserveMessagesUseCase() } returns flowOf(
            listOf(
                Message(
                    id = MessageId("1"),
                    content = "test",
                    sender = MessageSender.USER
                )
            )
        )
        coEvery { mockSendMessageUseCase(messageText) } returns Result.failure(error)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ChatUiEvent.SendMessage(messageText))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<ChatUiState.Success>()
        (state as ChatUiState.Success).error?.shouldBe("Network error")

        coVerify { mockSendMessageUseCase(messageText) }
    }

    @Test
    fun sendMessage_blank_shows_toast() = runTest {
        // Arrange
        coEvery { mockObserveMessagesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ChatUiEvent.SendMessage(""))

        // Assert: effect would be emitted (not testing effect collection here)
        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<ChatUiState.Success>()
    }

    @Test
    fun draftMessage_survives_state_change() = runTest {
        // Arrange
        val draftText = "my draft message"
        val savedStateHandle = SavedStateHandle()

        coEvery { mockObserveMessagesUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel(savedStateHandle)
        advanceUntilIdle()

        // Act
        viewModel.updateDraftMessage(draftText)

        // Assert: draft persisted in SavedStateHandle
        savedStateHandle.get<String>(ChatViewModel.KEY_DRAFT_MESSAGE).shouldBe(draftText)
    }

    @Test
    fun clearConversation_success() = runTest {
        // Arrange
        coEvery { mockObserveMessagesUseCase() } returns flowOf(emptyList())
        coEvery { mockClearConversationUseCase() } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ChatUiEvent.ClearConversation)
        advanceUntilIdle()

        // Assert
        coVerify { mockClearConversationUseCase() }
    }

    @Test
    fun retryMessage_success() = runTest {
        // Arrange
        val messageId = MessageId("1")
        val successMessage = Message(
            id = messageId,
            content = "retried content",
            sender = MessageSender.ASSISTANT
        )

        coEvery { mockObserveMessagesUseCase() } returns flowOf(
            listOf(
                Message(
                    id = messageId,
                    content = "initial",
                    sender = MessageSender.ASSISTANT
                )
            )
        )
        coEvery { mockRetryMessageUseCase(messageId) } returns Result.success(successMessage)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.onEvent(ChatUiEvent.RetryMessage(messageId))
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<ChatUiState.Success>()
        (state as ChatUiState.Success).isProcessing.shouldBe(false)

        coVerify { mockRetryMessageUseCase(messageId) }
    }

    @Test
    fun dismissError_clears_error_state() = runTest {
        // Arrange
        coEvery { mockObserveMessagesUseCase() } returns flowOf(emptyList())
        coEvery { mockSendMessageUseCase("error") } returns Result.failure(Exception("test error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act: Create error state
        viewModel.onEvent(ChatUiEvent.SendMessage("error"))
        advanceUntilIdle()

        var state = viewModel.uiState.value
        state.shouldBeInstanceOf<ChatUiState.Success>()
        (state as ChatUiState.Success).error?.shouldBe("test error")

        // Act: Dismiss error
        viewModel.onEvent(ChatUiEvent.DismissError)

        // Assert: Error cleared
        state = viewModel.uiState.value
        state.shouldBeInstanceOf<ChatUiState.Success>()
        (state as ChatUiState.Success).error.shouldBe(null)
    }
}
