package com.novachat.app.data.repository

import com.novachat.app.data.mapper.MessageMapper
import com.novachat.app.data.model.MessageEntity
import com.novachat.app.domain.model.Message
import com.novachat.app.domain.model.MessageId
import com.novachat.app.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory implementation of MessageRepository.
 *
 * This implementation stores messages in memory using a MutableStateFlow,
 * providing reactive updates to observers. Messages are lost when the app
 * process terminates, which is acceptable for a chat application.
 *
 * For persistence across app restarts, this could be extended to use Room
 * database or other persistent storage.
 *
 * Thread-safety is ensured using a Mutex for all write operations.
 *
 * @since 1.0.0
 */
class MessageRepositoryImpl : MessageRepository {
    /**
     * Internal storage for message entities.
     * Using LinkedHashMap to maintain insertion order.
     */
    private val messagesMap = LinkedHashMap<String, MessageEntity>()

    /**
     * Mutex for thread-safe write operations.
     */
    private val mutex = Mutex()

    /**
     * State flow of message entities for reactive updates.
     */
    private val messagesFlow = MutableStateFlow<List<MessageEntity>>(emptyList())

    /**
     * Observes all messages in the current conversation.
     *
     * The Flow emits the complete list of messages whenever any message
     * is added, updated, or removed. Messages are automatically mapped
     * from data entities to domain models.
     *
     * @return Flow emitting lists of domain messages, ordered by timestamp
     */
    override fun observeMessages(): Flow<List<Message>> {
        return messagesFlow.asStateFlow()
            .map { entities -> MessageMapper.toDomainList(entities) }
    }

    /**
     * Adds a new message to the conversation.
     *
     * If a message with the same ID already exists, this operation has no effect
     * (idempotent behavior). The message is converted to an entity and stored.
     *
     * @param message The domain message to add
     * @return Result.success if added successfully, Result.failure on error
     */
    override suspend fun addMessage(message: Message): Result<Unit> {
        return try {
            val entity = MessageMapper.toEntity(message)

            mutex.withLock {
                // Idempotent: don't add if already exists
                if (!messagesMap.containsKey(entity.id)) {
                    messagesMap[entity.id] = entity
                    emitMessages()
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing message in the conversation.
     *
     * The message is identified by its ID. If no message with that ID exists,
     * the operation fails.
     *
     * @param message The updated domain message
     * @return Result.success if updated, Result.failure if not found or error
     */
    override suspend fun updateMessage(message: Message): Result<Unit> {
        return try {
            val entity = MessageMapper.toEntity(message)

            mutex.withLock {
                if (!messagesMap.containsKey(entity.id)) {
                    return Result.failure(
                        NoSuchElementException("Message not found: ${entity.id}")
                    )
                }

                messagesMap[entity.id] = entity
                emitMessages()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves a specific message by its identifier.
     *
     * @param id The message identifier
     * @return The domain message if found, null if not found
     */
    override suspend fun getMessage(id: MessageId): Message? {
        return try {
            mutex.withLock {
                messagesMap[id.value]?.let { entity ->
                    MessageMapper.toDomain(entity)
                }
            }
        } catch (e: Exception) {
            // Log error in production
            null
        }
    }

    /**
     * Deletes all messages from the conversation.
     *
     * This operation cannot be undone.
     *
     * @return Result.success if cleared, Result.failure on error
     */
    override suspend fun clearAllMessages(): Result<Unit> {
        return try {
            mutex.withLock {
                messagesMap.clear()
                emitMessages()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets the count of messages in the conversation.
     *
     * @return The total number of messages
     */
    override suspend fun getMessageCount(): Int {
        return mutex.withLock {
            messagesMap.size
        }
    }

    /**
     * Emits the current messages list to the flow.
     * Must be called while holding the mutex lock.
     */
    private fun emitMessages() {
        val sortedMessages = messagesMap.values
            .sortedBy { it.timestampMillis }
            .toList()
        messagesFlow.update { sortedMessages }
    }
}
