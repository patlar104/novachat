package com.novachat.feature.ai.presentation.viewmodel

import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.ModelParameters
import com.novachat.feature.ai.domain.model.OfflineCapability
import com.novachat.feature.ai.domain.usecase.ObserveAiConfigurationUseCase
import com.novachat.feature.ai.domain.usecase.ObserveAiModeAvailabilityUseCase
import com.novachat.feature.ai.domain.usecase.ObserveWaitForDebuggerOnNextLaunchUseCase
import com.novachat.feature.ai.domain.usecase.SetWaitForDebuggerOnNextLaunchUseCase
import com.novachat.feature.ai.domain.usecase.UpdateAiConfigurationUseCase
import com.novachat.feature.ai.presentation.model.SettingsUiEvent
import com.novachat.feature.ai.presentation.model.SettingsUiState
import com.novachat.feature.ai.presentation.model.UiEffect
import com.novachat.feature.ai.testutil.MainDispatcherExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    private val mockObserveAiConfigurationUseCase = mockk<ObserveAiConfigurationUseCase>()
    private val mockObserveAiModeAvailabilityUseCase = mockk<ObserveAiModeAvailabilityUseCase>()
    private val mockObserveWaitForDebuggerOnNextLaunchUseCase =
        mockk<ObserveWaitForDebuggerOnNextLaunchUseCase>()
    private val mockSetWaitForDebuggerOnNextLaunchUseCase =
        mockk<SetWaitForDebuggerOnNextLaunchUseCase>()
    private val mockUpdateAiConfigurationUseCase = mockk<UpdateAiConfigurationUseCase>()

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            observeAiConfigurationUseCase = mockObserveAiConfigurationUseCase,
            observeAiModeAvailabilityUseCase = mockObserveAiModeAvailabilityUseCase,
            observeWaitForDebuggerOnNextLaunchUseCase = mockObserveWaitForDebuggerOnNextLaunchUseCase,
            setWaitForDebuggerOnNextLaunchUseCase = mockSetWaitForDebuggerOnNextLaunchUseCase,
            updateAiConfigurationUseCase = mockUpdateAiConfigurationUseCase,
        )
    }

    @Test
    fun init_includes_wait_for_debugger_flag_in_success_state() = runTest {
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT,
        )
        coEvery { mockObserveAiConfigurationUseCase() } returns flowOf(configuration)
        coEvery { mockObserveAiModeAvailabilityUseCase() } returns flowOf(OfflineCapability.Available)
        coEvery { mockObserveWaitForDebuggerOnNextLaunchUseCase() } returns flowOf(true)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<SettingsUiState.Success>()
        (state as SettingsUiState.Success).waitForDebuggerOnNextLaunch.shouldBe(true)
    }

    @Test
    fun toggle_wait_for_debugger_event_calls_set_use_case() = runTest {
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT,
        )
        coEvery { mockObserveAiConfigurationUseCase() } returns flowOf(configuration)
        coEvery { mockObserveAiModeAvailabilityUseCase() } returns flowOf(OfflineCapability.Available)
        coEvery { mockObserveWaitForDebuggerOnNextLaunchUseCase() } returns flowOf(false)
        coEvery { mockSetWaitForDebuggerOnNextLaunchUseCase(true) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.ToggleWaitForDebuggerOnNextLaunch(true))
        advanceUntilIdle()

        coVerify { mockSetWaitForDebuggerOnNextLaunchUseCase(true) }
    }

    @Test
    fun enabling_wait_for_debugger_emits_relaunch_guidance() = runTest {
        val configuration = AiConfiguration(
            mode = AiMode.ONLINE,
            modelParameters = ModelParameters.DEFAULT,
        )
        coEvery { mockObserveAiConfigurationUseCase() } returns flowOf(configuration)
        coEvery { mockObserveAiModeAvailabilityUseCase() } returns flowOf(OfflineCapability.Available)
        coEvery { mockObserveWaitForDebuggerOnNextLaunchUseCase() } returns flowOf(false)
        coEvery { mockSetWaitForDebuggerOnNextLaunchUseCase(true) } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val effectDeferred = async { viewModel.uiEffect.first() }
        viewModel.onEvent(SettingsUiEvent.ToggleWaitForDebuggerOnNextLaunch(true))
        advanceUntilIdle()

        val effect = effectDeferred.await()
        effect.shouldBeInstanceOf<UiEffect.ShowSnackbar>()
        (effect as UiEffect.ShowSnackbar).message.contains("Close the app and relaunch with Debug")
            .shouldBe(true)
    }
}
