package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class UpdateAiConfigurationUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(configuration: AiConfiguration): Result<Unit> {
        val validationResult = configuration.validate()
        if (validationResult.isFailure) {
            return validationResult
        }

        val modeAvailable = aiRepository.isModeAvailable(configuration.mode)
        if (!modeAvailable) {
            return Result.failure(
                IllegalStateException(
                    "AI mode ${configuration.mode} is not available on this device"
                )
            )
        }

        return preferencesRepository.updateAiConfiguration(configuration)
    }
}
