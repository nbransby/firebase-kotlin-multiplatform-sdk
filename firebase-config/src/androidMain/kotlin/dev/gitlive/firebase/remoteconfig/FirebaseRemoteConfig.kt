@file:JvmName("android")

package dev.gitlive.firebase.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfigClientException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigFetchThrottledException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigServerException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.google.firebase.remoteconfig.FirebaseRemoteConfig as AndroidFirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo as AndroidFirebaseRemoteConfigInfo
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings as AndroidFirebaseRemoteConfigSettings

public val FirebaseRemoteConfig.android: AndroidFirebaseRemoteConfig get() = AndroidFirebaseRemoteConfig.getInstance()

public actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = FirebaseRemoteConfig(com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance())

public actual fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig = FirebaseRemoteConfig(com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance(app.android))

public actual class FirebaseRemoteConfig internal constructor(internal val android: AndroidFirebaseRemoteConfig) {
    public actual val all: Map<String, FirebaseRemoteConfigValue>
        get() = android.all.mapValues { FirebaseRemoteConfigValue(it.value) }

    public actual val info: FirebaseRemoteConfigInfo
        get() = android.info.asCommon()

    public actual suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit) {
        val settings = FirebaseRemoteConfigSettings().apply(init)
        val androidSettings = com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(settings.minimumFetchInterval.inWholeSeconds)
            .setFetchTimeoutInSeconds(settings.fetchTimeout.inWholeSeconds)
            .build()
        android.setConfigSettingsAsync(androidSettings).await()
    }

    public actual suspend fun setDefaults(vararg defaults: Pair<String, Any?>) {
        android.setDefaultsAsync(defaults.toMap()).await()
    }

    public actual suspend fun fetch(minimumFetchInterval: Duration?) {
        minimumFetchInterval
            ?.also { android.fetch(it.inWholeSeconds).await() }
            ?: run { android.fetch().await() }
    }

    public actual suspend fun activate(): Boolean = android.activate().await()
    public actual suspend fun ensureInitialized() {
        android.ensureInitialized().await()
    }
    public actual suspend fun fetchAndActivate(): Boolean = android.fetchAndActivate().await()
    public actual fun getKeysByPrefix(prefix: String): Set<String> = android.getKeysByPrefix(prefix)
    public actual fun getValue(key: String): FirebaseRemoteConfigValue = FirebaseRemoteConfigValue(android.getValue(key))
    public actual suspend fun reset() {
        android.reset().await()
    }

    private fun AndroidFirebaseRemoteConfigSettings.asCommon(): FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings(
        fetchTimeout = fetchTimeoutInSeconds.seconds,
        minimumFetchInterval = minimumFetchIntervalInSeconds.seconds,
    )

    private fun AndroidFirebaseRemoteConfigInfo.asCommon(): FirebaseRemoteConfigInfo {
        val lastFetchStatus = when (lastFetchStatus) {
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS -> FetchStatus.Success
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET -> FetchStatus.NoFetchYet
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE -> FetchStatus.Failure
            AndroidFirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED -> FetchStatus.Throttled
            else -> error("Unknown last fetch status value: $lastFetchStatus")
        }

        return FirebaseRemoteConfigInfo(
            configSettings = configSettings.asCommon(),
            fetchTime = Instant.fromEpochMilliseconds(fetchTimeMillis),
            lastFetchStatus = lastFetchStatus,
        )
    }
}

public actual typealias FirebaseRemoteConfigException = com.google.firebase.remoteconfig.FirebaseRemoteConfigException
public actual typealias FirebaseRemoteConfigClientException = FirebaseRemoteConfigClientException
public actual typealias FirebaseRemoteConfigFetchThrottledException = FirebaseRemoteConfigFetchThrottledException
public actual typealias FirebaseRemoteConfigServerException = FirebaseRemoteConfigServerException
