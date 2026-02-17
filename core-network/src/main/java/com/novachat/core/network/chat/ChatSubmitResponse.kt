package com.novachat.core.network.chat

/**
 * Response for POST /v1/chat/submit — 202 ACK (SPEC-1).
 */
data class ChatSubmitResponse(
    val requestId: String,
    val status: String,
    val region: String,
    val degraded: Boolean = false,
    val etaMs: Long = 3000L
)
