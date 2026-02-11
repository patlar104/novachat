package com.novachat.feature.ai.domain.repository

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.ThemePreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeAiConfiguration(): Flow<AiConfiguration>

    suspend fun updateAiConfiguration(configuration: AiConfiguration): Result<Unit>

    suspend fun updateAiMode(mode: AiMode): Result<Unit>

    suspend fun clearAll(): Result<Unit>

    fun observeThemePreferences(): Flow<ThemePreferences>

    suspend fun updateThemePreferences(preferences: ThemePreferences): Result<Unit>
}
