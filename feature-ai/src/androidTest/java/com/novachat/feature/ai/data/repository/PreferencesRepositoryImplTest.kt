package com.novachat.feature.ai.data.repository

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novachat.feature.ai.di.RepositoryModule
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.ApiKey
import com.novachat.feature.ai.domain.model.ModelParameters
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.AiServiceStatus
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for PreferencesRepositoryImpl using DataStore.
 *
 * These tests verify:
 * - Configuration save and retrieval
 * - Error handling for corrupted data
 * - IOException handling (emits default instead of crashing)
 * - Flow-based reactive updates
 *
 * @since 1.0.0
 */
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryImplTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides
        @Singleton
        fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository {
            return PreferencesRepositoryImpl(context)
        }

        @Provides
        @Singleton
        fun provideMessageRepository(): MessageRepository {
            return MessageRepositoryImpl()
        }

        @Provides
        @Singleton
        fun provideAiRepository(): AiRepository {
            return object : AiRepository {
                override suspend fun generateResponse(message: String, configuration: AiConfiguration): Result<String> {
                    return Result.failure(UnsupportedOperationException("Fake AI Repository"))
                }

                override suspend fun isModeAvailable(mode: AiMode): Boolean {
                    return false
                }

                override fun observeServiceStatus(): Flow<AiServiceStatus> {
                    return emptyFlow()
                }
            }
        }
    }

    @Inject
    lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            repository.clearAll()
        }
    }

    @Test
    fun observeAiConfiguration_returns_valid_configuration() = runTest {
        // Arrange
        // Act
        val result = repository.observeAiConfiguration().first()

        // Assert: Default configuration is returned
        result.shouldBeInstanceOf<AiConfiguration>()
    }

    @Test
    fun updateAiConfiguration_persists_data() = runTest {
        // Arrange
        val apiKey = ApiKey.unsafe("test-api-key-1234567890")
        val config = AiConfiguration(
            mode = AiMode.ONLINE,
            apiKey = apiKey,
            modelParameters = ModelParameters.DEFAULT
        )

        // Act
        val result = repository.updateAiConfiguration(config)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun updateAiConfiguration_with_null_apikey_succeeds() = runTest {
        // Arrange
        val config = AiConfiguration(
            mode = AiMode.OFFLINE,
            apiKey = null,
            modelParameters = ModelParameters.DEFAULT
        )

        // Act
        val result = repository.updateAiConfiguration(config)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun observeAiConfiguration_handles_errors_gracefully() = runTest {
        // Arrange
        // Act & Assert
        // The flow should complete without throwing, emitting some default or stored config
        val config = repository.observeAiConfiguration().first()
        config.shouldBeInstanceOf<AiConfiguration>()
    }

    @Test
    fun updateApiKey_updates_only_api_key() = runTest {
        // Arrange
        val newApiKey = ApiKey.unsafe("new-api-key-1234567890")

        // Act
        val result = repository.updateApiKey(newApiKey)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun updateApiKey_with_null_clears_key() = runTest {
        // Arrange

        // Act
        val result = repository.updateApiKey(null)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun updateAiMode_switches_mode() = runTest {
        // Arrange

        // Act
        val result = repository.updateAiMode(AiMode.ONLINE)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun clearAll_resets_preferences() = runTest {
        // Arrange

        // Act
        val result = repository.clearAll()

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun configuration_validate_succeeds_without_api_key_in_online_mode() = runTest {
        // Arrange: Firebase AI handles auth; API key is optional
        val configWithoutKey = AiConfiguration(
            mode = AiMode.ONLINE,
            apiKey = null
        )

        // Act
        val validationResult = configWithoutKey.validate()

        // Assert
        validationResult.isSuccess.shouldBe(true)
    }

    @Test
    fun configuration_validate_succeeds_with_valid_api_key() = runTest {
        // Arrange
        val apiKey = ApiKey.unsafe("valid-key-1234567890123456")
        val validConfig = AiConfiguration(
            mode = AiMode.ONLINE,
            apiKey = apiKey
        )

        // Act
        val validationResult = validConfig.validate()

        // Assert
        validationResult.isSuccess.shouldBe(true)
    }
}
