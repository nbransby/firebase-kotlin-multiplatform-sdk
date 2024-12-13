package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.NativeDocumentReferenceType
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.await
import dev.gitlive.firebase.firestore.awaitResult
import dev.gitlive.firebase.firestore.toEncodedMap
import dev.gitlive.firebase.firestore.toException
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.ios
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

internal actual class NativeDocumentReference actual constructor(actual val nativeValue: NativeDocumentReferenceType) {

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val listener =
            ios.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
                snapshot?.let { trySend(snapshot) }
                error?.let { close(error.toException()) }
            }
        awaitClose { listener.remove() }
    }

    val ios: NativeDocumentReferenceType by ::nativeValue

    actual val id: String
        get() = ios.documentID

    actual val path: String
        get() = ios.path

    actual val parent: NativeCollectionReferenceWrapper
        get() = NativeCollectionReferenceWrapper(ios.parent)

    actual fun collection(collectionPath: String) = ios.collectionWithPath(collectionPath)

    actual suspend fun get(source: Source) =
        awaitResult { ios.getDocumentWithSource(source.toIosSource(), it) }

    actual suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions) = await {
        when (setOptions) {
            is SetOptions.Merge -> ios.setData(encodedData.ios, true, it)
            is SetOptions.Overwrite -> ios.setData(encodedData.ios, false, it)
            is SetOptions.MergeFields -> ios.setData(encodedData.ios, setOptions.fields, it)
            is SetOptions.MergeFieldPaths -> ios.setData(
                encodedData.ios,
                setOptions.encodedFieldPaths,
                it,
            )
        }
    }

    actual suspend fun updateEncoded(encodedData: EncodedObject) = await {
        ios.updateData(encodedData.ios, it)
    }

    actual suspend fun updateEncoded(encodedFieldsAndValues: List<FieldAndValue>) =
        await {
            ios.updateData(encodedFieldsAndValues.toEncodedMap(), it)
        }

    actual suspend fun delete() = await { ios.deleteDocumentWithCompletion(it) }

    actual val snapshots get() = callbackFlow {
        val listener = ios.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(snapshot) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is NativeDocumentReference && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()
}
