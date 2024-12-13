package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import kotlin.jvm.JvmName

// ** Helper method to perform an update operation. */
@JvmName("performUpdateFields")
@PublishedApi
internal inline fun encodeFieldAndValue(
    fieldsAndValues: Array<out Pair<String, Any?>>,
    buildSettings: EncodeSettings.Builder.() -> Unit,
): List<Pair<String, Any?>>? = encodeFieldAndValue(fieldsAndValues, encodeField = { it }, encodeValue = { encode(it, buildSettings) })

/** Helper method to perform an update operation. */
@JvmName("performUpdateFieldPaths")
@PublishedApi
internal inline fun encodeFieldAndValue(
    fieldsAndValues: Array<out Pair<FieldPath, Any?>>,
    buildSettings: EncodeSettings.Builder.() -> Unit,
): List<Pair<EncodedFieldPath, Any?>>? = encodeFieldAndValue(fieldsAndValues, { it.encoded }, { encode(it, buildSettings) })

/** Helper method to perform an update operation in Android and JS. */
@PublishedApi
internal inline fun <T, K> encodeFieldAndValue(
    fieldsAndValues: Array<out Pair<T, Any?>>,
    encodeField: (T) -> K,
    encodeValue: (Any?) -> Any?,
): List<Pair<K, Any?>>? = fieldsAndValues.takeUnless { fieldsAndValues.isEmpty() }
    ?.map { (field, value) -> encodeField(field) to value?.let { encodeValue(it) } }

internal fun <K, R> List<Pair<K, Any?>>.performUpdate(
    update: (K, Any?, Array<Any?>) -> R,
) = update(
    this[0].first,
    this[0].second,
    this.drop(1).flatMap { (field, value) -> listOf(field, value) }.toTypedArray(),
)
