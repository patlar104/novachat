package com.novachat.app

import android.app.Application
import com.novachat.app.di.AppContainer

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
class NovaChatApplication : Application() {
    
    /**
     * Dependency injection container.
     * Created lazily on first access and reused throughout the app lifecycle.
     */
    lateinit var container: AppContainer
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize dependency injection container
        container = AppContainer(this)
    }
}
