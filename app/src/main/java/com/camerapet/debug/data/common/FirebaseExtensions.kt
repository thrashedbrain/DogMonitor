package com.camerapet.debug.data.common

import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun DocumentReference.setStringListener(tag: String) =
    suspendCancellableCoroutine<String?> { cont ->
        this.addSnapshotListener { value, error ->
            if (cont.isActive) {
                cont.resume(value?.getString(tag))
                cont.cancel()
            }
        }
    }

suspend fun DocumentReference.setStringEventListener(
    tag: String,
    scope: CoroutineScope
): Flow<String?> = callbackFlow {
    addSnapshotListener { value, error ->
        trySend(value?.getString(tag))
    }
    awaitClose {  }
}