package com.novachat.feature.ai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.novachat.feature.ai.data.mapper.AiConfigurationMapper
import com.novachat.feature.ai.data.model.AiConfigurationEntity
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.ApiKey
import com.novachat.feature.ai.domain.model.ThemeMode
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * DataStore extension property for preferences.
 * Creates a singleton DataStore instance.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "novachat_preferences"
)

/**
 * Implementation of PreferencesRepository using DataStore.
 *
 * This implementation provides type-safe, asynchronous storage for user preferences
 * using Jetpack DataStore Preferences. All operations are thread-safe and support
 * reactive updates through Kotlin Flows.
 *
 * DataStore advantages over SharedPreferences:
 * - Fully asynchronous with Kotlin coroutines
 * - Type safety with preference keys
 * - Data consistency guarantees
 * - Exception handling with Flow
 * - No UI thread blocking
 *
 * @property context Android context for accessing DataStore
 *
 * @since 1.0.0
 */
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    /**
     * Preference keys for DataStore.
     * Using a companion object ensures keys are created only once.
     */
    private companion object {
        val KEY_AI_MODE = stringPreferencesKey("ai_mode")
        val KEY_API_KEY = stringPreferencesKey("api_key")
        val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        val KEY_TOP_K = intPreferencesKey("top_k")
        val KEY_TOP_P = floatPreferencesKey("top_p")
        val KEY_MAX_OUTPUT_TOKENS = intPreferencesKey("max_output_tokens")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    /**
     * Observes the current AI configuration.
     *
     * The Flow emits the complete configuration whenever any part changes.
     * If no configuration is stored, emits the default configuration.
     *
     * Errors during reading are caught and result in the default configuration
     * being emitted, ensuring the app remains functional even if DataStore fails.
     *
     * @return Flow emitting current AI configuration
     */
    override fun observeAiConfiguration(): Flow<AiConfiguration> {
        return context.dataStore.data
            .catch { exception ->
                // If DataStore fails to read, emit empty preferences
                // This ensures the app doesn't crash and uses defaults
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                // Read all values from DataStore with fallback to defaults
                // Note: API key is always null - Firebase Functions handles authentication
                val entity = AiConfigurationEntity(
                    aiModeValue = preferences[KEY_AI_MODE]
                        ?: AiConfigurationEntity.MODE_ONLINE,
                    apiKeyValue = null, // Always null - Firebase Functions proxy handles auth
                    temperature = preferences[KEY_TEMPERATURE] ?: 0.7f,
                    topK = preferences[KEY_TOP_K] ?: 40,
                    topP = preferences[KEY_TOP_P] ?: 0.95f,
                    maxOutputTokens = preferences[KEY_MAX_OUTPUT_TOKENS] ?: 2048
                )

                // Convert to domain model
                // If conversion fails, use default configuration
                try {
                    AiConfigurationMapper.toDomain(entity)
                } catch (e: IllegalArgumentException) {
                    // Log error in production
                    // Return default configuration
                    AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = null,
                        modelParameters = com.novachat.feature.ai.domain.model.ModelParameters.DEFAULT
                    )
                }
            }
    }

    /**
     * Updates the AI configuration.
     *
     * Validates the configuration before persisting. All fields are updated
     * atomically in a single DataStore edit operation.
     *
     * @param configuration The new configuration to save
     * @return Result.success if saved successfully, Result.failure on error
     */
    override suspend fun updateAiConfiguration(
        configuration: AiConfiguration
    ): Result<Unit> {
        return try {
            // Validate before saving
            val validationResult = configuration.validate()
            if (validationResult.isFailure) {
                return Result.failure(
                    validationResult.exceptionOrNull()
                        ?: IllegalArgumentException("Invalid configuration")
                )
            }

            // Convert to entity
            val entity = AiConfigurationMapper.toEntity(configuration)

            // Atomic update of all preferences
            // Note: API key is always cleared - Firebase Functions handles authentication
            context.dataStore.edit { preferences ->
                preferences[KEY_AI_MODE] = entity.aiModeValue
                preferences.remove(KEY_API_KEY) // Always clear - not used with Firebase proxy
                preferences[KEY_TEMPERATURE] = entity.temperature
                preferences[KEY_TOP_K] = entity.topK
                preferences[KEY_TOP_P] = entity.topP
                preferences[KEY_MAX_OUTPUT_TOKENS] = entity.maxOutputTokens
            }

            Result.success(Unit)

        } catch (e: IOException) {
            Result.failure(IOException("Failed to save configuration", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates only the AI mode, preserving other configuration.
     *
     * This is a convenience method that reads the current configuration,
     * updates only the mode, and saves it back.
     *
     * @param mode The new AI mode
     * @return Result.success if saved successfully, Result.failure on error
     */
    override suspend fun updateAiMode(mode: AiMode): Result<Unit> {
        return try {
            val modeValue = when (mode) {
                AiMode.ONLINE -> AiConfigurationEntity.MODE_ONLINE
                AiMode.OFFLINE -> AiConfigurationEntity.MODE_OFFLINE
            }

            context.dataStore.edit { preferences ->
                preferences[KEY_AI_MODE] = modeValue
            }

            Result.success(Unit)

        } catch (e: IOException) {
            Result.failure(IOException("Failed to save AI mode", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates only the API key, preserving other configuration.
     *
     * **Deprecated**: API keys are not used with Firebase Functions proxy.
     * This method always clears the API key for backward compatibility.
     *
     * @param apiKey The new API key (ignored - always cleared)
     * @return Result.success if saved successfully, Result.failure on error
     */
    override suspend fun updateApiKey(apiKey: ApiKey?): Result<Unit> {
        return try {
            // Always clear API key - Firebase Functions handles authentication
            context.dataStore.edit { preferences ->
                preferences.remove(KEY_API_KEY)
            }

            Result.success(Unit)

        } catch (e: IOException) {
            Result.failure(IOException("Failed to clear API key", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears all stored preferences, resetting to defaults.
     *
     * This operation removes all keys from DataStore, causing the
     * configuration Flow to emit the default configuration.
     *
     * @return Result.success if cleared successfully, Result.failure on error
     */
    override suspend fun clearAll(): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences.clear()
            }

            Result.success(Unit)

        } catch (e: IOException) {
            Result.failure(IOException("Failed to clear preferences", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeThemePreferences(): Flow<ThemePreferences> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val themeModeValue = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
                val themeMode = try {
                    ThemeMode.valueOf(themeModeValue)
                } catch (e: IllegalArgumentException) {
                    ThemeMode.SYSTEM
                }
                val dynamicColor = preferences[KEY_DYNAMIC_COLOR] ?: true
                ThemePreferences(themeMode = themeMode, dynamicColor = dynamicColor)
            }
    }

    override suspend fun updateThemePreferences(preferences: ThemePreferences): Result<Unit> {
        return try {
            context.dataStore.edit { editPreferences ->
                editPreferences[KEY_THEME_MODE] = preferences.themeMode.name
                editPreferences[KEY_DYNAMIC_COLOR] = preferences.dynamicColor
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(IOException("Failed to save theme preferences", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
