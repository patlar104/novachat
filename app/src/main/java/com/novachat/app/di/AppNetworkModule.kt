package com.novachat.app.di

import com.novachat.app.network.DefaultChatSubmitBaseUrlProvider
import com.novachat.core.network.chat.ChatSubmitBaseUrlProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    @Provides
    @Singleton
    fun provideChatSubmitBaseUrlProvider(): ChatSubmitBaseUrlProvider =
        DefaultChatSubmitBaseUrlProvider()
}
