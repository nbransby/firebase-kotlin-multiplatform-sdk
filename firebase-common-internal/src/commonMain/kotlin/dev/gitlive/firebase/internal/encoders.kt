/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.FirebaseEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("encode(strategy, value) { encodeDefaults = shouldEncodeElementDefault }"))
public fun <T> encode(strategy: SerializationStrategy<T>, value: T, shouldEncodeElementDefault: Boolean): Any? = encode(strategy, value) {
    this.encodeDefaults = shouldEncodeElementDefault
}

public inline fun <T> encode(strategy: SerializationStrategy<T>, value: T, buildSettings: EncodeSettings.Builder.() -> Unit): Any? =
    encode(strategy, value, EncodeSettingsImpl.Builder().apply(buildSettings).buildEncodeSettings())

@PublishedApi
internal fun <T> encode(strategy: SerializationStrategy<T>, value: T, encodeSettings: EncodeSettings): Any? =
    FirebaseEncoderImpl(encodeSettings).apply { encodeSerializableValue(strategy, value) }.value

@Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("encode(value) { this.encodeDefaults = shouldEncodeElementDefault }"))
public inline fun <reified T> encode(value: T, shouldEncodeElementDefault: Boolean): Any? = encode(value) {
    this.encodeDefaults = shouldEncodeElementDefault
}

public inline fun <reified T> encode(value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Any? =
    encode(value, EncodeSettingsImpl.Builder().apply(buildSettings).buildEncodeSettings())

/**
 * Encodes data as an [EncodedObject].
 * This is not recommended for manual use, but may be done by the library internally.
 * @throws IllegalArgumentException if [value] is not valid as an [EncodedObject] (e.g. not encodable in the form Map<String:Any?>
 */
public inline fun <T : Any> encodeAsObject(strategy: SerializationStrategy<T>, value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): EncodedObject {
    if (value is Map<*, *> && value.keys.any { it !is String }) {
        throw IllegalArgumentException("$value is a Map containing non-String keys. Must be of the form Map<String, Any?>")
    }
    val encoded = encode(strategy, value, buildSettings) ?: throw IllegalArgumentException("$value was encoded as null. Must be of the form Map<String, Any?>")
    return encoded.asNativeMap()?.asEncodedObject() ?: throw IllegalArgumentException("$value was encoded as ${encoded::class}. Must be of the form Map<String, Any?>")
}

/**
 * Encodes data as an [EncodedObject].
 * This is not recommended for manual use, but may be done by the library internally.
 * @throws IllegalArgumentException if [value] is not valid as an [EncodedObject] (e.g. not encodable in the form Map<String:Any?>
 */
public inline fun <reified T : Any> encodeAsObject(value: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): EncodedObject {
    if (value is Map<*, *> && value.keys.any { it !is String }) {
        throw IllegalArgumentException("$value is a Map containing non-String keys. Must be of the form Map<String, Any?>")
    }
    val encoded = encode(value, buildSettings) ?: throw IllegalArgumentException("$value was encoded as null. Must be of the form Map<String, Any?>")
    return encoded.asNativeMap()?.asEncodedObject() ?: throw IllegalArgumentException("$value was encoded as ${encoded::class}. Must be of the form Map<String, Any?>")
}

@PublishedApi
internal inline fun <reified T> encode(value: T, encodeSettings: EncodeSettings): Any? = value?.let {
    FirebaseEncoderImpl(encodeSettings).apply {
        encodeSerializableValue(it.firebaseSerializer(), it)
    }.value
}

internal expect fun FirebaseEncoderImpl.structureEncoder(descriptor: SerialDescriptor): FirebaseCompositeEncoder

@PublishedApi
internal class FirebaseEncoderImpl(
    internal val settings: EncodeSettings,
) : FirebaseEncoder {

    public constructor(shouldEncodeElementDefault: Boolean) : this(
        EncodeSettingsImpl.Builder().apply { this.encodeDefaults = shouldEncodeElementDefault }.buildEncodeSettings(),
    )

    public var value: Any? = null

    override val serializersModule: SerializersModule = settings.serializersModule

    private var polymorphicDiscriminator: String? = null

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val encoder = structureEncoder(descriptor)
        if (polymorphicDiscriminator != null) {
            encoder.encodePolymorphicClassDiscriminator(polymorphicDiscriminator!!, descriptor.serialName)
            polymorphicDiscriminator = null
        }
        return encoder
    }

    override fun encodeBoolean(value: Boolean) {
        this.value = value
    }

    override fun encodeByte(value: Byte) {
        this.value = value
    }

    override fun encodeChar(value: Char) {
        this.value = value
    }

    override fun encodeDouble(value: Double) {
        this.value = value
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        this.value = enumDescriptor.getElementName(index)
    }

    override fun encodeFloat(value: Float) {
        this.value = value
    }

    override fun encodeInt(value: Int) {
        this.value = value
    }

    override fun encodeLong(value: Long) {
        this.value = value
    }

    override fun encodeNotNullMark() {
        // no-op
    }

    override fun encodeNull() {
        this.value = null
    }

    override fun encodeShort(value: Short) {
        this.value = value
    }

    override fun encodeString(value: String) {
        this.value = value
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        encodePolymorphically(serializer, value) {
            polymorphicDiscriminator = it
        }
    }
}

internal open class FirebaseCompositeEncoder(
    private val settings: EncodeSettings,
    private val end: () -> Unit = {},
    private val setPolymorphicType: (String, String) -> Unit = { _, _ -> },
    private val set: (descriptor: SerialDescriptor, index: Int, value: Any?) -> Unit,
) : CompositeEncoder {

    override val serializersModule: SerializersModule = settings.serializersModule

    override fun endStructure(descriptor: SerialDescriptor): Unit = end()

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int): Boolean = settings.encodeDefaults

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?,
    ): Unit = set(
        descriptor,
        index,
        value?.let {
            FirebaseEncoderImpl(settings).apply {
                encodeSerializableValue(serializer, value)
            }.value
        },
    )

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ): Unit = set(
        descriptor,
        index,
        FirebaseEncoderImpl(settings).apply {
            encodeSerializableValue(serializer, value)
        }.value,
    )

    public fun <T> encodeObject(descriptor: SerialDescriptor, index: Int, value: T): Unit = set(descriptor, index, value)

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean): Unit = set(descriptor, index, value)

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte): Unit = set(descriptor, index, value)

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char): Unit = set(descriptor, index, value)

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double): Unit = set(descriptor, index, value)

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float): Unit = set(descriptor, index, value)

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int): Unit = set(descriptor, index, value)

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long): Unit = set(descriptor, index, value)

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short): Unit = set(descriptor, index, value)

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String): Unit = set(descriptor, index, value)

    @ExperimentalSerializationApi
    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder =
        FirebaseEncoderImpl(settings)

    public fun encodePolymorphicClassDiscriminator(discriminator: String, type: String) {
        setPolymorphicType(discriminator, type)
    }
}
