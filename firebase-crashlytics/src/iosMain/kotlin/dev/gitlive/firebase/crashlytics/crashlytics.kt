package dev.gitlive.firebase.crashlytics

import cocoapods.FirebaseCrashlytics.FIRCrashlytics
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

public val FirebaseCrashlytics.ios: FIRCrashlytics get() = FIRCrashlytics.crashlytics()

public actual val Firebase.crashlytics: FirebaseCrashlytics get() =
    FirebaseCrashlytics(FIRCrashlytics.crashlytics())

public actual fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics = FirebaseCrashlytics(FIRCrashlytics.crashlytics())

public actual class FirebaseCrashlytics internal constructor(internal val ios: FIRCrashlytics) {

    public actual fun recordException(exception: Throwable) {
        ios.recordError(exception.asNSError())
    }
    public actual fun log(message: String) {
        ios.log(message)
    }
    public actual fun setUserId(userId: String) {
        ios.setUserID(userId)
    }
    public actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        ios.setCrashlyticsCollectionEnabled(enabled)
    }
    public actual fun sendUnsentReports() {
        ios.sendUnsentReports()
    }
    public actual fun deleteUnsentReports() {
        ios.deleteUnsentReports()
    }
    public actual fun didCrashOnPreviousExecution(): Boolean = ios.didCrashDuringPreviousExecution()
    public actual fun setCustomKey(key: String, value: String) {
        ios.setCustomValue(value, key)
    }
    public actual fun setCustomKey(key: String, value: Boolean) {
        ios.setCustomValue(value.toString(), key)
    }
    public actual fun setCustomKey(key: String, value: Double) {
        ios.setCustomValue(value.toString(), key)
    }
    public actual fun setCustomKey(key: String, value: Float) {
        ios.setCustomValue(value.toString(), key)
    }
    public actual fun setCustomKey(key: String, value: Int) {
        ios.setCustomValue(value.toString(), key)
    }
    public actual fun setCustomKey(key: String, value: Long) {
        ios.setCustomValue(value.toString(), key)
    }

    @Suppress("UNCHECKED_CAST")
    public actual fun setCustomKeys(customKeys: Map<String, Any>) {
        ios.setCustomKeysAndValues(customKeys as Map<Any?, *>)
    }
}

public actual open class FirebaseCrashlyticsException internal constructor(message: String) : FirebaseException(message)

private fun Throwable.asNSError(): NSError {
    val userInfo = mutableMapOf<Any?, Any>()
    userInfo["KotlinException"] = this
    val message = message
    if (message != null) {
        userInfo[NSLocalizedDescriptionKey] = message
    }
    return NSError.errorWithDomain(this::class.qualifiedName, 0, userInfo)
}
