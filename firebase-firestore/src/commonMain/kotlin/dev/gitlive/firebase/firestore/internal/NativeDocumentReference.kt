package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.NativeCollectionReference
import dev.gitlive.firebase.firestore.NativeDocumentReferenceType
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.internal.EncodedObject
import kotlinx.coroutines.flow.Flow

internal expect class NativeDocumentReference(nativeValue: NativeDocumentReferenceType) {
    val nativeValue: NativeDocumentReferenceType
    val id: String
    val path: String
    val snapshots: Flow<NativeDocumentSnapshot>
    val parent: NativeCollectionReferenceWrapper
    fun snapshots(includeMetadataChanges: Boolean = false): Flow<NativeDocumentSnapshot>

    fun collection(collectionPath: String): NativeCollectionReference
    suspend fun get(source: Source = Source.DEFAULT): NativeDocumentSnapshot
    suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions)
    suspend fun updateEncoded(encodedData: EncodedObject)
    suspend fun updateEncoded(encodedFieldsAndValues: List<FieldAndValue>)
    suspend fun delete()
}
