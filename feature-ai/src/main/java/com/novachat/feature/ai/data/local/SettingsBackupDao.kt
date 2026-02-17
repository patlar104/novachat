package com.novachat.feature.ai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsBackupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SettingsBackupEntity)

    @Query("SELECT * FROM settings_backup WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): SettingsBackupEntity?

    @Query("DELETE FROM settings_backup WHERE key = :key")
    suspend fun deleteByKey(key: String)
}
