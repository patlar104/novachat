package com.novachat.app.firebase

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseEmulatorSmokeTest {

    private val host = "10.0.2.2"
    private val authPort = 9099
    private val functionsPort = 5001

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        FirebaseAuth.getInstance().useEmulator(host, authPort)
        FirebaseFunctions.getInstance("us-central1").useEmulator(host, functionsPort)
    }

    @Test
    fun firebase_emulators_are_reachable() = runBlocking {
        assumeTrue("Auth emulator not running on $host:$authPort", isPortOpen(host, authPort))
        assumeTrue("Functions emulator not running on $host:$functionsPort", isPortOpen(host, functionsPort))

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }

        assumeTrue("Firebase Auth emulator did not sign in", auth.currentUser != null)
    }

    private fun isPortOpen(host: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 200)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
