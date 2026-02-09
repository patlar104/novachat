package com.novachat.feature.ai.domain.model

/**
 * User preferences for app theme and appearance.
 *
 * @property themeMode Whether to use system, light, or dark theme
 * @property dynamicColor Whether to use Material You dynamic colors (Android 12+)
 *
 * @since 1.0.0
 */
data class ThemePreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true
) {
    companion object {
        val DEFAULT = ThemePreferences()
    }
}

/**
 * Theme mode preference for light/dark appearance.
 *
 * @since 1.0.0
 */
enum class ThemeMode {
    /** Follow system light/dark setting */
    SYSTEM,

    /** Always use light theme */
    LIGHT,

    /** Always use dark theme */
    DARK
}
