package com.novachat.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.novachat.feature.ai.di.AiContainer
import com.novachat.feature.ai.di.AiContainerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
class NovaChatApplication : Application(), AiContainerProvider {
    
    /**
     * Dependency injection container.
     * Created lazily on first access and reused throughout the app lifecycle.
     */
    override lateinit var aiContainer: AiContainer
        private set
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase Auth and sign in anonymously
        // This is required for Firebase Functions authentication
        initializeFirebaseAuth()
        
        // Initialize dependency injection container
        aiContainer = AiContainer(this)
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
                android.util.Log.e("NovaChatApplication", "Failed to sign in anonymously", e)
            }
        }
    }
}
