package com.novachat.feature.ai.di

import android.content.Context
import com.novachat.feature.ai.data.chat.ChatStatusObserver
import com.novachat.feature.ai.data.local.ConversationDao
import com.novachat.feature.ai.data.local.MessageDao
import com.novachat.feature.ai.data.local.NovaChatDatabase
import com.novachat.feature.ai.data.local.OutboundRequestDao
import com.novachat.feature.ai.data.local.SettingsBackupDao
import com.novachat.feature.ai.data.observability.ChatObservability
import com.novachat.feature.ai.data.observability.ChatObservabilityLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    const val DEFAULT_CONVERSATION_ID = "default"

    @Provides
    @Singleton
    fun provideNovaChatDatabase(
        @ApplicationContext context: Context
    ): NovaChatDatabase =
        androidx.room.Room.databaseBuilder(
            context,
            NovaChatDatabase::class.java,
            "novachat.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideMessageDao(db: NovaChatDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideConversationDao(db: NovaChatDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideOutboundRequestDao(db: NovaChatDatabase): OutboundRequestDao = db.outboundRequestDao()

    @Provides
    fun provideSettingsBackupDao(db: NovaChatDatabase): SettingsBackupDao = db.settingsBackupDao()

    @Provides
    @Named("default_conversation_id")
    fun provideDefaultConversationId(): String = DEFAULT_CONVERSATION_ID

    @Provides
    @Singleton
    fun provideChatStatusObserver(): ChatStatusObserver = ChatStatusObserver()

    @Provides
    @Singleton
    fun provideChatObservability(logger: ChatObservabilityLogger): ChatObservability = logger
}
