package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.externals.MultiFactorUser
import kotlinx.coroutines.await
import dev.gitlive.firebase.auth.externals.MultiFactorAssertion as JsMultiFactorAssertion
import dev.gitlive.firebase.auth.externals.MultiFactorInfo as JsMultiFactorInfo
import dev.gitlive.firebase.auth.externals.MultiFactorResolver as JsMultiFactorResolver
import dev.gitlive.firebase.auth.externals.MultiFactorSession as JsMultiFactorSession

actual class MultiFactor(val js: MultiFactorUser) {
    actual val enrolledFactors: List<MultiFactorInfo>
        get() = rethrow { js.enrolledFactors.asSequence().filterNotNull().map { MultiFactorInfo(it) }.toList() }
    actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?): Unit =
        rethrow { js.enroll(multiFactorAssertion.js, displayName).await() }
    actual suspend fun getSession(): MultiFactorSession =
        rethrow { MultiFactorSession(js.getSession().await()) }
    actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo): Unit =
        rethrow { js.unenroll(multiFactorInfo.js).await() }
    actual suspend fun unenroll(factorUid: String): Unit =
        rethrow { js.unenroll(factorUid).await() }
}

actual class MultiFactorInfo(val js: JsMultiFactorInfo) {
    actual val displayName: String?
        get() = rethrow { js.displayName }
    actual val enrollmentTime: Double
        get() = error("Date error: ${js.enrollmentTime}") //rethrow { ((js.enrollmentTime).getTime() / 1000.0) }
    actual val factorId: String
        get() = rethrow { js.factorId }
    actual val uid: String
        get() = rethrow { js.uid }
}

actual class MultiFactorAssertion(val js: JsMultiFactorAssertion) {
    actual val factorId: String
        get() = rethrow { js.factorId }
}

actual class MultiFactorSession(val js: JsMultiFactorSession)

actual class MultiFactorResolver(val js: JsMultiFactorResolver) {
    actual val auth: FirebaseAuth = rethrow { FirebaseAuth(js.auth) }
    actual val hints: List<MultiFactorInfo> = rethrow { js.hints.asSequence().filterNotNull().map { MultiFactorInfo(it) }.toList() }
    actual val session: MultiFactorSession = rethrow { MultiFactorSession(js.session) }

    actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = rethrow { AuthResult(js.resolveSignIn(assertion.js).await()) }
}
