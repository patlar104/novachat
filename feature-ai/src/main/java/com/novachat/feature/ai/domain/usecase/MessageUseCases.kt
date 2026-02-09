package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.MessageStatus
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Use case for sending a user message and getting an AI response.
 *
 * This use case encapsulates the complete flow of:
 * 1. Validating and storing the user's message
 * 2. Creating a placeholder for the AI response
 * 3. Generating the AI response
 * 4. Updating the response message with the result
 *
 * It follows the Single Responsibility Principle by focusing solely on
 * the "send message and get response" business logic.
 *
 * @property messageRepository Repository for message persistence
 * @property aiRepository Repository for AI interactions
 * @property preferencesRepository Repository for configuration
 *
 * @since 1.0.0
 */
class SendMessageUseCase(
    private val messageRepository: MessageRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Executes the send message flow.
     *
     * This is a suspending function that performs the following steps:
     * 1. Validates the input message
     * 2. Creates and stores a user message
     * 3. Creates a placeholder AI message with Processing status
     * 4. Retrieves current AI configuration
     * 5. Generates AI response
     * 6. Updates AI message with response or error
     *
     * @param messageText The text content of the user's message
     * @return Result.success with the AI response message, Result.failure if operation failed
     */
    suspend operator fun invoke(messageText: String): Result<Message> {
        // Validate input
        if (messageText.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Message text cannot be blank")
            )
        }

        // Create and store user message
        val userMessage = Message(
            content = messageText,
            sender = MessageSender.USER
        )

        val userMessageResult = messageRepository.addMessage(userMessage)
        if (userMessageResult.isFailure) {
            return Result.failure(
                userMessageResult.exceptionOrNull()
                    ?: Exception("Failed to store user message")
            )
        }

        // Create placeholder AI message
        val aiMessage = Message(
            content = "",
            sender = MessageSender.ASSISTANT,
            status = MessageStatus.Processing
        )

        val aiMessageResult = messageRepository.addMessage(aiMessage)
        if (aiMessageResult.isFailure) {
            return Result.failure(
                aiMessageResult.exceptionOrNull()
                    ?: Exception("Failed to create AI message placeholder")
            )
        }

        // Get current AI configuration
        val configuration = try {
            preferencesRepository.observeAiConfiguration().first()
        } catch (e: Exception) {
            // Update AI message with error status
            val failedMessage = aiMessage.withStatus(
                MessageStatus.Failed(
                    error = e,
                    isRetryable = true
                )
            )
            messageRepository.updateMessage(failedMessage)
            return Result.failure(e)
        }

        // Validate configuration
        val validationResult = configuration.validate()
        if (validationResult.isFailure) {
            val error = validationResult.exceptionOrNull()
                ?: Exception("Invalid AI configuration")
            val failedMessage = aiMessage.copy(
                content = "Configuration error: ${error.message}",
                status = MessageStatus.Failed(
                    error = error,
                    isRetryable = false
                )
            )
            messageRepository.updateMessage(failedMessage)
            return Result.failure(error)
        }

        // Generate AI response
        val responseResult = aiRepository.generateResponse(
            message = messageText,
            configuration = configuration
        )

        // Update AI message with result
        val finalMessage = responseResult.fold(
            onSuccess = { responseText ->
                aiMessage.copy(
                    content = responseText,
                    status = MessageStatus.Sent
                )
            },
            onFailure = { error ->
                aiMessage.copy(
                    content = "Error: ${error.message}",
                    status = MessageStatus.Failed(
                        error = error,
                        isRetryable = true
                    )
                )
            }
        )

        val updateResult = messageRepository.updateMessage(finalMessage)
        if (updateResult.isFailure) {
            return Result.failure(
                updateResult.exceptionOrNull()
                    ?: Exception("Failed to update AI message")
            )
        }

        return if (responseResult.isSuccess) {
            Result.success(finalMessage)
        } else {
            Result.failure(
                responseResult.exceptionOrNull()
                    ?: Exception("AI generation failed")
            )
        }
    }
}

/**
 * Use case for observing the conversation message stream.
 *
 * This use case provides a single point of access for observing messages,
 * allowing for future enhancements like filtering, sorting, or pagination
 * without changing the consumer code.
 *
 * @property messageRepository Repository for message access
 *
 * @since 1.0.0
 */
class ObserveMessagesUseCase(
    private val messageRepository: MessageRepository
) {
    /**
     * Returns a Flow of all messages in the conversation.
     *
     * The Flow emits a new list whenever messages are added, updated, or removed.
     * Messages are guaranteed to be ordered by timestamp (oldest first).
     *
     * @return Flow emitting lists of messages
     */
    operator fun invoke() = messageRepository.observeMessages()
}

/**
 * Use case for clearing the conversation history.
 *
 * This use case handles the business logic of clearing messages,
 * including any necessary validation or side effects.
 *
 * @property messageRepository Repository for message operations
 *
 * @since 1.0.0
 */
class ClearConversationUseCase(
    private val messageRepository: MessageRepository
) {
    /**
     * Clears all messages from the conversation.
     *
     * This is a destructive operation with no undo. Callers should
     * confirm with the user before invoking.
     *
     * @return Result.success if cleared successfully, Result.failure on error
     */
    suspend operator fun invoke(): Result<Unit> {
        return messageRepository.clearAllMessages()
    }
}

/**
 * Use case for updating AI configuration.
 *
 * This use case handles configuration updates with validation,
 * ensuring that invalid configurations are never persisted.
 *
 * @property preferencesRepository Repository for preferences
 * @property aiRepository Repository for AI service checks
 *
 * @since 1.0.0
 */
