package com.novachat.feature.ai.data.local

import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.MessageStatus
import java.time.Instant

object RoomMessageMapper {

    fun toRoomEntity(conversationId: String, message: Message): MessageRoomEntity =
        MessageRoomEntity(
            message_id = message.id.value,
            conversation_id = conversationId,
            request_id = message.requestId,
            role = when (message.sender) {
                MessageSender.USER -> "USER"
                MessageSender.ASSISTANT -> "ASSISTANT"
            },
            content = message.content,
            status = when (message.status) {
                MessageStatus.Sent -> "SENT"
                MessageStatus.Queued -> "QUEUED"
                MessageStatus.Processing -> "PROCESSING"
                MessageStatus.Completed -> "COMPLETED"
                MessageStatus.Deferred -> "DEFERRED"
                is MessageStatus.Failed -> "FAILED"
            },
            error_code = message.errorCode ?: (message.status as? MessageStatus.Failed)?.errorCode,
            created_at_ms = message.timestamp.toEpochMilli(),
            updated_at_ms = message.timestamp.toEpochMilli(),
            token_in = message.tokenIn,
            token_out = message.tokenOut
        )

    fun toDomain(entity: MessageRoomEntity): Message {
        val sender = when (entity.role) {
            "USER" -> MessageSender.USER
            "ASSISTANT" -> MessageSender.ASSISTANT
            else -> MessageSender.ASSISTANT
        }
        val status = when (entity.status) {
            "SENT" -> MessageStatus.Sent
            "QUEUED" -> MessageStatus.Queued
            "PROCESSING" -> MessageStatus.Processing
            "COMPLETED" -> MessageStatus.Completed
            "DEFERRED" -> MessageStatus.Deferred
            "FAILED" -> MessageStatus.Failed(
                error = Exception(entity.error_code ?: "Unknown error"),
                isRetryable = true,
                errorCode = entity.error_code
            )
            else -> MessageStatus.Sent
        }
        return Message(
            id = MessageId.from(entity.message_id),
            content = entity.content,
            sender = sender,
            timestamp = Instant.ofEpochMilli(entity.created_at_ms),
            status = status,
            requestId = entity.request_id,
            errorCode = entity.error_code,
            tokenIn = entity.token_in,
            tokenOut = entity.token_out
        )
    }

}
