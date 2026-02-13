package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.MessageStatus
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(messageText: String): Result<Message> {
        if (messageText.isBlank()) {
            return Result.failure(IllegalArgumentException("Message text cannot be blank"))
        }

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

        val configuration = try {
            preferencesRepository.observeAiConfiguration().first()
        } catch (e: Exception) {
            val failedMessage = aiMessage.withStatus(
                MessageStatus.Failed(
                    error = e,
                    isRetryable = true
                )
            )
            messageRepository.updateMessage(failedMessage)
            return Result.failure(e)
        }

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

        val responseResult = aiRepository.generateResponse(
            message = messageText,
            configuration = configuration
        )

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
