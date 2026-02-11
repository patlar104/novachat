package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class ObserveWaitForDebuggerOnNextLaunchUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke() = preferencesRepository.observeWaitForDebuggerOnNextLaunch()
}
