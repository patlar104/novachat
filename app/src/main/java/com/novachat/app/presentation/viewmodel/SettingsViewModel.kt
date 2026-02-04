package com.novachat.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.model.ApiKey
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
        private const val KEY_DRAFT_API_KEY = "draft_api_key"
        private const val KEY_SHOW_SAVE_SUCCESS = "show_save_success"
    }
    
    val draftApiKey: StateFlow<String> = savedStateHandle.getStateFlow(
        key = KEY_DRAFT_API_KEY,
        initialValue = ""
    )
    
    val showSaveSuccess: StateFlow<Boolean> = savedStateHandle.getStateFlow(
        key = KEY_SHOW_SAVE_SUCCESS,
        initialValue = false
    )
    
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
                    // Update draft with current API key if draft is empty
                    if (draftApiKey.value.isEmpty()) {
                        updateDraftApiKey(configuration.apiKey?.value ?: "")
                    }
                    
                    _uiState.update {
                        SettingsUiState.Success(configuration = configuration)
                    }
                }
        }
    }
    
    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SaveApiKey -> handleSaveApiKey(event.apiKey)
            is SettingsUiEvent.ChangeAiMode -> handleChangeAiMode(event.mode)
            is SettingsUiEvent.TestConfiguration -> handleTestConfiguration()
            is SettingsUiEvent.DismissSaveSuccess -> handleDismissSaveSuccess()
            is SettingsUiEvent.NavigateBack -> handleNavigateBack()
            is SettingsUiEvent.ScreenLoaded -> handleScreenLoaded()
        }
    }
    
    fun updateDraftApiKey(apiKey: String) {
        savedStateHandle[KEY_DRAFT_API_KEY] = apiKey
    }
    
    private fun handleSaveApiKey(apiKeyString: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState !is SettingsUiState.Success) {
                emitEffect(UiEffect.ShowToast("Please wait for settings to load"))
                return@launch
            }
            
            val apiKey = if (apiKeyString.isBlank()) {
                null
            } else {
                ApiKey.create(apiKeyString)
            }
            
            if (apiKey == null && apiKeyString.isNotBlank()) {
                emitEffect(UiEffect.ShowSnackbar(
                    message = "Invalid API key format",
                    actionLabel = "Dismiss"
                ))
                return@launch
            }
            
            val updatedConfig = currentState.configuration.copy(apiKey = apiKey)
            val result = updateAiConfigurationUseCase(updatedConfig)
            
            result.fold(
                onSuccess = {
                    savedStateHandle[KEY_SHOW_SAVE_SUCCESS] = true
                    emitEffect(UiEffect.ShowToast("API key saved successfully"))
                    
                    // Auto-hide success message after 2 seconds
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(2000)
                        savedStateHandle[KEY_SHOW_SAVE_SUCCESS] = false
                    }
                },
                onFailure = { exception ->
                    emitEffect(UiEffect.ShowSnackbar(
                        message = "Failed to save: ${exception.message}",
                        actionLabel = "Dismiss"
                    ))
                }
            )
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
                    emitEffect(UiEffect.ShowToast("AI mode changed to ${mode.name}"))
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
    
    private fun handleDismissSaveSuccess() {
        savedStateHandle[KEY_SHOW_SAVE_SUCCESS] = false
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
