package com.novachat.feature.ai.data.repository

import com.novachat.feature.ai.data.local.OutboundRequestDao
import com.novachat.feature.ai.data.local.OutboundRequestEntity
import com.novachat.feature.ai.domain.repository.OutboundRequest
import com.novachat.feature.ai.domain.repository.OutboundRequestRepository
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OutboundRequestRepositoryRoomImpl @Inject constructor(
    private val dao: OutboundRequestDao,
    @param:Named("default_conversation_id") private val defaultConversationId: String
) : OutboundRequestRepository {

    override suspend fun insert(
        requestId: String,
        conversationId: String,
        userMessageId: String,
        state: String,
        nextRetryAtMs: Long
    ): Result<Unit> = try {
        dao.insert(
            OutboundRequestEntity(
                request_id = requestId,
                conversation_id = conversationId,
                user_message_id = userMessageId,
                region = null,
                state = state,
                attempt_count = 1,
                next_retry_at_ms = nextRetryAtMs,
                ack_at_ms = null,
                done_at_ms = null,
                last_error = null
            )
        )
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markAcked(
        requestId: String,
        region: String?,
        ackAtMs: Long
    ): Result<Unit> = try {
        dao.markAcked(requestId, "QUEUED", region, ackAtMs)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun markDone(
        requestId: String,
        state: String,
        doneAtMs: Long,
        lastError: String?
    ): Result<Unit> = try {
        dao.markDone(requestId, state, doneAtMs, lastError)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getByRequestId(requestId: String): OutboundRequest? =
        dao.getById(requestId)?.toDomain()

    override fun observePendingForReconciliation(
        states: List<String>,
        nowMs: Long
    ): Flow<List<OutboundRequest>> =
        dao.observePendingForReconciliation(states, nowMs).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getPendingForReconciliation(
        states: List<String>,
        nowMs: Long
    ): List<OutboundRequest> =
        dao.getPendingForReconciliation(states, nowMs).map { it.toDomain() }

    override suspend fun updateNextRetry(requestId: String, nextRetryAtMs: Long): Result<Unit> = try {
        dao.updateNextRetry(requestId, nextRetryAtMs)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun OutboundRequestEntity.toDomain(): OutboundRequest =
    OutboundRequest(
        requestId = request_id,
        conversationId = conversation_id,
        userMessageId = user_message_id,
        region = region,
        state = state,
        attemptCount = attempt_count,
        nextRetryAtMs = next_retry_at_ms,
        ackAtMs = ack_at_ms,
        doneAtMs = done_at_ms,
        lastError = last_error
    )
