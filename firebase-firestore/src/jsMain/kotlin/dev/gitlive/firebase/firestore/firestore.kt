/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.externals.getApp
import dev.gitlive.firebase.firestore.externals.MemoryCacheSettings
import dev.gitlive.firebase.firestore.externals.PersistentCacheSettings
import dev.gitlive.firebase.firestore.externals.memoryEagerGarbageCollector
import dev.gitlive.firebase.firestore.externals.memoryLocalCache
import dev.gitlive.firebase.firestore.externals.memoryLruGarbageCollector
import dev.gitlive.firebase.firestore.externals.persistentLocalCache
import dev.gitlive.firebase.firestore.internal.NativeDocumentSnapshotWrapper
import dev.gitlive.firebase.firestore.internal.NativeFirebaseFirestoreWrapper
import dev.gitlive.firebase.js
import kotlin.js.Json
import kotlin.js.json
import dev.gitlive.firebase.firestore.externals.Firestore as JsFirestore
import dev.gitlive.firebase.firestore.externals.CollectionReference as JsCollectionReference
import dev.gitlive.firebase.firestore.externals.DocumentChange as JsDocumentChange
import dev.gitlive.firebase.firestore.externals.DocumentReference as JsDocumentReference
import dev.gitlive.firebase.firestore.externals.DocumentSnapshot as JsDocumentSnapshot
import dev.gitlive.firebase.firestore.externals.FieldPath as JsFieldPath
import dev.gitlive.firebase.firestore.externals.Query as JsQuery
import dev.gitlive.firebase.firestore.externals.QuerySnapshot as JsQuerySnapshot
import dev.gitlive.firebase.firestore.externals.SnapshotMetadata as JsSnapshotMetadata
import dev.gitlive.firebase.firestore.externals.Transaction as JsTransaction
import dev.gitlive.firebase.firestore.externals.WriteBatch as JsWriteBatch
import dev.gitlive.firebase.firestore.externals.documentId as jsDocumentId

public actual val Firebase.firestore: FirebaseFirestore get() =
    rethrow { FirebaseFirestore(NativeFirebaseFirestoreWrapper(getApp())) }

public actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore = rethrow { FirebaseFirestore(NativeFirebaseFirestoreWrapper(app.js)) }

internal actual data class NativeFirebaseFirestore(val js: JsFirestore)

public operator fun FirebaseFirestore.Companion.invoke(js: JsFirestore): FirebaseFirestore = FirebaseFirestore(
    NativeFirebaseFirestore(js),
)
public val FirebaseFirestore.js: JsFirestore get() = native.js

public actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
) {

    public actual companion object {
        public actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024

        // According to documentation, default JS Firestore cache size is 40MB, not 100MB
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 40 * 1024 * 1024
    }

    public actual class Builder internal constructor(
        public actual var sslEnabled: Boolean,
        public actual var host: String,
        public actual var cacheSettings: LocalCacheSettings,
    ) {

        public actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
        )
        public actual constructor(settings: FirebaseFirestoreSettings) : this(settings.sslEnabled, settings.host, settings.cacheSettings)

        public actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings)
    }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    internal val js: Json get() = json().apply {
        set("ssl", sslEnabled)
        set("host", host)
        set(
            "localCache",
            when (cacheSettings) {
                is LocalCacheSettings.Persistent -> persistentLocalCache(
                    json(
                        "cacheSizeBytes" to cacheSettings.sizeBytes,
                    ).asDynamic() as PersistentCacheSettings,
                )
                is LocalCacheSettings.Memory -> {
                    val garbageCollectorSettings = when (val garbageCollectorSettings = cacheSettings.garbaseCollectorSettings) {
                        is MemoryGarbageCollectorSettings.Eager -> memoryEagerGarbageCollector()
                        is MemoryGarbageCollectorSettings.LRUGC -> memoryLruGarbageCollector(json("cacheSizeBytes" to garbageCollectorSettings.sizeBytes))
                    }
                    memoryLocalCache(json("garbageCollector" to garbageCollectorSettings).asDynamic() as MemoryCacheSettings)
                }
            },
        )
    }
}

public actual fun firestoreSettings(
    settings: FirebaseFirestoreSettings?,
    builder: FirebaseFirestoreSettings.Builder.() -> Unit,
): FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder().apply {
    settings?.let {
        sslEnabled = it.sslEnabled
        host = it.host
        cacheSettings = it.cacheSettings
    }
}.apply(builder).build()

internal actual data class NativeWriteBatch(val js: JsWriteBatch)

public operator fun WriteBatch.Companion.invoke(js: JsWriteBatch): WriteBatch = WriteBatch(NativeWriteBatch(js))
public val WriteBatch.js: JsWriteBatch get() = native.js

internal actual data class NativeTransaction(val js: JsTransaction)

public operator fun Transaction.Companion.invoke(js: JsTransaction): Transaction = Transaction(NativeTransaction(js))
public val Transaction.js: JsTransaction get() = native.js

/** A class representing a platform specific Firebase DocumentReference. */
internal actual typealias NativeDocumentReferenceType = JsDocumentReference

public operator fun DocumentReference.Companion.invoke(js: JsDocumentReference): DocumentReference = DocumentReference(js)
public val DocumentReference.js: NativeDocumentReferenceType get() = native.js

internal actual open class NativeQuery(open val js: JsQuery)
internal val JsQuery.wrapped get() = NativeQuery(this)

