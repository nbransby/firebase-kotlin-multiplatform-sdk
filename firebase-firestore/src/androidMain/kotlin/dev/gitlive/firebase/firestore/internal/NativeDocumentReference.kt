package dev.gitlive.firebase.firestore.internal

import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.firestore.MetadataChanges
import dev.gitlive.firebase.firestore.NativeDocumentReferenceType
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.performUpdate
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.android
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal actual class NativeDocumentReference actual constructor(actual val nativeValue: NativeDocumentReferenceType) {
    val android: NativeDocumentReferenceType by ::nativeValue
    actual val id: String
        get() = android.id

    actual val path: String
        get() = android.path

    actual val parent: NativeCollectionReferenceWrapper
        get() = NativeCollectionReferenceWrapper(android.parent)

    actual fun collection(collectionPath: String) = android.collection(collectionPath)

    actual suspend fun get(source: Source) =
        android.get(source.toAndroidSource()).await()

    actual suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions) {
        val task = (
            setOptions.android?.let {
                android.set(encodedData.android, it)
            } ?: android.set(encodedData.android)
            )
        task.await()
    }

    actual suspend fun updateEncoded(encodedData: EncodedObject) {
        android.update(encodedData.android).await()
    }

    actual suspend fun updateEncoded(encodedFieldsAndValues: List<FieldAndValue>) {
        encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }?.let {
            encodedFieldsAndValues.performUpdate(android::update, android::update)
        }?.await()
    }

    actual suspend fun delete() {
        android.delete().await()
    }

    actual val snapshots: Flow<NativeDocumentSnapshot> get() = snapshots()

    actual fun snapshots(includeMetadataChanges: Boolean) = addSnapshotListener(includeMetadataChanges) { snapshot, exception ->
        snapshot?.let { trySend(snapshot) }
        exception?.let { close(exception) }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is NativeDocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    private fun addSnapshotListener(
        includeMetadataChanges: Boolean = false,
        listener: ProducerScope<NativeDocumentSnapshot>.(com.google.firebase.firestore.DocumentSnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit,
    ) = callbackFlow {
        val executor = callbackExecutorMap[android.firestore] ?: TaskExecutors.MAIN_THREAD
        val metadataChanges =
            if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val registration =
            android.addSnapshotListener(executor, metadataChanges) { snapshots, exception ->
                listener(snapshots, exception)
            }
        awaitClose { registration.remove() }
    }
}
