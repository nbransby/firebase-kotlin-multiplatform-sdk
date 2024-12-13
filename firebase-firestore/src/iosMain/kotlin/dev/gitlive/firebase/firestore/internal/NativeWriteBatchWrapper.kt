package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.NativeWriteBatch
import dev.gitlive.firebase.firestore.await
import dev.gitlive.firebase.firestore.ios
import dev.gitlive.firebase.firestore.toEncodedMap
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.ios

internal actual class NativeWriteBatchWrapper actual constructor(actual val native: NativeWriteBatch) {

    actual fun setEncoded(
        documentRef: DocumentReference,
        encodedData: EncodedObject,
        setOptions: SetOptions,
    ): NativeWriteBatchWrapper = when (setOptions) {
        is SetOptions.Merge -> native.setData(encodedData.ios, documentRef.ios, true)
        is SetOptions.Overwrite -> native.setData(encodedData.ios, documentRef.ios, false)
        is SetOptions.MergeFields -> native.setData(encodedData.ios, documentRef.ios, setOptions.fields)
        is SetOptions.MergeFieldPaths -> native.setData(encodedData.ios, documentRef.ios, setOptions.encodedFieldPaths)
    }.let { this }

    actual fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeWriteBatchWrapper = native.updateData(encodedData.ios, documentRef.ios).let { this }

    actual fun updateEncoded(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<FieldAndValue>,
    ): NativeWriteBatchWrapper = native.updateData(
        encodedFieldsAndValues.toEncodedMap(),
        documentRef.ios,
    ).let { this }

    actual fun delete(documentRef: DocumentReference) =
        native.deleteDocument(documentRef.ios).let { this }

    actual suspend fun commit() = await { native.commitWithCompletion(it) }
}
