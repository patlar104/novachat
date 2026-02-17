package com.novachat.feature.ai.data.repository

import com.novachat.feature.ai.data.local.ConversationDao
import com.novachat.feature.ai.data.local.ConversationEntity
import com.novachat.feature.ai.data.local.MessageDao
import com.novachat.feature.ai.data.local.MessageRoomEntity
import com.novachat.feature.ai.data.local.RoomMessageMapper
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.repository.MessageRepository
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed MessageRepository for SPEC-1 durable chat.
 * Uses a default conversation ID for MVP (single chat).
 */
class MessageRepositoryRoomImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    @param:Named("default_conversation_id") private val defaultConversationId: String
) : MessageRepository {

    override fun observeMessages(): Flow<List<Message>> = flow {
        ensureDefaultConversation()
        emitAll(
            messageDao.observeByConversation(defaultConversationId)
                .map { entities -> entities.map { RoomMessageMapper.toDomain(it) } }
        )
    }

    override suspend fun addMessage(message: Message): Result<Unit> = try {
        ensureDefaultConversation()
        val entity = RoomMessageMapper.toRoomEntity(defaultConversationId, message)
            .copy(updated_at_ms = System.currentTimeMillis())
        messageDao.insert(entity)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateMessage(message: Message): Result<Unit> = try {
        val existing = messageDao.getById(message.id.value)
            ?: return Result.failure(NoSuchElementException("Message not found: ${message.id}"))
        val updated = RoomMessageMapper.toRoomEntity(defaultConversationId, message)
            .copy(
                created_at_ms = existing.created_at_ms,
                updated_at_ms = System.currentTimeMillis()
            )
        messageDao.update(updated)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMessage(id: MessageId): Message? =
        messageDao.getById(id.value)?.let { RoomMessageMapper.toDomain(it) }

    override suspend fun clearAllMessages(): Result<Unit> = try {
        messageDao.deleteByConversation(defaultConversationId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMessageCount(): Int =
        messageDao.countByConversation(defaultConversationId)

    private suspend fun ensureDefaultConversation() {
        if (conversationDao.getById(defaultConversationId) == null) {
            val now = System.currentTimeMillis()
            conversationDao.insert(
                ConversationEntity(
                    conversation_id = defaultConversationId,
                    title = "Chat",
                    created_at_ms = now,
                    updated_at_ms = now,
                    last_preview = null,
                    archived = 0
                )
            )
        }
    }
}
