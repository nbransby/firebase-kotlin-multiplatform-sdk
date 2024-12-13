package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.NativeCollectionReference
import dev.gitlive.firebase.firestore.NativeDocumentReferenceType
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.errorToException
import dev.gitlive.firebase.firestore.externals.deleteDoc
import dev.gitlive.firebase.firestore.externals.getDoc
import dev.gitlive.firebase.firestore.externals.getDocFromCache
import dev.gitlive.firebase.firestore.externals.getDocFromServer
import dev.gitlive.firebase.firestore.externals.onSnapshot
import dev.gitlive.firebase.firestore.externals.refEqual
import dev.gitlive.firebase.firestore.externals.setDoc
import dev.gitlive.firebase.firestore.externals.updateDoc
import dev.gitlive.firebase.firestore.performUpdate
import dev.gitlive.firebase.firestore.rethrow
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.js
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.js.json

internal actual class NativeDocumentReference actual constructor(actual val nativeValue: NativeDocumentReferenceType) {
    val js: NativeDocumentReferenceType = nativeValue

    actual val id: String
        get() = rethrow { js.id }

    actual val path: String
        get() = rethrow { js.path }

    actual val parent: NativeCollectionReferenceWrapper
        get() = rethrow { NativeCollectionReferenceWrapper(js.parent) }

    actual fun collection(collectionPath: String) = rethrow {
        NativeCollectionReference(
            dev.gitlive.firebase.firestore.externals.collection(
                js,
                collectionPath,
            ),
        )
    }

    actual suspend fun get(source: Source) = rethrow {
        NativeDocumentSnapshot(
            js.get(source).await(),
        )
    }

    actual val snapshots: Flow<NativeDocumentSnapshot> get() = snapshots()

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val unsubscribe = onSnapshot(
            js,
            json("includeMetadataChanges" to includeMetadataChanges),
            { trySend(NativeDocumentSnapshot(it)) },
            { close(errorToException(it)) },
        )
        awaitClose { unsubscribe() }
    }

    actual suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions) = rethrow {
        setDoc(js, encodedData.js, setOptions.js).await()
    }

    actual suspend fun updateEncoded(encodedData: EncodedObject) = rethrow {
        updateDoc(
            js,
            encodedData.js,
        ).await()
    }

    actual suspend fun updateEncoded(encodedFieldsAndValues: List<FieldAndValue>) {
        rethrow {
            encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }
                ?.performUpdate(
                    updateAsField = { field, value, moreFieldsAndValues ->
                        updateDoc(js, field, value, *moreFieldsAndValues)
                    },
                    updateAsFieldPath = { fieldPath, value, moreFieldsAndValues ->
                        updateDoc(js, fieldPath, value, *moreFieldsAndValues)
                    },
                )
                ?.await()
        }
    }

    actual suspend fun delete() = rethrow { deleteDoc(js).await() }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is NativeDocumentReference &&
            refEqual(
                nativeValue,
                other.nativeValue,
            )
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = "DocumentReference(path=$path)"
}

private fun NativeDocumentReferenceType.get(source: Source) = when (source) {
    Source.DEFAULT -> getDoc(this)
    Source.CACHE -> getDocFromCache(this)
    Source.SERVER -> getDocFromServer(this)
}
