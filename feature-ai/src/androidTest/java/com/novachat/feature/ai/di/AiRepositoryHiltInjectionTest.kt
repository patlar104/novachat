package com.novachat.feature.ai.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.Message
import com.novachat.feature.ai.domain.model.MessageId
import com.novachat.feature.ai.domain.model.ModelParameters
import com.novachat.feature.ai.domain.model.OfflineCapability
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.domain.repository.AiRepository
import com.novachat.feature.ai.domain.repository.AiServiceStatus
import com.novachat.feature.ai.domain.repository.MessageRepository
import com.novachat.feature.ai.domain.repository.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AiRepositoryHiltInjectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var aiRepository: AiRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun hilt_injects_test_ai_repository() = runTest {
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )

        val result = aiRepository.generateResponse("Hello", configuration)

        aiRepository.shouldBeInstanceOf<FakeAiRepository>()
        result.getOrNull().shouldBe("Test response")
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestAiRepositoryModule {

        @Provides
        @Singleton
        fun provideAiRepository(): AiRepository = FakeAiRepository()

        @Provides
        @Singleton
        fun provideMessageRepository(): MessageRepository = FakeMessageRepository()

        @Provides
        @Singleton
        fun providePreferencesRepository(): PreferencesRepository = FakePreferencesRepository()
    }
}

private class FakeAiRepository : AiRepository {

    private val statusFlow = MutableStateFlow<AiServiceStatus>(
        AiServiceStatus.Available(AiMode.DEFAULT_MODEL_NAME)
    )
    private val offlineCapabilityFlow = MutableStateFlow<OfflineCapability>(
        OfflineCapability.Unavailable("Offline engine unavailable")
    )

    override suspend fun generateResponse(
        message: String,
        configuration: AiConfiguration
    ): Result<String> {
        return Result.success("Test response")
    }

    override suspend fun isModeAvailable(mode: AiMode): Boolean {
        return mode == AiMode.ONLINE
    }

    override fun observeServiceStatus(): Flow<AiServiceStatus> {
        return statusFlow.asStateFlow()
    }

    override fun observeOfflineCapability(): Flow<OfflineCapability> {
        return offlineCapabilityFlow.asStateFlow()
    }
}

private class FakeMessageRepository : MessageRepository {

    private val messagesFlow = MutableStateFlow<List<Message>>(emptyList())

    override fun observeMessages(): Flow<List<Message>> = messagesFlow.asStateFlow()

    override suspend fun addMessage(message: Message): Result<Unit> {
        val updated = messagesFlow.value + message
        messagesFlow.value = updated
        return Result.success(Unit)
    }

    override suspend fun updateMessage(message: Message): Result<Unit> {
        val updated = messagesFlow.value.map { existing ->
            if (existing.id == message.id) message else existing
        }
        messagesFlow.value = updated
        return Result.success(Unit)
    }

    override suspend fun getMessage(id: MessageId): Message? {
        return messagesFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun clearAllMessages(): Result<Unit> {
        messagesFlow.value = emptyList()
        return Result.success(Unit)
    }

    override suspend fun getMessageCount(): Int {
        return messagesFlow.value.size
    }
}

private class FakePreferencesRepository : PreferencesRepository {

    private val aiConfigFlow = MutableStateFlow(
        AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )
    )
    private val themePreferencesFlow = MutableStateFlow(ThemePreferences.DEFAULT)
    private val waitForDebuggerOnNextLaunchFlow = MutableStateFlow(false)

    override fun observeAiConfiguration(): Flow<AiConfiguration> = aiConfigFlow.asStateFlow()

    override suspend fun updateAiConfiguration(configuration: AiConfiguration): Result<Unit> {
        aiConfigFlow.value = configuration
        return Result.success(Unit)
    }

    override suspend fun updateAiMode(mode: AiMode): Result<Unit> {
        aiConfigFlow.value = aiConfigFlow.value.copy(mode = mode)
        return Result.success(Unit)
    }

    override suspend fun clearAll(): Result<Unit> {
        aiConfigFlow.value = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT
        )
        themePreferencesFlow.value = ThemePreferences.DEFAULT
        waitForDebuggerOnNextLaunchFlow.value = false
        return Result.success(Unit)
    }

    override fun observeThemePreferences(): Flow<ThemePreferences> =
        themePreferencesFlow.asStateFlow()

    override suspend fun updateThemePreferences(preferences: ThemePreferences): Result<Unit> {
        themePreferencesFlow.value = preferences
        return Result.success(Unit)
    }

    override fun observeWaitForDebuggerOnNextLaunch(): Flow<Boolean> =
        waitForDebuggerOnNextLaunchFlow.asStateFlow()

    override suspend fun setWaitForDebuggerOnNextLaunch(enabled: Boolean): Result<Unit> {
        waitForDebuggerOnNextLaunchFlow.value = enabled
        return Result.success(Unit)
    }

    override suspend fun consumeWaitForDebuggerOnNextLaunch(): Result<Boolean> {
        val shouldWait = waitForDebuggerOnNextLaunchFlow.value
        waitForDebuggerOnNextLaunchFlow.value = false
        return Result.success(shouldWait)
    }
}
