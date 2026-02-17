package com.novachat.feature.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MessageRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MessageRoomEntity>)

    @Update
    suspend fun update(entity: MessageRoomEntity)

    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY created_at_ms ASC")
    fun observeByConversation(conversationId: String): Flow<List<MessageRoomEntity>>

    @Query("SELECT * FROM messages WHERE message_id = :messageId LIMIT 1")
    suspend fun getById(messageId: String): MessageRoomEntity?

    @Query("SELECT * FROM messages WHERE request_id = :requestId LIMIT 1")
    suspend fun getByRequestId(requestId: String): MessageRoomEntity?

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteByConversation(conversationId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE conversation_id = :conversationId")
    suspend fun countByConversation(conversationId: String): Int
}
