package com.novachat.feature.ai.domain.offline

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.OfflineCapability
import kotlinx.coroutines.flow.Flow

interface OfflineAiEngine {
    fun observeCapability(): Flow<OfflineCapability>

    suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String>
}
