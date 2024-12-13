package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.internal.SpecialValueSerializer
import dev.gitlive.firebase.firestore.DoubleAsTimestampSerializer.SERVER_TIMESTAMP
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException

/** A serializer for [BaseTimestamp]. Must be used with [FirebaseEncoder]/[FirebaseDecoder]. */
public object BaseTimestampSerializer : KSerializer<BaseTimestamp> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = { value ->
        when (value) {
            Timestamp.ServerTimestamp -> FieldValue.serverTimestamp.nativeValue
            is Timestamp -> value.nativeValue
            else -> throw SerializationException("Cannot serialize $value")
        }
    },
    fromNativeValue = { value ->
        when (value) {
            is NativeTimestamp -> Timestamp(value)
            FieldValue.serverTimestamp.nativeValue -> Timestamp.ServerTimestamp
            else -> throw SerializationException("Cannot deserialize $value")
        }
    },
)

/** A serializer for [Timestamp]. Must be used with [FirebaseEncoder]/[FirebaseDecoder]. */
public object TimestampSerializer : KSerializer<Timestamp> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = Timestamp::nativeValue,
    fromNativeValue = { value ->
        when (value) {
            is NativeTimestamp -> Timestamp(value)
            else -> throw SerializationException("Cannot deserialize $value")
        }
    },
)

/** A serializer for [Timestamp.ServerTimestamp]. Must be used with [FirebaseEncoder]/[FirebaseDecoder]. */
public object ServerTimestampSerializer : KSerializer<Timestamp.ServerTimestamp> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = { FieldValue.serverTimestamp.nativeValue },
    fromNativeValue = { value ->
        when (value) {
            FieldValue.serverTimestamp.nativeValue -> Timestamp.ServerTimestamp
            else -> throw SerializationException("Cannot deserialize $value")
        }
    },
)

/** A serializer for a Double field which is stored as a Timestamp. */
public object DoubleAsTimestampSerializer : KSerializer<Double> by SpecialValueSerializer(
    serialName = "Timestamp",
    toNativeValue = { value ->
        when (value) {
            SERVER_TIMESTAMP -> FieldValue.serverTimestamp.nativeValue
            else -> Timestamp.fromMilliseconds(value).nativeValue
        }
    },
    fromNativeValue = { value ->
        when (value) {
            FieldValue.serverTimestamp.nativeValue -> SERVER_TIMESTAMP
            is NativeTimestamp -> Timestamp(value).toMilliseconds()
            is Double -> value
            else -> throw SerializationException("Cannot deserialize $value")
        }
    },
) {
    public const val SERVER_TIMESTAMP: Double = Double.POSITIVE_INFINITY
}
