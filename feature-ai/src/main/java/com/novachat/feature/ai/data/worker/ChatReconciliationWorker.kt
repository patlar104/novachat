package com.novachat.feature.ai.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.novachat.feature.ai.data.observability.ChatObservability
import com.novachat.feature.ai.domain.repository.OutboundRequestRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * SPEC-1: Reconcile pending outbound requests (QUEUED_LOCAL, QUEUED, PROCESSING).
 * Run periodically or when app comes to foreground; re-submit or re-attach observer.
 */
@HiltWorker
class ChatReconciliationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val outboundRequestRepository: OutboundRequestRepository,
    private val chatObservability: ChatObservability
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val nowMs = System.currentTimeMillis()
        val pending = outboundRequestRepository.getPendingForReconciliation(
            states = listOf("QUEUED_LOCAL", "QUEUED", "PROCESSING"),
            nowMs = nowMs
        )
        for (req in pending) {
            when (req.state) {
                "QUEUED_LOCAL" -> {
                    chatObservability.emit("local_queue_retry", mapOf("request_id" to req.requestId))
                    outboundRequestRepository.updateNextRetry(
                        req.requestId,
                        nowMs + nextBackoffMs(req.attemptCount)
                    )
                    // Actual re-submit would call submit API; for now we just bump retry time.
                    // Full implementation would inject AiRepository and MessageRepository
                    // and call submitAsync + observeCompletion.
                }
                "QUEUED", "PROCESSING" -> {
                    outboundRequestRepository.updateNextRetry(
                        req.requestId,
                        nowMs + nextBackoffMs(req.attemptCount)
                    )
                    // Re-attach Firestore listener when app is in foreground is handled
                    // by the UI/ViewModel when it observes messages.
                }
            }
        }
        return Result.success()
    }

    private fun nextBackoffMs(attemptCount: Int): Long {
        if (attemptCount <= 0) return 30_000L
        val exp = (1L shl attemptCount.coerceAtMost(8)) * 30_000L
        return exp.coerceAtMost(15 * 60 * 1000L)
    }
}
