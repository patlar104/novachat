package com.novachat.feature.ai.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbound_requests",
    indices = [Index(value = ["state", "next_retry_at_ms"])]
)
data class OutboundRequestEntity(
    @PrimaryKey
    val request_id: String,
    val conversation_id: String,
    val user_message_id: String,
    val region: String?,
    val state: String,
    val attempt_count: Int,
    val next_retry_at_ms: Long,
    val ack_at_ms: Long?,
    val done_at_ms: Long?,
    val last_error: String?
)
