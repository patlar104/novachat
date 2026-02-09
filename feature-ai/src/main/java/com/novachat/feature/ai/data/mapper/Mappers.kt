package com.novachat.feature.ai.data.mapper

import com.novachat.feature.ai.data.model.AiConfigurationEntity
import com.novachat.feature.ai.data.model.MessageEntity
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.ApiKey
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.model.MessageSender
import com.novachat.feature.ai.domain.model.MessageStatus
import com.novachat.feature.ai.domain.model.ModelParameters
import java.time.Instant

/**
 * Mapper for converting between domain Message and data MessageEntity.
 *
 * This class handles the bidirectional mapping between the domain layer's
 * Message model and the data layer's MessageEntity model.
 *
 * The separation ensures that changes to persistence format don't affect
 * business logic and vice versa, following Clean Architecture principles.
 *
 * @since 1.0.0
 */
object MessageMapper {
    /**
     * Converts a domain Message to a data MessageEntity.
     *
     * This mapping flattens the rich domain model into a simple structure
     * suitable for persistence. Sealed interfaces are converted to string
     * constants, and value classes are unwrapped to their underlying values.
     *
     * @param message The domain message to convert
     * @return The corresponding data entity
     */
    fun toEntity(message: Message): MessageEntity {
        val senderType = when (message.sender) {
            MessageSender.USER -> MessageEntity.SENDER_USER
            MessageSender.ASSISTANT -> MessageEntity.SENDER_ASSISTANT
        }

        val (statusType, errorMessage, isRetryable) = when (val status = message.status) {
            MessageStatus.Sent -> Triple(
                MessageEntity.STATUS_SENT,
                null,
                true
            )
            MessageStatus.Processing -> Triple(
                MessageEntity.STATUS_PROCESSING,
                null,
                true
            )
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

    /**
     * Converts a data MessageEntity to a domain Message.
     *
     * This mapping reconstructs the rich domain model from the flat
     * persistence structure. String constants are converted to sealed
     * interfaces, and primitive types are wrapped in value classes.
     *
     * @param entity The data entity to convert
     * @return The corresponding domain message
     * @throws IllegalArgumentException if entity contains invalid data
     */
    fun toDomain(entity: MessageEntity): Message {
        require(entity.isValid()) {
            "Cannot convert invalid MessageEntity to domain Message"
        }

        val messageId = MessageId.from(entity.id)

        val sender = when (entity.senderType) {
            MessageEntity.SENDER_USER -> MessageSender.USER
            MessageEntity.SENDER_ASSISTANT -> MessageSender.ASSISTANT
            else -> throw IllegalArgumentException(
                "Unknown sender type: ${entity.senderType}"
            )
        }

        val timestamp = Instant.ofEpochMilli(entity.timestampMillis)

        val status = when (entity.statusType) {
            MessageEntity.STATUS_SENT -> MessageStatus.Sent
            MessageEntity.STATUS_PROCESSING -> MessageStatus.Processing
            MessageEntity.STATUS_FAILED -> MessageStatus.Failed(
                error = Exception(entity.errorMessage ?: "Unknown error"),
                isRetryable = entity.isRetryable
            )
            else -> throw IllegalArgumentException(
                "Unknown status type: ${entity.statusType}"
            )
        }

        return Message(
            id = messageId,
            content = entity.content,
            sender = sender,
            timestamp = timestamp,
            status = status
        )
    }

    /**
     * Converts a list of domain Messages to data MessageEntities.
     *
     * @param messages The domain messages to convert
     * @return The corresponding data entities
     */
    fun toEntityList(messages: List<Message>): List<MessageEntity> {
        return messages.map { toEntity(it) }
    }

    /**
     * Converts a list of data MessageEntities to domain Messages.
     *
     * Invalid entities are filtered out with a warning in production,
     * but should be caught in testing.
     *
     * @param entities The data entities to convert
     * @return The corresponding domain messages
     */
    fun toDomainList(entities: List<MessageEntity>): List<Message> {
        return entities.mapNotNull { entity ->
            try {
                toDomain(entity)
            } catch (e: IllegalArgumentException) {
                // Log error in production, skip invalid entity
                // TODO: Add logging when logger is available
                null
            }
        }
    }
}

/**
 * Mapper for converting between domain AiConfiguration and data AiConfigurationEntity.
 *
 * This class handles the bidirectional mapping between configuration models,
 * ensuring proper conversion of complex domain types to simple persistence types.
 *
 * @since 1.0.0
 */
object AiConfigurationMapper {
    /**
     * Converts a domain AiConfiguration to a data AiConfigurationEntity.
     *
     * This mapping flattens the configuration model, unwrapping value classes
     * and converting sealed interfaces to string constants.
     *
     * @param configuration The domain configuration to convert
     * @return The corresponding data entity
     */
    fun toEntity(configuration: AiConfiguration): AiConfigurationEntity {
        val aiModeValue = when (configuration.mode) {
            AiMode.ONLINE -> AiConfigurationEntity.MODE_ONLINE
            AiMode.OFFLINE -> AiConfigurationEntity.MODE_OFFLINE
        }

        // API key always null - Firebase Functions proxy handles authentication server-side
        val apiKeyValue = null

        return AiConfigurationEntity(
            aiModeValue = aiModeValue,
            apiKeyValue = apiKeyValue,
            temperature = configuration.modelParameters.temperature,
            topK = configuration.modelParameters.topK,
            topP = configuration.modelParameters.topP,
            maxOutputTokens = configuration.modelParameters.maxOutputTokens
        )
    }

    /**
     * Converts a data AiConfigurationEntity to a domain AiConfiguration.
     *
     * This mapping reconstructs the rich domain model from the flat
     * persistence structure, validating data and wrapping primitives
     * in appropriate domain types.
     *
     * @param entity The data entity to convert
     * @return The corresponding domain configuration
     * @throws IllegalArgumentException if entity contains invalid data
     */
    fun toDomain(entity: AiConfigurationEntity): AiConfiguration {
        require(entity.isValid()) {
            "Cannot convert invalid AiConfigurationEntity to domain AiConfiguration"
        }

        val mode = when (entity.aiModeValue) {
            AiConfigurationEntity.MODE_ONLINE -> AiMode.ONLINE
            AiConfigurationEntity.MODE_OFFLINE -> AiMode.OFFLINE
            else -> throw IllegalArgumentException(
                "Unknown AI mode: ${entity.aiModeValue}"
            )
        }

        // API key always null - Firebase Functions proxy handles authentication
        // Legacy API keys in storage are ignored
        val apiKey: ApiKey? = null

        val modelParameters = ModelParameters(
            temperature = entity.temperature,
            topK = entity.topK,
            topP = entity.topP,
            maxOutputTokens = entity.maxOutputTokens
        )

        return AiConfiguration(
            mode = mode,
            apiKey = apiKey,
            modelParameters = modelParameters
        )
    }
}
