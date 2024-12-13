package dev.gitlive.firebase.auth

import dev.gitlive.firebase.auth.externals.MultiFactorUser
import kotlinx.coroutines.await
import kotlin.js.Date
import dev.gitlive.firebase.auth.externals.MultiFactorAssertion as JsMultiFactorAssertion
import dev.gitlive.firebase.auth.externals.MultiFactorInfo as JsMultiFactorInfo
import dev.gitlive.firebase.auth.externals.MultiFactorResolver as JsMultiFactorResolver
import dev.gitlive.firebase.auth.externals.MultiFactorSession as JsMultiFactorSession

public val MultiFactor.js get() = js

public actual class MultiFactor(internal val js: MultiFactorUser) {
    public actual val enrolledFactors: List<MultiFactorInfo>
        get() = rethrow { js.enrolledFactors.map { MultiFactorInfo(it) } }
    public actual suspend fun enroll(multiFactorAssertion: MultiFactorAssertion, displayName: String?): Unit = rethrow { js.enroll(multiFactorAssertion.js, displayName).await() }
    public actual suspend fun getSession(): MultiFactorSession = rethrow { MultiFactorSession(js.getSession().await()) }
    public actual suspend fun unenroll(multiFactorInfo: MultiFactorInfo): Unit = rethrow { js.unenroll(multiFactorInfo.js).await() }
    public actual suspend fun unenroll(factorUid: String): Unit = rethrow { js.unenroll(factorUid).await() }
}

public val MultiFactorInfo.js get() = js

public actual class MultiFactorInfo(internal val js: JsMultiFactorInfo) {
    public actual val displayName: String?
        get() = rethrow { js.displayName }
    public actual val enrollmentTime: Double
        get() = rethrow { (Date(js.enrollmentTime).getTime() / 1000.0) }
    public actual val factorId: String
        get() = rethrow { js.factorId }
    public actual val uid: String
        get() = rethrow { js.uid }
}

public val MultiFactorAssertion.js get() = js

public actual class MultiFactorAssertion(internal val js: JsMultiFactorAssertion) {
    public actual val factorId: String
        get() = rethrow { js.factorId }
}

public val MultiFactorSession.js get() = js

public actual class MultiFactorSession(internal val js: JsMultiFactorSession)

public val MultiFactorResolver.js get() = js

public actual class MultiFactorResolver(internal val js: JsMultiFactorResolver) {
    public actual val auth: FirebaseAuth = rethrow { FirebaseAuth(js.auth) }
    public actual val hints: List<MultiFactorInfo> = rethrow { js.hints.map { MultiFactorInfo(it) } }
    public actual val session: MultiFactorSession = rethrow { MultiFactorSession(js.session) }

    public actual suspend fun resolveSignIn(assertion: MultiFactorAssertion): AuthResult = rethrow { AuthResult(js.resolveSignIn(assertion.js).await()) }
}
