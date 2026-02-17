package com.novachat.feature.ai.domain.repository

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.OfflineCapability
import com.novachat.feature.ai.domain.model.RequestCompletionState
import com.novachat.feature.ai.domain.model.SubmitRequest
import com.novachat.feature.ai.domain.model.SubmitResult
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String>

    /** SPEC-1 async path: submit and get 202 ACK. */
    suspend fun submitAsync(request: SubmitRequest): Result<SubmitResult>

    /** SPEC-1: observe completion (Firestore) until COMPLETED/FAILED. */
    fun observeCompletion(requestId: String): Flow<RequestCompletionState>

    suspend fun isModeAvailable(mode: AiMode): Boolean

    fun observeServiceStatus(): Flow<AiServiceStatus>

    fun observeOfflineCapability(): Flow<OfflineCapability>
}
