package com.novachat.feature.ai.di

import com.novachat.feature.ai.data.repository.AiRepositoryImpl
import com.novachat.feature.ai.data.repository.MessageRepositoryImpl
import com.novachat.feature.ai.data.repository.PreferencesRepositoryImpl
import com.novachat.feature.ai.data.offline.UnavailableOfflineAiEngine
import com.novachat.feature.ai.domain.offline.OfflineAiEngine
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository implementations.
 *
 * All bindings are installed in [SingletonComponent] to ensure a single
 * instance of each repository across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        impl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository

    @Binds
    @Singleton
    abstract fun bindOfflineAiEngine(
        impl: UnavailableOfflineAiEngine
    ): OfflineAiEngine
}
