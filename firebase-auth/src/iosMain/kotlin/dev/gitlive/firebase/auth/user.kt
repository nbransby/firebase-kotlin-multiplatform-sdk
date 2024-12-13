/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.auth

import cocoapods.FirebaseAuth.FIRAuthDataResult
import cocoapods.FirebaseAuth.FIRUser
import cocoapods.FirebaseAuth.FIRUserInfoProtocol
import cocoapods.FirebaseAuth.FIRUserMetadata
import platform.Foundation.NSURL

public val FirebaseUser.ios: FIRUser get() = ios

public actual class FirebaseUser internal constructor(internal val ios: FIRUser) {
    public actual val uid: String
        get() = ios.uid()
    public actual val displayName: String?
        get() = ios.displayName()
    public actual val email: String?
        get() = ios.email()
    public actual val phoneNumber: String?
        get() = ios.phoneNumber()
    public actual val photoURL: String?
        get() = ios.photoURL()?.absoluteString
    public actual val isAnonymous: Boolean
        get() = ios.anonymous()
    public actual val isEmailVerified: Boolean
        get() = ios.emailVerified()
    public actual val metaData: UserMetaData?
        get() = UserMetaData(ios.metadata())
    public actual val multiFactor: MultiFactor
        get() = MultiFactor(ios.multiFactor())
    public actual val providerData: List<UserInfo>
        get() = ios.providerData().mapNotNull { provider -> (provider as? FIRUserInfoProtocol)?.let { UserInfo(it) } }
    public actual val providerId: String
        get() = ios.providerID()

    public actual suspend fun delete(): Unit = ios.await { deleteWithCompletion(it) }

    public actual suspend fun reload(): Unit = ios.await { reloadWithCompletion(it) }

    public actual suspend fun getIdToken(forceRefresh: Boolean): String? = ios.awaitResult { getIDTokenForcingRefresh(forceRefresh, it) }

    public actual suspend fun getIdTokenResult(forceRefresh: Boolean): AuthTokenResult = AuthTokenResult(ios.awaitResult { getIDTokenResultForcingRefresh(forceRefresh, it) })

    public actual suspend fun linkWithCredential(credential: AuthCredential): AuthResult = AuthResult(ios.awaitResult { linkWithCredential(credential.ios, it) })

    public actual suspend fun reauthenticate(credential: AuthCredential) {
        ios.awaitResult<FIRUser, FIRAuthDataResult?> { reauthenticateWithCredential(credential.ios, it) }
    }

    public actual suspend fun reauthenticateAndRetrieveData(credential: AuthCredential): AuthResult = AuthResult(ios.awaitResult { reauthenticateWithCredential(credential.ios, it) })

    public actual suspend fun sendEmailVerification(actionCodeSettings: ActionCodeSettings?): Unit = ios.await {
        actionCodeSettings?.let { settings -> sendEmailVerificationWithActionCodeSettings(settings.toIos(), it) }
            ?: sendEmailVerificationWithCompletion(it)
    }

    public actual suspend fun unlink(provider: String): FirebaseUser? {
        val user: FIRUser? = ios.awaitResult { unlinkFromProvider(provider, it) }
        return user?.let {
            FirebaseUser(it)
        }
    }
    public actual suspend fun updateEmail(email: String): Unit = ios.await { updateEmail(email, it) }
    public actual suspend fun updatePassword(password: String): Unit = ios.await { updatePassword(password, it) }
    public actual suspend fun updatePhoneNumber(credential: PhoneAuthCredential): Unit = ios.await { updatePhoneNumberCredential(credential.ios, it) }
    public actual suspend fun updateProfile(displayName: String?, photoUrl: String?) {
        val request = ios.profileChangeRequest()
            .apply { setDisplayName(displayName) }
            .apply { setPhotoURL(photoUrl?.let { NSURL.URLWithString(it) }) }
        ios.await { request.commitChangesWithCompletion(it) }
    }
    public actual suspend fun verifyBeforeUpdateEmail(newEmail: String, actionCodeSettings: ActionCodeSettings?): Unit = ios.await {
        actionCodeSettings?.let { actionSettings -> sendEmailVerificationBeforeUpdatingEmail(newEmail, actionSettings.toIos(), it) } ?: sendEmailVerificationBeforeUpdatingEmail(newEmail, it)
    }
}

public val UserInfo.ios: FIRUserInfoProtocol get() = ios

public actual class UserInfo(internal val ios: FIRUserInfoProtocol) {
    public actual val displayName: String?
        get() = ios.displayName()
    public actual val email: String?
        get() = ios.email()
    public actual val phoneNumber: String?
        get() = ios.phoneNumber()
    public actual val photoURL: String?
        get() = ios.photoURL()?.absoluteString
    public actual val providerId: String
        get() = ios.providerID()
    public actual val uid: String
        get() = ios.uid()
}

public val UserMetaData.ios: FIRUserMetadata get() = ios

public actual class UserMetaData(internal val ios: FIRUserMetadata) {
    public actual val creationTime: Double?
        get() = ios.creationDate()?.timeIntervalSinceReferenceDate
    public actual val lastSignInTime: Double?
        get() = ios.lastSignInDate()?.timeIntervalSinceReferenceDate
}
