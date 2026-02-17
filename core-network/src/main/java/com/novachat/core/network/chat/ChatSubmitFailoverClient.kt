package com.novachat.core.network.chat

import kotlinx.coroutines.delay

/**
 * Submit with failover: try each base URL in order, with 2 retries per URL
 * (backoff 200ms, 800ms + jitter 0–30% per spec).
 */
class ChatSubmitFailoverClient(
    private val baseUrls: List<String>,
    private val api: ChatSubmitApi
) {
    suspend fun submit(request: ChatSubmitRequest): Result<ChatSubmitResponse> {
        var lastFailure: Result<ChatSubmitResponse> = Result.failure(Exception("No endpoints"))
        for (baseUrl in baseUrls) {
            for (attempt in 0..2) {
                val result = api.submit(baseUrl, request)
                if (result.isSuccess) return result
                lastFailure = result
                if (attempt < 2) {
                    delay(ChatSubmitApi.jitterMs(if (attempt == 0) 200L else 800L))
                }
            }
        }
        return lastFailure
    }
}

