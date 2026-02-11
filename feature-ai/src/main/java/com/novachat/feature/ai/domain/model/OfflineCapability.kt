package com.novachat.feature.ai.domain.model

sealed interface OfflineCapability {
    data object Checking : OfflineCapability

    data object Available : OfflineCapability

    data class Unavailable(val reason: String) : OfflineCapability
}
