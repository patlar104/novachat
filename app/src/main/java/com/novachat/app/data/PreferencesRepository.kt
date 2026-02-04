package com.novachat.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing user preferences
 */
class PreferencesRepository(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    
    companion object {
        private val API_KEY = stringPreferencesKey("api_key")
        private val AI_MODE = stringPreferencesKey("ai_mode")
    }
    
    val apiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[API_KEY]
    }
    
    val aiMode: Flow<AiMode> = context.dataStore.data.map { preferences ->
        val mode = preferences[AI_MODE] ?: AiMode.ONLINE.name
        AiMode.valueOf(mode)
    }
    
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }
    
    suspend fun saveAiMode(mode: AiMode) {
        context.dataStore.edit { preferences ->
            preferences[AI_MODE] = mode.name
        }
    }
}
