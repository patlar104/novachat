package com.novachat.feature.ai.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository for outbound chat requests (async path).
 * Used for WorkManager reconciliation and tracking request state.
 */
interface OutboundRequestRepository {

    suspend fun insert(
        requestId: String,
        conversationId: String,
        userMessageId: String,
        state: String,
        nextRetryAtMs: Long
    ): Result<Unit>

    suspend fun markAcked(
        requestId: String,
        region: String?,
        ackAtMs: Long
    ): Result<Unit>

    suspend fun markDone(
        requestId: String,
        state: String,
        doneAtMs: Long,
        lastError: String?
    ): Result<Unit>

    suspend fun getByRequestId(requestId: String): OutboundRequest?

    fun observePendingForReconciliation(
        states: List<String>,
        nowMs: Long
    ): Flow<List<OutboundRequest>>

    suspend fun getPendingForReconciliation(
        states: List<String>,
        nowMs: Long
    ): List<OutboundRequest>

    suspend fun updateNextRetry(requestId: String, nextRetryAtMs: Long): Result<Unit>
}

data class OutboundRequest(
    val requestId: String,
    val conversationId: String,
    val userMessageId: String,
    val region: String?,
    val state: String,
    val attemptCount: Int,
    val nextRetryAtMs: Long,
    val ackAtMs: Long?,
    val doneAtMs: Long?,
    val lastError: String?
)
