package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.MessageStatus
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class RetryMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(messageId: MessageId): Result<Message> {
        val message = messageRepository.getMessage(messageId)
            ?: return Result.failure(
                IllegalArgumentException("Message not found: $messageId")
            )

        if (message.sender != MessageSender.ASSISTANT) {
            return Result.failure(IllegalStateException("Can only retry AI messages"))
        }

        val status = message.status
        if (status !is MessageStatus.Failed || !status.isRetryable) {
            return Result.failure(
                IllegalStateException("Message is not in a retryable failed state")
            )
        }

        val processingMessage = message.withStatus(MessageStatus.Processing)
        messageRepository.updateMessage(processingMessage)

        val configuration = try {
            preferencesRepository.observeAiConfiguration().first()
        } catch (e: Exception) {
            val failedMessage = message.withStatus(
                MessageStatus.Failed(error = e, isRetryable = true)
            )
            messageRepository.updateMessage(failedMessage)
            return Result.failure(e)
        }

        val messages = messageRepository.observeMessages().first()
        val messageIndex = messages.indexOfFirst { it.id == messageId }
        val userMessage = if (messageIndex > 0) {
            messages.getOrNull(messageIndex - 1)
        } else {
            null
        }

        if (userMessage == null || userMessage.sender != MessageSender.USER) {
            val error = Exception("Could not find original user message for retry")
            val failedMessage = message.withStatus(
                MessageStatus.Failed(error = error, isRetryable = false)
            )
            messageRepository.updateMessage(failedMessage)
            return Result.failure(error)
        }

        val responseResult = aiRepository.generateResponse(
            message = userMessage.content,
            configuration = configuration
        )

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
