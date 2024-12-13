package dev.gitlive.firebase.perf

import cocoapods.FirebasePerformance.FIRPerformance
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.perf.metrics.Trace

public val FirebasePerformance.ios: FIRPerformance get() = FIRPerformance.sharedInstance()

public actual val Firebase.performance: FirebasePerformance get() =
    FirebasePerformance(FIRPerformance.sharedInstance())

public actual fun Firebase.performance(app: FirebaseApp): FirebasePerformance = FirebasePerformance(FIRPerformance.sharedInstance())

public actual class FirebasePerformance(internal val ios: FIRPerformance) {

    public actual fun newTrace(traceName: String): Trace = Trace(ios.traceWithName(traceName))

    public actual fun isPerformanceCollectionEnabled(): Boolean = ios.isDataCollectionEnabled()

    public actual fun setPerformanceCollectionEnabled(enable: Boolean) {
        ios.dataCollectionEnabled = enable
    }
}

public actual open class FirebasePerformanceException(message: String) : FirebaseException(message)
