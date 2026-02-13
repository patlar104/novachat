package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.ModelParameters
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

/**
 * Unit tests for SendMessageUseCase.
 *
 * These tests verify:
 * - Message validation and storage
 * - AI response generation
 * - Error handling and retry logic
 * - Configuration validation
 *
 * Uses MockK for mocking and kotlin-coroutines-test for async testing.
 *
 * @since 1.0.0
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SendMessageUseCaseTest {

    private val mockMessageRepository = mockk<MessageRepository>()
    private val mockAiRepository = mockk<AiRepository>()
    private val mockPreferencesRepository = mockk<PreferencesRepository>()

    private fun createUseCase(): SendMessageUseCase {
        return SendMessageUseCase(
            messageRepository = mockMessageRepository,
            aiRepository = mockAiRepository,
            preferencesRepository = mockPreferencesRepository
        )
    }

    @Test
    fun invoke_with_blank_message_returns_failure() = runTest {
        // Arrange
        val useCase = createUseCase()

        // Act
        val result = useCase("")

        // Assert
        result.isFailure.shouldBe(true)
        result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
    }

    @Test
    fun invoke_with_valid_message_stores_user_message() = runTest {
        // Arrange
        val messageText = "hello ai"
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        coEvery { mockMessageRepository.addMessage(any()) } returns Result.success(Unit)
        coEvery { mockPreferencesRepository.observeAiConfiguration() } returns flowOf(configuration)
        coEvery { mockAiRepository.generateResponse(messageText, configuration) } returns Result.success("response")
        coEvery { mockMessageRepository.updateMessage(any()) } returns Result.success(Unit)

        val useCase = createUseCase()

        // Act
        val result = useCase(messageText)

        // Assert
        result.isSuccess.shouldBe(true)
        coVerify(atLeast = 2) { mockMessageRepository.addMessage(any()) }
    }

    @Test
    fun invoke_calls_ai_repository_with_configuration() = runTest {
        // Arrange
        val messageText = "test message"
        val aiResponse = "ai response"
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        coEvery { mockMessageRepository.addMessage(any()) } returns Result.success(Unit)
        coEvery { mockPreferencesRepository.observeAiConfiguration() } returns flowOf(configuration)
        coEvery { mockAiRepository.generateResponse(messageText, configuration) } returns Result.success(aiResponse)
        coEvery { mockMessageRepository.updateMessage(any()) } returns Result.success(Unit)

        val useCase = createUseCase()

        // Act
        val result = useCase(messageText)

        // Assert
        result.isSuccess.shouldBe(true)
        coVerify { mockAiRepository.generateResponse(messageText, configuration) }
    }

    @Test
    fun invoke_handles_ai_error_gracefully() = runTest {
        // Arrange
        val messageText = "error message"
        val aiError = Exception("AI service unavailable")
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        coEvery { mockMessageRepository.addMessage(any()) } returns Result.success(Unit)
        coEvery { mockPreferencesRepository.observeAiConfiguration() } returns flowOf(configuration)
        coEvery { mockAiRepository.generateResponse(messageText, configuration) } returns Result.failure(aiError)
        coEvery { mockMessageRepository.updateMessage(any()) } returns Result.success(Unit)

        val useCase = createUseCase()

        // Act
        val result = useCase(messageText)

        // Assert
        result.isFailure.shouldBe(true)
        coVerify { mockMessageRepository.updateMessage(any()) }
    }

    @Test
    fun invoke_succeeds_with_online_mode_without_api_key() = runTest {
        // Arrange: Firebase AI handles auth; API key is optional for online mode
        val messageText = "message"
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        coEvery { mockMessageRepository.addMessage(any()) } returns Result.success(Unit)
        coEvery { mockPreferencesRepository.observeAiConfiguration() } returns flowOf(configuration)
        coEvery { mockAiRepository.generateResponse(messageText, configuration) } returns Result.success("response")
        coEvery { mockMessageRepository.updateMessage(any()) } returns Result.success(Unit)

        val useCase = createUseCase()

        // Act
        val result = useCase(messageText)

        // Assert: Succeeds with Firebase (API key not required)
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun invoke_succeeds_with_offline_mode() = runTest {
        // Arrange
        val messageText = "offline message"
        val aiResponse = "offline response"
        val configuration = AiConfiguration(
            mode = AiMode.OFFLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        coEvery { mockMessageRepository.addMessage(any()) } returns Result.success(Unit)
        coEvery { mockPreferencesRepository.observeAiConfiguration() } returns flowOf(configuration)
        coEvery { mockAiRepository.generateResponse(messageText, configuration) } returns Result.success(aiResponse)
        coEvery { mockMessageRepository.updateMessage(any()) } returns Result.success(Unit)

        val useCase = createUseCase()

        // Act
        val result = useCase(messageText)

        // Assert
        result.isSuccess.shouldBe(true)
    }
}
