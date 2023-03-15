package dev.gitlive.firebase.perf.metrics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.perf.FirebasePerformance
import dev.gitlive.firebase.perf.performance
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TraceTest {

    private lateinit var performance: FirebasePerformance

    @BeforeTest
    fun initializeFirebase() {
        Firebase
            .takeIf { Firebase.apps(dev.gitlive.firebase.perf.context).isEmpty() }
            ?.apply {
                initialize(
                    dev.gitlive.firebase.perf.context,
                    FirebaseOptions(
                        applicationId = "1:846484016111:ios:dd1f6688bad7af768c841a",
                        apiKey = "AIzaSyCK87dcMFhzCz_kJVs2cT2AVlqOTLuyWV0",
                        databaseUrl = "https://fir-kotlin-sdk.firebaseio.com",
                        storageBucket = "fir-kotlin-sdk.appspot.com",
                        projectId = "fir-kotlin-sdk",
                        gcmSenderId = "846484016111"
                    )
                )
            }

        performance = Firebase.performance


    }

    @Test
    fun testGetLongMetric() {
        val trace = performance.newTrace("testGetLongMetric")
        trace.start()
        trace.putMetric("Get Long Metric Test", 1L)

//        assertEquals(1L,  trace.getLongMetric("Get Long Metric Test"))
//        trace.stop()
    }

    @Test
    fun testIncrementMetric() {
        val trace = performance.newTrace("testIncrementMetric")
        trace.start()
        trace.putMetric("Get Increment Metric Test", 1L)

        trace.incrementMetric("Get Increment Metric Test", 1L)

        assertEquals(2L,  trace.getLongMetric("Get Increment Metric Test"))
        trace.stop()
    }

    @Test
    fun testPutMetric() {
        val trace = performance.newTrace("testPutMetric")
        trace.start()
        trace.putMetric("Get Put Metric Test", 1L)

        assertEquals(1L,  trace.getLongMetric("Get Put Metric Test"))
        trace.stop()
    }

    @Test
    fun testGetAttributes() {
        val trace = performance.newTrace("testGetAttributes")
        trace.start()
        val values = listOf(1, 2, 3)

        values.forEach {
            trace.putAttribute("Test_Get_Attributes_$it", "Test Get Attributes Value $it")
        }

        val attributes = trace.getAttributes()
        trace.getAttributes().onEachIndexed { index, entry ->

            assertEquals("Test_Get_Attributes_$index", entry.key)
            assertEquals("Test Get Attributes Value $index", entry.value)
        }

        values.forEach {
            trace.removeAttribute("Test_Get_Attributes_$it")
        }
        trace.stop()
    }

    @Test
    fun testGetAttribute() {
        val trace = performance.newTrace("testGetAttribute")
        trace.start()
        trace.putAttribute("Test_Get_Attribute", "Test Get Attribute Value")

        assertEquals("Test Get Attribute Value", trace.getAttribute("Test_Get_Attribute"))
        trace.stop()
    }

    @Test
    fun testPutAttribute() {
        val trace = performance.newTrace("testPutAttribute")
        trace.start()
        trace.putAttribute("Test_Put_Attribute", "Test Put Attribute Value")

        assertEquals("Test Put Attribute Value", trace.getAttribute("Test_Put_Attribute"))
        trace.stop()
    }
}