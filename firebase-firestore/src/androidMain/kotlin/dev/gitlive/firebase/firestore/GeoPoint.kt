package dev.gitlive.firebase.firestore

import kotlinx.serialization.Serializable

/** A class representing a platform specific Firebase GeoPoint. */
public actual typealias NativeGeoPoint = com.google.firebase.firestore.GeoPoint

/** A class representing a Firebase GeoPoint. */
@Serializable(with = GeoPointSerializer::class)
public actual class GeoPoint internal actual constructor(internal actual val nativeValue: NativeGeoPoint) {
    public actual constructor(latitude: Double, longitude: Double) : this(NativeGeoPoint(latitude, longitude))
    public actual val latitude: Double = nativeValue.latitude
    public actual val longitude: Double = nativeValue.longitude

    override fun equals(other: Any?): Boolean = this === other || other is GeoPoint && nativeValue == other.nativeValue
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = nativeValue.toString()
}
