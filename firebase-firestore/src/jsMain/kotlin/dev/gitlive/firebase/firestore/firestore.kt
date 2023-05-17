/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.js.Json
import kotlin.js.json

actual val Firebase.firestore get() =
    rethrow { dev.gitlive.firebase.firestore; FirebaseFirestore(firebase.firestore()) }

actual fun Firebase.firestore(app: FirebaseApp) =
    rethrow { dev.gitlive.firebase.firestore; FirebaseFirestore(firebase.app().firestore()) }

actual data class FirebaseFirestore(val js: firebase.firestore.Firestore) {

    actual data class Settings(
        actual val sslEnabled: Boolean? = null,
        actual val host: String? = null,
        actual val cacheSettings: LocalCacheSettings? = null
    ) {
        actual companion object {
            actual fun create(sslEnabled: Boolean?, host: String?, cacheSettings: LocalCacheSettings?) = Settings(sslEnabled, host, cacheSettings)
        }
    }

    private var lastSettings = Settings()

    actual fun collection(collectionPath: String) = rethrow { CollectionReference(js.collection(collectionPath)) }

    actual fun document(documentPath: String) = rethrow { DocumentReference(js.doc(documentPath)) }

    actual fun collectionGroup(collectionId: String) = rethrow { Query(js.collectionGroup(collectionId)) }

    actual fun batch() = rethrow { WriteBatch(js.batch()) }

    actual fun setLoggingEnabled(loggingEnabled: Boolean) =
        rethrow { firebase.firestore.setLogLevel( if(loggingEnabled) "error" else "silent") }

    actual suspend fun <T> runTransaction(func: suspend Transaction.() -> T) =
        rethrow { js.runTransaction { GlobalScope.promise { Transaction(it).func() } }.await() }

    actual suspend fun clearPersistence() =
        rethrow { js.clearPersistence().await() }

    actual fun useEmulator(host: String, port: Int) = rethrow { js.useEmulator(host, port) }

    actual fun setSettings(settings: Settings) {
        lastSettings = settings
        if(settings.cacheSettings is LocalCacheSettings.Persistent) js.enablePersistence()

        js.settings(json().apply {
            settings.sslEnabled?.let { set("ssl", it) }
            settings.host?.let { set("host", it) }
            when (val cacheSettings = settings.cacheSettings) {
                is LocalCacheSettings.Persistent -> cacheSettings.sizeBytes
                is LocalCacheSettings.Memory -> when (val garbaseCollectorSettings = cacheSettings.garbaseCollectorSettings) {
                    is LocalCacheSettings.Memory.GarbageCollectorSettings.Eager -> null
                    is LocalCacheSettings.Memory.GarbageCollectorSettings.LRUGC -> garbaseCollectorSettings.sizeBytes
                }
                null -> null
            }?.let { set("cacheSizeBytes", it) }
        })
    }

    actual fun updateSettings(settings: Settings) = setSettings(
        Settings(settings.sslEnabled ?: lastSettings.sslEnabled, settings.host ?: lastSettings.host, settings.cacheSettings ?: lastSettings.cacheSettings)
    )

    actual suspend fun disableNetwork() {
        rethrow { js.disableNetwork().await() }
    }

    actual suspend fun enableNetwork() {
        rethrow { js.enableNetwork().await() }
    }
}

val SetOptions.js: Json get() = when (this) {
    is SetOptions.Merge -> json("merge" to true)
    is SetOptions.Overwrite -> json("merge" to false)
    is SetOptions.MergeFields -> json("mergeFields" to fields.toTypedArray())
    is SetOptions.MergeFieldPaths -> json("mergeFields" to encodedFieldPaths.toTypedArray())
}

