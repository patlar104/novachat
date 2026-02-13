package com.novachat.feature.ai.domain.repository

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.OfflineCapability
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String>

    suspend fun isModeAvailable(mode: AiMode): Boolean

    fun observeServiceStatus(): Flow<AiServiceStatus>

    fun observeOfflineCapability(): Flow<OfflineCapability>
}
