package com.novachat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novachat.feature.ai.di.aiContainer
import com.novachat.feature.ai.domain.model.ThemePreferences
import com.novachat.feature.ai.presentation.model.NavigationDestination
import com.novachat.feature.ai.presentation.viewmodel.ChatViewModel
import com.novachat.feature.ai.presentation.viewmodel.SettingsViewModel
import com.novachat.feature.ai.presentation.viewmodel.ThemeViewModel
import com.novachat.feature.ai.presentation.viewmodel.ViewModelFactory
import com.novachat.feature.ai.ui.ChatScreen
import com.novachat.feature.ai.ui.SettingsScreen
import com.novachat.feature.ai.ui.theme.NovaChatTheme
import com.novachat.feature.ai.ui.theme.resolveDarkTheme

/**
 * Main activity for NovaChat application.
 *
 * This activity sets up the navigation and theme for the app.
 * It uses the new architecture with dependency injection and
 * proper ViewModel creation.
 *
 * @since 1.0.0
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ViewModelFactory(aiContainer))
            val themePrefs by themeViewModel.themePreferences.collectAsStateWithLifecycle(
                ThemePreferences.DEFAULT
            )
            NovaChatTheme(
                darkTheme = themePrefs.resolveDarkTheme(isSystemInDarkTheme()),
                dynamicColor = themePrefs.dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NovaChatApp()
                }
            }
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
    
    // Create ViewModelFactory from app container
    val viewModelFactory = ViewModelFactory(
        container = (navController.context as ComponentActivity).aiContainer
    )
    
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.Chat.route
    ) {
        composable(NavigationDestination.Chat.route) {
            val chatViewModel: ChatViewModel = viewModel(factory = viewModelFactory)
            
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = {
                    navController.navigate(NavigationDestination.Settings.route)
                }
            )
        }
        
        composable(NavigationDestination.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
            val themeViewModel: ThemeViewModel = viewModel(factory = viewModelFactory)
            
            SettingsScreen(
                viewModel = settingsViewModel,
                themeViewModel = themeViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
