package com.novachat.app.di

import android.content.Context
import com.novachat.app.NovaChatApplication
import com.novachat.app.data.repository.AiRepositoryImpl
import com.novachat.app.data.repository.MessageRepositoryImpl
import com.novachat.app.data.repository.PreferencesRepositoryImpl
import com.novachat.app.domain.repository.AiRepository
import com.novachat.app.domain.repository.MessageRepository
import com.novachat.app.domain.repository.PreferencesRepository
import com.novachat.app.domain.usecase.ClearConversationUseCase
import com.novachat.app.domain.usecase.ObserveAiConfigurationUseCase
import com.novachat.app.domain.usecase.ObserveMessagesUseCase
import com.novachat.app.domain.usecase.ObserveThemePreferencesUseCase
import com.novachat.app.domain.usecase.RetryMessageUseCase
import com.novachat.app.domain.usecase.SendMessageUseCase
import com.novachat.app.domain.usecase.UpdateAiConfigurationUseCase
import com.novachat.app.domain.usecase.UpdateThemePreferencesUseCase

/**
 * Dependency injection container for the application.
 *
 * This class provides manual dependency injection, creating and managing
 * singleton instances of repositories and use cases. It follows a simple
 * Service Locator pattern suitable for small to medium applications.
 *
 * For larger applications, consider using Hilt or Koin instead.
 *
 * Thread-safety: All lazy properties are thread-safe by default.
 *
 * @property context Application context for creating repositories
 *
 * @since 1.0.0
 */
class AppContainer(private val context: Context) {
    
    // ═══════════════════════════════════════════════════════════════════════
    // Repositories (Data Layer)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Message repository singleton.
     * Provides in-memory message storage with reactive updates.
     */
    val messageRepository: MessageRepository by lazy {
        MessageRepositoryImpl()
    }
    
    /**
     * AI repository singleton.
     * Handles communication with AI services via Firebase Functions proxy (Gemini API) and AICore (when available).
     */
    val aiRepository: AiRepository by lazy {
        AiRepositoryImpl(context)
    }
    
    /**
     * Preferences repository singleton.
     * Manages user preferences with DataStore.
     */
    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(context)
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // Use Cases (Domain Layer)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Send message use case.
     * Orchestrates sending a user message and receiving an AI response.
     */
    val sendMessageUseCase: SendMessageUseCase by lazy {
        SendMessageUseCase(
            messageRepository = messageRepository,
            aiRepository = aiRepository,
            preferencesRepository = preferencesRepository
        )
    }
    
    /**
     * Observe messages use case.
     * Provides reactive stream of conversation messages.
     */
    val observeMessagesUseCase: ObserveMessagesUseCase by lazy {
        ObserveMessagesUseCase(
            messageRepository = messageRepository
        )
    }
    
    /**
     * Clear conversation use case.
     * Deletes all messages from the conversation.
     */
    val clearConversationUseCase: ClearConversationUseCase by lazy {
        ClearConversationUseCase(
            messageRepository = messageRepository
        )
    }
    
    /**
     * Update AI configuration use case.
     * Saves and validates AI configuration changes.
     */
    val updateAiConfigurationUseCase: UpdateAiConfigurationUseCase by lazy {
        UpdateAiConfigurationUseCase(
            preferencesRepository = preferencesRepository,
            aiRepository = aiRepository
        )
    }
    
    /**
     * Observe AI configuration use case.
     * Provides reactive stream of configuration changes.
     */
    val observeAiConfigurationUseCase: ObserveAiConfigurationUseCase by lazy {
        ObserveAiConfigurationUseCase(
            preferencesRepository = preferencesRepository
        )
    }
    
    /**
     * Retry message use case.
     * Retries sending a failed message.
     */
    val retryMessageUseCase: RetryMessageUseCase by lazy {
        RetryMessageUseCase(
            messageRepository = messageRepository,
            aiRepository = aiRepository,
            preferencesRepository = preferencesRepository
        )
    }

    /**
     * Observe theme preferences use case.
     * Provides reactive stream of theme settings.
     */
    val observeThemePreferencesUseCase: ObserveThemePreferencesUseCase by lazy {
        ObserveThemePreferencesUseCase(preferencesRepository = preferencesRepository)
    }

    /**
     * Update theme preferences use case.
     * Saves theme mode and dynamic color settings.
     */
    val updateThemePreferencesUseCase: UpdateThemePreferencesUseCase by lazy {
        UpdateThemePreferencesUseCase(preferencesRepository = preferencesRepository)
    }
}

/**
 * Extension property to get the AppContainer from Context.
 *
 * This provides easy access to dependencies from anywhere in the app
 * that has access to a Context.
 *
 * Usage: context.appContainer.sendMessageUseCase
 */
val Context.appContainer: AppContainer
    get() = (applicationContext as? NovaChatApplication)?.container
        ?: throw IllegalStateException(
            "Application must be an instance of NovaChatApplication"
        )
