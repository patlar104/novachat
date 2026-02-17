package com.novachat.feature.ai.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings_backup",
    indices = [Index(value = ["updated_at_ms"], orders = [Index.Order.DESC])]
)
data class SettingsBackupEntity(
    @PrimaryKey
    val key: String,
    val json_value: String,
    val checksum: String?,
    val updated_at_ms: Long
)