actual class WriteBatch(val js: firebase.firestore.WriteBatch) : BaseWriteBatch() {

    actual val async = Async(js)

    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): BaseWriteBatch = rethrow { js.set(documentRef.js, encodedData, setOptions.js) }.let { this }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        encodedFieldsAndValues: List<Pair<String, Any?>>,
        merge: Boolean
    ): BaseWriteBatch = rethrow {
            val serializedItem = encodedData as Json
            val serializedFieldAndValues = json(*encodedFieldsAndValues.toTypedArray())

            val result = serializedItem.add(serializedFieldAndValues)
            if (merge) {
                js.set(documentRef.js, result, json("merge" to merge))
            } else {
                js.set(documentRef.js, result)
            }
        }.let { this }

    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseWriteBatch = rethrow { js.update(documentRef.js, encodedData) }
            .let { this }

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    override fun updateEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseWriteBatch = rethrow {
        val serializedItem = encodedData as Json
        val serializedFieldAndValues = json(*encodedFieldsAndValues.toTypedArray())

        val result = serializedItem.add(serializedFieldAndValues)
        js.update(documentRef.js, result)
    }.let { this }

    override fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseWriteBatch = rethrow {
        encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    override fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): BaseWriteBatch = rethrow {
        encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun commit() = rethrow { async.commit().await() }

    @Suppress("DeferredIsResult")
    actual class Async(private val js: firebase.firestore.WriteBatch) {
        actual fun commit() = rethrow { js.commit().asDeferred() }
    }
}

actual class Transaction(val js: firebase.firestore.Transaction) : BaseTransaction() {

    override fun setEncoded(
        documentRef: DocumentReference,
        encodedData: Any,
        setOptions: SetOptions
    ): BaseTransaction = rethrow {
        js.set(documentRef.js, encodedData, setOptions.js)
    }
    .let { this }

    override fun updateEncoded(documentRef: DocumentReference, encodedData: Any): BaseTransaction = rethrow { js.update(documentRef.js, encodedData) }
            .let { this }

    override fun updateEncodedFieldsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<String, Any?>>
    ): BaseTransaction = rethrow {
        encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    override fun updateEncodedFieldPathsAndValues(
        documentRef: DocumentReference,
        encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>
    ): BaseTransaction = rethrow {
        encodedFieldsAndValues.performUpdate { field, value, moreFieldsAndValues ->
            js.update(documentRef.js, field, value, *moreFieldsAndValues)
        }
    }.let { this }

    actual fun delete(documentRef: DocumentReference) =
        rethrow { js.delete(documentRef.js) }
            .let { this }

    actual suspend fun get(documentRef: DocumentReference) =
        rethrow { DocumentSnapshot(js.get(documentRef.js).await()) }
}

/** A class representing a platform specific Firebase DocumentReference. */
actual typealias NativeDocumentReference = firebase.firestore.DocumentReference

@Serializable(with = DocumentReferenceSerializer::class)
actual class DocumentReference actual constructor(internal actual val nativeValue: NativeDocumentReference) : BaseDocumentReference() {
    val js: NativeDocumentReference = nativeValue

    actual val id: String
        get() = rethrow { js.id }

    actual val path: String
        get() = rethrow { js.path }

    actual val parent: CollectionReference
        get() = rethrow { CollectionReference(js.parent) }

    override val async = Async(nativeValue)

    actual fun collection(collectionPath: String) = rethrow { CollectionReference(js.collection(collectionPath)) }

    actual suspend fun get() = rethrow { DocumentSnapshot(js.get().await()) }

    actual val snapshots get() = callbackFlow<DocumentSnapshot> {
        val unsubscribe = js.onSnapshot(
            { trySend(DocumentSnapshot(it)) },
            { close(errorToException(it)) }
        )
        awaitClose { unsubscribe() }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is DocumentReference && nativeValue.isEqual(other.nativeValue)
    override fun hashCode(): Int = nativeValue.hashCode()
    override fun toString(): String = "DocumentReference(path=$path)"

    @Suppress("DeferredIsResult")
    class Async(@PublishedApi internal val js: NativeDocumentReference) : BaseDocumentReference.Async() {

        override fun setEncoded(encodedData: Any, setOptions: SetOptions): Deferred<Unit> = rethrow {
            js.set(encodedData, setOptions.js).asDeferred()
        }

        override fun updateEncoded(encodedData: Any): Deferred<Unit> = rethrow { js.update(encodedData).asDeferred() }

        override fun updateEncodedFieldsAndValues(encodedFieldsAndValues: List<Pair<String, Any?>>): Deferred<Unit> = rethrow {
            encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }
                ?.performUpdate { field, value, moreFieldsAndValues ->
                js.update(field, value, *moreFieldsAndValues)
            }
                ?.asDeferred() ?: CompletableDeferred(Unit)
        }

        override fun updateEncodedFieldPathsAndValues(encodedFieldsAndValues: List<Pair<EncodedFieldPath, Any?>>): Deferred<Unit> = rethrow {
            encodedFieldsAndValues.takeUnless { encodedFieldsAndValues.isEmpty() }
                ?.performUpdate { field, value, moreFieldsAndValues ->
                    js.update(field, value, *moreFieldsAndValues)
                }
                ?.asDeferred() ?: CompletableDeferred(Unit)
        }

        override fun delete() = rethrow { js.delete().asDeferred() }
    }
}

