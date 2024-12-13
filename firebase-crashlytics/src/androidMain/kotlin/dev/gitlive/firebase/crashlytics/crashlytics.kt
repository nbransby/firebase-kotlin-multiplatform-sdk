package dev.gitlive.firebase.crashlytics

import com.google.firebase.FirebaseException
import com.google.firebase.crashlytics.CustomKeysAndValues.Builder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android

public val FirebaseCrashlytics.android: com.google.firebase.crashlytics.FirebaseCrashlytics get() = com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()

public actual val Firebase.crashlytics: FirebaseCrashlytics get() =
    FirebaseCrashlytics(com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance())

public actual fun Firebase.crashlytics(app: FirebaseApp): FirebaseCrashlytics = FirebaseCrashlytics(app.android.get(com.google.firebase.crashlytics.FirebaseCrashlytics::class.java))

public actual class FirebaseCrashlytics internal constructor(internal val android: com.google.firebase.crashlytics.FirebaseCrashlytics) {

    public actual fun recordException(exception: Throwable) {
        android.recordException(exception)
    }
    public actual fun log(message: String) {
        android.log(message)
    }
    public actual fun setUserId(userId: String) {
        android.setUserId(userId)
    }
    public actual fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        android.setCrashlyticsCollectionEnabled(enabled)
    }
    public actual fun sendUnsentReports() {
        android.sendUnsentReports()
    }
    public actual fun deleteUnsentReports() {
        android.deleteUnsentReports()
    }
    public actual fun didCrashOnPreviousExecution(): Boolean = android.didCrashOnPreviousExecution()
    public actual fun setCustomKey(key: String, value: String) {
        android.setCustomKey(key, value)
    }
    public actual fun setCustomKey(key: String, value: Boolean) {
        android.setCustomKey(key, value)
    }
    public actual fun setCustomKey(key: String, value: Double) {
        android.setCustomKey(key, value)
    }
    public actual fun setCustomKey(key: String, value: Float) {
        android.setCustomKey(key, value)
    }
    public actual fun setCustomKey(key: String, value: Int) {
        android.setCustomKey(key, value)
    }
    public actual fun setCustomKey(key: String, value: Long) {
        android.setCustomKey(key, value)
    }
    public actual fun setCustomKeys(customKeys: Map<String, Any>) {
        android.setCustomKeys(
            Builder().apply {
                customKeys.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Boolean -> putBoolean(key, value)
                        is Double -> putDouble(key, value)
                        is Float -> putFloat(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                    }
                }
            }.build(),
        )
    }
}

public actual open class FirebaseCrashlyticsException(message: String) : FirebaseException(message)
