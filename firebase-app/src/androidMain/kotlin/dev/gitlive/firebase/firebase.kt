/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase

import android.content.Context

public actual typealias FirebaseException = com.google.firebase.FirebaseException

public actual typealias FirebaseNetworkException = com.google.firebase.FirebaseNetworkException

public actual typealias FirebaseTooManyRequestsException = com.google.firebase.FirebaseTooManyRequestsException

public actual typealias FirebaseApiNotAvailableException = com.google.firebase.FirebaseApiNotAvailableException

public val FirebaseApp.android: com.google.firebase.FirebaseApp get() = android

public actual val Firebase.app: FirebaseApp
    get() = FirebaseApp(com.google.firebase.FirebaseApp.getInstance())

public actual fun Firebase.app(name: String): FirebaseApp = FirebaseApp(com.google.firebase.FirebaseApp.getInstance(name))

public actual fun Firebase.initialize(context: Any?): FirebaseApp? = com.google.firebase.FirebaseApp.initializeApp(context as Context)?.let { FirebaseApp(it) }

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions, name: String): FirebaseApp = FirebaseApp(com.google.firebase.FirebaseApp.initializeApp(context as Context, options.toAndroid(), name))

public actual fun Firebase.initialize(context: Any?, options: FirebaseOptions): FirebaseApp = FirebaseApp(com.google.firebase.FirebaseApp.initializeApp(context as Context, options.toAndroid()))

public actual data class FirebaseApp internal constructor(internal val android: com.google.firebase.FirebaseApp) {
    actual val name: String
        get() = android.name
    actual val options: FirebaseOptions
        get() = android.options.run { FirebaseOptions(applicationId, apiKey, databaseUrl, gaTrackingId, storageBucket, projectId, gcmSenderId) }

    public actual suspend fun delete() {
        android.delete()
    }
}

public actual fun Firebase.apps(context: Any?): List<FirebaseApp> = com.google.firebase.FirebaseApp.getApps(context as Context)
    .map { FirebaseApp(it) }

private fun FirebaseOptions.toAndroid() = com.google.firebase.FirebaseOptions.Builder()
    .setApplicationId(applicationId)
    .setApiKey(apiKey)
    .setDatabaseUrl(databaseUrl)
    .setGaTrackingId(gaTrackingId)
    .setStorageBucket(storageBucket)
    .setProjectId(projectId)
    .setGcmSenderId(gcmSenderId)
    .build()
