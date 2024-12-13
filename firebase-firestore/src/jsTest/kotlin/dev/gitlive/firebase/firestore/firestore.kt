/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import kotlin.js.json

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual fun encodedAsMap(encoded: Any?): Map<String, Any?> = (js("Object").entries(encoded) as Array<Array<Any>>).associate {
    it[0] as String to it[1]
}
actual fun Map<String, Any?>.asEncoded(): Any = json(*entries.map { (key, value) -> key to value }.toTypedArray())
