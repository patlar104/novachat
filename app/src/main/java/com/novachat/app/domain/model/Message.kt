package com.novachat.app.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Domain model representing a chat message.
 *
 * This is the core domain entity for messages, independent of any data source
 * or UI representation. Following Clean Architecture principles, this model
 * contains only business logic and is free from framework dependencies.
 *
 * @property id Unique identifier for the message, generated using UUID v4
 * @property content The actual text content of the message
 * @property sender Indicates who sent the message (user or AI assistant)
 * @property timestamp When the message was created, using Instant for proper time handling
 * @property status Current delivery/processing status of the message
 *
 * @since 1.0.0
 */
data class Message(
    val id: MessageId = MessageId.generate(),
    val content: String,
    val sender: MessageSender,
    val timestamp: Instant = Instant.now(),
    val status: MessageStatus = MessageStatus.Sent
) {
    /**
     * Validates that the message content is not blank.
     * Messages with only whitespace are considered invalid.
     *
     * @return true if the message content contains non-whitespace characters
     */
    fun isValid(): Boolean = content.isNotBlank()

    /**
     * Creates a copy of this message with an updated status.
     *
     * @param newStatus The new status to apply to the message
     * @return A new Message instance with the updated status
     */
    fun withStatus(newStatus: MessageStatus): Message = copy(status = newStatus)

    /**
     * Checks if this message was sent by the user.
     *
     * @return true if the sender is USER
     */
    fun isFromUser(): Boolean = sender == MessageSender.USER

    /**
     * Checks if this message was sent by the AI assistant.
     *
     * @return true if the sender is ASSISTANT
     */
    fun isFromAssistant(): Boolean = sender == MessageSender.ASSISTANT
}

/**
 * Type-safe wrapper for message identifiers.
 *
 * Using a value class provides compile-time safety while having zero runtime overhead.
 * This prevents accidental mixing of message IDs with other string types.
 *
 * @property value The underlying UUID string representation
 */
@JvmInline
value class MessageId(val value: String) {
    companion object {
        /**
         * Generates a new unique message identifier using UUID v4.
         *
         * @return A new MessageId with a randomly generated UUID
         */
        fun generate(): MessageId = MessageId(UUID.randomUUID().toString())

        /**
         * Creates a MessageId from an existing string value.
         * Use this when reconstructing messages from persistent storage.
         *
         * @param value The UUID string to wrap
         * @return A MessageId wrapping the provided value
         */
        fun from(value: String): MessageId = MessageId(value)
    }

    override fun toString(): String = value
}

/**
 * Represents who sent a message in the conversation.
 *
 * Using a sealed interface allows for future extension with additional sender types
 * (e.g., SYSTEM for system messages) while maintaining exhaustive when expressions.
 */
sealed interface MessageSender {
    /**
     * Message sent by the human user.
     */
    data object USER : MessageSender

    /**
     * Message sent by the AI assistant.
     */
    data object ASSISTANT : MessageSender
}

/**
 * Represents the current status of a message in the conversation.
 *
 * This sealed hierarchy allows for type-safe status handling and ensures
 * all possible states are handled in when expressions.
 */
sealed interface MessageStatus {
    /**
     * Message has been successfully sent/received.
     */
    data object Sent : MessageStatus

    /**
     * Message is currently being processed (e.g., AI is generating a response).
     */
    data object Processing : MessageStatus

    /**
     * Message failed to send or process.
     *
     * @property error The exception that caused the failure
     * @property isRetryable Whether the operation can be retried
     */
    data class Failed(
        val error: Throwable,
        val isRetryable: Boolean = true
    ) : MessageStatus
}
