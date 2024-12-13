/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import kotlinx.coroutines.CoroutineScope
import kotlin.js.json
import kotlin.time.Duration.Companion.minutes

actual fun runTest(test: suspend CoroutineScope.() -> Unit) = kotlinx.coroutines.test.runTest(timeout = 5.minutes) { test() }
actual fun runBlockingTest(action: suspend CoroutineScope.() -> Unit) {
    kotlinx.coroutines.test.runTest { action() }
}

actual fun nativeMapOf(vararg pairs: Pair<Any, Any?>): Any = json(*pairs.map { (key, value) -> ((key as? String) ?: JSON.stringify(key)) to value }.toTypedArray())
actual fun nativeListOf(vararg elements: Any?): Any = elements
actual fun nativeAssertEquals(expected: Any?, actual: Any?) {
    kotlin.test.assertEquals(JSON.stringify(expected), JSON.stringify(actual))
}
