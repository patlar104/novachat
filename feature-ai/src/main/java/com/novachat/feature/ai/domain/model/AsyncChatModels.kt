package com.novachat.feature.ai.domain.model

/**
 * Domain types for SPEC-1 async chat submit and completion.
 */
data class SubmitRequest(
    val requestId: String,
    val conversationId: String,
    val messageId: String,
    val messageText: String,
    val modelProfile: String,
    val clientTsMs: Long,
    val appInstanceId: String? = null
)

data class SubmitResult(
    val requestId: String,
    val status: String,
    val region: String,
    val degraded: Boolean,
    val etaMs: Long
)

data class RequestCompletionState(
    val requestId: String,
    val state: String,
    val attempt: Int = 1,
    val errorCode: String? = null,
    val responseText: String? = null
) {
    val isTerminal: Boolean get() = state == "COMPLETED" || state == "FAILED"
}
