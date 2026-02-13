package com.novachat.feature.ai.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novachat.feature.ai.presentation.model.NavigationDestination
import com.novachat.feature.ai.presentation.model.UiEffect
import com.novachat.feature.ai.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, onNavigateToSettings: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val draftMessage by viewModel.draftMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

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
                is UiEffect.Navigate -> {
                    when (effect.destination) {
                        NavigationDestination.Settings -> onNavigateToSettings()
                        else -> Unit
                    }
                }
                else -> Unit
            }
        }
    }

    ChatScreenContent(
        uiState = uiState,
        draftMessage = draftMessage,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onDraftMessageChange = viewModel::updateDraftMessage
    )
}
