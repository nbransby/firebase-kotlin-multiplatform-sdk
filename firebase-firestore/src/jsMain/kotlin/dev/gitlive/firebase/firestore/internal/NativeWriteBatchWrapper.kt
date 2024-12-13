package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.NativeWriteBatch
import dev.gitlive.firebase.firestore.externals.WriteBatch
import dev.gitlive.firebase.firestore.js
import dev.gitlive.firebase.firestore.performUpdate
import dev.gitlive.firebase.firestore.rethrow
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.js
import kotlinx.coroutines.await

internal actual class NativeWriteBatchWrapper internal actual constructor(actual val native: NativeWriteBatch) {

    constructor(js: WriteBatch) : this(NativeWriteBatch(js))

    val js = native.js

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions,
    ): NativeWriteBatchWrapper = rethrow { js.set(documentRef.js, encodedData.js, setOptions.js) }.let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeWriteBatchWrapper = rethrow { js.update(documentRef.js, encodedData.js) }
        .let { this }

    actual fun updateEncoded(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<FieldAndValue>,
    ): NativeWriteBatchWrapper = rethrow {
        encodedFieldsAndValues.performUpdate(
            updateAsField = { field, value, moreFieldsAndValues ->
                js.update(documentRef.js, field, value, *moreFieldsAndValues)
            },
            updateAsFieldPath = { fieldPath, value, moreFieldsAndValues ->
                js.update(documentRef.js, fieldPath, value, *moreFieldsAndValues)
            },
        )
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun commit() = rethrow { js.commit().await() }
}
