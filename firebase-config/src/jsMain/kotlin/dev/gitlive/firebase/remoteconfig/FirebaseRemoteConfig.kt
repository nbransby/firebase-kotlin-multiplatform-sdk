package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.js
import dev.gitlive.firebase.remoteconfig.externals.*
import kotlinx.coroutines.await
import kotlinx.datetime.Instant
import kotlin.js.json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public actual val Firebase.remoteConfig: FirebaseRemoteConfig
    get() = rethrow { FirebaseRemoteConfig(getRemoteConfig()) }

public actual fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig = rethrow {
    FirebaseRemoteConfig(getRemoteConfig(app.js))
}

public val FirebaseRemoteConfig.js get() = js

public actual class FirebaseRemoteConfig internal constructor(internal val js: RemoteConfig) {
    public actual val all: Map<String, FirebaseRemoteConfigValue>
        get() = rethrow { getAllKeys().associateWith { getValue(it) } }

    public actual val info: FirebaseRemoteConfigInfo
        get() = rethrow {
            FirebaseRemoteConfigInfo(
                configSettings = js.settings.toFirebaseRemoteConfigSettings(),
                fetchTime = Instant.fromEpochMilliseconds(js.fetchTimeMillis.toLong()),
                lastFetchStatus = js.lastFetchStatus.toFetchStatus(),
            )
        }

    public actual suspend fun activate(): Boolean = rethrow { activate(js).await() }
    public actual suspend fun ensureInitialized(): Unit = rethrow { ensureInitialized(js).await() }

    public actual suspend fun fetch(minimumFetchInterval: Duration?): Unit = rethrow { fetchConfig(js).await() }

    public actual suspend fun fetchAndActivate(): Boolean = rethrow { fetchAndActivate(js).await() }

    public actual fun getValue(key: String): FirebaseRemoteConfigValue = rethrow {
        FirebaseRemoteConfigValue(getValue(js, key))
    }

    public actual fun getKeysByPrefix(prefix: String): Set<String> = getAllKeys().filter { it.startsWith(prefix) }.toSet()

    private fun getAllKeys(): Set<String> {
        val objectKeys = js("Object.keys")
        return objectKeys(getAll(js)).unsafeCast<Array<String>>().toSet()
    }

    public actual suspend fun reset() {
        // not implemented for JS target
    }

    public actual suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit) {
        val settings = FirebaseRemoteConfigSettings().apply(init)
        js.settings.apply {
            fetchTimeoutMillis = settings.fetchTimeout.inWholeMilliseconds
            minimumFetchIntervalMillis = settings.minimumFetchInterval.inWholeMilliseconds
        }
    }

    public actual suspend fun setDefaults(vararg defaults: Pair<String, Any?>): Unit = rethrow {
        js.defaultConfig = json(*defaults)
    }

    private fun Settings.toFirebaseRemoteConfigSettings(): FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings(
        fetchTimeout = fetchTimeoutMillis.toLong().milliseconds,
        minimumFetchInterval = minimumFetchIntervalMillis.toLong().milliseconds,
    )

    private fun String.toFetchStatus(): FetchStatus = when (this) {
        "no-fetch-yet" -> FetchStatus.NoFetchYet
        "success" -> FetchStatus.Success
        "failure" -> FetchStatus.Failure
        "throttle" -> FetchStatus.Throttled
        else -> error("Unknown FetchStatus: $this")
    }
}

public actual open class FirebaseRemoteConfigException(code: String, cause: Throwable) : FirebaseException(code, cause)

public actual class FirebaseRemoteConfigClientException(code: String, cause: Throwable) : FirebaseRemoteConfigException(code, cause)

public actual class FirebaseRemoteConfigFetchThrottledException(code: String, cause: Throwable) : FirebaseRemoteConfigException(code, cause)

public actual class FirebaseRemoteConfigServerException(code: String, cause: Throwable) : FirebaseRemoteConfigException(code, cause)

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
                FirebaseRemoteConfigException(code, error as Throwable)
            }
        }
    }
