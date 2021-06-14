package dev.gitlive.firebase.remoteconfig

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException

expect val Firebase.remoteConfig: FirebaseRemoteConfig

expect fun Firebase.remoteConfig(app: FirebaseApp): FirebaseRemoteConfig

expect class FirebaseRemoteConfig {
    val all: Map<String, FirebaseRemoteConfigValue>
    val info: FirebaseRemoteConfigInfo

    suspend fun activate(): Boolean
    suspend fun ensureInitialized()
    suspend fun fetch(minimumFetchIntervalInSeconds: Long? = null)
    suspend fun fetchAndActivate(): Boolean
    fun getKeysByPrefix(prefix: String): Set<String>
    fun getValue(key: String): FirebaseRemoteConfigValue
    suspend fun reset()
    suspend fun settings(init: FirebaseRemoteConfigSettings.() -> Unit)
    suspend fun setDefaults(vararg defaults: Pair<String, Any?>)
}

inline operator fun <reified T> FirebaseRemoteConfig.get(key: String): T {
    val configValue = getValue(key)
    return when(T::class) {
        Boolean::class -> configValue.asBoolean() as T
        Double::class -> configValue.asDouble() as T
        Long::class -> configValue.asLong() as T
        String::class -> configValue.asString() as T
        FirebaseRemoteConfigValue::class -> configValue as T
        else -> throw IllegalArgumentException()
    }
}

expect open class FirebaseRemoteConfigException : FirebaseException
expect class FirebaseRemoteConfigClientException : FirebaseRemoteConfigException
expect class FirebaseRemoteConfigFetchThrottledException : FirebaseRemoteConfigException
expect class FirebaseRemoteConfigServerException : FirebaseRemoteConfigException
