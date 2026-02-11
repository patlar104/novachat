package com.novachat.feature.ai.data.repository

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novachat.feature.ai.di.RepositoryModule
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.ModelParameters
import com.novachat.feature.ai.domain.model.OfflineCapability
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
                override suspend fun generateResponse(
                    message: String,
                    configuration: AiConfiguration
                ): Result<String> {
                    return Result.failure(UnsupportedOperationException("Fake AI Repository"))
                }

                override suspend fun isModeAvailable(mode: AiMode): Boolean {
                    return false
                }

                override fun observeServiceStatus(): Flow<AiServiceStatus> {
                    return emptyFlow()
                }

                override fun observeOfflineCapability(): Flow<OfflineCapability> {
                    return flowOf(OfflineCapability.Unavailable("Unavailable in fake"))
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
        val result = repository.observeAiConfiguration().first()
        result.shouldBeInstanceOf<AiConfiguration>()
    }

    @Test
    fun updateAiConfiguration_persists_data() = runTest {
        val config = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        val result = repository.updateAiConfiguration(config)

        result.isSuccess.shouldBe(true)
    }

    @Test
    fun updateAiConfiguration_with_online_mode_succeeds() = runTest {
        val config = AiConfiguration(
            mode = AiMode.OFFLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        val result = repository.updateAiConfiguration(config)

        result.isSuccess.shouldBe(true)
    }

    @Test
    fun observeAiConfiguration_handles_errors_gracefully() = runTest {
        val config = repository.observeAiConfiguration().first()
        config.shouldBeInstanceOf<AiConfiguration>()
    }

    @Test
    fun updateAiMode_switches_mode() = runTest {
        val result = repository.updateAiMode(AiMode.ONLINE)
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun clearAll_resets_preferences() = runTest {
        val result = repository.clearAll()
        result.isSuccess.shouldBe(true)
    }

    @Test
    fun configuration_validate_succeeds_without_api_key_in_online_mode() = runTest {
        val configWithoutKey = AiConfiguration(mode = AiMode.ONLINE)
        val validationResult = configWithoutKey.validate()
        validationResult.isSuccess.shouldBe(true)
    }
}
