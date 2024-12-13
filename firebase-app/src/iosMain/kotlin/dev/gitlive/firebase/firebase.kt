/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import cocoapods.FirebaseCore.*
import kotlinx.coroutines.CompletableDeferred

public actual open class FirebaseException(message: String) : Exception(message)
public actual open class FirebaseNetworkException(message: String) : FirebaseException(message)
public actual open class FirebaseTooManyRequestsException(message: String) : FirebaseException(message)
public actual open class FirebaseApiNotAvailableException(message: String) : FirebaseException(message)

public val FirebaseApp.ios: FIRApp get() = ios

public actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(FIRApp.defaultApp()!!)

public actual fun Firebase.app(name: String): FirebaseApp = FirebaseApp(FIRApp.appNamed(name)!!)

public actual fun Firebase.initialize(context: Any?): FirebaseApp? = FIRApp.configure().let { app }

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FIRApp.configureWithName(name, options.toIos()).let { app(name) }

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FIRApp.configureWithOptions(options.toIos()).let { app }

public actual data class FirebaseApp internal constructor(internal val ios: FIRApp) {
    actual val name: String
        get() = ios.name
    actual val options: FirebaseOptions
        get() = ios.options.run { FirebaseOptions(bundleID, APIKey!!, databaseURL!!, trackingID, storageBucket, projectID, GCMSenderID) }

    public actual suspend fun delete() {
        val deleted = CompletableDeferred<Unit>()
        ios.deleteApp { deleted.complete(Unit) }
        deleted.await()
    }
}

public actual fun Firebase.apps(context: Any?): List<FirebaseApp> = FIRApp.allApps()
    .orEmpty()
    .values
    .map { FirebaseApp(it as FIRApp) }

private fun FirebaseOptions.toIos() = FIROptions(this@toIos.applicationId, this@toIos.gcmSenderId ?: "").apply {
    APIKey = this@toIos.apiKey
    databaseURL = this@toIos.databaseUrl
    trackingID = this@toIos.gaTrackingId
    storageBucket = this@toIos.storageBucket
    projectID = this@toIos.projectId
}
