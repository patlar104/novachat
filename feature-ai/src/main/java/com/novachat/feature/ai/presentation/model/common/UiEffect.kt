package com.novachat.feature.ai.presentation.model

sealed interface UiEffect {
    data class ShowToast(val message: String) : UiEffect

    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null
    ) : UiEffect

    data class Navigate(val destination: NavigationDestination) : UiEffect

    data object NavigateBack : UiEffect
}
