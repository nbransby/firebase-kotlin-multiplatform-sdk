/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import dev.gitlive.firebase.externals.deleteApp
import dev.gitlive.firebase.externals.getApp
import dev.gitlive.firebase.externals.getApps
import dev.gitlive.firebase.externals.initializeApp
import kotlin.js.json
import dev.gitlive.firebase.externals.FirebaseApp as JsFirebaseApp

public actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(getApp())

public actual fun Firebase.app(name: String): FirebaseApp = FirebaseApp(getApp(name))

public actual fun Firebase.initialize(context: Any?): FirebaseApp? = throw UnsupportedOperationException("Cannot initialize firebase without options in JS")

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(initializeApp(options.toJson(), name))

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(initializeApp(options.toJson()))

public val FirebaseApp.js: JsFirebaseApp get() = js

public actual class FirebaseApp internal constructor(internal val js: JsFirebaseApp) {
    public actual val name: String
        get() = js.name
    public actual val options: FirebaseOptions
        get() = js.options.run {
            FirebaseOptions(appId, apiKey, databaseURL, gaTrackingId, storageBucket, projectId, messagingSenderId, authDomain)
        }

    public actual suspend fun delete() {
        deleteApp(js)
    }
}

public actual fun Firebase.apps(context: Any?): List<FirebaseApp> = getApps().map { FirebaseApp(it) }

private fun FirebaseOptions.toJson() = json(
    "apiKey" to apiKey,
    "appId" to applicationId,
    "databaseURL" to (databaseUrl ?: undefined),
    "storageBucket" to (storageBucket ?: undefined),
    "projectId" to (projectId ?: undefined),
    "gaTrackingId" to (gaTrackingId ?: undefined),
    "messagingSenderId" to (gcmSenderId ?: undefined),
    "authDomain" to (authDomain ?: undefined),
)

public actual open class FirebaseException(code: String?, cause: Throwable) : Exception("$code: ${cause.message}", cause)
public actual open class FirebaseNetworkException(code: String?, cause: Throwable) : FirebaseException(code, cause)
public actual open class FirebaseTooManyRequestsException(code: String?, cause: Throwable) : FirebaseException(code, cause)
public actual open class FirebaseApiNotAvailableException(code: String?, cause: Throwable) : FirebaseException(code, cause)
