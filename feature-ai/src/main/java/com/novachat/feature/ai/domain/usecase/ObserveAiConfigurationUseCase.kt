package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class ObserveAiConfigurationUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke() = preferencesRepository.observeAiConfiguration()
}
