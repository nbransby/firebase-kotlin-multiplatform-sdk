package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.SnapshotMetadata
import dev.gitlive.firebase.firestore.externals.DocumentSnapshot
import dev.gitlive.firebase.firestore.rethrow
import kotlin.js.json

internal actual class NativeDocumentSnapshotWrapper internal actual constructor(actual val native: NativeDocumentSnapshot) {

    constructor(js: DocumentSnapshot) : this(NativeDocumentSnapshot(js))

    val js: DocumentSnapshot = native.js

    actual val id get() = rethrow { js.id }
    actual val reference get() = rethrow { NativeDocumentReference(js.ref) }

    actual fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow {
        js.get(field, getTimestampsOptions(serverTimestampBehavior))
    }

    actual fun getEncoded(fieldPath: EncodedFieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow {
        js.get(fieldPath, getTimestampsOptions(serverTimestampBehavior))
    }

    actual fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? = rethrow {
        js.data(getTimestampsOptions(serverTimestampBehavior))
    }

    actual fun contains(field: String) = rethrow { js.get(field) != undefined }
    actual fun contains(fieldPath: EncodedFieldPath) = rethrow { js.get(fieldPath) != undefined }
    actual val exists get() = rethrow { js.exists() }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)

    fun getTimestampsOptions(serverTimestampBehavior: ServerTimestampBehavior) = json("serverTimestamps" to serverTimestampBehavior.name.lowercase())
}
