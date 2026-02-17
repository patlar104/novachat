package com.novachat.feature.ai.data.settings

import com.novachat.feature.ai.data.local.SettingsBackupDao
import com.novachat.feature.ai.data.local.SettingsBackupEntity
import javax.inject.Inject

/**
 * SPEC-1: On DataStore corruption, restore from settings_backup when ff_settings_guard is true.
 */
class SettingsBackupGuard @Inject constructor(
    private val settingsBackupDao: SettingsBackupDao
) {

    suspend fun backup(key: String, jsonValue: String, checksum: String) {
        settingsBackupDao.insert(
            SettingsBackupEntity(
                key = key,
                json_value = jsonValue,
                checksum = checksum,
                updated_at_ms = System.currentTimeMillis()
            )
        )
    }

    suspend fun restore(key: String): String? =
        settingsBackupDao.getByKey(key)?.json_value
}
