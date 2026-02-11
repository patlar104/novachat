package com.novachat.core.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthSessionProvider(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun ensureAuthenticated(): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            if (auth.currentUser == null) {
                Result.failure(
                    SecurityException(
                        "Anonymous Firebase sign-in did not complete. Please retry."
                    )
                )
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(
                SecurityException(
                    "Failed to sign in with Firebase. Check Play Services and network, then retry.",
                    e
                )
            )
        }
    }
}
