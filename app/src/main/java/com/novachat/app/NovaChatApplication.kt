package com.novachat.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.novachat.feature.ai.data.worker.ChatReconciliationWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Custom Application class for NovaChat.
 *
 * This class initializes the dependency injection container
 * and provides it to the rest of the application.
 *
 * Must be declared in AndroidManifest.xml:
 * <application android:name=".NovaChatApplication" ...>
 *
 * @since 1.0.0
 */
@HiltAndroidApp
class NovaChatApplication : Application(), Configuration.Provider {

    @javax.inject.Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private companion object {
        const val TAG = "NovaChatApplication"
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG && BuildConfig.FUNCTIONS_EMULATOR_HOST.isNotEmpty()) {
            FirebaseFunctions.getInstance("us-central1").useEmulator(
                BuildConfig.FUNCTIONS_EMULATOR_HOST,
                5002
            )
            Log.d(TAG, "Using Functions emulator at ${BuildConfig.FUNCTIONS_EMULATOR_HOST}:5002")
        }

        initializeFirebaseAuthIfPlayServicesAvailable()
        scheduleChatReconciliation()
    }

    private fun scheduleChatReconciliation() {
        val request = PeriodicWorkRequestBuilder<ChatReconciliationWorker>(30, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "chat_reconciliation",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun initializeFirebaseAuthIfPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val availabilityCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (availabilityCode != ConnectionResult.SUCCESS) {
            Log.w(
                TAG,
                "Skipping startup Firebase auth init: Google Play Services unavailable " +
                    "(${apiAvailability.getErrorString(availabilityCode)} / code=$availabilityCode)"
            )
            return
        }
        initializeFirebaseAuth()
    }

    private fun initializeFirebaseAuth() {
        val auth = FirebaseAuth.getInstance()
        applicationScope.launch {
            try {
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign in anonymously at startup", e)
            }
        }
    }
}
