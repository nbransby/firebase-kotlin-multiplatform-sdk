/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import dev.gitlive.firebase.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

expect val emulatorHost: String
expect val context: Any

/** @return a map extracted from the encoded data. */
expect fun encodedAsMap(encoded: Any?): Map<String, Any?>

/** @return pairs as raw encoded data. */
expect fun Map<String, Any?>.asEncoded(): Any

@IgnoreForAndroidUnitTest
abstract class BaseFirebaseFirestoreTest {

    @Serializable
    data class FirestoreTest(
        val prop1: String,
        val time: Double = 0.0,
        val count: Int = 0,
        val list: List<String> = emptyList(),
        val optional: String? = null,
        val nested: NestedObject? = null,
        val nestedList: List<NestedObject> = emptyList(),
        @Serializable(with = DurationAsIntSerializer::class)
        val duration: Duration = Duration.ZERO,
    )

    @Serializable
    data class NestedObject(
        val prop2: String,
    )

    // Long would be better but JS does not seem to support it on the Firebase level https://stackoverflow.com/questions/31930406/storing-long-type-in-firebase
    class DurationAsIntSerializer : KSerializer<Duration> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("millisecondsSinceEpoch", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: Duration) {
            encoder.encodeInt(value.inWholeMilliseconds.toInt())
        }

        override fun deserialize(decoder: Decoder): Duration = decoder.decodeInt().milliseconds
    }

    lateinit var firebaseApp: FirebaseApp
    lateinit var firestore: FirebaseFirestore

    @BeforeTest
    fun initializeFirebase() {
        val app = Firebase.apps(context).firstOrNull() ?: Firebase.initialize(
            context,
            FirebaseOptions(
                applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                storageBucket = "fir-kotlin-sdk.appspot.com",
                projectId = "fir-kotlin-sdk",
                gcmSenderId = "846484016111",
            ),
        )
        firebaseApp = app

        firestore = Firebase.firestore(app).apply {
            settings = firestoreSettings {
                cacheSettings = memoryCacheSettings {
                    gcSettings = memoryEagerGcSettings { }
                }
            }
            useEmulator(emulatorHost, 8080)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }
}

@IgnoreForAndroidUnitTest
class FirebaseFirestoreTest : BaseFirebaseFirestoreTest() {

    @Test
    fun testMultiple() = runTest {
        Firebase.firestore(firebaseApp).disableNetwork()
        Firebase.firestore(firebaseApp).enableNetwork()
    }
}
