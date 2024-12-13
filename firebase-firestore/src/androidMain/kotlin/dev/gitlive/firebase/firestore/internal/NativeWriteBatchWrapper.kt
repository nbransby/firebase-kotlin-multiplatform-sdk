package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.NativeWriteBatch
import dev.gitlive.firebase.firestore.android
import dev.gitlive.firebase.firestore.performUpdate
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.android
import kotlinx.coroutines.tasks.await

internal actual class NativeWriteBatchWrapper internal actual constructor(actual val native: NativeWriteBatch) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions,
    ): NativeWriteBatchWrapper = (
        setOptions.android?.let {
            native.set(documentRef.android, encodedData.android, it)
        } ?: native.set(documentRef.android, encodedData.android)
        ).let {
        this
    }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject) = native.update(documentRef.android, encodedData.android).let { this }

    actual fun updateEncoded(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<FieldAndValue>,
    ) = encodedFieldsAndValues.performUpdate(
        updateAsField = { field, value, moreFieldsAndValues ->
            native.update(documentRef.android, field, value, *moreFieldsAndValues)
        },
        updateAsFieldPath = { fieldPath, value, moreFieldsAndValues ->
            native.update(documentRef.android, fieldPath, value, *moreFieldsAndValues)
        },
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.delete(documentRef.android).let { this }

    actual suspend fun commit() {
        native.commit().await()
    }
}
