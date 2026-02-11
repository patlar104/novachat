package com.novachat.feature.ai.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.novachat.feature.ai.BuildConfig
import com.novachat.feature.ai.R
import com.novachat.feature.ai.domain.model.AiConfiguration
import com.novachat.feature.ai.domain.model.AiMode
import com.novachat.feature.ai.domain.model.OfflineCapability
import com.novachat.feature.ai.domain.model.ThemeMode
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.ui.theme.NovaChatTheme

@Composable
private fun ThemeSection(
    themePrefs: ThemePreferences,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.appearance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.theme_mode),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ThemeMode.SYSTEM to R.string.theme_system,
                    ThemeMode.LIGHT to R.string.theme_light,
                    ThemeMode.DARK to R.string.theme_dark
                ).forEach { (mode, labelRes) ->
                    FilterChip(
                        selected = themePrefs.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dynamic_color),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.dynamic_color_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = themePrefs.dynamicColor,
                    onCheckedChange = onDynamicColorChange
                )
            }
        }
    }
}

@Composable
private fun DeveloperOptionsSection(
    waitForDebuggerOnNextLaunch: Boolean,
    onToggleWaitForDebuggerOnNextLaunch: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.developer_options),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.wait_for_debugger_next_launch),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.wait_for_debugger_next_launch_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = waitForDebuggerOnNextLaunch,
                    onCheckedChange = onToggleWaitForDebuggerOnNextLaunch
                )
            }
        }
    }
}

@Composable
fun SettingsScreenContent(
    configuration: AiConfiguration,
    offlineCapability: OfflineCapability,
    waitForDebuggerOnNextLaunch: Boolean,
    onChangeAiMode: (AiMode) -> Unit,
    onToggleWaitForDebuggerOnNextLaunch: (Boolean) -> Unit,
    themePrefs: ThemePreferences,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val offlineEnabled = offlineCapability is OfflineCapability.Available
    val offlineReason = (offlineCapability as? OfflineCapability.Unavailable)?.reason

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ThemeSection(
            themePrefs = themePrefs,
            onThemeModeChange = onThemeModeChange,
            onDynamicColorChange = onDynamicColorChange
        )

        if (BuildConfig.DEBUG) {
            DeveloperOptionsSection(
                waitForDebuggerOnNextLaunch = waitForDebuggerOnNextLaunch,
                onToggleWaitForDebuggerOnNextLaunch = onToggleWaitForDebuggerOnNextLaunch,
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.ai_mode),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChangeAiMode(AiMode.ONLINE) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = configuration.mode == AiMode.ONLINE,
                        onClick = { onChangeAiMode(AiMode.ONLINE) }
                    )
                    Text(
                        text = stringResource(R.string.online_mode),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = offlineEnabled) { onChangeAiMode(AiMode.OFFLINE) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = configuration.mode == AiMode.OFFLINE,
                        onClick = { onChangeAiMode(AiMode.OFFLINE) },
                        enabled = offlineEnabled
                    )
                    Text(
                        text = stringResource(R.string.offline_mode),
                        modifier = Modifier.padding(start = 8.dp),
                        color = if (offlineEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )
                }

                if (offlineReason != null) {
                    Text(
                        text = offlineReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = stringResource(R.string.note_offline_mode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (configuration.mode == AiMode.ONLINE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.online_mode_info_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.online_mode_info_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "About NovaChat",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "NovaChat is an AI chatbot assistant that supports online and scaffolded offline AI modes.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Version 1.0 - Built with 2026 Android best practices",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private val mockOnlineConfig = AiConfiguration(
    mode = AiMode.ONLINE
)

private val mockOfflineConfig = AiConfiguration(
    mode = AiMode.OFFLINE
)

@Preview(showBackground = true, name = "Settings - Online Mode")
@Composable
fun SettingsScreenContentOnlinePreview() {
    NovaChatTheme {
        SettingsScreenContent(
            configuration = mockOnlineConfig,
            offlineCapability = OfflineCapability.Unavailable("Offline model not installed"),
            waitForDebuggerOnNextLaunch = false,
            onChangeAiMode = {},
            onToggleWaitForDebuggerOnNextLaunch = {},
            themePrefs = ThemePreferences.DEFAULT,
            onThemeModeChange = {},
            onDynamicColorChange = {},
        )
    }
}

@Preview(showBackground = true, name = "Settings - Offline Mode")
@Composable
fun SettingsScreenContentOfflinePreview() {
    NovaChatTheme {
        SettingsScreenContent(
            configuration = mockOfflineConfig,
            offlineCapability = OfflineCapability.Available,
            waitForDebuggerOnNextLaunch = false,
            onChangeAiMode = {},
            onToggleWaitForDebuggerOnNextLaunch = {},
            themePrefs = ThemePreferences.DEFAULT,
            onThemeModeChange = {},
            onDynamicColorChange = {},
        )
    }
}
