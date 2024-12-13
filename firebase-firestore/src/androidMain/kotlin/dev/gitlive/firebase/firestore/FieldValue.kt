package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** Represents a platform specific Firebase FieldValue. */
public typealias NativeFieldValue = com.google.firebase.firestore.FieldValue

/** Represents a Firebase FieldValue. */
@Serializable(with = FieldValueSerializer::class)
public actual class FieldValue internal actual constructor(internal actual val nativeValue: Any) {
    init {
        require(nativeValue is NativeFieldValue)
    }
    override fun equals(other: Any?): Boolean = this === other || other is FieldValue && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()

    public actual companion object {
        public actual val serverTimestamp: FieldValue get() = FieldValue(NativeFieldValue.serverTimestamp())
        public actual val delete: FieldValue get() = FieldValue(NativeFieldValue.delete())
        public actual fun increment(value: Int): FieldValue = FieldValue(NativeFieldValue.increment(value.toDouble()))
        public actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayUnion(*elements))
        public actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.arrayRemove(*elements))
    }
}
