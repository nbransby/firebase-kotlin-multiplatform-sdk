package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.NativeCollectionReference
import dev.gitlive.firebase.firestore.await
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.ios

internal actual class NativeCollectionReferenceWrapper internal actual constructor(actual override val native: NativeCollectionReference) : NativeQueryWrapper(native) {

    actual val path: String
        get() = native.path

    actual val document get() = NativeDocumentReference(native.documentWithAutoID())

    actual val parent get() = native.parent?.let { NativeDocumentReference(it) }

    actual fun document(documentPath: String) = NativeDocumentReference(native.documentWithPath(documentPath))

    actual suspend fun addEncoded(data: EncodedObject) = NativeDocumentReference(await { native.addDocumentWithData(data.ios, it) })
}
