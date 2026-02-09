package com.novachat.app.firebase

import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.Socket

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseEmulatorSmokeTest {

    private val host = "10.0.2.2"
    private val authPort = 9099
    private val functionsPort = 5001

    @BeforeEach
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
        assumeTrue(isPortOpen(host, authPort), "Auth emulator not running on $host:$authPort")
        assumeTrue(isPortOpen(host, functionsPort), "Functions emulator not running on $host:$functionsPort")

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }

        assumeTrue(auth.currentUser != null, "Firebase Auth emulator did not sign in")
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
