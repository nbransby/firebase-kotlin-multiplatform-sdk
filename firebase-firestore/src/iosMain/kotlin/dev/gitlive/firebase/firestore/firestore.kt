/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package dev.gitlive.firebase.firestore

import cocoapods.FirebaseFirestoreInternal.*
import cocoapods.FirebaseFirestoreInternal.FIRDocumentChangeType.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.firestore.internal.NativeDocumentSnapshotWrapper
import dev.gitlive.firebase.ios
import kotlinx.coroutines.CompletableDeferred
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.numberWithLong
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_t

public val FirebaseFirestore.ios: FIRFirestore get() = FIRFirestore.firestore()

public actual val Firebase.firestore: FirebaseFirestore get() =
    FirebaseFirestore(FIRFirestore.firestore())

public actual fun Firebase.firestore(app: FirebaseApp, databaseId: String?): FirebaseFirestore = FirebaseFirestore(
    databaseId?.let { FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp, it) } ?: FIRFirestore.firestoreForApp(app.ios as objcnames.classes.FIRApp),
)

public val LocalCacheSettings.ios: FIRLocalCacheSettingsProtocol get() = when (this) {
    is LocalCacheSettings.Persistent -> FIRPersistentCacheSettings(NSNumber.numberWithLong(sizeBytes))
    is LocalCacheSettings.Memory -> FIRMemoryCacheSettings(
        when (garbaseCollectorSettings) {
            is MemoryGarbageCollectorSettings.Eager -> FIRMemoryEagerGCSettings()
            is MemoryGarbageCollectorSettings.LRUGC -> FIRMemoryLRUGCSettings(NSNumber.numberWithLong(garbaseCollectorSettings.sizeBytes))
        },
    )
}

internal actual typealias NativeFirebaseFirestore = FIRFirestore

public operator fun FirebaseFirestore.Companion.invoke(ios: FIRFirestore): FirebaseFirestore = FirebaseFirestore(ios)

public actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
    val dispatchQueue: dispatch_queue_t,
) {

    public actual companion object {
        public actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024
    }

    public actual class Builder(
        public actual var sslEnabled: Boolean,
        public actual var host: String,
        public actual var cacheSettings: LocalCacheSettings,
        public var dispatchQueue: dispatch_queue_t,
    ) {

        public actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
            dispatch_get_main_queue(),
        )

        public actual constructor(settings: FirebaseFirestoreSettings) : this(
            settings.sslEnabled,
            settings.host,
            settings.cacheSettings,
            settings.dispatchQueue,
        )

        public actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings, dispatchQueue)
    }

    val ios: FIRFirestoreSettings get() = FIRFirestoreSettings().apply {
        cacheSettings = this@FirebaseFirestoreSettings.cacheSettings.ios
        sslEnabled = this@FirebaseFirestoreSettings.sslEnabled
        host = this@FirebaseFirestoreSettings.host
        dispatchQueue = this@FirebaseFirestoreSettings.dispatchQueue
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
        dispatchQueue = it.dispatchQueue
    }
}.apply(builder).build()

internal actual typealias NativeWriteBatch = FIRWriteBatch

public operator fun WriteBatch.Companion.invoke(ios: FIRWriteBatch): WriteBatch = WriteBatch(ios)
public val WriteBatch.ios: FIRWriteBatch get() = native

internal actual typealias NativeTransaction = FIRTransaction

public operator fun Transaction.Companion.invoke(ios: FIRTransaction): Transaction = Transaction(ios)
public val Transaction.ios: FIRTransaction get() = native

/** A class representing a platform specific Firebase DocumentReference. */
internal actual typealias NativeDocumentReferenceType = FIRDocumentReference

public operator fun DocumentReference.Companion.invoke(ios: FIRDocumentReference): DocumentReference = DocumentReference(ios)
public val DocumentReference.ios: FIRDocumentReference get() = native.ios

internal actual typealias NativeQuery = FIRQuery

public operator fun Query.Companion.invoke(ios: FIRQuery): Query = Query(ios)
public val Query.ios: NativeQuery get() = native

internal actual typealias NativeCollectionReference = FIRCollectionReference

public operator fun CollectionReference.Companion.invoke(ios: FIRCollectionReference): CollectionReference = CollectionReference(ios)
public val CollectionReference.ios: FIRCollectionReference get() = native

public actual class FirebaseFirestoreException(message: String, public val code: FirestoreExceptionCode) : FirebaseException(message)

public actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

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

public actual enum class Direction {
    ASCENDING,
    DESCENDING,
}

public val ChangeType.ios: FIRDocumentChangeType get() = ios

public actual enum class ChangeType(internal val ios: FIRDocumentChangeType) {
    ADDED(FIRDocumentChangeTypeAdded),
    MODIFIED(FIRDocumentChangeTypeModified),
    REMOVED(FIRDocumentChangeTypeRemoved),
}

