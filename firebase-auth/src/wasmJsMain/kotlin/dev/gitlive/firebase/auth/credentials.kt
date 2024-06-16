package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.externals.ApplicationVerifier
import dev.gitlive.firebase.auth.externals.EmailAuthProvider
import dev.gitlive.firebase.auth.externals.FacebookAuthProvider
import dev.gitlive.firebase.auth.externals.GithubAuthProvider
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.PhoneAuthProvider
import dev.gitlive.firebase.auth.externals.TwitterAuthProvider
import dev.gitlive.firebase.json
import kotlinx.coroutines.await
import dev.gitlive.firebase.auth.externals.AuthCredential as JsAuthCredential
import dev.gitlive.firebase.auth.externals.OAuthProvider as JsOAuthProvider

actual open class AuthCredential(val js: JsAuthCredential) {
    actual val providerId: String
        get() = js.providerId
}

actual class PhoneAuthCredential(js: JsAuthCredential) : AuthCredential(js)
actual class OAuthCredential(js: JsAuthCredential) : AuthCredential(js)

actual object EmailAuthProvider {
    actual fun credential(email: String, password: String): AuthCredential =
        AuthCredential(EmailAuthProvider.credential(email, password))

    actual fun credentialWithLink(
        email: String,
        emailLink: String
    ): AuthCredential = AuthCredential(EmailAuthProvider.credentialWithLink(email, emailLink))
}

actual object FacebookAuthProvider {
    actual fun credential(accessToken: String): AuthCredential =
        AuthCredential(FacebookAuthProvider.credential(accessToken))
}

actual object GithubAuthProvider {
    actual fun credential(token: String): AuthCredential =
        AuthCredential(GithubAuthProvider.credential(token))
}

actual object GoogleAuthProvider {
    actual fun credential(idToken: String?, accessToken: String?): AuthCredential {
        require(idToken != null || accessToken != null) {
            "Both parameters are optional but at least one must be present."
        }
        return AuthCredential(GoogleAuthProvider.credential(idToken, accessToken))
    }
}

actual class OAuthProvider(val js: JsOAuthProvider) {

    actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth
    ) : this(JsOAuthProvider(provider)) {
        rethrow {
            scopes.forEach { js.addScope(it) }
            js.setCustomParameters(json(customParameters))
        }
    }
    actual companion object {
        actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential = rethrow {
            JsOAuthProvider(providerId)
                .credential(
                    json(
                        "accessToken" to (accessToken),
                        "idToken" to (idToken),
                        "rawNonce" to (rawNonce)
                    ),
                    accessToken
                )
                .let { OAuthCredential(it) }
        }
    }
}

actual class PhoneAuthProvider(val js: PhoneAuthProvider) {

    actual constructor(auth: FirebaseAuth) : this(PhoneAuthProvider(auth.js))

    actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(PhoneAuthProvider.credential(verificationId, smsCode))
    actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential = rethrow {
        val verificationId: JsString = js.verifyPhoneNumber(phoneNumber, verificationProvider.verifier).await()
        val verificationCode = verificationProvider.getVerificationCode(verificationId.toString())
        credential(verificationId.toString(), verificationCode)
    }
}

actual interface PhoneVerificationProvider {
    val verifier: ApplicationVerifier
    suspend fun getVerificationCode(verificationId: String): String
}

actual object TwitterAuthProvider {
    actual fun credential(token: String, secret: String): AuthCredential = AuthCredential(TwitterAuthProvider.credential(token, secret))
}
