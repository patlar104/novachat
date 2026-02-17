package com.novachat.feature.ai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ConversationEntity::class,
        MessageRoomEntity::class,
        OutboundRequestEntity::class,
        SettingsBackupEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NovaChatDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun outboundRequestDao(): OutboundRequestDao
    abstract fun settingsBackupDao(): SettingsBackupDao
}
