package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.firestore.externals.Transaction
import dev.gitlive.firebase.firestore.js
import dev.gitlive.firebase.firestore.performUpdate
import dev.gitlive.firebase.firestore.rethrow
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.js
import kotlinx.coroutines.await

internal actual class NativeTransactionWrapper internal actual constructor(actual val native: NativeTransaction) {

    constructor(js: Transaction) : this(NativeTransaction(js))

    val js = native.js

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions,
    ): NativeTransactionWrapper = rethrow {
        js.set(documentRef.js, encodedData.js, setOptions.js)
    }
        .let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeTransactionWrapper = rethrow { js.update(documentRef.js, encodedData.js) }
        .let { this }

    actual fun updateEncoded(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<FieldAndValue>,
    ): NativeTransactionWrapper = rethrow {
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

    actual suspend fun get(documentRef: DocumentReference) =
        rethrow { NativeDocumentSnapshotWrapper(js.get(documentRef.js).await()) }
}
