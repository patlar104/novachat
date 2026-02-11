package com.novachat.app

import android.app.Application
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.novachat.feature.ai.domain.usecase.ConsumeWaitForDebuggerOnNextLaunchUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

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
class NovaChatApplication : Application() {
    @Inject
    lateinit var consumeWaitForDebuggerOnNextLaunchUseCase: ConsumeWaitForDebuggerOnNextLaunchUseCase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private companion object {
        const val TAG = "NovaChatApplication"
        const val DEBUGGER_ATTACH_TIMEOUT_MS = 10_000L
        const val DEBUGGER_POLL_INTERVAL_MS = 100L
    }
    
    override fun onCreate() {
        super.onCreate()

        maybeWaitForDebugger()

        FirebaseApp.initializeApp(this)

        // Initialize Firebase Auth and sign in anonymously
        // This is required for Firebase Functions authentication
        initializeFirebaseAuthIfPlayServicesAvailable()
    }

    private fun maybeWaitForDebugger() {
        if (!BuildConfig.DEBUG) {
            return
        }

        val consumeResult = runBlocking {
            consumeWaitForDebuggerOnNextLaunchUseCase()
        }
        consumeResult.fold(
            onSuccess = { shouldWait ->
                if (shouldWait) {
                    waitForDebuggerWithTimeout()
                }
            },
            onFailure = { exception ->
                Log.w(TAG, "Failed to read debugger wait preference; continuing startup", exception)
            }
        )
    }

    private fun waitForDebuggerWithTimeout() {
        if (Debug.isDebuggerConnected()) {
            Log.i(TAG, "Debugger already connected; continuing startup")
            return
        }

        Log.i(
            TAG,
            "Waiting up to $DEBUGGER_ATTACH_TIMEOUT_MS ms for debugger before app startup"
        )
        val waitDeadline = SystemClock.elapsedRealtime() + DEBUGGER_ATTACH_TIMEOUT_MS
        while (!Debug.isDebuggerConnected() && SystemClock.elapsedRealtime() < waitDeadline) {
            SystemClock.sleep(DEBUGGER_POLL_INTERVAL_MS)
        }

        if (Debug.isDebuggerConnected()) {
            Log.i(TAG, "Debugger connected; continuing startup")
        } else {
            Log.w(
                TAG,
                "Debugger did not attach within $DEBUGGER_ATTACH_TIMEOUT_MS ms; continuing startup"
            )
        }
    }

    /**
     * Starts auth initialization only when Google Play Services is ready.
     *
     * On devices where Play Services is missing/outdated, we skip eager sign-in and
     * rely on request-time checks to surface recoverable guidance.
     */
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
    
    /**
     * Initializes Firebase Authentication and signs in anonymously.
     * 
     * Anonymous authentication is required to call Firebase Functions.
     * The user will be automatically signed in as an anonymous user.
     */
    private fun initializeFirebaseAuth() {
        val auth = FirebaseAuth.getInstance()
        applicationScope.launch {
            try {
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }
            } catch (e: Exception) {
                // Log error but don't crash - app can still work
                Log.e(TAG, "Failed to sign in anonymously at startup", e)
            }
        }
    }
}
