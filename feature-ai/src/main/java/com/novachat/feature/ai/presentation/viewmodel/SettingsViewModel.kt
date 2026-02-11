package com.novachat.feature.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.OfflineCapability
import com.novachat.feature.ai.domain.usecase.ObserveAiConfigurationUseCase
import com.novachat.feature.ai.domain.usecase.ObserveAiModeAvailabilityUseCase
import com.novachat.feature.ai.domain.usecase.ObserveWaitForDebuggerOnNextLaunchUseCase
import com.novachat.feature.ai.domain.usecase.SetWaitForDebuggerOnNextLaunchUseCase
import com.novachat.feature.ai.domain.usecase.UpdateAiConfigurationUseCase
import com.novachat.feature.ai.presentation.model.SettingsUiEvent
import com.novachat.feature.ai.presentation.model.SettingsUiState
import com.novachat.feature.ai.presentation.model.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeAiConfigurationUseCase: ObserveAiConfigurationUseCase,
    private val observeAiModeAvailabilityUseCase: ObserveAiModeAvailabilityUseCase,
    private val observeWaitForDebuggerOnNextLaunchUseCase: ObserveWaitForDebuggerOnNextLaunchUseCase,
    private val setWaitForDebuggerOnNextLaunchUseCase: SetWaitForDebuggerOnNextLaunchUseCase,
    private val updateAiConfigurationUseCase: UpdateAiConfigurationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Initial)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        observeConfiguration()
    }

    private fun observeConfiguration() {
        viewModelScope.launch {
            combine(
                observeAiConfigurationUseCase(),
                observeAiModeAvailabilityUseCase(),
                observeWaitForDebuggerOnNextLaunchUseCase(),
            ) { configuration, offlineCapability, waitForDebuggerOnNextLaunch ->
                SettingsUiState.Success(
                    configuration = configuration,
                    offlineCapability = offlineCapability,
                    waitForDebuggerOnNextLaunch = waitForDebuggerOnNextLaunch,
                )
            }
                .catch { exception ->
                    _uiState.update {
                        SettingsUiState.Error(
                            message = "Failed to load settings: ${exception.message}"
                        )
                    }
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.ChangeAiMode -> handleChangeAiMode(event.mode)
            is SettingsUiEvent.ToggleWaitForDebuggerOnNextLaunch ->
                handleToggleWaitForDebuggerOnNextLaunch(event.enabled)
            is SettingsUiEvent.NavigateBack -> handleNavigateBack()
        }
    }

    private fun handleChangeAiMode(mode: AiMode) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState !is SettingsUiState.Success) {
                return@launch
            }

            val offlineCapability = currentState.offlineCapability
            if (mode == AiMode.OFFLINE && offlineCapability is OfflineCapability.Unavailable) {
                emitEffect(
                    UiEffect.ShowSnackbar(
                        message = offlineCapability.reason,
                        actionLabel = "Dismiss"
                    )
                )
                return@launch
            }

            val updatedConfig = currentState.configuration.copy(mode = mode)
            val result = updateAiConfigurationUseCase(updatedConfig)

            result.fold(
                onSuccess = {
                    val modeName = when (mode) {
                        AiMode.ONLINE -> "Online"
                        AiMode.OFFLINE -> "Offline"
                    }
                    emitEffect(UiEffect.ShowToast("AI mode changed to $modeName"))
                },
                onFailure = { exception ->
                    emitEffect(
                        UiEffect.ShowSnackbar(
                            message = "Failed to change mode: ${exception.message}",
                            actionLabel = "Dismiss"
                        )
                    )
                }
            )
        }
    }

    private fun handleNavigateBack() {
        emitEffect(UiEffect.NavigateBack)
    }

    private fun handleToggleWaitForDebuggerOnNextLaunch(enabled: Boolean) {
        viewModelScope.launch {
            val result = setWaitForDebuggerOnNextLaunchUseCase(enabled)
            result.fold(
                onSuccess = {
                    if (enabled) {
                        emitEffect(
                            UiEffect.ShowSnackbar(
                                message = "Wait for debugger is armed for next launch. " +
                                    "Close the app and relaunch with Debug.",
                                actionLabel = "Dismiss"
                            )
                        )
                    } else {
                        emitEffect(
                            UiEffect.ShowToast("Wait for debugger on next launch disabled")
                        )
                    }
                },
                onFailure = { exception ->
                    emitEffect(
                        UiEffect.ShowSnackbar(
                            message = "Failed to update debugger wait setting: ${exception.message}",
                            actionLabel = "Dismiss"
                        )
                    )
                }
            )
        }
    }

    private fun emitEffect(effect: UiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
