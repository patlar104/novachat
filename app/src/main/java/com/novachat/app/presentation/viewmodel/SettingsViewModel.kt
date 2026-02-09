package com.novachat.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.usecase.ObserveAiConfigurationUseCase
import com.novachat.app.domain.usecase.UpdateAiConfigurationUseCase
import com.novachat.app.presentation.model.SettingsUiEvent
import com.novachat.app.presentation.model.SettingsUiState
import com.novachat.app.presentation.model.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the settings screen following 2026 Android best practices.
 *
 * This ViewModel:
 * - Uses use cases instead of repositories directly
 * - Implements SavedStateHandle for process death survival
 * - Uses sealed UI state classes for type safety
 * - Separates one-time effects from persistent state
 * - Handles all user events through a single entry point
 * - Validates configuration before saving
 *
 * @property savedStateHandle For surviving process death
 * @property observeAiConfigurationUseCase Observes current configuration
 * @property updateAiConfigurationUseCase Updates configuration
 *
 * @since 1.0.0
 */
class SettingsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val observeAiConfigurationUseCase: ObserveAiConfigurationUseCase,
    private val updateAiConfigurationUseCase: UpdateAiConfigurationUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Initial)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()
    
    private companion object {
        // Removed API key state - no longer needed with Firebase Functions proxy
    }
    
    init {
        observeConfiguration()
    }
    
    private fun observeConfiguration() {
        viewModelScope.launch {
            observeAiConfigurationUseCase()
                .catch { exception ->
                    _uiState.update {
                        SettingsUiState.Error(
                            message = "Failed to load settings: ${exception.message}"
                        )
                    }
                }
                .collect { configuration ->
                    _uiState.update {
                        SettingsUiState.Success(configuration = configuration)
                    }
                }
        }
    }
    
    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SaveApiKey -> {
                // API key saving no longer needed - Firebase Functions handles authentication
                // Keep event handler for compatibility but do nothing
            }
            is SettingsUiEvent.ChangeAiMode -> handleChangeAiMode(event.mode)
            is SettingsUiEvent.TestConfiguration -> handleTestConfiguration()
            is SettingsUiEvent.DismissSaveSuccess -> {
                // No-op - kept for compatibility
            }
            is SettingsUiEvent.NavigateBack -> handleNavigateBack()
            is SettingsUiEvent.ScreenLoaded -> handleScreenLoaded()
        }
    }
    
    private fun handleChangeAiMode(mode: AiMode) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState !is SettingsUiState.Success) {
                return@launch
            }
            
            val updatedConfig = currentState.configuration.copy(mode = mode)
            val result = updateAiConfigurationUseCase(updatedConfig)
            
            result.fold(
                onSuccess = {
                    val modeName = when(mode) {
                        AiMode.ONLINE -> "Online"
                        AiMode.OFFLINE -> "Offline"
                    }
                    emitEffect(UiEffect.ShowToast("AI mode changed to $modeName"))
                },
                onFailure = { exception ->
                    emitEffect(UiEffect.ShowSnackbar(
                        message = "Failed to change mode: ${exception.message}",
                        actionLabel = "Dismiss"
                    ))
                }
            )
        }
    }
    
    private fun handleTestConfiguration() {
        val currentState = _uiState.value
        
        if (currentState !is SettingsUiState.Success) {
            return
        }
        
        if (currentState.isValidConfiguration()) {
            emitEffect(UiEffect.ShowToast("Configuration is valid ✓"))
        } else {
            val message = currentState.getValidationMessage() ?: "Invalid configuration"
            emitEffect(UiEffect.ShowSnackbar(
                message = message,
                actionLabel = "Dismiss"
            ))
        }
    }
    
    
    private fun handleNavigateBack() {
        emitEffect(UiEffect.NavigateBack)
    }
    
    private fun handleScreenLoaded() {
        // Currently no action needed
    }
    
    private fun emitEffect(effect: UiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
