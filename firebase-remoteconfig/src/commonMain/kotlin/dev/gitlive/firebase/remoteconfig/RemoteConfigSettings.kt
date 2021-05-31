package dev.gitlive.firebase.remoteconfig

private const val CONNECTION_TIMEOUT_IN_SECONDS = 60L

// https://firebase.google.com/docs/remote-config/get-started?hl=en&platform=android#throttling
private const val DEFAULT_FETCH_INTERVAL_IN_SECONDS = 12 * 3600L

data class RemoteConfigSettings(
    val fetchTimeoutInSeconds: Long = CONNECTION_TIMEOUT_IN_SECONDS,
    val minimumFetchIntervalInSeconds: Long = DEFAULT_FETCH_INTERVAL_IN_SECONDS,
)
