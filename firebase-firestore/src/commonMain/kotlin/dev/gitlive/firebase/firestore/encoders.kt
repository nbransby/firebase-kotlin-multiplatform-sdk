package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings

/** @return whether value is special and shouldn't be encoded/decoded. */
@PublishedApi
internal expect fun isSpecialValue(value: Any): Boolean

@PublishedApi
internal inline fun <reified T> encode(value: T, buildSettings: EncodeSettings.Builder.() -> Unit): Any? = if (value?.let(::isSpecialValue) == true) {
    value
} else {
    dev.gitlive.firebase.internal.encode(value, buildSettings)
}
