package com.novachat.feature.ai.data.mapper

import com.novachat.feature.ai.data.model.AiConfigurationEntity
import com.novachat.feature.ai.data.model.MessageEntity
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.MessageStatus
import com.novachat.feature.ai.domain.model.ModelParameters
import java.time.Instant

object MessageMapper {
    fun toEntity(message: Message): MessageEntity {
        val senderType = when (message.sender) {
            MessageSender.USER -> MessageEntity.SENDER_USER
            MessageSender.ASSISTANT -> MessageEntity.SENDER_ASSISTANT
        }

        val (statusType, errorMessage, isRetryable) = when (val status = message.status) {
            MessageStatus.Sent -> Triple(MessageEntity.STATUS_SENT, null, true)
            MessageStatus.Processing -> Triple(MessageEntity.STATUS_PROCESSING, null, true)
            is MessageStatus.Failed -> Triple(
                MessageEntity.STATUS_FAILED,
                status.error.message ?: "Unknown error",
                status.isRetryable
            )
        }

        return MessageEntity(
            id = message.id.value,
            content = message.content,
            senderType = senderType,
            timestampMillis = message.timestamp.toEpochMilli(),
            statusType = statusType,
            errorMessage = errorMessage,
            isRetryable = isRetryable
        )
    }

    fun toDomain(entity: MessageEntity): Message {
        require(entity.isValid()) {
            "Cannot convert invalid MessageEntity to domain Message"
        }

        val messageId = MessageId.from(entity.id)

        val sender = when (entity.senderType) {
            MessageEntity.SENDER_USER -> MessageSender.USER
            MessageEntity.SENDER_ASSISTANT -> MessageSender.ASSISTANT
            else -> throw IllegalArgumentException("Unknown sender type: ${entity.senderType}")
        }

        val timestamp = Instant.ofEpochMilli(entity.timestampMillis)

        val status = when (entity.statusType) {
            MessageEntity.STATUS_SENT -> MessageStatus.Sent
            MessageEntity.STATUS_PROCESSING -> MessageStatus.Processing
            MessageEntity.STATUS_FAILED -> MessageStatus.Failed(
                error = Exception(entity.errorMessage ?: "Unknown error"),
                isRetryable = entity.isRetryable
            )
            else -> throw IllegalArgumentException("Unknown status type: ${entity.statusType}")
        }

        return Message(
            id = messageId,
            content = entity.content,
            sender = sender,
            timestamp = timestamp,
            status = status
        )
    }

    fun toEntityList(messages: List<Message>): List<MessageEntity> {
        return messages.map { toEntity(it) }
    }

    fun toDomainList(entities: List<MessageEntity>): List<Message> {
        return entities.mapNotNull { entity ->
            try {
                toDomain(entity)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}

object AiConfigurationMapper {
    fun toEntity(configuration: AiConfiguration): AiConfigurationEntity {
        val aiModeValue = when (configuration.mode) {
            AiMode.ONLINE -> AiConfigurationEntity.MODE_ONLINE
            AiMode.OFFLINE -> AiConfigurationEntity.MODE_OFFLINE
        }

        return AiConfigurationEntity(
            aiModeValue = aiModeValue,
            temperature = configuration.modelParameters.temperature,
            topK = configuration.modelParameters.topK,
            topP = configuration.modelParameters.topP,
            maxOutputTokens = configuration.modelParameters.maxOutputTokens
        )
    }

    fun toDomain(entity: AiConfigurationEntity): AiConfiguration {
        require(entity.isValid()) {
            "Cannot convert invalid AiConfigurationEntity to domain AiConfiguration"
        }

        val mode = when (entity.aiModeValue) {
            AiConfigurationEntity.MODE_ONLINE -> AiMode.ONLINE
            AiConfigurationEntity.MODE_OFFLINE -> AiMode.OFFLINE
            else -> throw IllegalArgumentException("Unknown AI mode: ${entity.aiModeValue}")
        }

        val modelParameters = ModelParameters(
            temperature = entity.temperature,
            topK = entity.topK,
            topP = entity.topP,
            maxOutputTokens = entity.maxOutputTokens
        )

        return AiConfiguration(
            mode = mode,
            modelParameters = modelParameters
        )
    }
}
