package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.copyFrom
import dev.gitlive.firebase.firestore.internal.FieldAndValue
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * A builder for generating a collection of updates to a document.
 * Updates can be applied to either a String field or to a [FieldPath].
 * Within this builder custom serialization can be applied to the update.
 *
 * ```
 * val update: FieldsAndValuesUpdateBuilder.() -> Unit = {
 *    "path" to 1
 *    FieldPath("subpath", "field") to "value"
 *    "otherPath".to(strategy, value)
 * }
 * ```
 */
public class FieldsAndValuesUpdateDSL internal constructor() : EncodeSettings.Builder {

    override var encodeDefaults: Boolean = true
    override var serializersModule: SerializersModule = EmptySerializersModule()

    @PublishedApi
    internal val fieldAndValueToAdd: MutableList<() -> FieldAndValue> = mutableListOf()
    internal val fieldsAndValues: List<FieldAndValue> get() = fieldAndValueToAdd.map { fieldAndValueToEncode ->
        fieldAndValueToEncode.invoke()
    }

    /**
     * Updates the field represented by a String to a given value
     * The [value] will be encoded according to the [EncodeSettings] set by this builder.
     * @param T the type of the value
     * @param value the value [T] to update to
     */
    public inline infix fun <reified T> String.to(value: T) {
        fieldAndValueToAdd.add {
            toEncoded(encode(value, { copyFrom(this@FieldsAndValuesUpdateDSL) }))
        }
    }

    /**
     * Updates a [FieldPath] to a given value
     * The [value] will be encoded according to the [EncodeSettings] set by this builder.
     * @param T the type of the value
     * @param value the value [T] to update to
     */
    public inline infix fun <reified T> FieldPath.to(value: T) {
        fieldAndValueToAdd.add {
            toEncoded(encode(value, { copyFrom(this@FieldsAndValuesUpdateDSL) }))
        }
    }

    /**
     * Updates the field represented by a String to a given value
     * The [value] will be encoded according to the [EncodeSettings] set by this builder.
     * @param T the type of the value
     * @param strategy the [SerializationStrategy] to apply to the value
     * @param value the value [T] to update to
     */
    public fun <T : Any> String.to(strategy: SerializationStrategy<T>, value: T) {
        fieldAndValueToAdd.add {
            toEncoded(dev.gitlive.firebase.internal.encode(strategy, value, { copyFrom(this@FieldsAndValuesUpdateDSL) }))
        }
    }

    /**
     * Updates a [FieldPath] to a given value
     * The [value] will be encoded according to the [EncodeSettings] set by this builder.
     * @param T the type of the value
     * @param strategy the [SerializationStrategy] to apply to the value
     * @param value the value [T] to update to
     */
    public fun <T : Any> FieldPath.to(strategy: SerializationStrategy<T>, value: T) {
        fieldAndValueToAdd.add {
            toEncoded(dev.gitlive.firebase.internal.encode(strategy, value, { copyFrom(this@FieldsAndValuesUpdateDSL) }))
        }
    }

    /**
     * Provides an accessor for encoding values with [EncodeSettings]
     * @param dls the [WithEncoder] to specify the [EncodeSettings] and values to add
     */
    public fun withEncodeSettings(dls: FieldsAndValuesUpdateDSL.() -> Unit) {
        fieldAndValueToAdd.addAll(
            FieldsAndValuesUpdateDSL()
                .apply { copyFrom(this@FieldsAndValuesUpdateDSL) }
                .apply(dls)
                .fieldAndValueToAdd,
        )
    }

    @PublishedApi
    internal fun String.toEncoded(encodedValue: Any?): FieldAndValue.WithStringField =
        FieldAndValue.WithStringField(this, encodedValue)

    @PublishedApi
    internal fun FieldPath.toEncoded(encodedValue: Any?): FieldAndValue.WithFieldPath =
        FieldAndValue.WithFieldPath(this, encodedValue)
}
