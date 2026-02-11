package com.novachat.core.network

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class FirebaseCallableClient(
    region: String = "us-central1",
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance(region)
) {
    suspend fun call(functionName: String, data: Map<String, Any?>): Result<Map<String, Any?>> {
        return try {
            val result = functions.getHttpsCallable(functionName).call(data).await()
            @Suppress("UNCHECKED_CAST")
            val payload = (result.data as? Map<String, Any?>) ?: emptyMap()
            Result.success(payload)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
