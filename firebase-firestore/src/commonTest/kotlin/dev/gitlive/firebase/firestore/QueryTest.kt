package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds

@IgnoreForAndroidUnitTest
open class QueryTest : BaseFirebaseFirestoreTest() {

    companion object {
        const val COLLECTION = "testFirestoreQuerying"
        val testOne = FirestoreTest(
            "aaa",
            0.0,
            1,
            listOf("a", "aa", "aaa"),
            "notNull",
            NestedObject("ddd"),
            listOf(NestedObject("l1"), NestedObject("l2"), NestedObject("l3")),
            100.milliseconds,
        )
        val testTwo = FirestoreTest(
            "bbb",
            0.0,
            2,
            listOf("b", "bb", "ccc"),
            null,
            NestedObject("eee"),
            listOf(NestedObject("l2"), NestedObject("l4"), NestedObject("l5")),
            200.milliseconds,
        )
        val testThree = FirestoreTest(
            "ccc",
            1.0,
            3,
            listOf("c", "cc", "ccc"),
            "notNull",
            NestedObject("fff"),
            listOf(NestedObject("l3"), NestedObject("l6"), NestedObject("l7")),
            300.milliseconds,
        )
    }

    private val collection get() = firestore.collection(COLLECTION)

    @Test
    fun testStringOrderBy() = runTestWithFirestoreData {
        val resultDocs = collection
            .orderBy(FirestoreTest::prop1.name)
            .get()
            .documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get(FirestoreTest::prop1.name))
        assertEquals("bbb", resultDocs[1].get(FirestoreTest::prop1.name))
        assertEquals("ccc", resultDocs[2].get(FirestoreTest::prop1.name))
    }

    @Test
    fun testFieldOrderBy() = runTestWithFirestoreData {
        val resultDocs = firestore.collection(COLLECTION)
            .orderBy(FieldPath(FirestoreTest::prop1.name)).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get(FirestoreTest::prop1.name))
        assertEquals("bbb", resultDocs[1].get(FirestoreTest::prop1.name))
        assertEquals("ccc", resultDocs[2].get(FirestoreTest::prop1.name))
    }

    @Test
    fun testStringOrderByAscending() = runTestWithFirestoreData {
        val resultDocs = firestore.collection(COLLECTION)
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get(FirestoreTest::prop1.name))
        assertEquals("bbb", resultDocs[1].get(FirestoreTest::prop1.name))
        assertEquals("ccc", resultDocs[2].get(FirestoreTest::prop1.name))
    }

    @Test
    fun testFieldOrderByAscending() = runTestWithFirestoreData {
        val resultDocs = firestore.collection(COLLECTION)
            .orderBy(FieldPath(FirestoreTest::prop1.name), Direction.ASCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("aaa", resultDocs[0].get(FirestoreTest::prop1.name))
        assertEquals("bbb", resultDocs[1].get(FirestoreTest::prop1.name))
        assertEquals("ccc", resultDocs[2].get(FirestoreTest::prop1.name))
    }

    @Test
    fun testStringOrderByDescending() = runTestWithFirestoreData {
        val resultDocs = firestore.collection(COLLECTION)
            .orderBy(FirestoreTest::prop1.name, Direction.DESCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].get(FirestoreTest::prop1.name))
        assertEquals("bbb", resultDocs[1].get(FirestoreTest::prop1.name))
        assertEquals("aaa", resultDocs[2].get(FirestoreTest::prop1.name))
    }

    @Test
    fun testFieldOrderByDescending() = runTestWithFirestoreData {
        val resultDocs = firestore.collection(COLLECTION)
            .orderBy(FieldPath(FirestoreTest::prop1.name), Direction.DESCENDING).get().documents
        assertEquals(3, resultDocs.size)
        assertEquals("ccc", resultDocs[0].get(FirestoreTest::prop1.name))
        assertEquals("bbb", resultDocs[1].get(FirestoreTest::prop1.name))
        assertEquals("aaa", resultDocs[2].get(FirestoreTest::prop1.name))
    }

    @Test
    fun testQueryEqualTo() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { FirestoreTest::prop1.name equalTo testOne.prop1 }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::prop1.name) equalTo testTwo.prop1 }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testTwo)

        val nullableQuery = collection
            .where { FieldPath(FirestoreTest::optional.name).isNull }

        nullableQuery.assertDocuments(FirestoreTest.serializer(), testTwo)

        val serializeFieldQuery = collection.where {
            FirestoreTest::nested.name.equalTo(NestedObject.serializer(), testOne.nested!!)
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::nested.name).equalTo(NestedObject.serializer(), testTwo.nested!!)
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testTwo)
    }

    @Test
    fun testQueryNotEqualTo() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { FirestoreTest::prop1.name notEqualTo testOne.prop1 }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::prop1.name) notEqualTo testTwo.prop1 }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testThree)

        val nullableQuery = collection
            .where { FieldPath(FirestoreTest::optional.name).isNotNull }

        nullableQuery.assertDocuments(FirestoreTest.serializer(), testOne, testThree)

        val serializeFieldQuery = collection.where {
            FirestoreTest::nested.name.notEqualTo(NestedObject.serializer(), testOne.nested!!)
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::nested.name).notEqualTo(NestedObject.serializer(), testTwo.nested!!)
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testThree)
    }

    @Test
    fun testQueryLessThan() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { "count" lessThan testThree.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::count.name) lessThan testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val serializeFieldQuery = collection.where {
            "duration".lessThan(DurationAsIntSerializer(), testThree.duration)
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::duration.name).lessThan(DurationAsIntSerializer(), testTwo.duration)
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    @Test
    fun testQueryGreaterThan() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { "count" greaterThan testOne.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::count.name) greaterThan testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val serializeFieldQuery = collection.where {
            "duration".greaterThan(DurationAsIntSerializer(), testOne.duration)
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::duration.name).greaterThan(DurationAsIntSerializer(), testTwo.duration)
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testThree)
    }

    @Test
    fun testQueryLessThanOrEqualTo() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { "count" lessThanOrEqualTo testOne.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::count.name) lessThanOrEqualTo testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val serializeFieldQuery = collection.where {
            "duration".lessThanOrEqualTo(DurationAsIntSerializer(), testOne.duration)
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::duration.name).lessThanOrEqualTo(DurationAsIntSerializer(), testTwo.duration)
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)
    }

    @Test
    fun testQueryGreaterThanOrEqualTo() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { "count" greaterThanOrEqualTo testThree.count }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::count.name) greaterThanOrEqualTo testTwo.count }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val serializeFieldQuery = collection.where {
            "duration".greaterThanOrEqualTo(DurationAsIntSerializer(), testTwo.duration)
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::duration.name).greaterThanOrEqualTo(DurationAsIntSerializer(), testThree.duration)
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testThree)
    }

    @Test
    fun testQueryArrayContains() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { "list" contains "a" }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::list.name) contains "ccc" }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree, testTwo)

        val serializeFieldQuery = collection.where {
            "nestedList".contains(NestedObject.serializer(), NestedObject("l2"))
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::nestedList.name).contains(NestedObject.serializer(), NestedObject("l3"))
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testThree)
    }

    @Test
    fun testQueryArrayContainsAny() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { "list" containsAny listOf("a", "b") }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::list.name) containsAny listOf("c", "d") }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val serializeFieldQuery = collection.where {
            "nestedList".containsAny(NestedObject.serializer(), listOf(NestedObject("l1"), NestedObject("l4")))
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::nestedList.name).containsAny(NestedObject.serializer(), listOf(NestedObject("l5"), NestedObject("l7")))
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testThree, testTwo)
    }

    @Test
    fun testQueryInArray() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { FirestoreTest::prop1.name inArray listOf("aaa", "bbb") }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::prop1.name) inArray listOf("ccc", "ddd") }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val serializeFieldQuery = collection.where {
            FirestoreTest::nested.name.inArray(NestedObject.serializer(), listOf(NestedObject("ddd"), NestedObject("eee")))
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::nested.name).inArray(NestedObject.serializer(), listOf(NestedObject("eee"), NestedObject("fff")))
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testThree, testTwo)
    }

    @Test
    fun testQueryNotInArray() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { FirestoreTest::prop1.name notInArray listOf("aaa", "bbb") }

        fieldQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val pathQuery = collection
            .where { FieldPath(FirestoreTest::prop1.name) notInArray listOf("ccc", "ddd") }

        pathQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val serializeFieldQuery = collection.where {
            FirestoreTest::nested.name.notInArray(NestedObject.serializer(), listOf(NestedObject("ddd"), NestedObject("eee")))
        }
        serializeFieldQuery.assertDocuments(FirestoreTest.serializer(), testThree)

        val serializePathQuery = collection.where {
            FieldPath(FirestoreTest::nested.name).notInArray(NestedObject.serializer(), listOf(NestedObject("eee"), NestedObject("fff")))
        }
        serializePathQuery.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    @Test
    fun testCompoundQuery() = runTestWithFirestoreData {
        val andQuery = collection
            .where {
                FieldPath(FirestoreTest::prop1.name) inArray listOf("aaa", "bbb") and (FieldPath(FirestoreTest::count.name) equalTo 1)
            }
        andQuery.assertDocuments(FirestoreTest.serializer(), testOne)

        val orQuery = collection
            .where {
                FieldPath(FirestoreTest::prop1.name) equalTo "aaa" or (FieldPath(FirestoreTest::count.name) equalTo 2)
            }
        orQuery.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val andOrQuery = collection
            .where {
                all(
                    any(
                        FieldPath(FirestoreTest::prop1.name) equalTo "aaa",
                        FieldPath(FirestoreTest::count.name) equalTo 2,
                    )!!,
                    FieldPath(FirestoreTest::list.name) contains "a",
                )
            }
        andOrQuery.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    @Test
    fun testQueryByDocumentId() = runTestWithFirestoreData {
        val fieldQuery = collection
            .where { FieldPath.documentId equalTo "one" }
        fieldQuery.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    @Test
    fun testQueryByTimestamp() = runTest {
        @Serializable
        data class DocumentWithTimestamp(
            val time: Timestamp,
        )

        val collection = firestore
            .collection("testQueryByTimestamp")

        val timestamp = Timestamp.fromMilliseconds(1693262549000.0)

        val pastTimestamp = Timestamp(timestamp.seconds - 60, 12345000) // note: iOS truncates 3 last digits of nanoseconds due to internal conversions
        val futureTimestamp = Timestamp(timestamp.seconds + 60, 78910000)

        val doc1 = collection.add(DocumentWithTimestamp.serializer(), DocumentWithTimestamp(pastTimestamp))
        val doc2 = collection.add(DocumentWithTimestamp.serializer(), DocumentWithTimestamp(futureTimestamp))

        try {
            val equalityQueryResult = collection.where {
                FieldPath(DocumentWithTimestamp::time.name) equalTo pastTimestamp
            }.get().documents.map { it.data(DocumentWithTimestamp.serializer()) }.toSet()

            assertEquals(setOf(DocumentWithTimestamp(pastTimestamp)), equalityQueryResult)

            val gtQueryResult = collection.where {
                FieldPath(DocumentWithTimestamp::time.name) greaterThan timestamp
            }.get().documents.map { it.data(DocumentWithTimestamp.serializer()) }.toSet()

            assertEquals(setOf(DocumentWithTimestamp(futureTimestamp)), gtQueryResult)
        } finally {
            doc1.delete()
            doc2.delete()
        }
    }

    @Test
    fun testStartAfterDocumentSnapshot() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val firstPage = query.limit(2)

        firstPage.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)
        val lastDocumentSnapshot = firstPage.get().documents.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.startAfter(lastDocumentSnapshot)
        secondPage.assertDocuments(FirestoreTest.serializer(), testThree)
    }

    @Test
    fun testStartAfterFieldValues() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        query.assertDocuments(FirestoreTest.serializer(), testOne, testTwo, testThree)

        val secondPage = query.startAfter("bbb")
        secondPage.assertDocuments(FirestoreTest.serializer(), testThree)

        val encodedQuery = collection
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedSecondPage = encodedQuery.startAfterFieldValues {
            addWithStrategy(NestedObject.serializer(), NestedObject("eee"))
        }
        encodedSecondPage.assertDocuments(FirestoreTest.serializer(), testThree)

        val multipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val multipleSecondPage = multipleQuery.startAfter(0.0, "aaa")
        multipleSecondPage.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val encodedMultipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedMultipleSecondPage = encodedMultipleQuery.startAfterFieldValues {
            add(0.0)
            addWithStrategy(NestedObject.serializer(), NestedObject("ddd"))
        }
        encodedMultipleSecondPage.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)
    }

    @Test
    fun testStartAtDocumentSnapshot() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val firstPage = query.limit(2)
        firstPage.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val lastDocumentSnapshot = firstPage.get().documents.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.startAt(lastDocumentSnapshot)
        secondPage.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)
    }

    @Test
    fun testStartAtFieldValues() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val secondPage = query.startAt("bbb")
        secondPage.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val encodedQuery = collection
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedSecondPage = encodedQuery.startAtFieldValues {
            addWithStrategy(NestedObject.serializer(), NestedObject("eee"))
        }
        encodedSecondPage.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val multipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val multipleSecondPage = multipleQuery.startAt(0.0, "bbb")
        multipleSecondPage.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)

        val encodedMultipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedMultipleSecondPage = encodedMultipleQuery.startAtFieldValues {
            add(0.0)
            addWithStrategy(NestedObject.serializer(), NestedObject("eee"))
        }
        encodedMultipleSecondPage.assertDocuments(FirestoreTest.serializer(), testTwo, testThree)
    }

    @Test
    fun testEndBeforeDocumentSnapshot() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val firstPage = query.limit(2)
        firstPage.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val lastDocumentSnapshot = firstPage.get().documents.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.endBefore(lastDocumentSnapshot)
        secondPage.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    @Test
    fun testEndBeforeFieldValues() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val secondPage = query.endBefore("bbb")
        secondPage.assertDocuments(FirestoreTest.serializer(), testOne)

        val encodedQuery = collection
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedSecondPage = encodedQuery.endBeforeFieldValues {
            addWithStrategy(NestedObject.serializer(), NestedObject("eee"))
        }
        encodedSecondPage.assertDocuments(FirestoreTest.serializer(), testOne)

        val multipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val multipleSecondPage = multipleQuery.endBefore(0.0, "bbb")
        multipleSecondPage.assertDocuments(FirestoreTest.serializer(), testOne)

        val encodedMultipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedMultipleSecondPage = encodedMultipleQuery.endBeforeFieldValues {
            add(0.0)
            addWithStrategy(NestedObject.serializer(), NestedObject("eee"))
        }
        encodedMultipleSecondPage.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    @Test
    fun testEndAtDocumentSnapshot() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val firstPage = query.limit(2) // First 2 results
        firstPage.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val lastDocumentSnapshot = firstPage.get().documents.lastOrNull()
        assertNotNull(lastDocumentSnapshot)

        val secondPage = query.endAt(lastDocumentSnapshot)
        secondPage.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)
    }

    @Test
    fun testEndAtFieldValues() = runTestWithFirestoreData {
        val query = collection
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val secondPage = query.endAt("bbb")
        secondPage.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val encodedQuery = collection
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedSecondPage = encodedQuery.endAtFieldValues {
            addWithStrategy(NestedObject.serializer(), NestedObject("eee"))
        }
        encodedSecondPage.assertDocuments(FirestoreTest.serializer(), testOne, testTwo)

        val multipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::prop1.name, Direction.ASCENDING)

        val multipleSecondPage = multipleQuery.endAt(0.0, "aaa")
        multipleSecondPage.assertDocuments(FirestoreTest.serializer(), testOne)

        val encodedMultipleQuery = collection
            .orderBy(FieldPath(FirestoreTest::time.name), Direction.ASCENDING)
            .orderBy(FirestoreTest::nested.name, Direction.ASCENDING)

        val encodedMultipleSecondPage = encodedMultipleQuery.endAtFieldValues {
            add(0.0)
            addWithStrategy(NestedObject.serializer(), NestedObject("ddd"))
        }
        encodedMultipleSecondPage.assertDocuments(FirestoreTest.serializer(), testOne)
    }

    private fun runTestWithFirestoreData(
        documentOne: FirestoreTest = testOne,
        documentTwo: FirestoreTest = testTwo,
        documentThree: FirestoreTest = testThree,
        block: suspend () -> Unit,
    ) = runTest {
        try {
            setupFirestoreData(documentOne, documentTwo, documentThree)
            block()
        } finally {
            cleanFirestoreData()
        }
    }

    private suspend fun setupFirestoreData(
        documentOne: FirestoreTest = testOne,
        documentTwo: FirestoreTest = testTwo,
        documentThree: FirestoreTest = testThree,
    ) {
        firestore.collection(COLLECTION)
            .document("one")
            .set(FirestoreTest.serializer(), documentOne)
        firestore.collection(COLLECTION)
            .document("two")
            .set(FirestoreTest.serializer(), documentTwo)
        firestore.collection(COLLECTION)
            .document("three")
            .set(FirestoreTest.serializer(), documentThree)
    }

    private suspend fun cleanFirestoreData() {
        firestore.collection(COLLECTION).document("one").delete()
        firestore.collection(COLLECTION).document("two").delete()
        firestore.collection(COLLECTION).document("three").delete()
    }

    private suspend fun <T> Query.assertDocuments(serializer: KSerializer<T>, vararg expected: T) {
        val documents = get().documents
        assertEquals(expected.size, documents.size)
        documents.forEachIndexed { index, documentSnapshot ->
            assertEquals(expected[index], documentSnapshot.data(serializer))
        }
    }
}
