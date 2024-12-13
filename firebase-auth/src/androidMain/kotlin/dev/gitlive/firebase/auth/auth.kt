/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")

package dev.gitlive.firebase.auth

import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.ActionCodeMultiFactorInfo
import com.google.firebase.auth.ActionCodeResult.*
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android as publicAndroid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

public val FirebaseAuth.android: com.google.firebase.auth.FirebaseAuth get() = com.google.firebase.auth.FirebaseAuth.getInstance()

public actual val Firebase.auth: FirebaseAuth
    get() = FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance())

public actual fun Firebase.auth(app: FirebaseApp): FirebaseAuth = FirebaseAuth(com.google.firebase.auth.FirebaseAuth.getInstance(app.publicAndroid))

public actual class FirebaseAuth internal constructor(internal val android: com.google.firebase.auth.FirebaseAuth) {
    public actual val currentUser: FirebaseUser?
        get() = android.currentUser?.let { FirebaseUser(it) }

    public actual val authStateChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val listener = AuthStateListener { auth -> trySend(auth.currentUser?.let { FirebaseUser(it) }) }
        android.addAuthStateListener(listener)
        awaitClose { android.removeAuthStateListener(listener) }
    }

    public actual val idTokenChanged: Flow<FirebaseUser?> get() = callbackFlow {
        val listener = com.google.firebase.auth.FirebaseAuth.IdTokenListener { auth -> trySend(auth.currentUser?.let { FirebaseUser(it) }) }
        android.addIdTokenListener(listener)
        awaitClose { android.removeIdTokenListener(listener) }
    }

    public actual var languageCode: String
        get() = android.languageCode ?: ""
        set(value) {
            android.setLanguageCode(value)
        }

    public actual suspend fun applyActionCode(code: String) {
        android.applyActionCode(code).await()
    }
    public actual suspend fun confirmPasswordReset(code: String, newPassword: String) {
        android.confirmPasswordReset(code, newPassword).await()
    }

    public actual suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult = AuthResult(android.createUserWithEmailAndPassword(email, password).await())

    @Suppress("DEPRECATION")
    public actual suspend fun fetchSignInMethodsForEmail(email: String): List<String> = android.fetchSignInMethodsForEmail(email).await().signInMethods.orEmpty()

    public actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?) {
        android.sendPasswordResetEmail(email, actionCodeSettings?.toAndroid()).await()
    }

    public actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings) {
        android.sendSignInLinkToEmail(email, actionCodeSettings.toAndroid()).await()
    }

    public actual fun isSignInWithEmailLink(link: String): Boolean = android.isSignInWithEmailLink(link)

    public actual suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult = AuthResult(android.signInWithEmailAndPassword(email, password).await())

    public actual suspend fun signInWithCustomToken(token: String): AuthResult = AuthResult(android.signInWithCustomToken(token).await())

    public actual suspend fun signInAnonymously(): AuthResult = AuthResult(android.signInAnonymously().await())

    public actual suspend fun signInWithCredential(authCredential: AuthCredential): AuthResult = AuthResult(android.signInWithCredential(authCredential.android).await())

    public actual suspend fun signInWithEmailLink(email: String, link: String): AuthResult = AuthResult(android.signInWithEmailLink(email, link).await())

    public actual suspend fun signOut(): Unit = android.signOut()

    public actual suspend fun updateCurrentUser(user: FirebaseUser) {
        android.updateCurrentUser(user.android).await()
    }
    public actual suspend fun verifyPasswordResetCode(code: String): String = android.verifyPasswordResetCode(code).await()

    public actual suspend fun <T : ActionCodeResult> checkActionCode(code: String): T {
        val result = android.checkActionCode(code).await()
        @Suppress("UNCHECKED_CAST")
        return when (result.operation) {
            SIGN_IN_WITH_EMAIL_LINK -> ActionCodeResult.SignInWithEmailLink
            VERIFY_EMAIL -> ActionCodeResult.VerifyEmail(result.info!!.email)
            PASSWORD_RESET -> ActionCodeResult.PasswordReset(result.info!!.email)
            RECOVER_EMAIL -> (result.info as ActionCodeEmailInfo).run {
                ActionCodeResult.RecoverEmail(email, previousEmail)
            }
            VERIFY_BEFORE_CHANGE_EMAIL -> (result.info as ActionCodeEmailInfo).run {
                ActionCodeResult.VerifyBeforeChangeEmail(email, previousEmail)
            }
            REVERT_SECOND_FACTOR_ADDITION -> (result.info as ActionCodeMultiFactorInfo).run {
                ActionCodeResult.RevertSecondFactorAddition(email, MultiFactorInfo(multiFactorInfo))
            }
            ERROR -> throw UnsupportedOperationException(result.operation.toString())
            else -> throw UnsupportedOperationException(result.operation.toString())
        } as T
    }

    public actual fun useEmulator(host: String, port: Int): Unit = android.useEmulator(host, port)
}

