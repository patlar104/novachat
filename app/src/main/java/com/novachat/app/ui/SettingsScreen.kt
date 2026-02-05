package com.novachat.app.ui

import androidx.compose.foundation.clickable // Added import for RadioButton fix
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novachat.app.R
import com.novachat.app.domain.model.AiConfiguration
import com.novachat.app.domain.model.AiMode
import com.novachat.app.domain.model.ApiKey
import com.novachat.app.presentation.model.SettingsUiEvent
import com.novachat.app.presentation.model.SettingsUiState
import com.novachat.app.presentation.model.UiEffect
import com.novachat.app.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

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
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val draftApiKey by viewModel.draftApiKey.collectAsStateWithLifecycle()
    val showSaveSuccess by viewModel.showSaveSuccess.collectAsStateWithLifecycle()
    
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
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(SettingsUiEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back) // Fixed: Hardcoded string
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                SettingsScreenContent(
                    configuration = state.configuration,
                    draftApiKey = draftApiKey,
                    onDraftApiKeyChange = { viewModel.updateDraftApiKey(it) },
                    onSaveApiKey = { viewModel.onEvent(SettingsUiEvent.SaveApiKey(draftApiKey)) },
                    onChangeAiMode = { viewModel.onEvent(SettingsUiEvent.ChangeAiMode(it)) },
                    showSaveSuccess = showSaveSuccess,
                    onDismissSaveSuccess = { viewModel.onEvent(SettingsUiEvent.DismissSaveSuccess) },
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
fun SettingsScreenContent(
    configuration: AiConfiguration,
    draftApiKey: String,
    onDraftApiKeyChange: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onChangeAiMode: (AiMode) -> Unit,
    showSaveSuccess: Boolean,
    onDismissSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
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
        
        // API Key Configuration
        if (configuration.mode == AiMode.ONLINE) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.api_key_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = draftApiKey,
                        onValueChange = onDraftApiKeyChange,
                        label = { Text(stringResource(R.string.api_key_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Text(
                        text = stringResource(R.string.api_key_source), // Fixed: Hardcoded string
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Button(
                        onClick = onSaveApiKey,
                        modifier = Modifier.align(Alignment.End),
                        enabled = draftApiKey.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                    
                    if (showSaveSuccess) {
                        LaunchedEffect(Unit) { // Use Unit as key for a non-restarting effect unless state changes
                            delay(3000) // Display for 3 seconds
                            onDismissSaveSuccess()
                        }
                        
                        Text(
                            text = stringResource(R.string.api_key_saved),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
    apiKey = ApiKey.unsafe("sk-test-api-key-1234567890"),
)

private val mockOfflineConfig = AiConfiguration(
    mode = AiMode.OFFLINE,
    apiKey = null
)

@Preview(showBackground = true, name = "Settings - Online Mode")
@Composable
fun SettingsScreenContentOnlinePreview() {
    SettingsScreenContent(
        configuration = mockOnlineConfig,
        draftApiKey = "sk-test-api-key-1234567890",
        onDraftApiKeyChange = {},
        onSaveApiKey = {},
        onChangeAiMode = {},
        showSaveSuccess = false,
        onDismissSaveSuccess = {}
    )
}

@Preview(showBackground = true, name = "Settings - Online Mode Saved")
@Composable
fun SettingsScreenContentOnlineSavedPreview() {
    SettingsScreenContent(
        configuration = mockOnlineConfig,
        draftApiKey = "sk-test-api-key-1234567890",
        onDraftApiKeyChange = {},
        onSaveApiKey = {},
        onChangeAiMode = {},
        showSaveSuccess = true,
        onDismissSaveSuccess = {}
    )
}

@Preview(showBackground = true, name = "Settings - Offline Mode")
@Composable
fun SettingsScreenContentOfflinePreview() {
    SettingsScreenContent(
        configuration = mockOfflineConfig,
        draftApiKey = "",
        onDraftApiKeyChange = {},
        onSaveApiKey = {},
        onChangeAiMode = {},
        showSaveSuccess = false,
        onDismissSaveSuccess = {}
    )
}
