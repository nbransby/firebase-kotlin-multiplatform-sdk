package dev.gitlive.firebase.database

import kotlinx.serialization.Serializable

private typealias NativeServerValue = com.google.firebase.database.ServerValue

/** Represents a Firebase ServerValue. */
@Serializable(with = ServerValueSerializer::class)
public actual class ServerValue internal actual constructor(
    internal actual val nativeValue: Any,
) {
    public actual companion object {
        public actual val TIMESTAMP: ServerValue get() = ServerValue(NativeServerValue.TIMESTAMP)
        public actual fun increment(delta: Double): ServerValue = ServerValue(NativeServerValue.increment(delta))
    }

    override fun equals(other: Any?): Boolean = this === other || other is ServerValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = "ServerValue($nativeValue)"
}
