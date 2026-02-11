package com.novachat.feature.ai.presentation.model

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.OfflineCapability

sealed interface SettingsUiState {
    data object Initial : SettingsUiState

    data object Loading : SettingsUiState

    data class Success(
        val configuration: AiConfiguration,
        val offlineCapability: OfflineCapability,
        val waitForDebuggerOnNextLaunch: Boolean
    ) : SettingsUiState {
        fun isValidConfiguration(): Boolean = configuration.validate().isSuccess

        fun getValidationMessage(): String? = configuration.validate().exceptionOrNull()?.message
    }

    data class Error(
        val message: String,
        val isRecoverable: Boolean = true
    ) : SettingsUiState
}
