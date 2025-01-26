package dev.gitlive.firebase.firestore.internal

import com.google.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.firestore.NativeFirebaseFirestore
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.android
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

internal actual class NativeFirebaseFirestoreWrapper actual constructor(actual val native: NativeFirebaseFirestore, actual val databaseId: String?) {

    actual fun collection(collectionPath: String) = native.collection(collectionPath)

    actual fun collectionGroup(collectionId: String) = native.collectionGroup(collectionId)

    actual fun document(documentPath: String) =
        NativeDocumentReference(native.document(documentPath))

    actual fun batch() = native.batch()

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(loggingEnabled)

    actual fun applySettings(settings: FirebaseFirestoreSettings) {
        native.firestoreSettings = firestoreSettings {
            isSslEnabled = settings.sslEnabled
            host = settings.host
            setLocalCacheSettings(settings.cacheSettings.android)
        }
        callbackExecutorMap[native] = settings.callbackExecutor
    }

    actual suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T): T =
        native.runTransaction { runBlocking { it.func() } }.await()

    actual suspend fun clearPersistence() =
        native.clearPersistence().await().run { }

    actual fun useEmulator(host: String, port: Int) {
        native.useEmulator(host, port)
    }

    actual suspend fun disableNetwork() =
        native.disableNetwork().await().run { }

    actual suspend fun enableNetwork() =
        native.enableNetwork().await().run { }
}
