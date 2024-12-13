package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.firestore.internal.FieldAndValue

internal fun <R> List<FieldAndValue>.performUpdate(
    updateAsField: (String, Any?, Array<Any?>) -> R,
    updateAsFieldPath: (EncodedFieldPath, Any?, Array<Any?>) -> R,
): R {
    val first = first()
    val remaining = drop(1).flatMap { fieldAndValue ->
        listOf(
            when (fieldAndValue) {
                is FieldAndValue.WithFieldPath -> fieldAndValue.path.encoded
                is FieldAndValue.WithStringField -> fieldAndValue.field
            },
            fieldAndValue.value,
        )
    }
    return when (first) {
        is FieldAndValue.WithFieldPath -> updateAsFieldPath(
            first.path.encoded,
            first.value,
            remaining.toTypedArray(),
        )

        is FieldAndValue.WithStringField -> updateAsField(
            first.field,
            first.value,
            remaining.toTypedArray(),
        )
    }
}

internal fun List<FieldAndValue>.toEncodedMap(): Map<Any?, Any?> = associate { fieldAndValue ->
    when (fieldAndValue) {
        is FieldAndValue.WithStringField -> fieldAndValue.field to fieldAndValue.value
        is FieldAndValue.WithFieldPath -> fieldAndValue.path.encoded to fieldAndValue.value
    }
}
