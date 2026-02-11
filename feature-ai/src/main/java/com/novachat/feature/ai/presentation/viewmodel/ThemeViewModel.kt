package com.novachat.feature.ai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novachat.feature.ai.domain.model.ThemeMode
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.domain.usecase.ObserveThemePreferencesUseCase
import com.novachat.feature.ai.domain.usecase.UpdateThemePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for app-level theme preferences.
 *
 * Observes and updates theme mode (system/light/dark) and dynamic color setting.
 *
 * @since 1.0.0
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val observeThemePreferencesUseCase: ObserveThemePreferencesUseCase,
    private val updateThemePreferencesUseCase: UpdateThemePreferencesUseCase
) : ViewModel() {

    private val _themePreferences = MutableStateFlow(ThemePreferences.DEFAULT)
    val themePreferences: StateFlow<ThemePreferences> = _themePreferences.asStateFlow()

    init {
        viewModelScope.launch {
            observeThemePreferencesUseCase()
                .catch {
                    _themePreferences.value = ThemePreferences.DEFAULT
                }
                .collect { prefs ->
                    _themePreferences.value = prefs
                }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val updated = _themePreferences.value.copy(themeMode = mode)
            updateThemePreferencesUseCase(updated)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _themePreferences.value.copy(dynamicColor = enabled)
            updateThemePreferencesUseCase(updated)
        }
    }
}
