@file:JsModule("firebase/auth")

package dev.gitlive.firebase.auth.externals

import dev.gitlive.firebase.JsObject
import dev.gitlive.firebase.Unsubscribe
import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Promise

external fun applyActionCode(auth: Auth, code: String): Promise<JsAny?>

external fun checkActionCode(auth: Auth, code: String): Promise<ActionCodeInfo>

external fun confirmPasswordReset(auth: Auth, code: String, newPassword: String): Promise<JsAny?>

external fun connectAuthEmulator(auth: Auth, url: String, options: JsAny? = definedExternally)

external fun createUserWithEmailAndPassword(
    auth: Auth,
    email: String,
    password: String
): Promise<AuthResult>

external fun deleteUser(user: User): Promise<JsAny?>

external fun fetchSignInMethodsForEmail(auth: Auth, email: String): Promise<JsArray<JsString>>

external fun getAuth(app: FirebaseApp? = definedExternally): Auth

external fun initializeAuth(app: FirebaseApp? = definedExternally, deps: JsAny = definedExternally): Auth

external fun getIdToken(user: User, forceRefresh: Boolean?): Promise<JsString>

external fun getIdTokenResult(user: User, forceRefresh: Boolean?): Promise<IdTokenResult>

external fun isSignInWithEmailLink(auth: Auth, link: String): Boolean

external fun linkWithCredential(user: User, credential: AuthCredential): Promise<AuthResult>

external fun multiFactor(user: User): MultiFactorUser

external fun onAuthStateChanged(auth: Auth, nextOrObserver: (User?) -> Unit): Unsubscribe

external fun onIdTokenChanged(auth: Auth, nextOrObserver: (User?) -> Unit): Unsubscribe

external fun sendEmailVerification(user: User, actionCodeSettings: JsAny?): Promise<JsAny?>

external fun reauthenticateWithCredential(
    user: User,
    credential: AuthCredential
): Promise<AuthResult>

external fun reload(user: User): Promise<JsAny?>

external fun sendPasswordResetEmail(
    auth: Auth,
    email: String,
    actionCodeSettings: JsAny?
): Promise<JsAny?>

external fun sendSignInLinkToEmail(
    auth: Auth,
    email: String,
    actionCodeSettings: JsAny?
): Promise<JsAny?>

external fun signInAnonymously(auth: Auth): Promise<AuthResult>

external fun signInWithCredential(auth: Auth, authCredential: AuthCredential): Promise<AuthResult>

external fun signInWithCustomToken(auth: Auth, token: String): Promise<AuthResult>

external fun signInWithEmailAndPassword(
    auth: Auth,
    email: String,
    password: String
): Promise<AuthResult>

external fun signInWithEmailLink(auth: Auth, email: String, link: String): Promise<AuthResult>

external fun signInWithPopup(auth: Auth, provider: AuthProvider): Promise<UserCredential>

external fun signInWithRedirect(auth: Auth, provider: AuthProvider): Promise<Nothing>

external fun getRedirectResult(auth: Auth): Promise<UserCredential?>

external fun signOut(auth: Auth): Promise<JsAny?>

external fun unlink(user: User, providerId: String): Promise<User>

external fun updateCurrentUser(auth: Auth, user: User?): Promise<JsAny?>

external fun updateEmail(user: User, newEmail: String): Promise<JsAny?>

external fun updatePassword(user: User, newPassword: String): Promise<JsAny?>

external fun updatePhoneNumber(user: User, phoneCredential: AuthCredential): Promise<JsAny?>

external fun updateProfile(user: User, profile: JsAny): Promise<JsAny?>

external fun verifyBeforeUpdateEmail(
    user: User,
    newEmail: String,
    actionCodeSettings: JsAny?
): Promise<JsAny?>

external fun verifyPasswordResetCode(auth: Auth, code: String): Promise<JsString>

external interface Auth {
    val currentUser: User?
    var languageCode: String?

    fun onAuthStateChanged(nextOrObserver: (User?) -> Unit): Unsubscribe
    fun onIdTokenChanged(nextOrObserver: (User?) -> Unit): Unsubscribe
    fun signOut(): Promise<JsAny?>
    fun updateCurrentUser(user: User?): Promise<JsAny?>
}

external interface UserInfo: JsAny {
    val displayName: String?
    val email: String?
    val phoneNumber: String?
    val photoURL: String?
    val providerId: String
    val uid: String
}

