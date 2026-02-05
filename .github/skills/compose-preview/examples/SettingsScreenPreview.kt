package com.novachat.app.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.presentation.model.SettingsUiState
import com.novachat.app.ui.previews.createPreviewSettingsViewModel
import com.novachat.app.ui.theme.NovaChatTheme

/**
 * Preview composables for the SettingsScreen.
 *
 * This file demonstrates previewing a different screen type with:
 * - Form states (loading, success, error)
 * - Configuration states (online mode, settings saved, etc.)
 * - Theme and device variations
 * - Component-level previews of form elements
 *
 * @see SettingsScreen
 * @see com.novachat.app.ui.previews.createPreviewSettingsViewModel
 */

// ============================================================
// SETTINGS SCREEN - UI STATE PREVIEWS
// ============================================================

@Preview(
    name = "Initial - Loading",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenInitialPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Initial
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Loading - Progress Spinner",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenLoadingPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Loading
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Success - Online Mode",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenSuccessOnlineModePreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                ),
                draftApiKey = "sk-proj-abc123def456-example-key"
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Success - Offline Mode",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenSuccessOfflineModePreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.OFFLINE,
                        apiKey = null  // Offline mode doesn't need API key
                    )
                ),
                draftApiKey = ""
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Success - Save Success Message",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenSuccessSavedPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                ),
                draftApiKey = "sk-proj-abc123def456-example-key",
                showSaveSuccess = true  // Show "Settings saved" message
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Success - Empty API Key",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenSuccessEmptyApiKeyPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = null
                    )
                ),
                draftApiKey = ""
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Error - Load Failed",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenErrorPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Error(
                    message = "Failed to load settings. Please try again.",
                    isRecoverable = true
                )
            ),
            onNavigateBack = {}
        )
    }
}

// ============================================================
// SETTINGS SCREEN - DEVICE PREVIEWS
// ============================================================

@Preview(
    name = "Standard Phone (412dp)",
    device = Devices.PIXEL_6,
    group = "SettingsScreen/Devices",
    showSystemUi = true
)
@Composable
fun SettingsScreenPhonePreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Compact Phone (320dp)",
    device = "spec:width=320dp,height=640dp,dpi=420",
    group = "SettingsScreen/Devices",
    showSystemUi = true
)
@Composable
fun SettingsScreenCompactPhonePreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Large Phone (480dp)",
    device = Devices.PIXEL_7_PRO,
    group = "SettingsScreen/Devices",
    showSystemUi = true
)
@Composable
fun SettingsScreenLargePhonePreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Tablet (600dp)",
    device = "spec:width=600dp,height=800dp,dpi=160",
    group = "SettingsScreen/Devices",
    showSystemUi = true
)
@Composable
fun SettingsScreenTabletPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

// ============================================================
// SETTINGS SCREEN - THEME PREVIEWS
// ============================================================

@Preview(
    name = "Light Mode",
    group = "SettingsScreen/Themes",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SettingsScreenLightThemePreview() {
    NovaChatTheme(darkTheme = false) {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Dark Mode",
    group = "SettingsScreen/Themes",
    showBackground = true,
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettingsScreenDarkThemePreview() {
    NovaChatTheme(darkTheme = true) {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

// ============================================================
// SETTINGS SCREEN - ACCESSIBILITY PREVIEWS
// ============================================================

@Preview(
    name = "Font Scale Normal (1x)",
    group = "SettingsScreen/Accessibility",
    fontScale = 1f,
    showBackground = true
)
@Composable
fun SettingsScreenNormalFontPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Font Scale Large (1.5x)",
    group = "SettingsScreen/Accessibility",
    fontScale = 1.5f,
    showBackground = true
)
@Composable
fun SettingsScreenLargeFontPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-abc123def456-example-key"
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(
    name = "Font Scale Extra Large (2x)",
    group = "SettingsScreen/Accessibility",
    fontScale = 2f,
    showBackground = true
)
@Composable
fun SettingsScreenExtraLargeFontPreview() {
    NovaChatTheme {
        SettingsScreen(
            viewModel = createPreviewSettingsViewModel(
                initialState = SettingsUiState.Success(
                    configuration = AiConfiguration(
                        mode = AiMode.ONLINE,
                        apiKey = "sk-proj-key"  // Shorter key for space
                    )
                )
            ),
            onNavigateBack = {}
        )
    }
}