actual open class Query(open val js: firebase.firestore.Query) {

    actual suspend fun get() =  rethrow { QuerySnapshot(js.get().await()) }

    actual fun limit(limit: Number) = Query(js.limit(limit.toDouble()))

    internal actual fun _where(field: String, equalTo: Any?) = rethrow { Query(js.where(field, "==", equalTo)) }
    internal actual fun _where(path: FieldPath, equalTo: Any?) = rethrow { Query(js.where(path.js, "==", equalTo)) }

    internal actual fun _where(field: String, equalTo: DocumentReference) = rethrow { Query(js.where(field, "==", equalTo.js)) }
    internal actual fun _where(path: FieldPath, equalTo: DocumentReference) = rethrow { Query(js.where(path.js, "==", equalTo.js)) }

    internal actual fun _where(
        field: String, lessThan: Any?, greaterThan: Any?, arrayContains: Any?, notEqualTo: Any?,
        lessThanOrEqualTo: Any?, greaterThanOrEqualTo: Any?
    ) = rethrow {
        Query(
            when {
                lessThan != null -> js.where(field, "<", lessThan)
                greaterThan != null -> js.where(field, ">", greaterThan)
                arrayContains != null -> js.where(field, "array-contains", arrayContains)
                notEqualTo != null -> js.where(field, "!=", notEqualTo)
                lessThanOrEqualTo != null -> js.where(field, "<=", lessThanOrEqualTo)
                greaterThanOrEqualTo != null -> js.where(field, ">=", greaterThanOrEqualTo)
                else -> js
            }
        )
    }

    internal actual fun _where(
        path: FieldPath, lessThan: Any?, greaterThan: Any?, arrayContains: Any?, notEqualTo: Any?,
        lessThanOrEqualTo: Any?, greaterThanOrEqualTo: Any?
    ) = rethrow {
        Query(
            when {
                lessThan != null -> js.where(path.js, "<", lessThan)
                greaterThan != null -> js.where(path.js, ">", greaterThan)
                arrayContains != null -> js.where(path.js, "array-contains", arrayContains)
                notEqualTo != null -> js.where(path.js, "!=", notEqualTo)
                lessThanOrEqualTo != null -> js.where(path.js, "<=", lessThanOrEqualTo)
                greaterThanOrEqualTo != null -> js.where(path.js, ">=", greaterThanOrEqualTo)
                else -> js
            }
        )
    }

    internal actual fun _where(
        field: String, inArray: List<Any>?, arrayContainsAny: List<Any>?, notInArray: List<Any>?
    ) = rethrow {
        Query(
            when {
                inArray != null -> js.where(field, "in", inArray.toTypedArray())
                arrayContainsAny != null -> js.where(field, "array-contains-any", arrayContainsAny.toTypedArray())
                notInArray != null -> js.where(field, "not-in", notInArray.toTypedArray())
                else -> js
            }
        )
    }

    internal actual fun _where(
        path: FieldPath, inArray: List<Any>?, arrayContainsAny: List<Any>?, notInArray: List<Any>?
    ) = rethrow {
        Query(
            when {
                inArray != null -> js.where(path.js, "in", inArray.toTypedArray())
                arrayContainsAny != null -> js.where(path.js, "array-contains-any", arrayContainsAny.toTypedArray())
                notInArray != null -> js.where(path.js, "not-in", notInArray.toTypedArray())
                else -> js
            }
        )
    }

    internal actual fun _orderBy(field: String, direction: Direction) = rethrow {
        Query(js.orderBy(field, direction.jsString))
    }

    internal actual fun _orderBy(field: FieldPath, direction: Direction) = rethrow {
        Query(js.orderBy(field.js, direction.jsString))
    }

    internal actual fun _startAfter(document: DocumentSnapshot) = rethrow { Query(js.startAfter(document.js)) }

    internal actual fun _startAfter(vararg fieldValues: Any) = rethrow { Query(js.startAfter(*fieldValues)) }

    internal actual fun _startAt(document: DocumentSnapshot) = rethrow { Query(js.startAt(document.js)) }

    internal actual fun _startAt(vararg fieldValues: Any) = rethrow { Query(js.startAt(*fieldValues)) }

    internal actual fun _endBefore(document: DocumentSnapshot) = rethrow { Query(js.endBefore(document.js)) }

    internal actual fun _endBefore(vararg fieldValues: Any) = rethrow { Query(js.endBefore(*fieldValues)) }

    internal actual fun _endAt(document: DocumentSnapshot) = rethrow { Query(js.endAt(document.js)) }

    internal actual fun _endAt(vararg fieldValues: Any) = rethrow { Query(js.endAt(*fieldValues)) }

    actual val snapshots get() = callbackFlow<QuerySnapshot> {
        val unsubscribe = rethrow {
            js.onSnapshot(
                { trySend(QuerySnapshot(it)) },
                { close(errorToException(it)) }
            )
        }
        awaitClose { rethrow { unsubscribe() } }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow<QuerySnapshot> {
        val unsubscribe = rethrow {
            js.onSnapshot(
                json("includeMetadataChanges" to includeMetadataChanges),
                { trySend(QuerySnapshot(it)) },
                { close(errorToException(it)) }
            )
        }
        awaitClose { rethrow { unsubscribe() } }
    }
}

actual class CollectionReference(override val js: firebase.firestore.CollectionReference) : Query(js) {

    actual val path: String
        get() =  rethrow { js.path }
    actual val async = Async(js)

    actual val document get() = rethrow { DocumentReference(js.doc()) }

    actual val parent get() = rethrow { js.parent?.let{DocumentReference(it)} }

    actual fun document(documentPath: String) = rethrow { DocumentReference(js.doc(documentPath)) }

    actual suspend inline fun <reified T> add(data: T, encodeSettings: EncodeSettings) =
        rethrow { DocumentReference(js.add(encode(data, encodeSettings)!!).await()) }
    actual suspend fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings) =
        rethrow { DocumentReference(js.add(encode(strategy, data, encodeSettings)!!).await()) }

    @Suppress("DeferredIsResult")
    actual class Async(@PublishedApi internal val js: firebase.firestore.CollectionReference) {
        actual inline fun <reified T> add(data: T, encodeSettings: EncodeSettings) =
            rethrow {
                js.add(encode(data, encodeSettings)!!).asDeferred()
                    .convert(::DocumentReference)
            }
        actual fun <T> add(strategy: SerializationStrategy<T>, data: T, encodeSettings: EncodeSettings) =
            rethrow {
                js.add(encode(strategy, data, encodeSettings)!!).asDeferred()
                    .convert(::DocumentReference)
            }
    }
}

