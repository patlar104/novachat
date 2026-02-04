package com.novachat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novachat.app.di.appContainer
import com.novachat.app.presentation.model.NavigationDestination
import com.novachat.app.presentation.viewmodel.ChatViewModel
import com.novachat.app.presentation.viewmodel.SettingsViewModel
import com.novachat.app.presentation.viewmodel.ViewModelFactory
import com.novachat.app.ui.ChatScreen
import com.novachat.app.ui.SettingsScreen
import com.novachat.app.ui.theme.NovaChatTheme

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
            NovaChatTheme {
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
        container = (navController.context as ComponentActivity).appContainer
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
            
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
