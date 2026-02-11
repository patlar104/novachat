package com.novachat.feature.ai.domain.usecase

import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import javax.inject.Inject

class UpdateThemePreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(preferences: ThemePreferences): Result<Unit> =
        preferencesRepository.updateThemePreferences(preferences)
}
