/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package dev.gitlive.firebase.functions

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.functions.externals.Functions
import dev.gitlive.firebase.functions.externals.HttpsCallable
import dev.gitlive.firebase.functions.externals.connectFunctionsEmulator
import dev.gitlive.firebase.functions.externals.getFunctions
import dev.gitlive.firebase.functions.externals.httpsCallable
import dev.gitlive.firebase.functions.externals.invoke
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.js
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlin.js.json
import kotlin.time.Duration
import kotlin.time.DurationUnit
import dev.gitlive.firebase.functions.externals.HttpsCallableResult as JsHttpsCallableResult
import dev.gitlive.firebase.functions.js as publicJs

public actual val Firebase.functions: FirebaseFunctions
    get() = rethrow { FirebaseFunctions(getFunctions()) }

public actual fun Firebase.functions(region: String): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(regionOrCustomDomain = region)) }

public actual fun Firebase.functions(app: FirebaseApp): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(app.js)) }

public actual fun Firebase.functions(app: FirebaseApp, region: String): FirebaseFunctions = rethrow { FirebaseFunctions(getFunctions(app.js, region)) }

public val FirebaseFunctions.js get() = js

public actual class FirebaseFunctions internal constructor(internal val js: Functions) {
    public actual fun httpsCallable(name: String, timeout: Duration?): HttpsCallableReference = rethrow { HttpsCallableReference(httpsCallable(js, name, timeout?.let { json("timeout" to timeout.toDouble(DurationUnit.MILLISECONDS)) }).native) }

    public actual fun useEmulator(host: String, port: Int) {
        connectFunctionsEmulator(js, host, port)
    }
}

@PublishedApi
internal actual data class NativeHttpsCallableReference(val js: HttpsCallable) {
    actual suspend fun invoke(encodedData: Any): HttpsCallableResult = rethrow {
        HttpsCallableResult(js(encodedData).await())
    }
    actual suspend fun invoke(): HttpsCallableResult = rethrow { HttpsCallableResult(js().await()) }
}

@PublishedApi
internal val HttpsCallable.native: NativeHttpsCallableReference get() = NativeHttpsCallableReference(this)

public val HttpsCallableReference.js: HttpsCallable get() = native.js

public val HttpsCallableResult.js: JsHttpsCallableResult get() = js

public actual class HttpsCallableResult(internal val js: JsHttpsCallableResult) {

    public actual inline fun <reified T> data(): T = rethrow { decode<T>(value = publicJs.data) }

    public actual inline fun <T> data(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit): T = rethrow { decode(strategy, publicJs.data, buildSettings) }
}

public actual class FirebaseFunctionsException(cause: Throwable, public val code: FunctionsExceptionCode, public val details: Any?) : FirebaseException(cause.message, cause)

public actual val FirebaseFunctionsException.code: FunctionsExceptionCode get() = code

public actual val FirebaseFunctionsException.details: Any? get() = details

public actual enum class FunctionsExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED,
}

@PublishedApi
internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.functions.rethrow { function() }

@PublishedApi
internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

@PublishedApi
internal fun errorToException(e: dynamic): FirebaseFunctionsException = (e?.code ?: e?.message ?: "")
    .toString()
    .lowercase()
    .let {
        when {
            "cancelled" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.CANCELLED, e.details)
            "invalid-argument" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.INVALID_ARGUMENT, e.details)
            "deadline-exceeded" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.DEADLINE_EXCEEDED, e.details)
            "not-found" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.NOT_FOUND, e.details)
            "already-exists" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.ALREADY_EXISTS, e.details)
            "permission-denied" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.PERMISSION_DENIED, e.details)
            "resource-exhausted" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.RESOURCE_EXHAUSTED, e.details)
            "failed-precondition" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.FAILED_PRECONDITION, e.details)
            "aborted" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.ABORTED, e.details)
            "out-of-range" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.OUT_OF_RANGE, e.details)
            "unimplemented" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.UNIMPLEMENTED, e.details)
            "internal" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.INTERNAL, e.details)
            "unavailable" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.UNAVAILABLE, e.details)
            "data-loss" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.DATA_LOSS, e.details)
            "unauthenticated" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.UNAUTHENTICATED, e.details)
            "unknown" in it -> FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.UNKNOWN, e.details)
            else -> {
                println("Unknown error code in ${JSON.stringify(e)}")
                FirebaseFunctionsException(e.unsafeCast<Throwable>(), FunctionsExceptionCode.UNKNOWN, e.details)
            }
        }
    }
