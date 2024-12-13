/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind

internal actual fun FirebaseDecoderImpl.structureDecoder(descriptor: SerialDescriptor, polymorphicIsNested: Boolean): CompositeDecoder = when (descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> decodeAsMap(false)
    StructureKind.LIST -> decodeAsList()
    StructureKind.MAP -> (value as? Map<*, *>).orEmpty().entries.toList().let {
        FirebaseCompositeDecoder(it.size, settings) { _, index -> it[index / 2].run { if (index % 2 == 0) key else value } }
    }
    is PolymorphicKind -> decodeAsMap(polymorphicIsNested)
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

internal actual fun getPolymorphicType(value: Any?, discriminator: String): String =
    (value as? Map<*, *>).orEmpty()[discriminator] as String

private fun FirebaseDecoderImpl.decodeAsList(): CompositeDecoder = (value as? List<*>).orEmpty().let {
    FirebaseCompositeDecoder(it.size, settings) { _, index -> it[index] }
}
private fun FirebaseDecoderImpl.decodeAsMap(isNestedPolymorphic: Boolean): CompositeDecoder = (value as? Map<*, *>).orEmpty().let { map ->
    FirebaseClassDecoder(map.size, settings, { map.containsKey(it) }) { desc, index ->
        if (isNestedPolymorphic) {
            if (desc.getElementName(index) == "value") {
                map
            } else {
                map[desc.getElementName(index)]
            }
        } else {
            map[desc.getElementName(index)]
        }
    }
}
