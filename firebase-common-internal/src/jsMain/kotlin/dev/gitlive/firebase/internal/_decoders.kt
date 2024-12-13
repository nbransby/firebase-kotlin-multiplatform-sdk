/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlin.js.Json

internal actual fun FirebaseDecoderImpl.structureDecoder(descriptor: SerialDescriptor, polymorphicIsNested: Boolean): CompositeDecoder = when (descriptor.kind) {
    StructureKind.CLASS, StructureKind.OBJECT -> decodeAsMap(false)
    StructureKind.LIST -> decodeAsList()
    StructureKind.MAP -> (js("Object").entries(value) as Array<Array<Any>>).let {
        FirebaseCompositeDecoder(
            it.size,
            settings,
        ) { desc, index ->
            it[index / 2].run {
                if (index % 2 == 0) {
                    val key = get(0) as String
                    if (desc.getElementDescriptor(index).kind == PrimitiveKind.STRING) {
                        key
                    } else {
                        JSON.parse(key)
                    }
                } else {
                    get(1)
                }
            }
        }
    }

    is PolymorphicKind -> decodeAsMap(polymorphicIsNested)
    else -> TODO("The firebase-kotlin-sdk does not support $descriptor for serialization yet")
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal actual fun getPolymorphicType(value: Any?, discriminator: String): String =
    (value as Json)[discriminator] as String

private fun FirebaseDecoderImpl.decodeAsList(): CompositeDecoder = (value as Array<*>).let {
    FirebaseCompositeDecoder(it.size, settings) { _, index -> it[index] }
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
private fun FirebaseDecoderImpl.decodeAsMap(isNestedPolymorphic: Boolean): CompositeDecoder = (value as Json).let { json ->
    FirebaseClassDecoder(js("Object").keys(value).length as Int, settings, { json[it] != undefined }) { desc, index ->
        if (isNestedPolymorphic) {
            if (desc.getElementName(index) == "value") {
                json
            } else {
                json[desc.getElementName(index)]
            }
        } else {
            json[desc.getElementName(index)]
        }
    }
}