actual class FirebaseFirestoreException(cause: Throwable, val code: FirestoreExceptionCode) : FirebaseException(code.toString(), cause)

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val FirebaseFirestoreException.code: FirestoreExceptionCode get() = code

actual class QuerySnapshot(val js: firebase.firestore.QuerySnapshot) {
    actual val documents
        get() = js.docs.map { DocumentSnapshot(it) }
    actual val documentChanges
        get() = js.docChanges().map { DocumentChange(it) }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)
}

actual class DocumentChange(val js: firebase.firestore.DocumentChange) {
    actual val document: DocumentSnapshot
        get() = DocumentSnapshot(js.doc)
    actual val newIndex: Int
        get() = js.newIndex
    actual val oldIndex: Int
        get() = js.oldIndex
    actual val type: ChangeType
        get() = ChangeType.values().first { it.jsString == js.type }
}

actual class DocumentSnapshot(val js: firebase.firestore.DocumentSnapshot) {

    actual val id get() = rethrow { js.id }
    actual val reference get() = rethrow { DocumentReference(js.ref) }

    actual inline fun <reified T : Any> data(serverTimestampBehavior: ServerTimestampBehavior): T =
        rethrow { decode(value = js.data(getTimestampsOptions(serverTimestampBehavior))) }

    actual fun <T> data(strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings, serverTimestampBehavior: ServerTimestampBehavior): T =
        rethrow { decode(strategy, js.data(getTimestampsOptions(serverTimestampBehavior)), decodeSettings) }

    actual inline fun <reified T> get(field: String, serverTimestampBehavior: ServerTimestampBehavior) =
        rethrow { decode<T>(value = js.get(field, getTimestampsOptions(serverTimestampBehavior))) }

    actual fun <T> get(field: String, strategy: DeserializationStrategy<T>, decodeSettings: DecodeSettings, serverTimestampBehavior: ServerTimestampBehavior) =
        rethrow { decode(strategy, js.get(field, getTimestampsOptions(serverTimestampBehavior)), decodeSettings) }

    actual fun contains(field: String) = rethrow { js.get(field) != undefined }
    actual val exists get() = rethrow { js.exists }
    actual val metadata: SnapshotMetadata get() = SnapshotMetadata(js.metadata)

    fun getTimestampsOptions(serverTimestampBehavior: ServerTimestampBehavior) =
        json("serverTimestamps" to serverTimestampBehavior.name.lowercase())
}

