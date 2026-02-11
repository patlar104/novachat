package com.novachat.app.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.novachat.app.domain.model.AiMode
import com.novachat.app.presentation.model.SettingsUiState
import com.novachat.app.ui.previews.PreviewSettingsScreenData
import com.novachat.app.ui.theme.NovaChatTheme

/**
 * Preview composables for the Settings screen pattern.
 *
 * This file demonstrates ViewModel-free previews using a stateless
 * SettingsScreenPreviewSurface and sample UI states.
 */

@Composable
private fun SettingsScreenPreviewSurface(
    uiState: SettingsUiState,
    draftApiKey: String = "",
    showSaveSuccess: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is SettingsUiState.Initial,
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            is SettingsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modeLabel = when (uiState.configuration.mode) {
                                AiMode.ONLINE -> "Online"
                                AiMode.OFFLINE -> "Offline (unavailable)"
                            }

                            Text(
                                text = "AI mode: $modeLabel",
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (uiState.configuration.mode == AiMode.OFFLINE) {
                                Text(
                                    text = "Offline mode requires AICore (not available).",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (uiState.configuration.mode == AiMode.ONLINE) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "API key",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                OutlinedTextField(
                                    value = draftApiKey,
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    label = { Text("Enter API key") }
                                )

                                Button(
                                    onClick = {},
                                    enabled = draftApiKey.isNotBlank(),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Save")
                                }

                                if (showSaveSuccess) {
                                    Text(
                                        text = "API key saved",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is SettingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.initialState())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.loadingState())
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
        SettingsScreenPreviewSurface(
            uiState = PreviewSettingsScreenData.successOnline(),
            draftApiKey = "sk-proj-abc123def456-example-key"
        )
    }
}

@Preview(
    name = "Success - Offline Mode (Unavailable)",
    group = "SettingsScreen/States",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
fun SettingsScreenSuccessOfflineModePreview() {
    NovaChatTheme {
        SettingsScreenPreviewSurface(
            uiState = PreviewSettingsScreenData.successOfflineUnavailable()
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
        SettingsScreenPreviewSurface(
            uiState = PreviewSettingsScreenData.successOnline(),
            draftApiKey = "sk-proj-abc123def456-example-key",
            showSaveSuccess = true
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
        SettingsScreenPreviewSurface(
            uiState = PreviewSettingsScreenData.successOnlineMissingKey(),
            draftApiKey = ""
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.errorRecoverable())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(uiState = PreviewSettingsScreenData.successOnline())
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
        SettingsScreenPreviewSurface(
            uiState = PreviewSettingsScreenData.successOnline(),
            draftApiKey = "sk-proj-key"
        )
    }
}
