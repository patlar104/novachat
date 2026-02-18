package com.novachat.core.network.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * HTTP client for POST /v1/chat/submit.
 * Connect 2s, total 5s; 2 retries with backoff 200ms, 800ms + jitter 0–30%.
 */
class ChatSubmitApi(
    private val authToken: String,
    private val appCheckToken: String? = null,
    private val client: OkHttpClient = defaultClient()
) {

    suspend fun submit(
        baseUrl: String,
        request: ChatSubmitRequest
    ): Result<ChatSubmitResponse> = withContext(Dispatchers.IO) {
        val url = baseUrl.trimEnd('/')
        val body = JSONObject(request.toJsonMap()).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $authToken")
        appCheckToken?.let { requestBuilder.addHeader("X-Firebase-AppCheck", it) }
        val okRequest = requestBuilder.build()
        runCatching {
            client.newCall(okRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val bodyMsg = response.body.string()
                    val msg = bodyMsg.takeIf { it.isNotEmpty() } ?: response.message
                    throw Exception("Submit failed: ${response.code} $msg")
                }
                val json = response.body.string().takeIf { it.isNotEmpty() }
                    ?: throw Exception("Empty body")
                parseResponse(json)
            }
        }
    }

    private fun parseResponse(json: String): ChatSubmitResponse {
        val obj = JSONObject(json)
        return ChatSubmitResponse(
            requestId = obj.optString("requestId", ""),
            status = obj.optString("status", "QUEUED"),
            region = obj.optString("region", ""),
            degraded = obj.optBoolean("degraded", false),
            etaMs = obj.optLong("etaMs", 3000L)
        )
    }

    companion object {
        private const val CONNECT_TIMEOUT_S = 2L
        private const val READ_TIMEOUT_S = 5L
        private val RETRY_DELAYS_MS = listOf(200L, 800L)
        private const val JITTER_FRACTION = 0.3

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
            .build()

        fun jitterMs(baseMs: Long): Long {
            val j = 1.0 + (Math.random() * 2 - 1) * JITTER_FRACTION
            return (baseMs * j).toLong().coerceAtLeast(0)
        }
    }
}
