package com.novachat.app.data

import java.util.UUID

/**
 * Represents a chat message in the conversation
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI mode for the chat
 */
enum class AiMode {
    ONLINE,  // Google Gemini cloud-based
    OFFLINE  // On-device AICore
}
