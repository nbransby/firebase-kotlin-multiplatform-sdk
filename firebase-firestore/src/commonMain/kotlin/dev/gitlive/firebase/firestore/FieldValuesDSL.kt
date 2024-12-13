package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.copyFrom
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * A builder for generating the field values of a [Query].
 * The order of the field values must match the order by clauses of the [Query]
 */
public class FieldValuesDSL internal constructor() : EncodeSettings.Builder {

    override var encodeDefaults: Boolean = true
    override var serializersModule: SerializersModule = EmptySerializersModule()

    @PublishedApi
    internal val fieldValuesToAdd: MutableList<() -> Any> = mutableListOf()
    internal val fieldValues get() = fieldValuesToAdd.map { valueToEncode ->
        valueToEncode.invoke()
    }

    /**
     * Adds a field value to the [Query]
     * The [value] will be encoded according to the [EncodeSettings] set by this builder.
     * @param T the type of the value to add
     * @param value the value [T] to add
     */
    public inline fun <reified T> add(value: T) {
        fieldValuesToAdd.add {
            encode(value, { copyFrom(this@FieldValuesDSL) })!!
        }
    }

    /**
     * Adds a field value to the [Query]
     * The [value] will be encoded according to the [EncodeSettings] set by this builder.
     * @param T the type of the value to add
     * @param strategy the [SerializationStrategy] to apply to the value
     * @param value the value [T] to add
     */
    public fun <T : Any> addWithStrategy(strategy: SerializationStrategy<T>, value: T) {
        fieldValuesToAdd.add {
            dev.gitlive.firebase.internal.encode(strategy, value, { copyFrom(this@FieldValuesDSL) })!!
        }
    }

    /**
     * Provides an accessor for encoding values with [EncodeSettings]
     * @param dls the [FieldValuesDSL] to specify the [EncodeSettings] and values to add
     */
    public fun withEncodeSettings(dls: FieldValuesDSL.() -> Unit) {
        fieldValuesToAdd.addAll(
            FieldValuesDSL()
                .apply { copyFrom(this@FieldValuesDSL) }
                .apply(dls).fieldValuesToAdd,
        )
    }
}
