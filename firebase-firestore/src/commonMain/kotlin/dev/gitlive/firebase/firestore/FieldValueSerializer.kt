package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.FirebaseDecoder
import dev.gitlive.firebase.FirebaseEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** A serializer for [FieldValue]. Must be used in conjunction with [FirebaseEncoder]. */
object FieldValueSerializer : KSerializer<FieldValue> {
    override val descriptor = buildClassSerialDescriptor("FieldValue") { }

    override fun serialize(encoder: Encoder, value: FieldValue) {
        if (encoder is FirebaseEncoder) {
            encoder.value = value.platformValue
        } else {
            throw IllegalArgumentException("This serializer must be used with FirebaseEncoder")
        }
    }

    override fun deserialize(decoder: Decoder): FieldValue {
        return if (decoder is FirebaseDecoder) {
            FieldValue(decoder.value!!)
        } else {
            throw IllegalArgumentException("This serializer must be used with FirebaseDecoder")
        }
    }
}
