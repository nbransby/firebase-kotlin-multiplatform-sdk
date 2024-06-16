/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.FirebaseNetworkException
import dev.gitlive.firebase.JsObject
import dev.gitlive.firebase.asSequence
import dev.gitlive.firebase.auth.externals.*
import dev.gitlive.firebase.json
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import dev.gitlive.firebase.auth.externals.AuthResult as JsAuthResult

actual val Firebase.auth
    get() = rethrow { FirebaseAuth(getAuth()) }

actual fun Firebase.auth(app: FirebaseApp) =
    rethrow { FirebaseAuth(getAuth(app.js)) }

actual class FirebaseAuth internal constructor(val js: Auth) {

    actual val currentUser: FirebaseUser?
        get() = rethrow { js.currentUser?.let { FirebaseUser(it) } }

    actual val authStateChanged get() = callbackFlow<FirebaseUser?> {
        val unsubscribe = js.onAuthStateChanged {
            trySend(it?.let { FirebaseUser(it) })
        }
        awaitClose { unsubscribe() }
    }

    actual val idTokenChanged get() = callbackFlow<FirebaseUser?> {
        val unsubscribe = js.onIdTokenChanged {
            trySend(it?.let { FirebaseUser(it) })
        }
        awaitClose { unsubscribe() }
    }

    actual var languageCode: String
        get() = js.languageCode ?: ""
        set(value) { js.languageCode = value }

    actual suspend fun applyActionCode(code: String): Unit = rethrow { applyActionCode(js, code).await() }
    actual suspend fun confirmPasswordReset(code: String, newPassword: String): Unit = rethrow { confirmPasswordReset(js, code, newPassword).await() }

    actual suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        rethrow { AuthResult(createUserWithEmailAndPassword(js, email, password).await()) }

    actual suspend fun fetchSignInMethodsForEmail(email: String): List<String> = rethrow { fetchSignInMethodsForEmail(js, email).await<JsArray<JsString>>().asSequence().map { it.toString() }.toList() }

    actual suspend fun sendPasswordResetEmail(email: String, actionCodeSettings: ActionCodeSettings?): Unit =
        rethrow { sendPasswordResetEmail(js, email, actionCodeSettings?.toJson()).await() }

    actual suspend fun sendSignInLinkToEmail(email: String, actionCodeSettings: ActionCodeSettings): Unit =
        rethrow { sendSignInLinkToEmail(js, email, actionCodeSettings.toJson()).await() }

    actual fun isSignInWithEmailLink(link: String) = rethrow { isSignInWithEmailLink(js, link) }

    actual suspend fun signInWithEmailAndPassword(email: String, password: String) =
        rethrow { AuthResult(signInWithEmailAndPassword(js, email, password).await()) }

    actual suspend fun signInWithCustomToken(token: String) =
        rethrow { AuthResult(signInWithCustomToken(js, token).await()) }

    actual suspend fun signInAnonymously() =
        rethrow { AuthResult(signInAnonymously(js).await()) }

    actual suspend fun signInWithCredential(authCredential: AuthCredential) =
        rethrow { AuthResult(signInWithCredential(js, authCredential.js).await()) }

    actual suspend fun signInWithEmailLink(email: String, link: String) =
        rethrow { AuthResult(signInWithEmailLink(js, email, link).await()) }

    actual suspend fun signOut(): Unit = rethrow { signOut(js).await() }

    actual suspend fun updateCurrentUser(user: FirebaseUser): Unit =
        rethrow { updateCurrentUser(js, user.js).await() }

    actual suspend fun verifyPasswordResetCode(code: String): String =
        rethrow { verifyPasswordResetCode(js, code).await() }

    actual suspend fun <T : ActionCodeResult> checkActionCode(code: String): T = rethrow {
        val result: ActionCodeInfo = checkActionCode(js, code).await()
        @Suppress("UNCHECKED_CAST")
        return when(result.operation) {
            "EMAIL_SIGNIN" -> ActionCodeResult.SignInWithEmailLink
            "VERIFY_EMAIL" -> ActionCodeResult.VerifyEmail(result.data.email!!)
            "PASSWORD_RESET" -> ActionCodeResult.PasswordReset(result.data.email!!)
            "RECOVER_EMAIL" -> ActionCodeResult.RecoverEmail(result.data.email!!, result.data.previousEmail!!)
            "VERIFY_AND_CHANGE_EMAIL" -> ActionCodeResult.VerifyBeforeChangeEmail(
                result.data.email!!,
                result.data.previousEmail!!
            )
            "REVERT_SECOND_FACTOR_ADDITION" -> ActionCodeResult.RevertSecondFactorAddition(
                result.data.email!!,
                result.data.multiFactorInfo?.let { MultiFactorInfo(it) }
            )
            else -> throw UnsupportedOperationException(result.operation)
        } as T
    }

    actual fun useEmulator(host: String, port: Int) = rethrow { connectAuthEmulator(js, "http://$host:$port") }
}

