package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.runBlockingTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import platform.Foundation.NSDate
import platform.Foundation.NSDefaultRunLoopMode
import platform.Foundation.NSRunLoop
import platform.Foundation.create
import platform.Foundation.runMode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val backgroundContext = newSingleThreadContext("background")

/**
 * This function performs is intended to test object sharing across several threads.
 * @param create a block for object creation
 * @param test a block to perform test on a thread different from the one used in [create]
 */
fun <T> runTestWithContextSwitch(create: suspend CoroutineScope.() -> T, test: suspend CoroutineScope.(T) -> Unit) =
    runBlocking {
        val testRun = MainScope().async {
            val objMain = create()
            withContext(backgroundContext) {
                test(objMain)
            }
            val objBcg = withContext(backgroundContext) {
                create()
            }
            test(objBcg)
        }
        while (testRun.isActive) {
            NSRunLoop.mainRunLoop.runMode(
                NSDefaultRunLoopMode,
                beforeDate = NSDate.create(timeInterval = 1.0, sinceDate = NSDate()),
            )
            yield()
        }
        testRun.await()
    }

class ContextSwitchTest {

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

        firestore = Firebase.firestore(app).apply {
            useEmulator(emulatorHost, 8080)
        }
    }

    @AfterTest
    fun deinitializeFirebase() = runBlockingTest {
        Firebase.apps(context).forEach {
            it.delete()
        }
    }

    private data class TestFieldValuesOps(
        val initial: List<Int>,
        val updates: List<Update>,
    ) {
        data class Update(
            val op: Pair<FieldPath, Any>,
            val expected: List<Int>?,
        )
    }

    @Serializable
    data class TestData(val values: List<Int>)

    @Test
    fun testFieldValuesOps() = runTestWithContextSwitch(
        create = {
            TestFieldValuesOps(
                initial = listOf(1),
                updates = listOf(
                    TestFieldValuesOps.Update(
                        FieldPath(TestData::values.name) to FieldValue.arrayUnion(2),
                        listOf(1, 2),
                    ),
                    TestFieldValuesOps.Update(
                        FieldPath(TestData::values.name) to FieldValue.arrayRemove(1),
                        listOf(2),
                    ),
                    TestFieldValuesOps.Update(
                        FieldPath(TestData::values.name) to FieldValue.delete,
                        null,
                    ),
                ),
            )
        },
    ) { data ->

        val doc = firestore.collection("fieldValuesOps")
            .document("fieldValuesOps")

        try {
            // store
            doc.set(
                strategy = TestData.serializer(),
                data = TestData(data.initial),
                merge = false,
            )

            // append & verify
            doc.update(data.updates[0].op)

            var savedData = doc.get().data(TestData.serializer())
            assertEquals(data.updates[0].expected, savedData.values)

            // remove & verify
            doc.update(data.updates[1].op)
            savedData = doc.get().data(TestData.serializer())
            assertEquals(data.updates[1].expected, savedData.values)

            val list = doc.get()
                .get(TestData::values.name, ListSerializer(Int.serializer()).nullable)
            assertEquals(data.updates[1].expected, list)
            // delete & verify
            doc.update(data.updates[2].op)
            val deletedList = doc.get()
                .get(TestData::values.name, ListSerializer(Int.serializer()).nullable)
            assertEquals(data.updates[2].expected, deletedList)
        } finally {
            doc.delete()
        }
    }
}
