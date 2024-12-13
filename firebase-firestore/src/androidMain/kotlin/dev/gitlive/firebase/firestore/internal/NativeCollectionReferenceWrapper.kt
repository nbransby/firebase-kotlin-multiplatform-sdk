package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.NativeCollectionReference
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.android
import kotlinx.coroutines.tasks.await

internal actual class NativeCollectionReferenceWrapper internal actual constructor(actual override val native: NativeCollectionReference) : NativeQueryWrapper(native) {

    actual val path: String
        get() = native.path

    actual val document: NativeDocumentReference
        get() = NativeDocumentReference(native.document())

    actual val parent: NativeDocumentReference?
        get() = native.parent?.let { NativeDocumentReference(it) }

    actual fun document(documentPath: String) = NativeDocumentReference(native.document(documentPath))

    actual suspend fun addEncoded(data: EncodedObject) = NativeDocumentReference(native.add(data.android).await())
}
