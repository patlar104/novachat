package com.novachat.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novachat.app.R
import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.model.ThemeMode
import com.novachat.app.domain.model.ThemePreferences
import com.novachat.app.presentation.model.SettingsUiEvent
import com.novachat.app.presentation.model.SettingsUiState
import com.novachat.app.presentation.model.UiEffect
import com.novachat.app.presentation.viewmodel.SettingsViewModel
import com.novachat.app.presentation.viewmodel.ThemeViewModel
import com.novachat.app.ui.theme.NovaChatTheme

/**
 * Settings screen composable using the new architecture.
 *
 * Demonstrates:
 * - Observing UI state with sealed classes
 * - Handling UI effects
 * - Form validation
 * - Configuration management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = effect.actionLabel,
                        duration = SnackbarDuration.Long
                    )
                }
                is UiEffect.NavigateBack -> onNavigateBack()
                else -> {}
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(SettingsUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is SettingsUiState.Initial -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is SettingsUiState.Success -> {
                val themePrefs by themeViewModel.themePreferences.collectAsStateWithLifecycle(
                    ThemePreferences.DEFAULT
                )
                SettingsScreenContent(
                    configuration = state.configuration,
                    onChangeAiMode = { viewModel.onEvent(SettingsUiEvent.ChangeAiMode(it)) },
                    themePrefs = themePrefs,
                    onThemeModeChange = { themeViewModel.setThemeMode(it) },
                    onDynamicColorChange = { themeViewModel.setDynamicColor(it) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            
            is SettingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "❌",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSection(
    themePrefs: ThemePreferences,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit
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
fun SettingsScreenContent(
    configuration: AiConfiguration,
    onChangeAiMode: (AiMode) -> Unit,
    themePrefs: ThemePreferences,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

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

        // AI Mode Selection
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
                
                // Online Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChangeAiMode(AiMode.ONLINE) } // Fixed: Increased touch target
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
                
                // Offline Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChangeAiMode(AiMode.OFFLINE) } // Fixed: Increased touch target
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = configuration.mode == AiMode.OFFLINE,
                        onClick = { onChangeAiMode(AiMode.OFFLINE) }
                    )
                    Text(
                        text = stringResource(R.string.offline_mode),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Text(
                    text = stringResource(R.string.note_offline_mode), // Fixed: Hardcoded string
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Online Mode Info Card
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
        
        // Information Card
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
                    text = "About NovaChat", // Assuming this is intentional for 'About'
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "NovaChat is an AI chatbot assistant that supports both online (Google Gemini) and offline (on-device) AI models.", // Assuming this is intentional for 'About'
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Version 1.0 - Built with 2026 Android best practices", // Assuming this is intentional for 'About'
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// ---------------------- Previews ----------------------

private val mockOnlineConfig = AiConfiguration(
    mode = AiMode.ONLINE,
    apiKey = null, // API key not required - Firebase Functions handles it
)

private val mockOfflineConfig = AiConfiguration(
    mode = AiMode.OFFLINE,
    apiKey = null
)

@Preview(showBackground = true, name = "Settings - Online Mode")
@Composable
fun SettingsScreenContentOnlinePreview() {
    NovaChatTheme {
        SettingsScreenContent(
            configuration = mockOnlineConfig,
            onChangeAiMode = {},
            themePrefs = ThemePreferences.DEFAULT,
            onThemeModeChange = {},
            onDynamicColorChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings - Offline Mode")
@Composable
fun SettingsScreenContentOfflinePreview() {
    NovaChatTheme {
        SettingsScreenContent(
            configuration = mockOfflineConfig,
            onChangeAiMode = {},
            themePrefs = ThemePreferences.DEFAULT,
            onThemeModeChange = {},
            onDynamicColorChange = {}
        )
    }
}