public operator fun Query.Companion.invoke(js: JsQuery): Query = Query(js.wrapped)
public val Query.js: dev.gitlive.firebase.firestore.externals.Query get() = native.js

internal actual data class NativeCollectionReference(override val js: JsCollectionReference) : NativeQuery(js)

public operator fun CollectionReference.Companion.invoke(js: JsCollectionReference): CollectionReference = CollectionReference(NativeCollectionReference(js))
public val CollectionReference.js: dev.gitlive.firebase.firestore.externals.CollectionReference get() = native.js

public actual class FirebaseFirestoreException(cause: Throwable, public val code: FirestoreExceptionCode) : FirebaseException(code.toString(), cause)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
public actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

public val QuerySnapshot.js: JsQuerySnapshot get() = js

public actual class QuerySnapshot(internal val js: JsQuerySnapshot) {
    public actual val documents: List<DocumentSnapshot>
        get() = js.docs.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it)) }
    public actual val documentChanges: List<DocumentChange>
        get() = js.docChanges().map { DocumentChange(it) }
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)
}

public val DocumentChange.js: JsDocumentChange get() = js

public actual class DocumentChange(internal val js: JsDocumentChange) {
    public actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(js.doc))
    public actual val newIndex: Int
        get() = js.newIndex
    public actual val oldIndex: Int
        get() = js.oldIndex
    public actual val type: ChangeType
        get() = ChangeType.entries.first { it.jsString == js.type }
}

internal actual data class NativeDocumentSnapshot(val js: JsDocumentSnapshot)

public operator fun DocumentSnapshot.Companion.invoke(js: JsDocumentSnapshot): DocumentSnapshot = DocumentSnapshot(NativeDocumentSnapshot(js))
public val DocumentSnapshot.js: dev.gitlive.firebase.firestore.externals.DocumentSnapshot get() = native.js

public val SnapshotMetadata.js: dev.gitlive.firebase.firestore.externals.SnapshotMetadata get() = js

public actual class SnapshotMetadata(internal val js: JsSnapshotMetadata) {
    public actual val hasPendingWrites: Boolean get() = js.hasPendingWrites
    public actual val isFromCache: Boolean get() = js.fromCache
}

public val FieldPath.js: dev.gitlive.firebase.firestore.externals.FieldPath get() = js

public actual class FieldPath private constructor(internal val js: JsFieldPath) {

    public actual companion object {
        public actual val documentId: FieldPath = FieldPath(jsDocumentId())
    }
    public actual constructor(vararg fieldNames: String) : this(
        dev.gitlive.firebase.firestore.rethrow {
            JsFieldPath(*fieldNames)
        },
    )
    public actual val documentId: FieldPath get() = FieldPath.documentId
    public actual val encoded: EncodedFieldPath = js
    override fun equals(other: Any?): Boolean = other is FieldPath && js.isEqual(other.js)
    override fun hashCode(): Int = js.hashCode()
    override fun toString(): String = js.toString()
}

public actual typealias EncodedFieldPath = JsFieldPath

public actual enum class FirestoreExceptionCode {
    OK,
    CANCELLED,
    UNKNOWN,
    INVALID_ARGUMENT,
    DEADLINE_EXCEEDED,
    NOT_FOUND,
    ALREADY_EXISTS,
    PERMISSION_DENIED,
    RESOURCE_EXHAUSTED,
    FAILED_PRECONDITION,
    ABORTED,
    OUT_OF_RANGE,
    UNIMPLEMENTED,
    INTERNAL,
    UNAVAILABLE,
    DATA_LOSS,
    UNAUTHENTICATED,
}

public actual enum class Direction(internal val jsString: String) {
    ASCENDING("asc"),
    DESCENDING("desc"),
}

public actual enum class ChangeType(internal val jsString: String) {
    ADDED("added"),
    MODIFIED("modified"),
    REMOVED("removed"),
}

internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.firestore.rethrow { function() }

internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw errorToException(e)
    }
}

internal fun errorToException(e: dynamic) = (e?.code ?: e?.message ?: "")
    .toString()
    .lowercase()
    .let {
        when {
            "cancelled" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.CANCELLED)
            "invalid-argument" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.INVALID_ARGUMENT)
            "deadline-exceeded" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.DEADLINE_EXCEEDED)
            "not-found" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.NOT_FOUND)
            "already-exists" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.ALREADY_EXISTS)
            "permission-denied" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.PERMISSION_DENIED)
            "resource-exhausted" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.RESOURCE_EXHAUSTED)
            "failed-precondition" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.FAILED_PRECONDITION)
            "aborted" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.ABORTED)
            "out-of-range" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.OUT_OF_RANGE)
            "unimplemented" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.UNIMPLEMENTED)
            "internal" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.INTERNAL)
            "unavailable" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.UNAVAILABLE)
            "data-loss" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.DATA_LOSS)
            "unauthenticated" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.UNAUTHENTICATED)
            "unknown" in it -> FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.UNKNOWN)
            else -> {
                println("Unknown error code in ${JSON.stringify(e)}")
                FirebaseFirestoreException(e.unsafeCast<Throwable>(), FirestoreExceptionCode.UNKNOWN)
            }
        }
    }

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
internal fun entriesOf(jsObject: dynamic): List<Pair<String, Any?>> = (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
    .invoke(jsObject)
    .map { entry -> entry[0] as String to entry[1] }

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
internal fun mapOf(jsObject: dynamic): Map<String, Any?> = entriesOf(jsObject).toMap()
