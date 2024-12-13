@file:JvmName("analyticsAndroid")

package dev.gitlive.firebase.analytics

import android.os.Bundle
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.setConsent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration

public actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(com.google.firebase.Firebase.analytics)

public actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics = FirebaseAnalytics(com.google.firebase.Firebase.analytics)

public val FirebaseAnalytics.android: com.google.firebase.analytics.FirebaseAnalytics get() = android

public actual class FirebaseAnalytics(internal val android: com.google.firebase.analytics.FirebaseAnalytics) {
    public actual fun logEvent(name: String, parameters: Map<String, Any>?) {
        android.logEvent(name, parameters?.toBundle())
    }
    public actual fun setUserProperty(name: String, value: String) {
        android.setUserProperty(name, value)
    }
    public actual fun setUserId(id: String?) {
        android.setUserId(id)
    }
    public actual fun resetAnalyticsData() {
        android.resetAnalyticsData()
    }
    public actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        android.setDefaultEventParameters(parameters.toBundle())
    }

    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        android.setAnalyticsCollectionEnabled(enabled)
    }

    public actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration) {
        android.setSessionTimeoutDuration(sessionTimeoutInterval.inWholeMilliseconds)
    }

    public actual suspend fun getSessionId(): Long? = android.sessionId.await()

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        consentSettings.entries.associate {
            it.key to when (it.value) {
                ConsentStatus.GRANTED -> com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus.GRANTED
                ConsentStatus.DENIED -> com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus.DENIED
            }
        }.let { androidConsentSettings ->
            android.setConsent {
                androidConsentSettings.entries.forEach {
                    when (it.key) {
                        ConsentType.AD_PERSONALIZATION ->
                            this.adPersonalization = it.value

                        ConsentType.AD_STORAGE ->
                            this.adStorage = it.value

                        ConsentType.AD_USER_DATA ->
                            this.adUserData = it.value

                        ConsentType.ANALYTICS_STORAGE ->
                            this.analyticsStorage = it.value
                    }
                }
            }
        }
    }

    public actual enum class ConsentType {
        AD_PERSONALIZATION,
        AD_STORAGE,
        AD_USER_DATA,
        ANALYTICS_STORAGE,
    }

    public actual enum class ConsentStatus {
        GRANTED,
        DENIED,
    }
}

public actual class FirebaseAnalyticsException(message: String) : Exception(message)

private fun Map<String, Any>.toBundle() = Bundle().apply {
    forEach { (key, value) ->
        when (value::class) {
            String::class -> putString(key, value as String)
            Int::class -> putInt(key, value as Int)
            Long::class -> putLong(key, value as Long)
            Double::class -> putDouble(key, value as Double)
            Boolean::class -> putBoolean(key, value as Boolean)
        }
    }
}
