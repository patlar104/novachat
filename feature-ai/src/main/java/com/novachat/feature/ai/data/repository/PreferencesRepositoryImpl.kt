package com.novachat.feature.ai.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.novachat.feature.ai.data.mapper.AiConfigurationMapper
import com.novachat.feature.ai.data.model.AiConfigurationEntity
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.ModelParameters
import com.novachat.feature.ai.domain.model.ThemeMode
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "novachat_preferences"
)

class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    private companion object {
        val KEY_AI_MODE = stringPreferencesKey("ai_mode")
        val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        val KEY_TOP_K = intPreferencesKey("top_k")
        val KEY_TOP_P = floatPreferencesKey("top_p")
        val KEY_MAX_OUTPUT_TOKENS = intPreferencesKey("max_output_tokens")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_WAIT_FOR_DEBUGGER_NEXT_LAUNCH = booleanPreferencesKey("wait_for_debugger_next_launch")
    }

    override fun observeAiConfiguration(): Flow<AiConfiguration> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val entity = AiConfigurationEntity(
                    aiModeValue = preferences[KEY_AI_MODE]
                        ?: AiConfigurationEntity.MODE_ONLINE,
                    temperature = preferences[KEY_TEMPERATURE] ?: 0.7f,
                    topK = preferences[KEY_TOP_K] ?: 40,
                    topP = preferences[KEY_TOP_P] ?: 0.95f,
                    maxOutputTokens = preferences[KEY_MAX_OUTPUT_TOKENS] ?: 2048
                )

                try {
                    AiConfigurationMapper.toDomain(entity)
                } catch (_: IllegalArgumentException) {
                    AiConfiguration(
                        mode = AiMode.ONLINE,
                        modelParameters = ModelParameters.DEFAULT
                    )
                }
            }
    }

    override suspend fun updateAiConfiguration(
        configuration: AiConfiguration
    ): Result<Unit> {
        return try {
            val validationResult = configuration.validate()
            if (validationResult.isFailure) {
                return Result.failure(
                    validationResult.exceptionOrNull()
                        ?: IllegalArgumentException("Invalid configuration")
                )
            }

            val entity = AiConfigurationMapper.toEntity(configuration)

            context.dataStore.edit { preferences ->
                preferences[KEY_AI_MODE] = entity.aiModeValue
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
                } catch (_: IllegalArgumentException) {
                    ThemeMode.SYSTEM
                }

                ThemePreferences(
                    themeMode = themeMode,
                    dynamicColor = preferences[KEY_DYNAMIC_COLOR] ?: true
                )
            }
    }

    override suspend fun updateThemePreferences(preferences: ThemePreferences): Result<Unit> {
        return try {
            context.dataStore.edit { datastorePrefs ->
                datastorePrefs[KEY_THEME_MODE] = preferences.themeMode.name
                datastorePrefs[KEY_DYNAMIC_COLOR] = preferences.dynamicColor
            }

            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(IOException("Failed to save theme preferences", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeWaitForDebuggerOnNextLaunch(): Flow<Boolean> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(androidx.datastore.preferences.core.emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences[KEY_WAIT_FOR_DEBUGGER_NEXT_LAUNCH] ?: false
            }
    }

    override suspend fun setWaitForDebuggerOnNextLaunch(enabled: Boolean): Result<Unit> {
        return try {
            context.dataStore.edit { preferences ->
                preferences[KEY_WAIT_FOR_DEBUGGER_NEXT_LAUNCH] = enabled
            }

            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(IOException("Failed to persist debugger wait preference", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun consumeWaitForDebuggerOnNextLaunch(): Result<Boolean> {
        return try {
            var shouldWait = false
            context.dataStore.edit { preferences ->
                shouldWait = preferences[KEY_WAIT_FOR_DEBUGGER_NEXT_LAUNCH] ?: false
                preferences[KEY_WAIT_FOR_DEBUGGER_NEXT_LAUNCH] = false
            }

            Result.success(shouldWait)
        } catch (e: IOException) {
            Result.failure(IOException("Failed to consume debugger wait preference", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
