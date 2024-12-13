package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlin.test.*

/**
 * These tests are separated from other tests because
 * testing Firestore Source requires toggling persistence settings per test.
 */
@IgnoreForAndroidUnitTest
class FirestoreSourceTest {
    lateinit var firestore: FirebaseFirestore

    companion object {
        val testDoc = BaseFirebaseFirestoreTest.FirestoreTest(
            "aaa",
            0.0,
            1,
            listOf("a", "aa", "aaa"),
            "notNull",
        )
    }

    private fun initializeFirebase(persistenceEnabled: Boolean = false) {
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

        firestore = Firebase.firestore(app).apply {
            settings = firestoreSettings {
                cacheSettings = if (persistenceEnabled) {
                    persistentCacheSettings { }
                } else {
                    memoryCacheSettings { }
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

    @Test
    fun testGetFromServer_withPersistence() = testFirebaseDoc(true) {
        val doc = get(Source.SERVER)
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGetFromServer_withoutPersistence() = testFirebaseDoc(false) {
        val doc = get(Source.SERVER)
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGetFromCache() = testFirebaseDoc(true) {
        // Warm up cache by setting a document
        set(testDoc)

        val cachedDoc = get(Source.CACHE)
        assertTrue(cachedDoc.exists)
        assertTrue(cachedDoc.metadata.isFromCache)
    }

    @Test
    fun testGetFromCache_withoutPersistence() = testFirebaseDoc(false) {
        assertFailsWith(FirebaseFirestoreException::class) {
            get(Source.CACHE)
        }
    }

    @Test
    fun testGetDefault_withPersistence() = testFirebaseDoc(false) {
        val doc = get(Source.DEFAULT)
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGet() = testFirebaseDoc(false) {
        val doc = get()
        assertTrue(doc.exists)
        assertFalse(doc.metadata.isFromCache)
    }

    @Test
    fun testGetDefault_withoutPersistence() = testFirebaseDoc(true) {
        val doc = get(Source.DEFAULT)
        assertTrue(doc.exists)
        // Firebase defaults to first fetching from server
        assertFalse(doc.metadata.isFromCache)
    }

    private fun testFirebaseDoc(
        persistenceEnabled: Boolean,
        block: suspend DocumentReference.() -> Unit,
    ) = runTest {
        initializeFirebase()
        val doc = firestore.collection("testFirestoreQuerying").document("one")
        doc.set(testDoc)

        Firebase.apps(context).forEach { it.delete() }

        initializeFirebase(persistenceEnabled = persistenceEnabled)

        val newDoc = firestore.collection("testFirestoreQuerying").document("one")
        try {
            newDoc.block()
        } finally {
            newDoc.delete()
        }
    }
}