public val AuthResult.android: com.google.firebase.auth.AuthResult get() = android

public actual class AuthResult(internal val android: com.google.firebase.auth.AuthResult) {
    public actual val user: FirebaseUser?
        get() = android.user?.let { FirebaseUser(it) }
    public actual val credential: AuthCredential?
        get() = android.credential?.let { AuthCredential(it) }
    public actual val additionalUserInfo: AdditionalUserInfo?
        get() = android.additionalUserInfo?.let { AdditionalUserInfo(it) }
}

public val AdditionalUserInfo.android: com.google.firebase.auth.AdditionalUserInfo
    get() = android

public actual class AdditionalUserInfo(
    internal val android: com.google.firebase.auth.AdditionalUserInfo,
) {
    public actual val providerId: String?
        get() = android.providerId
    public actual val username: String?
        get() = android.username
    public actual val profile: Map<String, Any?>?
        get() = android.profile
    public actual val isNewUser: Boolean
        get() = android.isNewUser
}

public val AuthTokenResult.android: com.google.firebase.auth.GetTokenResult get() = android

public actual class AuthTokenResult(internal val android: com.google.firebase.auth.GetTokenResult) {
//    actual val authTimestamp: Long
//        get() = android.authTimestamp
    public actual val claims: Map<String, Any>
        get() = android.claims

//    actual val expirationTimestamp: Long
//        get() = android.expirationTimestamp
//    actual val issuedAtTimestamp: Long
//        get() = android.issuedAtTimestamp
    public actual val signInProvider: String?
        get() = android.signInProvider
    public actual val token: String?
        get() = android.token
}

internal fun ActionCodeSettings.toAndroid() = com.google.firebase.auth.ActionCodeSettings.newBuilder()
    .setUrl(url)
    .also { androidPackageName?.run { it.setAndroidPackageName(packageName, installIfNotAvailable, minimumVersion) } }
    .also { dynamicLinkDomain?.run { it.setDynamicLinkDomain(this) } }
    .setHandleCodeInApp(canHandleCodeInApp)
    .also { iOSBundleId?.run { it.setIOSBundleId(this) } }
    .build()

public actual typealias FirebaseAuthException = com.google.firebase.auth.FirebaseAuthException
public actual typealias FirebaseAuthActionCodeException = com.google.firebase.auth.FirebaseAuthActionCodeException
public actual typealias FirebaseAuthEmailException = com.google.firebase.auth.FirebaseAuthEmailException
public actual typealias FirebaseAuthInvalidCredentialsException = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
public actual typealias FirebaseAuthWeakPasswordException = com.google.firebase.auth.FirebaseAuthWeakPasswordException
public actual typealias FirebaseAuthInvalidUserException = com.google.firebase.auth.FirebaseAuthInvalidUserException
public actual typealias FirebaseAuthMultiFactorException = com.google.firebase.auth.FirebaseAuthMultiFactorException
public actual typealias FirebaseAuthRecentLoginRequiredException = com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
public actual typealias FirebaseAuthUserCollisionException = com.google.firebase.auth.FirebaseAuthUserCollisionException
public actual typealias FirebaseAuthWebException = com.google.firebase.auth.FirebaseAuthWebException
