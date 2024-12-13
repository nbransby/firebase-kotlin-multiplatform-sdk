package dev.gitlive.firebase.database

import dev.gitlive.firebase.database.externals.serverTimestamp
import kotlinx.serialization.Serializable
import dev.gitlive.firebase.database.externals.increment as jsIncrement

/** Represents a Firebase ServerValue. */
@Serializable(with = ServerValueSerializer::class)
public actual class ServerValue internal actual constructor(
    internal actual val nativeValue: Any,
) {
    public actual companion object {
        public actual val TIMESTAMP: ServerValue get() = ServerValue(serverTimestamp())
        public actual fun increment(delta: Double): ServerValue = ServerValue(jsIncrement(delta))
    }

    override fun equals(other: Any?): Boolean = this === other || other is ServerValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = "ServerValue($nativeValue)"
}
