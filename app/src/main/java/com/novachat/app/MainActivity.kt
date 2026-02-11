package com.novachat.app

import android.os.Bundle
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novachat.feature.ai.domain.usecase.ConsumeWaitForDebuggerOnNextLaunchUseCase
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.presentation.model.NavigationDestination
import com.novachat.feature.ai.presentation.viewmodel.ChatViewModel
import com.novachat.feature.ai.presentation.viewmodel.SettingsViewModel
import com.novachat.feature.ai.presentation.viewmodel.ThemeViewModel
import com.novachat.feature.ai.ui.ChatScreen
import com.novachat.feature.ai.ui.SettingsScreen
import com.novachat.feature.ai.ui.theme.NovaChatTheme
import com.novachat.feature.ai.ui.theme.resolveDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main activity for NovaChat application.
 *
 * This activity sets up the navigation and theme for the app.
 * It uses the new architecture with dependency injection and
 * proper ViewModel creation.
 *
 * @since 1.0.0
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var consumeWaitForDebuggerOnNextLaunchUseCase: ConsumeWaitForDebuggerOnNextLaunchUseCase

    private var isWaitingForDebugger by mutableStateOf(false)

    private companion object {
        const val TAG = "MainActivity"
        const val DEBUGGER_ATTACH_TIMEOUT_MS = 10_000L
        const val DEBUGGER_POLL_INTERVAL_MS = 100L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            if (isWaitingForDebugger) {
                DebuggerWaitContent()
            } else {
                MainContent()
            }
        }

        lifecycleScope.launch {
            val shouldWaitForDebugger = consumeDebuggerWaitFlag()
            if (shouldWaitForDebugger && !Debug.isDebuggerConnected()) {
                isWaitingForDebugger = true
                waitForDebuggerWithTimeout()
                isWaitingForDebugger = false
            }
        }
    }

    private suspend fun consumeDebuggerWaitFlag(): Boolean {
        if (!BuildConfig.DEBUG) {
            return false
        }
        return consumeWaitForDebuggerOnNextLaunchUseCase().fold(
            onSuccess = { shouldWait -> shouldWait },
            onFailure = { error ->
                Log.w(TAG, "Failed to read debugger wait preference; continuing startup", error)
                false
            }
        )
    }

    private suspend fun waitForDebuggerWithTimeout() {
        Log.i(
            TAG,
            "Waiting up to $DEBUGGER_ATTACH_TIMEOUT_MS ms for debugger before showing app content"
        )

        val waitDeadline = SystemClock.elapsedRealtime() + DEBUGGER_ATTACH_TIMEOUT_MS
        while (!Debug.isDebuggerConnected() && SystemClock.elapsedRealtime() < waitDeadline) {
            delay(DEBUGGER_POLL_INTERVAL_MS)
        }

        if (Debug.isDebuggerConnected()) {
            Log.i(TAG, "Debugger connected; continuing app launch")
        } else {
            Log.w(
                TAG,
                "Debugger did not attach within $DEBUGGER_ATTACH_TIMEOUT_MS ms; continuing launch"
            )
        }
    }
}

@Composable
private fun DebuggerWaitContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                androidx.compose.material3.Text(
                    text = stringResource(R.string.waiting_for_debugger),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun MainContent(
    themeViewModel: ThemeViewModel = viewModel(),
) {
    val themePrefs by themeViewModel.themePreferences.collectAsStateWithLifecycle(
        initialValue = ThemePreferences.DEFAULT,
    )
    NovaChatTheme(
        darkTheme = themePrefs.resolveDarkTheme(isSystemInDarkTheme()),
        dynamicColor = themePrefs.dynamicColor,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NovaChatApp()
        }
    }
}

/**
 * Main composable for the NovaChat app.
 *
 * Sets up navigation between chat and settings screens,
 * creates ViewModels with proper dependency injection.
 */
@Composable
fun NovaChatApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Chat.route,
    ) {
        composable(NavigationDestination.Chat.route) {
            val chatViewModel: ChatViewModel = hiltViewModel()
            
            ChatScreen(
                viewModel = chatViewModel,
            ) {
                navController.navigate(NavigationDestination.Settings.route)
            }
        }
        
        composable(NavigationDestination.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeViewModel: ThemeViewModel = hiltViewModel()
            
            SettingsScreen(
                viewModel = settingsViewModel,
                themeViewModel = themeViewModel,
            ) {
                navController.popBackStack()
            }
        }
    }
}
