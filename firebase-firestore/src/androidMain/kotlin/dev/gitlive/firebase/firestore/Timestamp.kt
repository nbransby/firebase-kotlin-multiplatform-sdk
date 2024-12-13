@file:JvmName("androidTimestamp")

package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase Timestamp. */
public actual typealias NativeTimestamp = com.google.firebase.Timestamp

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
public actual sealed class BaseTimestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
public actual class Timestamp internal actual constructor(
    internal actual val nativeValue: NativeTimestamp,
) : BaseTimestamp() {
    public actual constructor(seconds: Long, nanoseconds: Int) : this(NativeTimestamp(seconds, nanoseconds))

    public actual val seconds: Long = nativeValue.seconds
    public actual val nanoseconds: Int = nativeValue.nanoseconds

    override fun equals(other: Any?): Boolean = this === other || other is Timestamp && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    public actual companion object {
        public actual fun now(): Timestamp = Timestamp(NativeTimestamp.now())
    }

    /** A server time timestamp. */
    @Serializable(with = ServerTimestampSerializer::class)
    public actual data object ServerTimestamp : BaseTimestamp()
}