class UpdateAiConfigurationUseCase(
    private val preferencesRepository: PreferencesRepository,
    private val aiRepository: AiRepository
) {
    /**
     * Updates the AI configuration after validation.
     *
     * Validates the configuration before persisting it. If the specified
     * mode is unavailable, the operation fails.
     *
     * @param configuration The new configuration to apply
     * @return Result.success if updated successfully, Result.failure if invalid or unavailable
     */
    suspend operator fun invoke(configuration: com.novachat.feature.ai.domain.model.AiConfiguration): Result<Unit> {
        // Validate the configuration
        val validationResult = configuration.validate()
        if (validationResult.isFailure) {
            return validationResult
        }

        // Check if the mode is available
        val modeAvailable = aiRepository.isModeAvailable(configuration.mode)
        if (!modeAvailable) {
            return Result.failure(
                IllegalStateException(
                    "AI mode ${configuration.mode} is not available on this device"
                )
            )
        }

        // Persist the configuration
        return preferencesRepository.updateAiConfiguration(configuration)
    }
}

/**
 * Use case for observing AI configuration changes.
 *
 * Provides reactive access to AI configuration, enabling UI to
 * update automatically when settings change.
 *
 * @property preferencesRepository Repository for preferences
 *
 * @since 1.0.0
 */
class ObserveAiConfigurationUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Returns a Flow of AI configuration.
     *
     * The Flow emits a new configuration whenever any part changes.
     *
     * @return Flow emitting AI configuration updates
     */
    operator fun invoke() = preferencesRepository.observeAiConfiguration()
}

/**
 * Use case for retrying a failed message.
 *
 * This use case re-attempts to generate a response for a failed message,
 * updating the message status accordingly.
 *
 * @property messageRepository Repository for message operations
 * @property aiRepository Repository for AI interactions
 * @property preferencesRepository Repository for configuration
 *
 * @since 1.0.0
 */
class RetryMessageUseCase(
    private val messageRepository: MessageRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository
) {
    /**
     * Retries generating a response for a failed message.
     *
     * The message must have a Failed status with isRetryable = true.
     * This reuses the original message content to generate a new response.
     *
     * @param messageId The ID of the failed message to retry
     * @return Result.success with updated message, Result.failure if retry failed or not allowed
     */
    suspend operator fun invoke(messageId: com.novachat.feature.ai.domain.model.MessageId): Result<Message> {
        // Retrieve the message
        val message = messageRepository.getMessage(messageId)
            ?: return Result.failure(
                IllegalArgumentException("Message not found: $messageId")
            )

        // Verify it's a failed AI message that's retryable
        if (message.sender != MessageSender.ASSISTANT) {
            return Result.failure(
                IllegalStateException("Can only retry AI messages")
            )
        }

        val status = message.status
        if (status !is MessageStatus.Failed || !status.isRetryable) {
            return Result.failure(
                IllegalStateException("Message is not in a retryable failed state")
            )
        }

        // Update status to Processing
        val processingMessage = message.withStatus(MessageStatus.Processing)
        messageRepository.updateMessage(processingMessage)

        // Get configuration and generate new response
        val configuration = try {
            preferencesRepository.observeAiConfiguration().first()
        } catch (e: Exception) {
            val failedMessage = message.withStatus(
                MessageStatus.Failed(error = e, isRetryable = true)
            )
            messageRepository.updateMessage(failedMessage)
            return Result.failure(e)
        }

        // Find the user message that preceded this AI message
        val messages = messageRepository.observeMessages().first()
        val messageIndex = messages.indexOfFirst { it.id == messageId }
        val userMessage = if (messageIndex > 0) {
            messages.getOrNull(messageIndex - 1)
        } else null

        if (userMessage == null || userMessage.sender != MessageSender.USER) {
            val error = Exception("Could not find original user message for retry")
            val failedMessage = message.withStatus(
                MessageStatus.Failed(error = error, isRetryable = false)
            )
            messageRepository.updateMessage(failedMessage)
            return Result.failure(error)
        }

        // Generate response
        val responseResult = aiRepository.generateResponse(
            message = userMessage.content,
            configuration = configuration
        )

        // Update message with result
        val finalMessage = responseResult.fold(
            onSuccess = { responseText ->
                message.copy(
                    content = responseText,
                    status = MessageStatus.Sent
                )
            },
            onFailure = { error ->
                message.copy(
                    content = "Error: ${error.message}",
                    status = MessageStatus.Failed(
                        error = error,
                        isRetryable = true
                    )
                )
            }
        )

        messageRepository.updateMessage(finalMessage)

        return if (responseResult.isSuccess) {
            Result.success(finalMessage)
        } else {
            Result.failure(
                responseResult.exceptionOrNull() ?: Exception("Retry failed")
            )
        }
    }
}

/**
 * Use case for observing theme preferences.
 *
 * Provides reactive access to theme settings (dark mode, dynamic color).
 *
 * @property preferencesRepository Repository for preferences
 *
 * @since 1.0.0
 */
class ObserveThemePreferencesUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke() = preferencesRepository.observeThemePreferences()
}

/**
 * Use case for updating theme preferences.
 *
 * @property preferencesRepository Repository for preferences
 *
 * @since 1.0.0
 */
class UpdateThemePreferencesUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(preferences: ThemePreferences): Result<Unit> =
        preferencesRepository.updateThemePreferences(preferences)
}