actual class AuthResult internal constructor(val js: JsAuthResult) {
    actual val user: FirebaseUser?
        get() = rethrow { js.user?.let { FirebaseUser(it) } }
}

actual class AuthTokenResult(val tokenResult: IdTokenResult) {
//    actual val authTimestamp: Long
//        get() = js.authTime
    actual val claims: Map<String, Any>
        get() = JsObjectKeys(tokenResult.claims).asSequence().mapNotNull { key ->
            val keyJsString = key as? JsString ?: return@mapNotNull null
            val value = tokenResult.claims[keyJsString] ?: return@mapNotNull null
            val keyString = keyJsString.toString()
            keyString to value
        }.toList().toMap()
//    actual val expirationTimestamp: Long
//        get() = android.expirationTime
//    actual val issuedAtTimestamp: Long
//        get() = js.issuedAtTime
    actual val signInProvider: String?
        get() = tokenResult.signInProvider
    actual val token: String?
        get() = tokenResult.token
}

internal fun ActionCodeSettings.toJson() = json(
    "url" to url,
    "android" to (androidPackageName?.run { json("installApp" to installIfNotAvailable, "minimumVersion" to minimumVersion, "packageName" to packageName) }),
    "dynamicLinkDomain" to (dynamicLinkDomain ?: null),
    "handleCodeInApp" to canHandleCodeInApp,
    "ios" to (iOSBundleId?.run { json("bundleId" to iOSBundleId) } ?: null)
)

actual open class FirebaseAuthException(code: String?, cause: Throwable): FirebaseException(code, cause)
actual open class FirebaseAuthActionCodeException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthEmailException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthInvalidCredentialsException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthWeakPasswordException(code: String?, cause: Throwable): FirebaseAuthInvalidCredentialsException(code, cause)
actual open class FirebaseAuthInvalidUserException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthMultiFactorException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthRecentLoginRequiredException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthUserCollisionException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)
actual open class FirebaseAuthWebException(code: String?, cause: Throwable): FirebaseAuthException(code, cause)


internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.auth.rethrow { function() }

private inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    }
}

private fun errorToException(o: JsObject): FirebaseException {
    val cause = Exception(o.toString())
    return when(val code = o["code".toJsString()]?.toString()?.lowercase()) {
        "auth/invalid-user-token" -> FirebaseAuthInvalidUserException(code, cause)
        "auth/requires-recent-login" -> FirebaseAuthRecentLoginRequiredException(code, cause)
        "auth/user-disabled" -> FirebaseAuthInvalidUserException(code, cause)
        "auth/user-token-expired" -> FirebaseAuthInvalidUserException(code, cause)
        "auth/web-storage-unsupported" -> FirebaseAuthWebException(code, cause)
        "auth/network-request-failed" -> FirebaseNetworkException(code, cause)
        "auth/timeout" -> FirebaseNetworkException(code, cause)
        "auth/weak-password" -> FirebaseAuthWeakPasswordException(code, cause)
        "auth/invalid-credential",
        "auth/invalid-verification-code",
        "auth/missing-verification-code",
        "auth/invalid-verification-id",
        "auth/missing-verification-id" -> FirebaseAuthInvalidCredentialsException(code, cause)
        "auth/maximum-second-factor-count-exceeded",
        "auth/second-factor-already-in-use" -> FirebaseAuthMultiFactorException(code, cause)
        "auth/credential-already-in-use" -> FirebaseAuthUserCollisionException(code, cause)
        "auth/email-already-in-use" -> FirebaseAuthUserCollisionException(code, cause)
        "auth/invalid-email" -> FirebaseAuthEmailException(code, cause)
//                "auth/app-deleted" ->
//                "auth/app-not-authorized" ->
//                "auth/argument-error" ->
//                "auth/invalid-api-key" ->
//                "auth/operation-not-allowed" ->
//                "auth/too-many-arguments" ->
//                "auth/unauthorized-domain" ->
        else -> {
            FirebaseAuthException(code, cause)
        }
    }
}

@JsFun("(output) => Object.keys(output)")
external fun JsObjectKeys(vararg output: JsObject?): JsArray<JsAny?>

fun <T: JsAny?> JsArray<T>.asSequence(): Sequence<T?> {
    var i = 0
    return sequence {
        while (i++ < length) {
            yield(get(i))
        }
    }
}
