package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.NativeWriteBatch
import dev.gitlive.firebase.internal.EncodedObject

internal expect class NativeWriteBatchWrapper internal constructor(native: NativeWriteBatch) {
    val native: NativeWriteBatch
    fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): NativeWriteBatchWrapper
    fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeWriteBatchWrapper
    fun updateEncoded(documentRef: DocumentReference, encodedFieldsAndValues: List<FieldAndValue>): NativeWriteBatchWrapper
    fun delete(documentRef: DocumentReference): NativeWriteBatchWrapper
    suspend fun commit()
}