actual class SnapshotMetadata(val js: firebase.firestore.SnapshotMetadata) {
    actual val hasPendingWrites: Boolean get() = js.hasPendingWrites
    actual val isFromCache: Boolean get() = js.fromCache
}

actual class FieldPath private constructor(val js: firebase.firestore.FieldPath) {
    actual constructor(vararg fieldNames: String) : this(dev.gitlive.firebase.firestore.rethrow {
        js("Reflect").construct(firebase.firestore.FieldPath, fieldNames).unsafeCast<firebase.firestore.FieldPath>()
    })
    actual val documentId: FieldPath get() = FieldPath(firebase.firestore.FieldPath.documentId)
    actual val encoded: EncodedFieldPath = js
    override fun equals(other: Any?): Boolean = other is FieldPath && js.isEqual(other.js)
    override fun hashCode(): Int = js.hashCode()
    override fun toString(): String = js.toString()
}

actual typealias EncodedFieldPath = firebase.firestore.FieldPath

//actual data class FirebaseFirestoreSettings internal constructor(
//    val cacheSizeBytes: Number? = undefined,
//    val host: String? = undefined,
//    val ssl: Boolean? = undefined,
//    var timestampsInSnapshots: Boolean? = undefined,
//    var enablePersistence: Boolean = false
//)

actual enum class FirestoreExceptionCode {
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
    UNAUTHENTICATED
}

actual enum class Direction(internal val jsString : String) {
    ASCENDING("asc"),
    DESCENDING("desc");
}

actual enum class ChangeType(internal val jsString : String) {
    ADDED("added"),
    MODIFIED("modified"),
    REMOVED("removed");
}

inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.firestore.rethrow { function() }

inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch(e: dynamic) {
        throw errorToException(e)
    }
}

fun errorToException(e: dynamic) = (e?.code ?: e?.message ?: "")
    .toString()
    .lowercase()
    .let {
        when {
            "cancelled" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.CANCELLED)
            "invalid-argument" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.INVALID_ARGUMENT)
            "deadline-exceeded" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.DEADLINE_EXCEEDED)
            "not-found" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.NOT_FOUND)
            "already-exists" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.ALREADY_EXISTS)
            "permission-denied" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.PERMISSION_DENIED)
            "resource-exhausted" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.RESOURCE_EXHAUSTED)
            "failed-precondition" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.FAILED_PRECONDITION)
            "aborted" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.ABORTED)
            "out-of-range" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.OUT_OF_RANGE)
            "unimplemented" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNIMPLEMENTED)
            "internal" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.INTERNAL)
            "unavailable" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAVAILABLE)
            "data-loss" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.DATA_LOSS)
            "unauthenticated" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNAUTHENTICATED)
            "unknown" in it -> FirebaseFirestoreException(e, FirestoreExceptionCode.UNKNOWN)
            else -> {
                println("Unknown error code in ${JSON.stringify(e)}")
                FirebaseFirestoreException(e, FirestoreExceptionCode.UNKNOWN)
            }
        }
}

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
fun entriesOf(jsObject: dynamic): List<Pair<String, Any?>> =
    (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
        .invoke(jsObject)
        .map { entry -> entry[0] as String to entry[1] }

// from: https://discuss.kotlinlang.org/t/how-to-access-native-js-object-as-a-map-string-any/509/8
fun mapOf(jsObject: dynamic): Map<String, Any?> =
    entriesOf(jsObject).toMap()
