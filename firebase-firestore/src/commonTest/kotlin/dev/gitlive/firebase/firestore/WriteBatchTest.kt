package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@IgnoreForAndroidUnitTest
class WriteBatchTest : BaseFirebaseFirestoreTest() {

    private val collection get() = firestore
        .collection("testServerTestSetBatch")

    @Test
    fun testSetBatch() = testBatch { doc1, doc2 ->
        val batch = firestore.batch()
        batch.set(
            documentRef = doc1,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop1",
                time = 123.0,
            ),
        )
        batch.set(
            documentRef = doc2,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop2",
                time = 456.0,
            ),
        )
        batch.commit()

        assertEquals("prop1", doc1.get().data(FirestoreTest.serializer()).prop1)
        assertEquals("prop2", doc2.get().data(FirestoreTest.serializer()).prop1)
    }

    @Test
    fun testSetBatchDoesNotEncodeEmptyValues() = testBatch { doc1, doc2 ->
        val batch = firestore.batch()
        batch.set(
            documentRef = doc1,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop1-set",
                time = 125.0,
            ),
        )
        batch.set(
            documentRef = doc2,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop2-set",
                time = 250.0,
            ),
        )
        batch.commit()

        assertEquals(125.0, doc1.get().get("time") as Double?)
        assertEquals("prop1-set", doc1.get().data(FirestoreTest.serializer()).prop1)
        assertEquals(250.0, doc2.get().get("time") as Double?)
        assertEquals("prop2-set", doc2.get().data(FirestoreTest.serializer()).prop1)
    }

    @Test
    fun testUpdateBatch() = testBatch { doc1, doc2 ->
        doc1.set(
            FirestoreTest(
                prop1 = "prop1",
                time = 123.0,
            ),
        )
        doc2.set(
            FirestoreTest(
                prop1 = "prop2",
                time = 456.0,
            ),
        )

        val batch = firestore.batch()
        batch.update(
            documentRef = doc1,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop1-updated",
                time = 345.0,
            ),
        ) {
            encodeDefaults = false
        }
        batch.update(
            documentRef = doc2,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop2-updated",
                time = 567.0,
            ),
        ) {
            encodeDefaults = false
        }
        batch.commit()

        assertEquals("prop1-updated", doc1.get().data(FirestoreTest.serializer()).prop1)
        assertEquals("prop2-updated", doc2.get().data(FirestoreTest.serializer()).prop1)
    }

    @Test
    fun testUpdateBatchDoesNotEncodeEmptyValues() = testBatch { doc1, doc2 ->
        doc1.set(
            FirestoreTest(
                prop1 = "prop1",
                time = 123.0,
            ),
        )
        doc2.set(
            FirestoreTest(
                prop1 = "prop2",
                time = 456.0,
            ),
        )
        val batch = firestore.batch()
        batch.update(
            documentRef = doc1,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop1-set",
                time = 126.0,
            ),
        ) {
            encodeDefaults = false
        }
        batch.update(
            documentRef = doc2,
            strategy = FirestoreTest.serializer(),
            data = FirestoreTest(
                prop1 = "prop2-set",
                time = 457.0,
            ),
        ) {
            encodeDefaults = false
        }
        batch.commit()

        assertEquals(126.0, doc1.get().get("time") as Double?)
        assertEquals("prop1-set", doc1.get().data(FirestoreTest.serializer()).prop1)
        assertEquals(457.0, doc2.get().get("time") as Double?)
        assertEquals("prop2-set", doc2.get().data(FirestoreTest.serializer()).prop1)
    }

    @Test
    fun testUpdateFieldValuesBatch() = testBatch { doc1, doc2 ->
        doc1.set(
            FirestoreTest(
                prop1 = "prop1",
                time = 123.0,
                duration = 800.milliseconds,
            ),
        )

        doc2.set(
            FirestoreTest(
                prop1 = "prop2",
                time = 456.0,
                duration = 700.milliseconds,
            ),
        )

        val batch = firestore.batch()
        batch.updateFields(doc1) {
            FirestoreTest::prop1.name to "prop1-updated"
            FieldPath(FirestoreTest::optional.name) to "notNull"
            FirestoreTest::duration.name.to(DurationAsIntSerializer(), 300.milliseconds)
            FieldPath(FirestoreTest::nested.name).to(NestedObject.serializer(), NestedObject("nested"))
        }
        batch.updateFields(doc2) {
            FirestoreTest::prop1.name to "prop2-updated"
            FieldPath(FirestoreTest::optional.name) to "alsoNotNull"
            FirestoreTest::duration.name.to(DurationAsIntSerializer(), 200.milliseconds)
            FieldPath(FirestoreTest::nested.name).to(NestedObject.serializer(), NestedObject("alsoNested"))
        }
        batch.commit()

        val updatedDoc1 = doc1.get().data(FirestoreTest.serializer())
        assertEquals("prop1-updated", updatedDoc1.prop1)
        assertEquals("notNull", updatedDoc1.optional)
        assertEquals(300.milliseconds, updatedDoc1.duration)
        assertEquals(NestedObject("nested"), updatedDoc1.nested)

        val updatedDoc2 = doc2.get().data(FirestoreTest.serializer())
        assertEquals("prop2-updated", updatedDoc2.prop1)
        assertEquals("alsoNotNull", updatedDoc2.optional)
        assertEquals(200.milliseconds, updatedDoc2.duration)
        assertEquals(NestedObject("alsoNested"), updatedDoc2.nested)
    }

    private fun testBatch(block: suspend (DocumentReference, DocumentReference) -> Unit) = runTest {
        val doc1 = collection
            .document("test1")
        val doc2 = collection
            .document("test2")

        try {
            block(doc1, doc2)
        } finally {
            doc1.delete()
            doc2.delete()
        }
    }
}
