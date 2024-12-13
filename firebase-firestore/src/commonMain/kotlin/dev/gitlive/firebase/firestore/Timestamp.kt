package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/** A class representing a platform specific Firebase Timestamp. */
public expect class NativeTimestamp

/** A base class that could be used to combine [Timestamp] and [Timestamp.ServerTimestamp] in the same field. */
@Serializable(with = BaseTimestampSerializer::class)
public expect sealed class BaseTimestamp

/** A class representing a Firebase Timestamp. */
@Serializable(with = TimestampSerializer::class)
public expect class Timestamp internal constructor(nativeValue: NativeTimestamp) : BaseTimestamp {
    public constructor(seconds: Long, nanoseconds: Int)
    public val seconds: Long
    public val nanoseconds: Int

    internal val nativeValue: NativeTimestamp

    public companion object {
        /** @return a local time timestamp. */
        public fun now(): Timestamp
    }

    /** A server time timestamp. */
    @Serializable(with = ServerTimestampSerializer::class)
    public data object ServerTimestamp : BaseTimestamp
}

public fun Timestamp.Companion.fromDuration(duration: Duration): Timestamp = duration.toComponents { seconds, nanoseconds ->
    Timestamp(seconds, nanoseconds)
}
public fun Timestamp.toDuration(): Duration = seconds.seconds + nanoseconds.nanoseconds

public fun Timestamp.Companion.fromMilliseconds(milliseconds: Double): Timestamp = fromDuration(milliseconds.milliseconds)
public fun Timestamp.toMilliseconds(): Double = toDuration().toDouble(DurationUnit.MILLISECONDS)
