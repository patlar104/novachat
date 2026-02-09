package com.novachat.feature.ai.di

import android.content.Context
import com.novachat.feature.ai.data.repository.AiRepositoryImpl
import com.novachat.feature.ai.data.repository.MessageRepositoryImpl
import com.novachat.feature.ai.data.repository.PreferencesRepositoryImpl
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import com.novachat.feature.ai.domain.usecase.ClearConversationUseCase
import com.novachat.feature.ai.domain.usecase.ObserveAiConfigurationUseCase
import com.novachat.feature.ai.domain.usecase.ObserveMessagesUseCase
import com.novachat.feature.ai.domain.usecase.ObserveThemePreferencesUseCase
import com.novachat.feature.ai.domain.usecase.RetryMessageUseCase
import com.novachat.feature.ai.domain.usecase.SendMessageUseCase
import com.novachat.feature.ai.domain.usecase.UpdateAiConfigurationUseCase
import com.novachat.feature.ai.domain.usecase.UpdateThemePreferencesUseCase

/**
 * Dependency injection container for the AI feature.
 *
 * This class provides manual dependency injection, creating and managing
 * singleton instances of repositories and use cases. It follows a simple
 * Service Locator pattern suitable for small to medium applications.
 *
 * Thread-safety: All lazy properties are thread-safe by default.
 *
 * @property context Application context for creating repositories
 *
 * @since 1.0.0
 */
class AiContainer(private val context: Context) {

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
 * Provider interface for exposing AiContainer from the application class.
 */
interface AiContainerProvider {
    val aiContainer: AiContainer
}

/**
 * Extension property to get the AiContainer from Context.
 */
val Context.aiContainer: AiContainer
    get() = (applicationContext as? AiContainerProvider)?.aiContainer
        ?: throw IllegalStateException(
            "Application must implement AiContainerProvider"
        )
