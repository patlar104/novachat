package com.novachat.feature.ai.data.offline

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.OfflineCapability
import com.novachat.feature.ai.domain.offline.OfflineAiEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnavailableOfflineAiEngine @Inject constructor() : OfflineAiEngine {
    private val capability = MutableStateFlow<OfflineCapability>(
        OfflineCapability.Unavailable(
            reason =
                "On-device AI is not available yet. Install a supported offline engine before using OFFLINE mode."
        )
    )

    override fun observeCapability(): Flow<OfflineCapability> = capability.asStateFlow()

    override suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        return Result.failure(
            UnsupportedOperationException(
                "Offline AI is not available yet on this build/device."
            )
        )
    }
}
