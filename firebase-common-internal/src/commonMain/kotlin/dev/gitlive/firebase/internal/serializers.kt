/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer

@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Any> T.firebaseSerializer(): SerializationStrategy<T> = runCatching { serializer<T>() }
    .getOrElse {
        when (this) {
            is Map<*, *> -> FirebaseMapSerializer()
            is List<*> -> FirebaseListSerializer()
            is Set<*> -> FirebaseListSerializer()
            else -> this::class.serializer()
        } as SerializationStrategy<T>
    }

public class FirebaseMapSerializer : KSerializer<Map<String, Any?>> {

    public lateinit var keys: List<String>
    public lateinit var map: Map<String, Any?>

    override val descriptor: SerialDescriptor = object : SerialDescriptor {
        override val kind = StructureKind.MAP
        override val serialName = "kotlin.Map<String, Any>"
        override val elementsCount get() = map.size
        override fun getElementIndex(name: String) = keys.indexOf(name)
        override fun getElementName(index: Int) = keys[index]
        override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = false
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: Map<String, Any?>) {
        map = value
        keys = value.keys.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, value.size)
        keys.forEachIndexed { index, key ->
            val listValue = map.getValue(key)
            val serializer = (listValue?.firebaseSerializer() ?: Unit.serializer()) as KSerializer<Any?>
            String.serializer().let {
                collectionEncoder.encodeSerializableElement(it.descriptor, index * 2, it, key)
            }
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor,
                index * 2 + 1,
                serializer,
                listValue,
            )
        }
        collectionEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Map<String, Any?> {
        val collectionDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
        val map = mutableMapOf<String, Any?>()
        for (index in 0 until collectionDecoder.decodeCollectionSize(descriptor) * 2 step 2) {
//            map[collectionDecoder.decodeNullableSerializableElement(index) as String] =
//                collectionDecoder.decodeNullableSerializableElement(index + 1)
        }
        return map
    }
}

public class FirebaseListSerializer : KSerializer<Iterable<Any?>> {

    public lateinit var list: List<Any?>

    override val descriptor: SerialDescriptor = object : SerialDescriptor {
        override val kind = StructureKind.LIST
        override val serialName = "kotlin.List<Any>"
        override val elementsCount get() = list.size
        override fun getElementIndex(name: String) = throw NotImplementedError()
        override fun getElementName(index: Int) = throw NotImplementedError()
        override fun getElementAnnotations(index: Int) = emptyList<Annotation>()
        override fun getElementDescriptor(index: Int) = throw NotImplementedError()
        override fun isElementOptional(index: Int) = false
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: Iterable<Any?>) {
        list = value.toList()
        val collectionEncoder = encoder.beginCollection(descriptor, list.size)
        list.forEachIndexed { index, listValue ->
            val serializer = (listValue?.firebaseSerializer() ?: Unit.serializer()) as KSerializer<Any>
            collectionEncoder.encodeNullableSerializableElement(
                serializer.descriptor,
                index,
                serializer,
                listValue,
            )
        }
        collectionEncoder.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): List<Any?> {
        throw NotImplementedError()
//        val collectionDecoder = decoder.beginStructure(descriptor) as FirebaseCompositeDecoder
//        val list = mutableListOf<Any?>()
//        list.forEachIndexed { index, _ ->
//            list.add(index, collectionDecoder.decodeNullableSerializableElement(index))
//        }
//        return list
    }
}

/**
 * A special case of serializer for values natively supported by Firebase and
 * don't require an additional encoding/decoding.
 */
public class SpecialValueSerializer<T>(
    serialName: String,
    private val toNativeValue: (T) -> Any?,
    private val fromNativeValue: (Any?) -> T,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName) { }

    override fun serialize(encoder: Encoder, value: T) {
        if (encoder is FirebaseEncoderImpl) {
            encoder.value = toNativeValue(value)
        } else {
            throw SerializationException("This serializer must be used with FirebaseEncoder")
        }
    }

    override fun deserialize(decoder: Decoder): T = if (decoder is FirebaseDecoderImpl) {
        fromNativeValue(decoder.value)
    } else {
        throw SerializationException("This serializer must be used with FirebaseDecoder")
    }
}