public fun NSError.toException(): FirebaseFirestoreException = when (domain) {
    FIRFirestoreErrorDomain -> when (code) {
        FIRFirestoreErrorCodeOK -> FirestoreExceptionCode.OK
        FIRFirestoreErrorCodeCancelled -> FirestoreExceptionCode.CANCELLED
        FIRFirestoreErrorCodeUnknown -> FirestoreExceptionCode.UNKNOWN
        FIRFirestoreErrorCodeInvalidArgument -> FirestoreExceptionCode.INVALID_ARGUMENT
        FIRFirestoreErrorCodeDeadlineExceeded -> FirestoreExceptionCode.DEADLINE_EXCEEDED
        FIRFirestoreErrorCodeNotFound -> FirestoreExceptionCode.NOT_FOUND
        FIRFirestoreErrorCodeAlreadyExists -> FirestoreExceptionCode.ALREADY_EXISTS
        FIRFirestoreErrorCodePermissionDenied -> FirestoreExceptionCode.PERMISSION_DENIED
        FIRFirestoreErrorCodeResourceExhausted -> FirestoreExceptionCode.RESOURCE_EXHAUSTED
        FIRFirestoreErrorCodeFailedPrecondition -> FirestoreExceptionCode.FAILED_PRECONDITION
        FIRFirestoreErrorCodeAborted -> FirestoreExceptionCode.ABORTED
        FIRFirestoreErrorCodeOutOfRange -> FirestoreExceptionCode.OUT_OF_RANGE
        FIRFirestoreErrorCodeUnimplemented -> FirestoreExceptionCode.UNIMPLEMENTED
        FIRFirestoreErrorCodeInternal -> FirestoreExceptionCode.INTERNAL
        FIRFirestoreErrorCodeUnavailable -> FirestoreExceptionCode.UNAVAILABLE
        FIRFirestoreErrorCodeDataLoss -> FirestoreExceptionCode.DATA_LOSS
        FIRFirestoreErrorCodeUnauthenticated -> FirestoreExceptionCode.UNAUTHENTICATED
        else -> FirestoreExceptionCode.UNKNOWN
    }
    else -> FirestoreExceptionCode.UNKNOWN
}.let { FirebaseFirestoreException(description!!, it) }

public val QuerySnapshot.ios: FIRQuerySnapshot get() = ios

public actual class QuerySnapshot(internal val ios: FIRQuerySnapshot) {
    public actual val documents: List<DocumentSnapshot>
        get() = ios.documents.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it as FIRDocumentSnapshot)) }
    public actual val documentChanges: List<DocumentChange>
        get() = ios.documentChanges.map { DocumentChange(it as FIRDocumentChange) }
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(ios.metadata)
}

public val DocumentChange.ios: FIRDocumentChange get() = ios

public actual class DocumentChange(internal val ios: FIRDocumentChange) {
    public actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(ios.document))
    public actual val newIndex: Int
        get() = ios.newIndex.toInt()
    public actual val oldIndex: Int
        get() = ios.oldIndex.toInt()
    public actual val type: ChangeType
        get() = ChangeType.entries.first { it.ios == ios.type }
}

internal actual typealias NativeDocumentSnapshot = FIRDocumentSnapshot

public operator fun DocumentSnapshot.Companion.invoke(ios: FIRDocumentSnapshot): DocumentSnapshot = DocumentSnapshot(ios)
public val DocumentSnapshot.ios: FIRDocumentSnapshot get() = native

public val SnapshotMetadata.ios: FIRSnapshotMetadata get() = ios

public actual class SnapshotMetadata(internal val ios: FIRSnapshotMetadata) {
    public actual val hasPendingWrites: Boolean get() = ios.pendingWrites
    public actual val isFromCache: Boolean get() = ios.fromCache
}

public val FieldPath.ios: FIRFieldPath get() = ios

public actual class FieldPath private constructor(internal val ios: FIRFieldPath) {
    public actual companion object {
        public actual val documentId: FieldPath = FieldPath(FIRFieldPath.documentID())
    }
    public actual constructor(vararg fieldNames: String) : this(FIRFieldPath(fieldNames.asList()))
    public actual val documentId: FieldPath get() = FieldPath.documentId
    public actual val encoded: EncodedFieldPath = ios
    override fun equals(other: Any?): Boolean = other is FieldPath && ios == other.ios
    override fun hashCode(): Int = ios.hashCode()
    override fun toString(): String = ios.toString()
}

public actual typealias EncodedFieldPath = FIRFieldPath

internal suspend inline fun <reified T> awaitResult(function: (callback: (T?, NSError?) -> Unit) -> Unit): T {
    val job = CompletableDeferred<T?>()
    function { result, error ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    return job.await() as T
}

internal suspend inline fun <T> await(function: (callback: (NSError?) -> Unit) -> T): T {
    val job = CompletableDeferred<Unit>()
    val result = function { error ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(error.toException())
        }
    }
    job.await()
    return result
}
