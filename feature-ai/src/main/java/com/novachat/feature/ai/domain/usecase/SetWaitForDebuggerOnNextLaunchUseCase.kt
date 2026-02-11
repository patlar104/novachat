package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class SetWaitForDebuggerOnNextLaunchUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> =
        preferencesRepository.setWaitForDebuggerOnNextLaunch(enabled)
}
