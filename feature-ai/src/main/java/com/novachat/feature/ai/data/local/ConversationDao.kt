package com.novachat.feature.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ConversationEntity)

    @Update
    suspend fun update(entity: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE conversation_id = :conversationId LIMIT 1")
    suspend fun getById(conversationId: String): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY updated_at_ms DESC")
    fun observeAll(): Flow<List<ConversationEntity>>

    @Query("DELETE FROM conversations WHERE conversation_id = :conversationId")
    suspend fun deleteById(conversationId: String)
}
