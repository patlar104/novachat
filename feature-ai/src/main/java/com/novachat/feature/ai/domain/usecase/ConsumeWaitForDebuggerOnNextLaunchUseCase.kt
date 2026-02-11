package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class ConsumeWaitForDebuggerOnNextLaunchUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): Result<Boolean> =
        preferencesRepository.consumeWaitForDebuggerOnNextLaunch()
}
