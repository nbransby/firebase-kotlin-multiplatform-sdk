/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.*

public actual open class AuthCredential(public open val ios: FIRAuthCredential) {
    public actual val providerId: String
        get() = ios.provider()
}

public actual class PhoneAuthCredential(override val ios: FIRPhoneAuthCredential) : AuthCredential(ios)
public actual class OAuthCredential(override val ios: FIROAuthCredential) : AuthCredential(ios)

public actual object EmailAuthProvider {
    public actual fun credential(
        email: String,
        password: String,
    ): AuthCredential = AuthCredential(FIREmailAuthProvider.credentialWithEmail(email = email, password = password))

    public actual fun credentialWithLink(
        email: String,
        emailLink: String,
    ): AuthCredential = AuthCredential(FIREmailAuthProvider.credentialWithEmail(email = email, link = emailLink))
}

public actual object FacebookAuthProvider {
    public actual fun credential(accessToken: String): AuthCredential = AuthCredential(FIRFacebookAuthProvider.credentialWithAccessToken(accessToken))
}

public actual object GithubAuthProvider {
    public actual fun credential(token: String): AuthCredential = AuthCredential(FIRGitHubAuthProvider.credentialWithToken(token))
}

public actual object GoogleAuthProvider {
    public actual fun credential(idToken: String?, accessToken: String?): AuthCredential {
        requireNotNull(idToken) { "idToken must not be null" }
        requireNotNull(accessToken) { "accessToken must not be null" }
        return AuthCredential(FIRGoogleAuthProvider.credentialWithIDToken(idToken, accessToken))
    }
}

public val OAuthProvider.ios: FIROAuthProvider get() = ios

public actual class OAuthProvider(internal val ios: FIROAuthProvider) {

    public actual constructor(
        provider: String,
        scopes: List<String>,
        customParameters: Map<String, String>,
        auth: FirebaseAuth,
    ) : this(FIROAuthProvider.providerWithProviderID(provider, auth.ios)) {
        ios.setScopes(scopes)
        @Suppress("UNCHECKED_CAST")
        ios.setCustomParameters(customParameters as Map<Any?, *>)
    }

    public actual companion object {
        public actual fun credential(providerId: String, accessToken: String?, idToken: String?, rawNonce: String?): OAuthCredential {
            val credential = when {
                idToken == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, accessToken = accessToken!!)
                accessToken == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce!!)
                rawNonce == null -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, accessToken = accessToken)
                else -> FIROAuthProvider.credentialWithProviderID(providerID = providerId, IDToken = idToken, rawNonce = rawNonce, accessToken = accessToken)
            }
            return OAuthCredential(credential)
        }
    }
}

public val PhoneAuthProvider.ios: FIRPhoneAuthProvider get() = ios

public actual class PhoneAuthProvider(internal val ios: FIRPhoneAuthProvider) {

    public actual constructor(auth: FirebaseAuth) : this(FIRPhoneAuthProvider.providerWithAuth(auth.ios))

    public actual fun credential(verificationId: String, smsCode: String): PhoneAuthCredential = PhoneAuthCredential(ios.credentialWithVerificationID(verificationId, smsCode))

    public actual suspend fun verifyPhoneNumber(phoneNumber: String, verificationProvider: PhoneVerificationProvider): AuthCredential {
        val verificationId: String = ios.awaitResult { ios.verifyPhoneNumber(phoneNumber, verificationProvider.delegate, it) }
        val verificationCode = verificationProvider.getVerificationCode()
        return credential(verificationId, verificationCode)
    }
}

public actual interface PhoneVerificationProvider {
    public val delegate: FIRAuthUIDelegateProtocol?
    public suspend fun getVerificationCode(): String
}

public actual object TwitterAuthProvider {
    public actual fun credential(token: String, secret: String): AuthCredential = AuthCredential(FIRTwitterAuthProvider.credentialWithToken(token, secret))
}
