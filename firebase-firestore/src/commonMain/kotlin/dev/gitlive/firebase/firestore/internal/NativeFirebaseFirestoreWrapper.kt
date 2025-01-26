package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.firestore.NativeCollectionReference
import dev.gitlive.firebase.firestore.NativeFirebaseFirestore
import dev.gitlive.firebase.firestore.NativeQuery
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.NativeWriteBatch

internal expect class NativeFirebaseFirestoreWrapper internal constructor(native: NativeFirebaseFirestore, databaseId: String? = null) {
    val native: NativeFirebaseFirestore
    val databaseId: String?

    fun collection(collectionPath: String): NativeCollectionReference
    fun collectionGroup(collectionId: String): NativeQuery
    fun document(documentPath: String): NativeDocumentReference
    fun batch(): NativeWriteBatch
    fun setLoggingEnabled(loggingEnabled: Boolean)
    fun applySettings(settings: FirebaseFirestoreSettings)
    suspend fun clearPersistence()
    suspend fun <T> runTransaction(func: suspend NativeTransaction.() -> T): T
    fun useEmulator(host: String, port: Int)
    suspend fun disableNetwork()
    suspend fun enableNetwork()
}
