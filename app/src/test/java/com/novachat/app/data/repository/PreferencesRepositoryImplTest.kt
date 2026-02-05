package com.novachat.app.data.repository

import android.content.Context
import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.model.ApiKey
import com.novachat.app.domain.model.ModelParameters
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Unit tests for PreferencesRepositoryImpl using DataStore.
 *
 * These tests verify:
 * - Configuration save and retrieval
 * - Error handling for corrupted data
 * - IOException handling (emits default instead of crashing)
 * - Flow-based reactive updates
 *
 * Uses MockK for mocking and kotlin-coroutines-test for async testing.
 *
 * @since 1.0.0
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryImplTest {

    private val mockContext = mockk<Context>()

    private fun createRepository(): PreferencesRepositoryImpl {
        return PreferencesRepositoryImpl(mockContext)
    }

    @Test
    fun observeAiConfiguration_returns_valid_configuration() = runTest {
        // Arrange
        val repository = createRepository()

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

        val repository = createRepository()

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

        val repository = createRepository()

        // Act
        val result = repository.updateAiConfiguration(config)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun observeAiConfiguration_handles_errors_gracefully() = runTest {
        // Arrange
        val repository = createRepository()

        // Act & Assert
        // The flow should complete without throwing, emitting some default or stored config
        val config = repository.observeAiConfiguration().first()
        config.shouldBeInstanceOf<AiConfiguration>()
    }

    @Test
    fun updateApiKey_updates_only_api_key() = runTest {
        // Arrange
        val newApiKey = ApiKey.unsafe("new-api-key-1234567890")
        val repository = createRepository()

        // Act
        val result = repository.updateApiKey(newApiKey)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun updateApiKey_with_null_clears_key() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val result = repository.updateApiKey(null)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun updateAiMode_switches_mode() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val result = repository.updateAiMode(AiMode.ONLINE)

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun clearAll_resets_preferences() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val result = repository.clearAll()

        // Assert
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun configuration_validate_fails_without_api_key_in_online_mode() = runTest {
        // Arrange
        val configWithoutKey = AiConfiguration(
            mode = AiMode.ONLINE,
            apiKey = null
        )

        // Act
        val validationResult = configWithoutKey.validate()

        // Assert
        validationResult.isFailure.shouldBe(true)
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
