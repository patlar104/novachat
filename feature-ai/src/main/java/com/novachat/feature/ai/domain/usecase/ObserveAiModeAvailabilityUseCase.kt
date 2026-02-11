package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.repository.AiRepository
import javax.inject.Inject

class ObserveAiModeAvailabilityUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke() = aiRepository.observeOfflineCapability()
}
