package com.novachat.feature.ai.data.observability

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Log-based implementation of [ChatObservability].
 * Includes app_version when available. Optional: add metrics upload (e.g. Firebase Analytics) later.
 */
class ChatObservabilityLogger @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ChatObservability {

    override fun emit(event: String, props: Map<String, Any?>) {
        val appVersion = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        }.getOrElse { "unknown" }
        val msg = buildString {
            append("chat_event=$event")
            append(" app_version=$appVersion")
            props.forEach { (k, v) -> if (v != null) append(" $k=$v") }
        }
        Log.d(TAG, msg)
    }

    private companion object {
        const val TAG = "ChatObservability"
    }
}
