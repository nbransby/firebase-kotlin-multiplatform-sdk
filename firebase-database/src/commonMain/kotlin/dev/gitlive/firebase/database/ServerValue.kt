package dev.gitlive.firebase.database

import dev.gitlive.firebase.FirebaseEncoder
import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.internal.SpecialValueSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/** Represents a Firebase ServerValue. */
@Serializable(with = ServerValueSerializer::class)
public expect class ServerValue internal constructor(nativeValue: Any) {
    internal val nativeValue: Any

    public companion object {
        public val TIMESTAMP: ServerValue
        public fun increment(delta: Double): ServerValue
    }
}

/** Serializer for [ServerValue]. Must be used with [FirebaseEncoder]/[FirebaseDecoder].*/
public object ServerValueSerializer : KSerializer<ServerValue> by SpecialValueSerializer(
    serialName = "ServerValue",
    toNativeValue = ServerValue::nativeValue,
    fromNativeValue = { raw ->
        raw?.let(::ServerValue) ?: throw SerializationException("Cannot deserialize $raw")
    },
)
