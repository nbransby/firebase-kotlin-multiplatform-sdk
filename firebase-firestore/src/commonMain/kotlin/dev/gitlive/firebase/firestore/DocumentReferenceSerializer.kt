package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.firestore.internal.NativeDocumentReference
import dev.gitlive.firebase.internal.SpecialValueSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

/**
 * A serializer for [DocumentReference]. If used with [FirebaseEncoder] performs serialization using native Firebase mechanisms.
 */
public object DocumentReferenceSerializer : KSerializer<DocumentReference> by SpecialValueSerializer(
    serialName = "DocumentReference",
    toNativeValue = { it.native.nativeValue },
    fromNativeValue = { value ->
        when (value) {
            is NativeDocumentReferenceType -> DocumentReference(NativeDocumentReference(value))
            else -> throw SerializationException("Cannot deserialize $value")
        }
    },
)
