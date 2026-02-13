package com.novachat.feature.ai.presentation.model

sealed interface NavigationDestination {
    data object Chat : NavigationDestination

    data object Settings : NavigationDestination

    val route: String
        get() = when (this) {
            Chat -> "chat"
            Settings -> "settings"
        }
}
