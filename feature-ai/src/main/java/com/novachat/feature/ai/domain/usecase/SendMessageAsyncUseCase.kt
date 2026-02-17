package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.MessageStatus
import com.novachat.feature.ai.domain.model.SubmitRequest
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.OutboundRequestRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import com.novachat.feature.ai.domain.util.RequestIdGenerator
import com.novachat.feature.ai.data.observability.ChatObservability
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

/**
 * SPEC-1 async path: persist to Room, submit, ACK, observe completion via Firestore.
 * Use when ff_async_ack is true and MessageRepository is Room-backed.
 */
class SendMessageAsyncUseCase @Inject constructor(
    @param:Named("message_repository_room") private val messageRepository: MessageRepository,
    private val outboundRequestRepository: OutboundRequestRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository,
    private val chatObservability: ChatObservability,
    @param:Named("default_conversation_id") private val defaultConversationId: String
) {

    suspend operator fun invoke(messageText: String): Result<Message> {
        if (messageText.isBlank()) {
            return Result.failure(IllegalArgumentException("Message text cannot be blank"))
        }

        val config = try {
            preferencesRepository.observeAiConfiguration().first()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val validationResult = config.validate()
        if (validationResult.isFailure) {
            return Result.failure(
                validationResult.exceptionOrNull() ?: Exception("Invalid configuration")
            )
        }

        val userMessage = Message(
            content = messageText,
            sender = MessageSender.USER,
            status = MessageStatus.Queued
        )
        var addResult = messageRepository.addMessage(userMessage)
        if (addResult.isFailure) {
            return Result.failure(addResult.exceptionOrNull() ?: Exception("Failed to store user message"))
        }

        val aiMessage = Message(
            content = "",
            sender = MessageSender.ASSISTANT,
            status = MessageStatus.Queued
        )
        addResult = messageRepository.addMessage(aiMessage)
        if (addResult.isFailure) {
            return Result.failure(addResult.exceptionOrNull() ?: Exception("Failed to create AI placeholder"))
        }

        val requestId = RequestIdGenerator.next()
        val messageId = aiMessage.id.value
        val nowMs = System.currentTimeMillis()
        outboundRequestRepository.insert(
            requestId = requestId,
            conversationId = defaultConversationId,
            userMessageId = userMessage.id.value,
            state = "QUEUED_LOCAL",
            nextRetryAtMs = nowMs + 30_000
        )

        val submitRequest = SubmitRequest(
            requestId = requestId,
            conversationId = defaultConversationId,
            messageId = messageId,
            messageText = messageText,
            modelProfile = "standard",
            clientTsMs = nowMs,
            appInstanceId = null
        )
        val submitResult = aiRepository.submitAsync(submitRequest)
        if (submitResult.isFailure) {
            val err = submitResult.exceptionOrNull() ?: Exception("Submit failed")
            val failedMsg = aiMessage.copy(
                content = "Error: ${err.message}",
                status = MessageStatus.Failed(error = err, isRetryable = true)
            )
            messageRepository.updateMessage(failedMsg)
            return Result.failure(err)
        }

        val ack = submitResult.getOrNull()!!
        outboundRequestRepository.markAcked(requestId, ack.region, System.currentTimeMillis())
        messageRepository.updateMessage(userMessage.copy(status = MessageStatus.Sent))
        messageRepository.updateMessage(aiMessage.copy(status = MessageStatus.Queued, requestId = requestId))

        var terminalState: com.novachat.feature.ai.domain.model.RequestCompletionState? = null
        aiRepository.observeCompletion(requestId).collect { state ->
            terminalState = state
            if (state.state == "PROCESSING") {
                messageRepository.updateMessage(
                    aiMessage.copy(status = MessageStatus.Processing, requestId = requestId)
                )
            }
        }
        val final = terminalState ?: return Result.failure(Exception("No completion state"))
        chatObservability.emit("completion_received", mapOf("request_id" to requestId, "state" to final.state))
        var finalMessage = aiMessage.copy(requestId = requestId)
        if (final.state == "COMPLETED") {
            finalMessage = aiMessage.copy(
                content = final.responseText ?: "",
                status = MessageStatus.Sent,
                requestId = requestId
            )
            outboundRequestRepository.markDone(requestId, "COMPLETED", System.currentTimeMillis(), null)
        } else {
            finalMessage = aiMessage.copy(
                content = "Error: ${final.errorCode ?: "Unknown"}",
                status = MessageStatus.Failed(
                    error = Exception(final.errorCode ?: "Request failed"),
                    isRetryable = true,
                    errorCode = final.errorCode
                ),
                requestId = requestId
            )
            outboundRequestRepository.markDone(
                requestId,
                "FAILED",
                System.currentTimeMillis(),
                final.errorCode
            )
        }
        messageRepository.updateMessage(finalMessage)
        return Result.success(finalMessage)
    }
}
