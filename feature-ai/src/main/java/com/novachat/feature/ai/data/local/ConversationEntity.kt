package com.novachat.feature.ai.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["updated_at_ms"], orders = [Index.Order.DESC])]
)
data class ConversationEntity(
    @PrimaryKey
    val conversation_id: String,
    val title: String,
    val created_at_ms: Long,
    val updated_at_ms: Long,
    val last_preview: String?,
    val archived: Int
)
