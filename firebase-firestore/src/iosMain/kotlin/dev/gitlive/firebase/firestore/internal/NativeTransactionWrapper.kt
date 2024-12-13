package dev.gitlive.firebase.firestore.internal

import cocoapods.FirebaseFirestoreInternal.FIRTransaction
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.ios
import dev.gitlive.firebase.firestore.toEncodedMap
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.ios

internal actual class NativeTransactionWrapper actual constructor(actual val native: FIRTransaction) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions,
    ): NativeTransactionWrapper = when (setOptions) {
        is SetOptions.Merge -> native.setData(encodedData.ios, documentRef.ios, true)
        is SetOptions.Overwrite -> native.setData(encodedData.ios, documentRef.ios, false)
        is SetOptions.MergeFields -> native.setData(encodedData.ios, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> native.setData(encodedData.ios, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeTransactionWrapper = native.updateData(encodedData.ios, documentRef.ios).let { this }

    actual fun updateEncoded(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<FieldAndValue>,
    ): NativeTransactionWrapper = native.updateData(
        encodedFieldsAndValues.toEncodedMap(),
        documentRef.ios,
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.deleteDocument(documentRef.ios).let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        throwError { NativeDocumentSnapshotWrapper(native.getDocument(documentRef.ios, it)!!) }
}
