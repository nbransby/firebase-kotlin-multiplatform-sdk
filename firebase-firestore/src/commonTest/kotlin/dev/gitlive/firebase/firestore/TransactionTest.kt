package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@IgnoreForAndroidUnitTest
class TransactionTest : BaseFirebaseFirestoreTest() {

    @Test
    fun runTransaction() = runTest {
        val collection = firestore.collection("testServerTestTransaction")
        val document = collection.document("doc1")
        try {
            document.set(
                strategy = FirestoreTest.serializer(),
                data = FirestoreTest(
                    prop1 = "prop1",
                    count = 0,
                ),
            )
            val result = firestore.runTransaction {
                val count = get(document).data(FirestoreTest.serializer()).count

                if (count < 1) {
                    updateFields(document) {
                        FirestoreTest::prop1.name to "newProperty"
                        FieldPath(FirestoreTest::count.name) to 5
                        FirestoreTest::duration.name.to(DurationAsIntSerializer(), 100.milliseconds)
                        FieldPath(FirestoreTest::nested.name).to(
                            NestedObject.serializer(),
                            NestedObject("nested"),
                        )
                    }
                    true
                } else {
                    throw IllegalStateException("Invalid count")
                }
            }
            assertTrue(result)

            val updated = document.get().data(FirestoreTest.serializer())
            assertEquals("newProperty", updated.prop1)
            assertEquals(5, updated.count)
            assertEquals(100.milliseconds, updated.duration)
            assertEquals(NestedObject("nested"), updated.nested)
        } finally {
            document.delete()
        }
    }
}
