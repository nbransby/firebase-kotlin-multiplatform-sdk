package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.externals.ApplicationVerifier
import dev.gitlive.firebase.auth.externals.EmailAuthProvider
import dev.gitlive.firebase.auth.externals.FacebookAuthProvider
import dev.gitlive.firebase.auth.externals.GithubAuthProvider
import dev.gitlive.firebase.auth.externals.GoogleAuthProvider
import dev.gitlive.firebase.auth.externals.PhoneAuthProvider as JsPhoneAuthProvider
import dev.gitlive.firebase.auth.externals.TwitterAuthProvider
import kotlinx.coroutines.await
import kotlin.js.json
import dev.gitlive.firebase.auth.externals.AuthCredential as JsAuthCredential
import dev.gitlive.firebase.auth.externals.OAuthProvider as JsOAuthProvider

public val AuthCredential.js: JsAuthCredential get() = js

public actual open class AuthCredential(internal val js: JsAuthCredential) {
    public actual val providerId: String
        get() = js.providerId
}

public actual class PhoneAuthCredential(js: JsAuthCredential) : AuthCredential(js)
public actual class OAuthCredential(js: JsAuthCredential) : AuthCredential(js)

public actual object EmailAuthProvider {
    public actual fun credential(email: String, password: String): AuthCredential = AuthCredential(EmailAuthProvider.credential(email, password))

    public actual fun credentialWithLink(
        email: String,
        emailLink: String,
    ): AuthCredential = AuthCredential(EmailAuthProvider.credentialWithLink(email, emailLink))
}

public actual object FacebookAuthProvider {
    public actual fun credential(accessToken: String): AuthCredential = AuthCredential(FacebookAuthProvider.credential(accessToken))
}

public actual object GithubAuthProvider {
    public actual fun credential(token: String): AuthCredential = AuthCredential(GithubAuthProvider.credential(token))
}

public actual object GoogleAuthProvider {
    public actual fun credential(idToken: String?, accessToken: String?): AuthCredential {
        require(idToken != null || accessToken != null) {
            "Both parameters are optional but at least one must be present."
        }
        return AuthCredential(GoogleAuthProvider.credential(idToken, accessToken))
    }
}

public val OAuthProvider.js: JsOAuthProvider get() = js

public actual class OAuthProvider(internal val js: JsOAuthProvider) {

    public actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth,
    ) : this(JsOAuthProvider(provider)) {
        rethrow {
            scopes.forEach { js.addScope(it) }
            js.setCustomParameters(customParameters)
        }
    }
    public actual companion object {
        public actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential = rethrow {
            JsOAuthProvider(providerId)
                .credential(
                    json(
                        "accessToken" to (accessToken ?: undefined),
                        "idToken" to (idToken ?: undefined),
                        "rawNonce" to (rawNonce ?: undefined),
                    ),
                    accessToken ?: undefined,
                )
                .let { OAuthCredential(it) }
        }
    }
}

public val PhoneAuthProvider.js: JsPhoneAuthProvider get() = js

public actual class PhoneAuthProvider(internal val js: JsPhoneAuthProvider) {

    public actual constructor(auth: FirebaseAuth) : this(JsPhoneAuthProvider(auth.js))

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(JsPhoneAuthProvider.credential(verificationId, smsCode))
    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential = rethrow {
        val verificationId = js.verifyPhoneNumber(phoneNumber, verificationProvider.verifier).await()
        val verificationCode = verificationProvider.getVerificationCode(verificationId)
        credential(verificationId, verificationCode)
    }
}

public actual interface PhoneVerificationProvider {
    public val verifier: ApplicationVerifier
    public suspend fun getVerificationCode(verificationId: String): String
}

public actual object TwitterAuthProvider {
    public actual fun credential(token: String, secret: String): AuthCredential = AuthCredential(TwitterAuthProvider.credential(token, secret))
}
