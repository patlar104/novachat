package com.novachat.feature.ai.domain.repository

import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun observeMessages(): Flow<List<Message>>

    suspend fun addMessage(message: Message): Result<Unit>

    suspend fun updateMessage(message: Message): Result<Unit>

    suspend fun getMessage(id: MessageId): Message?

    suspend fun clearAllMessages(): Result<Unit>

    suspend fun getMessageCount(): Int
}
