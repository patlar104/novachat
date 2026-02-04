package com.novachat.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.novachat.app.di.AppContainer

/**
 * Factory for creating ViewModels with dependency injection.
 *
 * This factory creates ViewModels with their required dependencies
 * from the AppContainer. It properly handles SavedStateHandle for
 * process death survival.
 *
 * Usage in Compose:
 * ```
 * val viewModel: ChatViewModel = viewModel(
 *     factory = ViewModelFactory(context.appContainer)
 * )
 * ```
 *
 * @property container The application's dependency injection container
 *
 * @since 1.0.0
 */
class ViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    
    /**
     * Creates a ViewModel instance with the appropriate dependencies.
     *
     * Supports:
     * - ChatViewModel
     * - SettingsViewModel
     *
     * @param modelClass The class of the ViewModel to create
     * @param extras Creation extras including SavedStateHandle
     * @return The created ViewModel instance
     * @throws IllegalArgumentException if the ViewModel class is not supported
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        // Get SavedStateHandle from extras
        val savedStateHandle = extras.createSavedStateHandle()
        
        return when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(
                    savedStateHandle = savedStateHandle,
                    sendMessageUseCase = container.sendMessageUseCase,
                    observeMessagesUseCase = container.observeMessagesUseCase,
                    clearConversationUseCase = container.clearConversationUseCase,
                    retryMessageUseCase = container.retryMessageUseCase
                ) as T
            }
            
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    savedStateHandle = savedStateHandle,
                    observeAiConfigurationUseCase = container.observeAiConfigurationUseCase,
                    updateAiConfigurationUseCase = container.updateAiConfigurationUseCase
                ) as T
            }
            
            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}
