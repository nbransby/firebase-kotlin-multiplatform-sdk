/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("tests")
package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.AdminFirebaseOptions
import dev.gitlive.firebase.CommonFirebaseOptions
import kotlinx.coroutines.runBlocking

actual val emulatorHost: String = "localhost"

actual val context: Any = Unit

actual val firebaseOptions: CommonFirebaseOptions = AdminFirebaseOptions("kotlin-sdk-test-firebase-adminsdk-lxwn3-6bc474d4ab.json")

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }