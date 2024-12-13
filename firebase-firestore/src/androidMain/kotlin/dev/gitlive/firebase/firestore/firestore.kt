/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")

package dev.gitlive.firebase.firestore

import com.google.android.gms.tasks.TaskExecutors
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android
import dev.gitlive.firebase.firestore.internal.NativeDocumentSnapshotWrapper
import java.util.concurrent.Executor
import com.google.firebase.firestore.CollectionReference as AndroidCollectionReference
import com.google.firebase.firestore.DocumentChange as AndroidDocumentChange
import com.google.firebase.firestore.DocumentReference as AndroidDocumentReference
import com.google.firebase.firestore.DocumentSnapshot as AndroidDocumentSnapshot
import com.google.firebase.firestore.FieldPath as AndroidFieldPath
import com.google.firebase.firestore.FirebaseFirestore as AndroidFirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException as AndroidFirebaseFirestoreException
import com.google.firebase.firestore.LocalCacheSettings as AndroidLocalCacheSettings
import com.google.firebase.firestore.Query as AndroidQuery
import com.google.firebase.firestore.QuerySnapshot as AndroidQuerySnapshot
import com.google.firebase.firestore.SnapshotMetadata as AndroidSnapshotMetadata
import com.google.firebase.firestore.Source as AndroidSource
import com.google.firebase.firestore.Transaction as AndroidTransaction
import com.google.firebase.firestore.WriteBatch as AndroidWriteBatch
import com.google.firebase.firestore.memoryCacheSettings as androidMemoryCacheSettings
import com.google.firebase.firestore.memoryEagerGcSettings as androidMemoryEagerGcSettings
import com.google.firebase.firestore.memoryLruGcSettings as androidMemoryLruGcSettings
import com.google.firebase.firestore.persistentCacheSettings as androidPersistentCacheSettings

public val FirebaseFirestore.android: AndroidFirebaseFirestore get() = AndroidFirebaseFirestore.getInstance()

public actual val Firebase.firestore: FirebaseFirestore get() =
    FirebaseFirestore(AndroidFirebaseFirestore.getInstance())

public actual fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore(AndroidFirebaseFirestore.getInstance(app.android))

public val LocalCacheSettings.android: AndroidLocalCacheSettings get() = when (this) {
    is LocalCacheSettings.Persistent -> androidPersistentCacheSettings {
        setSizeBytes(sizeBytes)
    }
    is LocalCacheSettings.Memory -> androidMemoryCacheSettings {
        setGcSettings(
            when (garbaseCollectorSettings) {
                is MemoryGarbageCollectorSettings.Eager -> androidMemoryEagerGcSettings { }
                is MemoryGarbageCollectorSettings.LRUGC -> androidMemoryLruGcSettings {
                    setSizeBytes(garbaseCollectorSettings.sizeBytes)
                }
            },
        )
    }
}

internal actual typealias NativeFirebaseFirestore = AndroidFirebaseFirestore

public operator fun FirebaseFirestore.Companion.invoke(android: AndroidFirebaseFirestore): FirebaseFirestore = FirebaseFirestore(android)

public actual data class FirebaseFirestoreSettings(
    actual val sslEnabled: Boolean,
    actual val host: String,
    actual val cacheSettings: LocalCacheSettings,
    val callbackExecutor: Executor,
) {

    public actual companion object {
        public actual val CACHE_SIZE_UNLIMITED: Long = -1L
        internal actual val DEFAULT_HOST: String = "firestore.googleapis.com"
        internal actual val MINIMUM_CACHE_BYTES: Long = 1 * 1024 * 1024
        internal actual val DEFAULT_CACHE_SIZE_BYTES: Long = 100 * 1024 * 1024
    }

    public actual class Builder internal constructor(
        public actual var sslEnabled: Boolean,
        public actual var host: String,
        public actual var cacheSettings: LocalCacheSettings,
        public var callbackExecutor: Executor,
    ) {

        public actual constructor() : this(
            true,
            DEFAULT_HOST,
            persistentCacheSettings { },
            TaskExecutors.MAIN_THREAD,
        )
        public actual constructor(settings: FirebaseFirestoreSettings) : this(settings.sslEnabled, settings.host, settings.cacheSettings, settings.callbackExecutor)

        public actual fun build(): FirebaseFirestoreSettings = FirebaseFirestoreSettings(sslEnabled, host, cacheSettings, callbackExecutor)
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
        callbackExecutor = it.callbackExecutor
    }
}.apply(builder).build()

