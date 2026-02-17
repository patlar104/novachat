package com.novachat.feature.ai.data.chat

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/**
 * Observes chat_requests/{requestId} via Firestore listener.
 * Emits until state is COMPLETED or FAILED (or flow is cancelled).
 */
class ChatStatusObserver(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observe(requestId: String): Flow<ChatRequestState> = callbackFlow {
        val ref = firestore.collection("chat_requests").document(requestId)
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ChatRequestState(requestId, "FAILED", errorCode = "LISTENER_ERROR"))
                close(error)
                return@addSnapshotListener
            }
            val data = snapshot?.data ?: return@addSnapshotListener
            val state = ChatRequestState(
                requestId = requestId,
                state = (data["state"] as? String) ?: "QUEUED",
                attempt = (data["attempt"] as? Number)?.toInt() ?: 1,
                errorCode = data["errorCode"] as? String,
                responseText = data["responseText"] as? String
            )
            trySend(state)
            if (state.isTerminal) close()
        }
        awaitClose { registration.remove() }
    }

    suspend fun getOnce(requestId: String): ChatRequestState? {
        val snapshot = firestore.collection("chat_requests").document(requestId).get().await()
        val data = snapshot.data ?: return null
        return ChatRequestState(
            requestId = requestId,
            state = (data["state"] as? String) ?: "QUEUED",
            attempt = (data["attempt"] as? Number)?.toInt() ?: 1,
            errorCode = data["errorCode"] as? String,
            responseText = data["responseText"] as? String
        )
    }
}
