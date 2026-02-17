package com.novachat.feature.ai.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversation_id", "created_at_ms"]),
        Index(value = ["request_id"])
    ]
)
data class MessageRoomEntity(
    @PrimaryKey
    val message_id: String,
    val conversation_id: String,
    val request_id: String?,
    val role: String,
    val content: String,
    val status: String,
    val error_code: String?,
    val created_at_ms: Long,
    val updated_at_ms: Long,
    val token_in: Int?,
    val token_out: Int?
)
