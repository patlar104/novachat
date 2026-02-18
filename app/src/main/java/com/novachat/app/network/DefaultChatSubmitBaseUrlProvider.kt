package com.novachat.app.network

import com.google.firebase.FirebaseApp
import com.novachat.app.BuildConfig
import com.novachat.core.network.chat.ChatSubmitBaseUrlProvider

/**
 * Provides Path B chat submit base URLs. When [BuildConfig.FUNCTIONS_EMULATOR_HOST]
 * is set, returns emulator URLs (port 5002); otherwise returns production URLs.
 */
class DefaultChatSubmitBaseUrlProvider : ChatSubmitBaseUrlProvider {

    override fun getBaseUrls(): List<String> {
        val projectId = try {
            FirebaseApp.getInstance().options.projectId ?: "unknown"
        } catch (_: Exception) {
            "unknown"
        }
        val host = BuildConfig.FUNCTIONS_EMULATOR_HOST
        return if (host.isNotEmpty()) {
            listOf(
                "http://$host:5002/$projectId/us-central1/chatSubmitPrimary",
                "http://$host:5002/$projectId/us-east1/chatSubmitSecondary"
            )
        } else {
            listOf(
                "https://us-central1-$projectId.cloudfunctions.net/chatSubmitPrimary",
                "https://us-east1-$projectId.cloudfunctions.net/chatSubmitSecondary"
            )
        }
    }
}