external interface User : UserInfo, JsAny {
    val emailVerified: Boolean
    val isAnonymous: Boolean
    val metadata: UserMetadata
    val providerData: JsArray<UserInfo>
    val refreshToken: String
    val tenantId: String?

    fun delete(): Promise<JsAny?>
    fun getIdToken(forceRefresh: Boolean?): Promise<JsString>
    fun getIdTokenResult(forceRefresh: Boolean?): Promise<IdTokenResult>
    fun reload(): Promise<JsAny?>
}

external interface UserMetadata {
    val creationTime: String?
    val lastSignInTime: String?
}

external interface IdTokenResult: JsAny {
    val authTime: String
    val claims: JsObject
    val expirationTime: String
    val issuedAtTime: String
    val signInProvider: String?
    val signInSecondFactor: String?
    val token: String
}

external interface ActionCodeInfo: JsAny {
    val operation: String
    val data: ActionCodeData
}

external interface ActionCodeData {
    val email: String?
    val multiFactorInfo: MultiFactorInfo?
    val previousEmail: String?
}

external interface AuthResult: JsAny {
    val credential: AuthCredential?
    val operationType: String?
    val user: User?
}

external interface AuthCredential {
    val providerId: String
    val signInMethod: String
}

external interface OAuthCredential : AuthCredential {
    val accessToken: String?
    val idToken: String?
    val secret: String?
}

external interface UserCredential: JsAny {
    val operationType: String
    val providerId: String?
    val user: User
}

external interface ProfileUpdateRequest {
    val displayName: String?
    val photoURL: String?
}

external interface MultiFactorUser {
    val enrolledFactors: JsArray<MultiFactorInfo>

    fun enroll(assertion: MultiFactorAssertion, displayName: String?): Promise<JsAny?>
    fun getSession(): Promise<MultiFactorSession>
    fun unenroll(option: MultiFactorInfo): Promise<JsAny?>
    fun unenroll(option: String): Promise<JsAny?>
}

external interface MultiFactorInfo: JsAny {
    val displayName: String?
    val enrollmentTime: String
    val factorId: String
    val uid: String
}

external interface MultiFactorAssertion {
    val factorId: String
}

external interface MultiFactorSession: JsAny

external interface MultiFactorResolver {
    val auth: Auth
    val hints: JsArray<MultiFactorInfo>
    val session: MultiFactorSession

    fun resolveSignIn(assertion: MultiFactorAssertion): Promise<AuthResult>
}

external interface AuthProvider

external interface AuthError

external object EmailAuthProvider : AuthProvider {
    fun credential(email: String, password: String): AuthCredential
    fun credentialWithLink(email: String, emailLink: String): AuthCredential
}

external object FacebookAuthProvider : AuthProvider {
    fun credential(token: String): AuthCredential
}

external object GithubAuthProvider : AuthProvider {
    fun credential(token: String): AuthCredential
}

external class GoogleAuthProvider : AuthProvider {
    fun addScope(scope: String)
    companion object {
        fun credential(idToken: String?, accessToken: String?): AuthCredential
        fun credentialFromResult(userCredential: UserCredential): OAuthCredential?
        fun credentialFromError(error: AuthError): OAuthCredential?
    }
}

external class OAuthProvider(providerId: String) : AuthProvider {
    val providerId: String
    fun credential(optionsOrIdToken: JsObject?, accessToken: String?): AuthCredential

    fun addScope(scope: String)
    fun setCustomParameters(customOAuthParameters: JsObject)
}

external interface OAuthCredentialOptions {
    val accessToken: String?
    val idToken: String?
    val rawNonce: String?
}

external class PhoneAuthProvider(auth: Auth?) : AuthProvider {
    companion object {
        fun credential(
            verificationId: String,
            verificationCode: String
        ): AuthCredential
    }

    fun verifyPhoneNumber(
        phoneInfoOptions: String,
        applicationVerifier: ApplicationVerifier
    ): Promise<JsString>
}

external interface ApplicationVerifier {
    val type: String
    fun verify(): Promise<JsString>
}

external object TwitterAuthProvider : AuthProvider {
    fun credential(token: String, secret: String): AuthCredential
}

external interface Persistence {
    val type: String
}

external val browserLocalPersistence: Persistence
external val browserSessionPersistence: Persistence
external val indexedDBLocalPersistence: Persistence
external val inMemoryPersistence: Persistence

external fun setPersistence(auth: Auth, persistence: Persistence): Promise<JsAny?>;
