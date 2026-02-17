package com.novachat.core.network.chat

/**
 * Request body for POST /v1/chat/submit (SPEC-1).
 */
data class ChatSubmitRequest(
    val requestId: String,
    val conversationId: String,
    val messageId: String,
    val messageText: String,
    val modelProfile: String,
    val clientTsMs: Long,
    val appInstanceId: String? = null
) {
    fun toJsonMap(): Map<String, Any?> = mapOf(
        "requestId" to requestId,
        "conversationId" to conversationId,
        "messageId" to messageId,
        "messageText" to messageText,
        "modelProfile" to modelProfile,
        "clientTsMs" to clientTsMs,
        "appInstanceId" to appInstanceId
    )
}
