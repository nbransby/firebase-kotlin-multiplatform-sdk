package dev.gitlive.firebase.firestore.internal

import cocoapods.FirebaseFirestoreInternal.FIRFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.firestore.NativeFirebaseFirestore
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.await
import dev.gitlive.firebase.firestore.awaitResult
import kotlinx.coroutines.runBlocking

@Suppress("UNCHECKED_CAST")
internal actual class NativeFirebaseFirestoreWrapper internal actual constructor(actual val native: NativeFirebaseFirestore, actual val databaseId: String?) {

    actual fun collection(collectionPath: String) = native.collectionWithPath(collectionPath)

    actual fun collectionGroup(collectionId: String) = native.collectionGroupWithID(collectionId)

    actual fun document(documentPath: String) =
        NativeDocumentReference(native.documentWithPath(documentPath))

    actual fun batch() = native.batch()

    actual fun setLoggingEnabled(loggingEnabled: Boolean): Unit =
        FIRFirestore.enableLogging(loggingEnabled)

    actual fun applySettings(settings: FirebaseFirestoreSettings) {
        native.settings = settings.ios
    }

    actual suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T) =
        awaitResult<Any?> {
            native.runTransactionWithBlock(
                { transaction, _ -> runBlocking { transaction!!.func() } },
                it,
            )
        } as T

    actual suspend fun clearPersistence() =
        await { native.clearPersistenceWithCompletion(it) }

    actual fun useEmulator(host: String, port: Int) {
        native.useEmulatorWithHost(host, port.toLong())
        native.settings = native.settings.apply {
            this.sslEnabled = false
        }
    }

    actual suspend fun disableNetwork() {
        await { native.disableNetworkWithCompletion(it) }
    }

    actual suspend fun enableNetwork() {
        await { native.enableNetworkWithCompletion(it) }
    }
}
