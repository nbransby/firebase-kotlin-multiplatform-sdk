/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseException
import dev.gitlive.firebase.firestore.internal.NativeCollectionReferenceWrapper
import dev.gitlive.firebase.firestore.internal.NativeDocumentReference
import dev.gitlive.firebase.firestore.internal.NativeDocumentSnapshotWrapper
import dev.gitlive.firebase.firestore.internal.NativeFirebaseFirestoreWrapper
import dev.gitlive.firebase.firestore.internal.NativeQueryWrapper
import dev.gitlive.firebase.firestore.internal.NativeTransactionWrapper
import dev.gitlive.firebase.firestore.internal.NativeWriteBatchWrapper
import dev.gitlive.firebase.firestore.internal.SetOptions
import dev.gitlive.firebase.firestore.internal.safeValue
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.encodeAsObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.jvm.JvmName

/** Returns the [FirebaseFirestore] instance of the default [FirebaseApp]. */
public expect val Firebase.firestore: FirebaseFirestore

/** Returns the [FirebaseFirestore] instance of a given [FirebaseApp]. */
public expect fun Firebase.firestore(app: FirebaseApp): FirebaseFirestore

internal expect class NativeFirebaseFirestore

public class FirebaseFirestore internal constructor(private val wrapper: NativeFirebaseFirestoreWrapper) {

    public companion object {}

    internal constructor(native: NativeFirebaseFirestore) : this(NativeFirebaseFirestoreWrapper(native))

    // Important to leave this as a get property since on JS it is initialized lazily
    internal val native: NativeFirebaseFirestore get() = wrapper.native

    public var settings: FirebaseFirestoreSettings
        @Deprecated("Property can only be written.", level = DeprecationLevel.ERROR)
        get() = throw NotImplementedError()
        set(value) {
            wrapper.applySettings(value)
        }

    public fun collection(collectionPath: String): CollectionReference = CollectionReference(wrapper.collection(collectionPath))
    public fun collectionGroup(collectionId: String): Query = Query(wrapper.collectionGroup(collectionId))
    public fun document(documentPath: String): DocumentReference = DocumentReference(wrapper.document(documentPath))
    public fun batch(): WriteBatch = WriteBatch(wrapper.batch())
    public fun setLoggingEnabled(loggingEnabled: Boolean) {
        wrapper.setLoggingEnabled(loggingEnabled)
    }
    public suspend fun clearPersistence() {
        wrapper.clearPersistence()
    }
    public suspend fun <T> runTransaction(func: suspend Transaction.() -> T): T = wrapper.runTransaction { func(Transaction(this)) }
    public fun useEmulator(host: String, port: Int) {
        wrapper.useEmulator(host, port)
    }

    @Deprecated("Use SettingsBuilder instead", replaceWith = ReplaceWith("settings = firestoreSettings { }", "dev.gitlive.firebase.firestore.firestoreSettings"))
    public fun setSettings(
        persistenceEnabled: Boolean? = null,
        sslEnabled: Boolean? = null,
        host: String? = null,
        cacheSizeBytes: Long? = null,
    ) {
        settings = firestoreSettings {
            this.sslEnabled = sslEnabled ?: true
            this.host = host ?: FirebaseFirestoreSettings.DEFAULT_HOST
            this.cacheSettings = if (persistenceEnabled != false) {
                LocalCacheSettings.Persistent(
                    cacheSizeBytes ?: FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED,
                )
            } else {
                val cacheSize = cacheSizeBytes ?: FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
                val garbageCollectionSettings =
                    if (cacheSize == FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) {
                        MemoryGarbageCollectorSettings.Eager
                    } else {
                        MemoryGarbageCollectorSettings.LRUGC(cacheSize)
                    }
                LocalCacheSettings.Memory(garbageCollectionSettings)
            }
        }
    }

    public suspend fun disableNetwork() {
        wrapper.disableNetwork()
    }
    public suspend fun enableNetwork() {
        wrapper.enableNetwork()
    }
}

