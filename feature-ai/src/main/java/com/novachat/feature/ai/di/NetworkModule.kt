package com.novachat.feature.ai.di

import com.novachat.core.network.AiProxyRemoteDataSource
import com.novachat.core.network.AuthSessionProvider
import com.novachat.core.network.PlayServicesChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthSessionProvider(): AuthSessionProvider = AuthSessionProvider()

    @Provides
    @Singleton
    fun providePlayServicesChecker(): PlayServicesChecker = PlayServicesChecker()

    @Provides
    @Singleton
    fun provideAiProxyRemoteDataSource(): AiProxyRemoteDataSource = AiProxyRemoteDataSource()
}