internal actual typealias NativeWriteBatch = AndroidWriteBatch

public operator fun WriteBatch.Companion.invoke(android: AndroidWriteBatch): WriteBatch = WriteBatch(android)
public val WriteBatch.android: AndroidWriteBatch get() = native

internal actual typealias NativeTransaction = AndroidTransaction

public operator fun Transaction.Companion.invoke(android: AndroidTransaction): Transaction = Transaction(android)
public val Transaction.android: AndroidTransaction get() = native

/** A class representing a platform specific Firebase DocumentReference. */
internal actual typealias NativeDocumentReferenceType = AndroidDocumentReference

public operator fun DocumentReference.Companion.invoke(android: AndroidDocumentReference): DocumentReference = DocumentReference(android)
public val DocumentReference.android: AndroidDocumentReference get() = native.android

internal actual typealias NativeQuery = AndroidQuery

public operator fun Query.Companion.invoke(android: AndroidQuery): Query = Query(android)
public val Query.android: AndroidQuery get() = native

public actual typealias Direction = AndroidQuery.Direction
public actual typealias ChangeType = AndroidDocumentChange.Type

internal actual typealias NativeCollectionReference = AndroidCollectionReference

public operator fun CollectionReference.Companion.invoke(android: AndroidCollectionReference): CollectionReference = CollectionReference(android)
public val CollectionReference.android: AndroidCollectionReference get() = native

public actual typealias FirebaseFirestoreException = AndroidFirebaseFirestoreException

@Suppress("ConflictingExtensionProperty")
public actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

public actual typealias FirestoreExceptionCode = AndroidFirebaseFirestoreException.Code

public val QuerySnapshot.android: AndroidQuerySnapshot get() = android

public actual class QuerySnapshot(internal val android: AndroidQuerySnapshot) {
    public actual val documents: List<DocumentSnapshot>
        get() = android.documents.map { DocumentSnapshot(NativeDocumentSnapshotWrapper(it)) }
    public actual val documentChanges: List<DocumentChange>
        get() = android.documentChanges.map { DocumentChange(it) }
    public actual val metadata: SnapshotMetadata get() = SnapshotMetadata(android.metadata)
}

public val DocumentChange.android: AndroidDocumentChange get() = android

public actual class DocumentChange(internal val android: AndroidDocumentChange) {
    public actual val document: DocumentSnapshot
        get() = DocumentSnapshot(NativeDocumentSnapshotWrapper(android.document))
    public actual val newIndex: Int
        get() = android.newIndex
    public actual val oldIndex: Int
        get() = android.oldIndex
    public actual val type: ChangeType
        get() = android.type
}

internal actual typealias NativeDocumentSnapshot = AndroidDocumentSnapshot

public operator fun DocumentSnapshot.Companion.invoke(android: AndroidDocumentSnapshot): DocumentSnapshot = DocumentSnapshot(android)
public val DocumentSnapshot.android: AndroidDocumentSnapshot get() = native

public val SnapshotMetadata.android: AndroidSnapshotMetadata get() = android

public actual class SnapshotMetadata(internal val android: AndroidSnapshotMetadata) {
    public actual val hasPendingWrites: Boolean get() = android.hasPendingWrites()
    public actual val isFromCache: Boolean get() = android.isFromCache
}

public val FieldPath.android: AndroidFieldPath get() = android

public actual class FieldPath private constructor(internal val android: AndroidFieldPath) {

    public actual companion object {
        public actual val documentId: FieldPath = FieldPath(AndroidFieldPath.documentId())
    }

    public actual constructor(vararg fieldNames: String) : this(
        AndroidFieldPath.of(
            *fieldNames,
        ),
    )

    public actual val documentId: FieldPath get() = FieldPath.documentId
    public actual val encoded: EncodedFieldPath = android
    override fun equals(other: Any?): Boolean = other is FieldPath && android == other.android
    override fun hashCode(): Int = android.hashCode()
    override fun toString(): String = android.toString()
}

public actual typealias EncodedFieldPath = AndroidFieldPath

internal typealias NativeSource = AndroidSource
