package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.FIRFieldValue
import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase FieldValue. */
private typealias NativeFieldValue = FIRFieldValue

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
        public actual val serverTimestamp: FieldValue get() = FieldValue(NativeFieldValue.fieldValueForServerTimestamp())
        public actual val delete: FieldValue get() = FieldValue(NativeFieldValue.fieldValueForDelete())
        public actual fun increment(value: Int): FieldValue = FieldValue(NativeFieldValue.fieldValueForIntegerIncrement(value.toLong()))
        public actual fun arrayUnion(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.fieldValueForArrayUnion(elements.asList()))
        public actual fun arrayRemove(vararg elements: Any): FieldValue = FieldValue(NativeFieldValue.fieldValueForArrayRemove(elements.asList()))
    }
}
