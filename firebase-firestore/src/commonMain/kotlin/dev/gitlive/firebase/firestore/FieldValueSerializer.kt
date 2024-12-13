package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.internal.SpecialValueSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

/** A serializer for [FieldValue]. Must be used in conjunction with [FirebaseEncoder]. */
public object FieldValueSerializer : KSerializer<FieldValue> by SpecialValueSerializer(
    serialName = "FieldValue",
    toNativeValue = FieldValue::nativeValue,
    fromNativeValue = { raw ->
        raw?.let(::FieldValue) ?: throw SerializationException("Cannot deserialize $raw")
    },
)
