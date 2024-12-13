package dev.gitlive.firebase.firestore.internal

import cocoapods.FirebaseFirestoreInternal.FIRServerTimestampBehavior
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.SnapshotMetadata
import platform.Foundation.NSNull

internal actual class NativeDocumentSnapshotWrapper actual constructor(actual val native: NativeDocumentSnapshot) {

    actual val id get() = native.documentID

    actual val reference get() = NativeDocumentReference(native.reference)

    actual fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior): Any? = native.valueForField(field, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }

    // Despite its name implying otherwise, valueForField accepts both a String representation of a Field and a FIRFieldPath
    actual fun getEncoded(fieldPath: EncodedFieldPath, serverTimestampBehavior: ServerTimestampBehavior): Any? = native.valueForField(fieldPath, serverTimestampBehavior.toIos())?.takeIf { it !is NSNull }

    actual fun encodedData(serverTimestampBehavior: ServerTimestampBehavior): Any? = native.dataWithServerTimestampBehavior(serverTimestampBehavior.toIos())
        ?.mapValues { (_, value) ->
            value?.takeIf { it !is NSNull }
        }

    actual fun contains(field: String) = native.valueForField(field) != null
    actual fun contains(fieldPath: EncodedFieldPath) = native.valueForField(fieldPath) != null

    actual val exists get() = native.exists

    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(native.metadata)

    fun ServerTimestampBehavior.toIos(): FIRServerTimestampBehavior = when (this) {
        ServerTimestampBehavior.ESTIMATE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorEstimate
        ServerTimestampBehavior.NONE -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorNone
        ServerTimestampBehavior.PREVIOUS -> FIRServerTimestampBehavior.FIRServerTimestampBehaviorPrevious
    }
}
