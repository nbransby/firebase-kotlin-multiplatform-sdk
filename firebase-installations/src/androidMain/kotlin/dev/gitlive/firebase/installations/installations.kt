package dev.gitlive.firebase.installations

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android
import kotlinx.coroutines.tasks.await

public val FirebaseInstallations.android: com.google.firebase.installations.FirebaseInstallations get() = com.google.firebase.installations.FirebaseInstallations.getInstance()

public actual val Firebase.installations: FirebaseInstallations
    get() = FirebaseInstallations(com.google.firebase.installations.FirebaseInstallations.getInstance())

public actual fun Firebase.installations(app: FirebaseApp): FirebaseInstallations = FirebaseInstallations(com.google.firebase.installations.FirebaseInstallations.getInstance(app.android))

public actual class FirebaseInstallations internal constructor(internal val android: com.google.firebase.installations.FirebaseInstallations) {

    public actual suspend fun delete(): Unit = android.delete().await().let { }

    public actual suspend fun getId(): String = android.id.await()

    public actual suspend fun getToken(forceRefresh: Boolean): String = android.getToken(forceRefresh).await().token
}

public actual typealias FirebaseInstallationsException = com.google.firebase.installations.FirebaseInstallationsException
