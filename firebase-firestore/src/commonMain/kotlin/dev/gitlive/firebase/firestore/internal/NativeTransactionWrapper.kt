package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.NativeTransaction
import dev.gitlive.firebase.internal.EncodedObject

internal expect class NativeTransactionWrapper internal constructor(native: NativeTransaction) {

    val native: NativeTransaction

    fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): NativeTransactionWrapper
    fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): NativeTransactionWrapper
    fun updateEncoded(documentRef: DocumentReference, encodedFieldsAndValues: List<FieldAndValue>): NativeTransactionWrapper
    fun delete(documentRef: DocumentReference): NativeTransactionWrapper
    suspend fun get(documentRef: DocumentReference): NativeDocumentSnapshotWrapper
}
