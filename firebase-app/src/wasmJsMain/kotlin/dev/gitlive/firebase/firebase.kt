/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import dev.gitlive.firebase.externals.deleteApp
import dev.gitlive.firebase.externals.getApp
import dev.gitlive.firebase.externals.getApps
import dev.gitlive.firebase.externals.initializeApp
import dev.gitlive.firebase.externals.FirebaseApp as JsFirebaseApp

actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(getApp())

actual fun Firebase.app(name: String): FirebaseApp =
    FirebaseApp(getApp(name))

actual fun Firebase.initialize(context: Any?): FirebaseApp? =
    throw UnsupportedOperationException("Cannot initialize firebase without options in JS")

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp =
    FirebaseApp(initializeApp(options.toJson(), name))

actual fun Firebase.initialize(context: Any?, options: FirebaseOptions) =
    FirebaseApp(initializeApp(options.toJson()))

actual class FirebaseApp internal constructor(val js: JsFirebaseApp) {
    actual val name: String
        get() = js.name
    actual val options: FirebaseOptions
        get() = js.options.run {
            FirebaseOptions(appId, apiKey, databaseURL, gaTrackingId, storageBucket, projectId, messagingSenderId, authDomain)
        }

    actual suspend fun delete() {
        "".toJsReference()
        deleteApp(js)
    }
}

actual fun Firebase.apps(context: Any?) = getApps().asSequence().filterNotNull().map { FirebaseApp(it) }.toList()

@JsName("Object")
external class JsObject : JsAny {
    operator fun get(key: JsString): JsAny?
    operator fun set(key: JsString, value: JsAny?)
}

fun json(params: List<Pair<String, Any?>>): JsObject {
    return json(*params.toTypedArray())
}

fun json(params: Map<String, Any?>): JsObject {
    return json(*params.entries.map { it.key to it.value }.toTypedArray())
}

fun json(vararg params: Pair<String, Any?>): JsObject {
    return JsObject().apply {
        params.forEach {
            val key = it.first.toJsString()
            when (val value = it.second) {
                is String -> set(key, value.toJsString())
                is Boolean -> set(key, value.toJsBoolean())
                is Int -> set(key, value.toJsNumber())
                is JsObject -> set(key, value)
                is JsString -> set(key, value)
                is JsBoolean -> set(key, value)
                is JsNumber -> set(key, value)
                is JsArray<*> -> set(key, value)
                else -> error("Unknown param $it")
            }
        }
    }
}

private fun FirebaseOptions.toJson() = JsObject().apply {
    set("apiKey".toJsString(), apiKey.toJsString())
    set("appId".toJsString(), applicationId.toJsString())
    set("databaseURL".toJsString(), (databaseUrl?.toJsString()))
    set("storageBucket".toJsString(), (storageBucket?.toJsString()))
    set("projectId".toJsString(), (projectId?.toJsString()))
    set("gaTrackingId".toJsString(), (gaTrackingId?.toJsString()))
    set("messagingSenderId".toJsString(), (gcmSenderId?.toJsString()))
    set("authDomain".toJsString(), (authDomain?.toJsString()))
}

actual open class FirebaseException(code: String?, cause: Throwable) : Exception("$code: ${cause.message}", cause)
actual open class FirebaseNetworkException(code: String?, cause: Throwable) : FirebaseException(code, cause)
actual open class FirebaseTooManyRequestsException(code: String?, cause: Throwable) : FirebaseException(code, cause)
actual open class FirebaseApiNotAvailableException(code: String?, cause: Throwable) : FirebaseException(code, cause)


fun <T: JsAny> JsArray<T>.asSequence(): Sequence<T?> {
    var i = 0
    return sequence {
        while (i++ < length) {
            yield(get(i))
        }
    }
}