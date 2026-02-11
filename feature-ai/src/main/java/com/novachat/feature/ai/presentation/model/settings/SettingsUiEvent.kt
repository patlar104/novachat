package com.novachat.feature.ai.presentation.model

import com.novachat.feature.ai.domain.model.AiMode

sealed interface SettingsUiEvent {
    data class ChangeAiMode(val mode: AiMode) : SettingsUiEvent

    data object NavigateBack : SettingsUiEvent
}
