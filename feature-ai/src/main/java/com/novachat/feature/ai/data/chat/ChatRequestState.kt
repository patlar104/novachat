package com.novachat.feature.ai.data.chat

/**
 * Client-side state for async chat request (from Firestore chat_requests or status API).
 */
data class ChatRequestState(
    val requestId: String,
    val state: String,
    val attempt: Int = 1,
    val errorCode: String? = null,
    val responseText: String? = null
) {
    val isTerminal: Boolean
        get() = state == "COMPLETED" || state == "FAILED"
}