public expect class FirebaseFirestoreSettings {

    public companion object {
        public val CACHE_SIZE_UNLIMITED: Long
        internal val DEFAULT_HOST: String
        internal val MINIMUM_CACHE_BYTES: Long
        internal val DEFAULT_CACHE_SIZE_BYTES: Long
    }

    public class Builder {
        public constructor()
        public constructor(settings: FirebaseFirestoreSettings)

        public var sslEnabled: Boolean
        public var host: String
        public var cacheSettings: LocalCacheSettings

        public fun build(): FirebaseFirestoreSettings
    }

    public val sslEnabled: Boolean
    public val host: String
    public val cacheSettings: LocalCacheSettings
}

public expect fun firestoreSettings(settings: FirebaseFirestoreSettings? = null, builder: FirebaseFirestoreSettings.Builder.() -> Unit): FirebaseFirestoreSettings

internal expect class NativeTransaction

public data class Transaction internal constructor(internal val nativeWrapper: NativeTransactionWrapper) {

    public companion object {}

    internal constructor(native: NativeTransaction) : this(NativeTransactionWrapper(native))

    internal val native: NativeTransaction = nativeWrapper.native

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, merge: Boolean = false): Transaction = set(documentRef, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun set(documentRef: DocumentReference, data: Any, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFields: String): Transaction = set(documentRef, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public fun set(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): Transaction = set(documentRef, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun set(documentRef: DocumentReference, data: Any, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false): Transaction = set(documentRef, strategy, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String): Transaction = set(documentRef, strategy, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): Transaction = set(documentRef, strategy, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): Transaction = Transaction(nativeWrapper.setEncoded(documentRef, encodedData, setOptions))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { this.encodeDefaults = encodeDefaults }"))
    public fun update(documentRef: DocumentReference, data: Any, encodeDefaults: Boolean): Transaction = update(documentRef, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun update(documentRef: DocumentReference, data: Any, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = updateEncoded(documentRef, encodeAsObject(data, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): Transaction = update(documentRef, strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = updateEncoded(documentRef, encodeAsObject(strategy, data, buildSettings))

    @JvmName("updateFields")
    public inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @JvmName("updateFieldPaths")
    public inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}): Transaction = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @PublishedApi
    internal fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): Transaction = Transaction(nativeWrapper.updateEncoded(documentRef, encodedData))

    @PublishedApi
    internal fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): Transaction = Transaction(nativeWrapper.updateEncodedFieldsAndValues(documentRef, encodedFieldsAndValues))

    @PublishedApi
    internal fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): Transaction = Transaction(nativeWrapper.updateEncodedFieldPathsAndValues(documentRef, encodedFieldsAndValues))

    public fun delete(documentRef: DocumentReference): Transaction = Transaction(nativeWrapper.delete(documentRef))
    public suspend fun get(documentRef: DocumentReference): DocumentSnapshot = DocumentSnapshot(nativeWrapper.get(documentRef))
}

internal expect open class NativeQuery

public open class Query internal constructor(internal val nativeQuery: NativeQueryWrapper) {

    public companion object {}

    internal constructor(native: NativeQuery) : this(NativeQueryWrapper(native))

    internal open val native: NativeQuery = nativeQuery.native

    public fun limit(limit: Number): Query = Query(nativeQuery.limit(limit))
    public val snapshots: Flow<QuerySnapshot> = nativeQuery.snapshots
    public fun snapshots(includeMetadataChanges: Boolean = false): Flow<QuerySnapshot> = nativeQuery.snapshots(includeMetadataChanges)
    public suspend fun get(source: Source = Source.DEFAULT): QuerySnapshot = nativeQuery.get(source)

    public fun where(builder: FilterBuilder.() -> Filter?): Query = builder(FilterBuilder())?.let { Query(nativeQuery.where(it)) } ?: this

    public fun orderBy(field: String, direction: Direction = Direction.ASCENDING): Query = Query(nativeQuery.orderBy(field, direction))
    public fun orderBy(field: FieldPath, direction: Direction = Direction.ASCENDING): Query = Query(nativeQuery.orderBy(field.encoded, direction))

    public fun startAfter(document: DocumentSnapshot): Query = Query(nativeQuery.startAfter(document.native))
    public fun startAfter(vararg fieldValues: Any): Query = Query(nativeQuery.startAfter(*(fieldValues.map { it.safeValue }.toTypedArray())))
    public fun startAt(document: DocumentSnapshot): Query = Query(nativeQuery.startAt(document.native))
    public fun startAt(vararg fieldValues: Any): Query = Query(nativeQuery.startAt(*(fieldValues.map { it.safeValue }.toTypedArray())))

    public fun endBefore(document: DocumentSnapshot): Query = Query(nativeQuery.endBefore(document.native))
    public fun endBefore(vararg fieldValues: Any): Query = Query(nativeQuery.endBefore(*(fieldValues.map { it.safeValue }.toTypedArray())))
    public fun endAt(document: DocumentSnapshot): Query = Query(nativeQuery.endAt(document.native))
    public fun endAt(vararg fieldValues: Any): Query = Query(nativeQuery.endAt(*(fieldValues.map { it.safeValue }.toTypedArray())))
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where { field equalTo equalTo }", "dev.gitlive.firebase.firestore"))
public fun Query.where(field: String, equalTo: Any?): Query = where {
    field equalTo equalTo
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where { path equalTo equalTo }", "dev.gitlive.firebase.firestore"))
public fun Query.where(path: FieldPath, equalTo: Any?): Query = where {
    path equalTo equalTo
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(field: String, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query = where {
    all(
        *listOfNotNull(
            lessThan?.let { field lessThan it },
            greaterThan?.let { field greaterThan it },
            arrayContains?.let { field contains it },
        ).toTypedArray(),
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(path: FieldPath, lessThan: Any? = null, greaterThan: Any? = null, arrayContains: Any? = null): Query = where {
    all(
        *listOfNotNull(
            lessThan?.let { path lessThan it },
            greaterThan?.let { path greaterThan it },
            arrayContains?.let { path contains it },
        ).toTypedArray(),
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(field: String, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query = where {
    all(
        *listOfNotNull(
            inArray?.let { field inArray it },
            arrayContainsAny?.let { field containsAny it },
        ).toTypedArray(),
    )
}

@Deprecated("Deprecated in favor of using a [FilterBuilder]", replaceWith = ReplaceWith("where {  }", "dev.gitlive.firebase.firestore"))
public fun Query.where(path: FieldPath, inArray: List<Any>? = null, arrayContainsAny: List<Any>? = null): Query = where {
    all(
        *listOfNotNull(
            inArray?.let { path inArray it },
            arrayContainsAny?.let { path containsAny it },
        ).toTypedArray(),
    )
}

internal expect class NativeWriteBatch

public data class WriteBatch internal constructor(internal val nativeWrapper: NativeWriteBatchWrapper) {

    public companion object {}

    internal constructor(native: NativeWriteBatch) : this(NativeWriteBatchWrapper(native))

    internal val native: NativeWriteBatch = nativeWrapper.native

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, merge: Boolean = false): WriteBatch = set(documentRef, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFields: String): WriteBatch = set(documentRef, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): WriteBatch = set(documentRef, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> set(documentRef: DocumentReference, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false): WriteBatch = set(documentRef, strategy, data, merge) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), if (merge) SetOptions.Merge else SetOptions.Overwrite)

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String): WriteBatch = set(documentRef, strategy, data, *mergeFields) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFields(mergeFields.asList()))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(documentRef, strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath): WriteBatch = set(documentRef, strategy, data, *mergeFieldPaths) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> set(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = setEncoded(documentRef, encodeAsObject(strategy, data, buildSettings), SetOptions.MergeFieldPaths(mergeFieldPaths.asList()))

    @PublishedApi
    internal fun setEncoded(documentRef: DocumentReference, encodedData: EncodedObject, setOptions: SetOptions): WriteBatch = WriteBatch(nativeWrapper.setEncoded(documentRef, encodedData, setOptions))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, data) { this.encodeDefaults = encodeDefaults }"))
    public inline fun <reified T : Any> update(documentRef: DocumentReference, data: T, encodeDefaults: Boolean): WriteBatch = update(documentRef, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <reified T : Any> update(documentRef: DocumentReference, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = updateEncoded(documentRef, encodeAsObject(data, buildSettings))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(documentRef, strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): WriteBatch = update(documentRef, strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    public inline fun <T : Any> update(documentRef: DocumentReference, strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = updateEncoded(documentRef, encodeAsObject(strategy, data, buildSettings))

    @JvmName("updateField")
    public inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = updateEncodedFieldsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @JvmName("updateFieldPath")
    public inline fun update(documentRef: DocumentReference, vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}): WriteBatch = updateEncodedFieldPathsAndValues(documentRef, encodeFieldAndValue(fieldsAndValues, buildSettings).orEmpty())

    @PublishedApi
    internal fun updateEncoded(documentRef: DocumentReference, encodedData: EncodedObject): WriteBatch = WriteBatch(nativeWrapper.updateEncoded(documentRef, encodedData))

    @PublishedApi
    internal fun updateEncodedFieldsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<String, Any?>>): WriteBatch = WriteBatch(nativeWrapper.updateEncodedFieldsAndValues(documentRef, encodedFieldsAndValues))

    @PublishedApi
    internal fun updateEncodedFieldPathsAndValues(documentRef: DocumentReference, encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): WriteBatch = WriteBatch(nativeWrapper.updateEncodedFieldPathsAndValues(documentRef, encodedFieldsAndValues))

    public fun delete(documentRef: DocumentReference): WriteBatch = WriteBatch(nativeWrapper.delete(documentRef))
    public suspend fun commit() {
        nativeWrapper.commit()
    }
}

/** A class representing a platform specific Firebase DocumentReference. */
internal expect class NativeDocumentReferenceType

/** A class representing a Firebase DocumentReference. */
@Serializable(with = DocumentReferenceSerializer::class)
public data class DocumentReference internal constructor(internal val native: NativeDocumentReference) {

    public companion object {}

    internal val nativeValue get() = native.nativeValue

    val id: String get() = native.id
    val path: String get() = native.path
    val snapshots: Flow<DocumentSnapshot> get() = native.snapshots.map(::DocumentSnapshot)
    val parent: CollectionReference get() = CollectionReference(native.parent)
    public fun snapshots(includeMetadataChanges: Boolean = false): Flow<DocumentSnapshot> = native.snapshots(includeMetadataChanges).map(::DocumentSnapshot)

    public fun collection(collectionPath: String): CollectionReference = CollectionReference(native.collection(collectionPath))
    public suspend fun get(source: Source = Source.DEFAULT): DocumentSnapshot = DocumentSnapshot(native.get(source))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, merge) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> set(data: T, encodeDefaults: Boolean, merge: Boolean = false) {
        set(data, merge) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> set(data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(data, buildSettings),
            if (merge) SetOptions.Merge else SetOptions.Overwrite,
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> set(data: T, encodeDefaults: Boolean, vararg mergeFields: String) {
        set(data, *mergeFields) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> set(data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(data, buildSettings),
            SetOptions.MergeFields(mergeFields.asList()),
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> set(data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) {
        set(data, *mergeFieldPaths) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> set(data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(data, buildSettings),
            SetOptions.MergeFieldPaths(mergeFieldPaths.asList()),
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, merge) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, merge: Boolean = false) {
        set(strategy, data, merge) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, merge: Boolean = false, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(strategy, data, buildSettings),
            if (merge) SetOptions.Merge else SetOptions.Overwrite,
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFields) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFields: String) {
        set(strategy, data, *mergeFields) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFields: String, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(strategy, data, buildSettings),
            SetOptions.MergeFields(mergeFields.asList()),
        )
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("set(strategy, data, mergeFieldPaths) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean, vararg mergeFieldPaths: FieldPath) {
        set(strategy, data, *mergeFieldPaths) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> set(strategy: SerializationStrategy<T>, data: T, vararg mergeFieldPaths: FieldPath, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        setEncoded(
            encodeAsObject(strategy, data, buildSettings),
            SetOptions.MergeFieldPaths(mergeFieldPaths.asList()),
        )
    }

    @PublishedApi
    internal suspend fun setEncoded(encodedData: EncodedObject, setOptions: SetOptions) {
        native.setEncoded(encodedData, setOptions)
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(data) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> update(data: T, encodeDefaults: Boolean) {
        update(data) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <reified T : Any> update(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncoded(encodeAsObject(data, buildSettings))
    }

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("update(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> update(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean) {
        update(strategy, data) {
            this.encodeDefaults = encodeDefaults
        }
    }
    public suspend inline fun <T : Any> update(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncoded(
            encodeAsObject(strategy, data, buildSettings),
        )
    }

    @PublishedApi
    internal suspend fun updateEncoded(encodedData: EncodedObject) {
        native.updateEncoded(encodedData)
    }

    @JvmName("updateFields")
    public suspend inline fun update(vararg fieldsAndValues: Pair<String, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncodedFieldsAndValues(
            encodeFieldAndValue(
                fieldsAndValues,
                buildSettings,
            ).orEmpty(),
        )
    }

    @PublishedApi
    internal suspend fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>) {
        native.updateEncodedFieldsAndValues(encodedFieldsAndValues)
    }

    @JvmName("updateFieldPaths")
    public suspend inline fun update(vararg fieldsAndValues: Pair<FieldPath, Any?>, buildSettings: EncodeSettings.Builder.() -> Unit = {}) {
        updateEncodedFieldPathsAndValues(
            encodeFieldAndValue(
                fieldsAndValues,
                buildSettings,
            ).orEmpty(),
        )
    }

    @PublishedApi
    internal suspend fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>) {
        native.updateEncodedFieldPathsAndValues(encodedFieldsAndValues)
    }

    public suspend fun delete() {
        native.delete()
    }
}

internal expect class NativeCollectionReference : NativeQuery

public data class CollectionReference internal constructor(internal val nativeWrapper: NativeCollectionReferenceWrapper) : Query(nativeWrapper) {

    public companion object {}

    internal constructor(native: NativeCollectionReference) : this(NativeCollectionReferenceWrapper(native))

    override val native: NativeCollectionReference = nativeWrapper.native

    val path: String get() = nativeWrapper.path
    val document: DocumentReference get() = DocumentReference(nativeWrapper.document)
    val parent: DocumentReference? get() = nativeWrapper.parent?.let(::DocumentReference)

    public fun document(documentPath: String): DocumentReference = DocumentReference(nativeWrapper.document(documentPath))

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(data) { this.encodeDefaults = encodeDefaults }"))
    public suspend inline fun <reified T : Any> add(data: T, encodeDefaults: Boolean): DocumentReference = add(data) {
        this.encodeDefaults = encodeDefaults
    }
    public suspend inline fun <reified T : Any> add(data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): DocumentReference = addEncoded(
        encodeAsObject(data, buildSettings),
    )

    @Deprecated("Deprecated. Use builder instead", replaceWith = ReplaceWith("add(strategy, data) { this.encodeDefaults = encodeDefaults }"))
    public suspend fun <T : Any> add(strategy: SerializationStrategy<T>, data: T, encodeDefaults: Boolean): DocumentReference = add(strategy, data) {
        this.encodeDefaults = encodeDefaults
    }
    public suspend inline fun <T : Any> add(strategy: SerializationStrategy<T>, data: T, buildSettings: EncodeSettings.Builder.() -> Unit = {}): DocumentReference = addEncoded(
        encodeAsObject(strategy, data, buildSettings),
    )

    @PublishedApi
    internal suspend fun addEncoded(data: EncodedObject): DocumentReference = DocumentReference(nativeWrapper.addEncoded(data))
}

public expect class FirebaseFirestoreException : FirebaseException

public expect val FirebaseFirestoreException.code: FirestoreExceptionCode

public expect enum class FirestoreExceptionCode {
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

public expect enum class Direction {
    ASCENDING,
    DESCENDING,
}

public expect class QuerySnapshot {
    public val documents: List<DocumentSnapshot>
    public val documentChanges: List<DocumentChange>
    public val metadata: SnapshotMetadata
}

public expect enum class ChangeType {
    ADDED,
    MODIFIED,
    REMOVED,
}

public expect class DocumentChange {
    public val document: DocumentSnapshot
    public val newIndex: Int
    public val oldIndex: Int
    public val type: ChangeType
}

internal expect class NativeDocumentSnapshot

public data class DocumentSnapshot internal constructor(internal val nativeWrapper: NativeDocumentSnapshotWrapper) {

    public companion object {}

    internal constructor(native: NativeDocumentSnapshot) : this(NativeDocumentSnapshotWrapper(native))

    internal val native: NativeDocumentSnapshot = nativeWrapper.native

    val exists: Boolean get() = nativeWrapper.exists
    val id: String get() = nativeWrapper.id
    val reference: DocumentReference get() = DocumentReference(nativeWrapper.reference)
    val metadata: SnapshotMetadata get() = nativeWrapper.metadata

    public fun contains(field: String): Boolean = nativeWrapper.contains(field)
    public fun contains(fieldPath: FieldPath): Boolean = nativeWrapper.contains(fieldPath.encoded)

    public inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(value = getEncoded(field, serverTimestampBehavior), buildSettings)
    public inline fun <T> get(field: String, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, getEncoded(field, serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun getEncoded(field: String, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = nativeWrapper.getEncoded(field, serverTimestampBehavior)

    public inline fun <reified T> get(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(value = getEncoded(fieldPath, serverTimestampBehavior), buildSettings)
    public inline fun <T> get(fieldPath: FieldPath, strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, getEncoded(fieldPath, serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun getEncoded(fieldPath: FieldPath, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = nativeWrapper.getEncoded(fieldPath.encoded, serverTimestampBehavior)

    public inline fun <reified T> data(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(encodedData(serverTimestampBehavior), buildSettings)
    public inline fun <T> data(strategy: DeserializationStrategy<T>, serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE, buildSettings: DecodeSettings.Builder.() -> Unit = {}): T = decode(strategy, encodedData(serverTimestampBehavior), buildSettings)

    @PublishedApi
    internal fun encodedData(serverTimestampBehavior: ServerTimestampBehavior = ServerTimestampBehavior.NONE): Any? = nativeWrapper.encodedData(serverTimestampBehavior)
}

public enum class ServerTimestampBehavior {
    ESTIMATE,
    NONE,
    PREVIOUS,
}

public expect class SnapshotMetadata {
    public val hasPendingWrites: Boolean
    public val isFromCache: Boolean
}

public expect class FieldPath(vararg fieldNames: String) {
    public companion object {
        public val documentId: FieldPath
    }

    @Deprecated("Use companion object instead", replaceWith = ReplaceWith("FieldPath.documentId"))
    public val documentId: FieldPath
    public val encoded: EncodedFieldPath
}

public expect class EncodedFieldPath

public enum class Source {
    CACHE,
    SERVER,
    DEFAULT,
}
