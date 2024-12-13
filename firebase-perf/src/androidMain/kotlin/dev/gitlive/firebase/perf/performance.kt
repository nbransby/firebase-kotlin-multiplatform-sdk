package dev.gitlive.firebase.perf

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android as publicAndroid
import dev.gitlive.firebase.perf.metrics.Trace

public val FirebasePerformance.android: com.google.firebase.perf.FirebasePerformance get() = com.google.firebase.perf.FirebasePerformance.getInstance()

public actual val Firebase.performance: FirebasePerformance get() =
    FirebasePerformance(com.google.firebase.perf.FirebasePerformance.getInstance())

public actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance = FirebasePerformance(app.publicAndroid.get(com.google.firebase.perf.FirebasePerformance::class.java))

public actual class FirebasePerformance(internal val android: com.google.firebase.perf.FirebasePerformance) {

    public actual fun newTrace(traceName: String): Trace = Trace(android.newTrace(traceName))

    public actual fun isPerformanceCollectionEnabled(): Boolean = android.isPerformanceCollectionEnabled

    public actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        android.isPerformanceCollectionEnabled = enable
    }
}

public actual open class FirebasePerformanceException(message: String) : com.google.firebase.FirebaseException(message)
