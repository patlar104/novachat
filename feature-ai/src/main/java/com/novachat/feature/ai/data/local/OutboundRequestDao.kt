package com.novachat.feature.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboundRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboundRequestEntity)

    @Update
    suspend fun update(entity: OutboundRequestEntity)

    @Query("SELECT * FROM outbound_requests WHERE request_id = :requestId LIMIT 1")
    suspend fun getById(requestId: String): OutboundRequestEntity?

    @Query("SELECT * FROM outbound_requests WHERE state IN (:states) AND (done_at_ms IS NULL OR next_retry_at_ms <= :nowMs) ORDER BY next_retry_at_ms ASC")
    suspend fun getPendingForReconciliation(
        states: List<String>,
        nowMs: Long
    ): List<OutboundRequestEntity>

    @Query("SELECT * FROM outbound_requests WHERE state IN (:states) AND (done_at_ms IS NULL OR next_retry_at_ms <= :nowMs) ORDER BY next_retry_at_ms ASC")
    fun observePendingForReconciliation(
        states: List<String>,
        nowMs: Long
    ): Flow<List<OutboundRequestEntity>>

    @Query("UPDATE outbound_requests SET state = :state, region = :region, ack_at_ms = :ackAtMs WHERE request_id = :requestId")
    suspend fun markAcked(requestId: String, state: String, region: String?, ackAtMs: Long?)

    @Query("UPDATE outbound_requests SET state = :state, done_at_ms = :doneAtMs, last_error = :lastError WHERE request_id = :requestId")
    suspend fun markDone(requestId: String, state: String, doneAtMs: Long?, lastError: String?)

    @Query("UPDATE outbound_requests SET next_retry_at_ms = :nextRetryAtMs WHERE request_id = :requestId")
    suspend fun updateNextRetry(requestId: String, nextRetryAtMs: Long)
}
