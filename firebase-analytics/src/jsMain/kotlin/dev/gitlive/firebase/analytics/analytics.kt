package dev.gitlive.firebase.analytics

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.analytics.externals.getAnalytics
import dev.gitlive.firebase.js
import kotlinx.coroutines.await
import kotlin.time.Duration

public actual val Firebase.analytics: FirebaseAnalytics
    get() = FirebaseAnalytics(getAnalytics())

public actual fun Firebase.analytics(app: FirebaseApp): FirebaseAnalytics = FirebaseAnalytics(getAnalytics(app.js))

public val FirebaseAnalytics.js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics get() = js

public actual class FirebaseAnalytics(internal val js: dev.gitlive.firebase.analytics.externals.FirebaseAnalytics) {
    public actual fun logEvent(
        name: String,
        parameters: Map<String, Any>?,
    ) {
        dev.gitlive.firebase.analytics.externals.logEvent(js, name, parameters)
    }

    public actual fun setUserProperty(name: String, value: String) {
        dev.gitlive.firebase.analytics.externals.setUserProperty(js, name, value)
    }

    public actual fun setUserId(id: String?) {
        dev.gitlive.firebase.analytics.externals.setUserId(js, id)
    }

    public actual fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        dev.gitlive.firebase.analytics.externals.setAnalyticsCollectionEnabled(js, enabled)
    }

    public actual fun setSessionTimeoutInterval(sessionTimeoutInterval: Duration) {
        dev.gitlive.firebase.analytics.externals.setSessionTimeoutInterval(js, sessionTimeoutInterval.inWholeMilliseconds)
    }

    public actual suspend fun getSessionId(): Long? = rethrow { dev.gitlive.firebase.analytics.externals.getSessionId(js).await() }

    public actual fun resetAnalyticsData() {
        dev.gitlive.firebase.analytics.externals.resetAnalyticsData(js)
    }

    public actual fun setDefaultEventParameters(parameters: Map<String, String>) {
        dev.gitlive.firebase.analytics.externals.setDefaultEventParameters(js, parameters)
    }

    public actual fun setConsent(consentSettings: Map<ConsentType, ConsentStatus>) {
        val consent = dev.gitlive.firebase.analytics.externals.ConsentSettings()
        consentSettings.forEach {
            when (it.key) {
                ConsentType.AD_PERSONALIZATION -> consent.ad_personalization = it.value.name
                ConsentType.AD_STORAGE -> consent.ad_storage = it.value.name
                ConsentType.AD_USER_DATA -> consent.ad_user_data = it.value.name
                ConsentType.ANALYTICS_STORAGE -> consent.analytics_storage = it.value.name
            }
        }
        dev.gitlive.firebase.analytics.externals.setConsent(js, consent)
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

public actual open class FirebaseAnalyticsException(code: String, cause: Throwable) : FirebaseException(code, cause)

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

internal fun errorToException(error: dynamic) = (error?.code ?: error?.message ?: "")
    .toString()
    .lowercase()
    .let { code ->
        when {
            else -> {
                println("Unknown error code in ${JSON.stringify(error)}")
                FirebaseAnalyticsException(code, error.unsafeCast<Throwable>())
            }
        }
    }
