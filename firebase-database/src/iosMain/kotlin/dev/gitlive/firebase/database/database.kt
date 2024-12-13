/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import cocoapods.FirebaseDatabase.FIRDataEventType
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildAdded
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildChanged
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildMoved
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeChildRemoved
import cocoapods.FirebaseDatabase.FIRDataEventType.FIRDataEventTypeValue
import cocoapods.FirebaseDatabase.FIRDataSnapshot
import cocoapods.FirebaseDatabase.FIRDatabase
import cocoapods.FirebaseDatabase.FIRDatabaseQuery
import cocoapods.FirebaseDatabase.FIRDatabaseReference
import cocoapods.FirebaseDatabase.FIRTransactionResult
import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.ChildEvent.Type
import dev.gitlive.firebase.database.ChildEvent.Type.ADDED
import dev.gitlive.firebase.database.ChildEvent.Type.CHANGED
import dev.gitlive.firebase.database.ChildEvent.Type.MOVED
import dev.gitlive.firebase.database.ChildEvent.Type.REMOVED
import dev.gitlive.firebase.database.ios as publicIos
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.ios
import dev.gitlive.firebase.internal.reencodeTransformation
import dev.gitlive.firebase.ios
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import platform.Foundation.NSError
import platform.Foundation.allObjects

public val FirebaseDatabase.ios: FIRDatabase get() = FIRDatabase.database()

public actual val Firebase.database: FirebaseDatabase
    by lazy { FirebaseDatabase(FIRDatabase.database()) }

public actual fun Firebase.database(url: String): FirebaseDatabase = FirebaseDatabase(FIRDatabase.databaseWithURL(url))

public actual fun Firebase.database(app: FirebaseApp): FirebaseDatabase = FirebaseDatabase(
    FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp),
)

public actual fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase = FirebaseDatabase(
    FIRDatabase.databaseForApp(app.ios as objcnames.classes.FIRApp, url),
)

public actual class FirebaseDatabase internal constructor(internal val ios: FIRDatabase) {

    public actual fun reference(path: String): DatabaseReference = DatabaseReference(NativeDatabaseReference(ios.referenceWithPath(path), ios.persistenceEnabled))

    public actual fun reference(): DatabaseReference = DatabaseReference(NativeDatabaseReference(ios.reference(), ios.persistenceEnabled))

    public actual fun setPersistenceEnabled(enabled: Boolean) {
        ios.persistenceEnabled = enabled
    }

    public actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {
        ios.setPersistenceCacheSizeBytes(cacheSizeInBytes.toULong())
    }

    public actual fun setLoggingEnabled(enabled: Boolean) {
        FIRDatabase.setLoggingEnabled(enabled)
    }

    public actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    public actual fun goOffline() {
        ios.goOffline()
    }

    public actual fun goOnline() {
        ios.goOnline()
    }
}

public fun Type.toEventType(): FIRDataEventType = when (this) {
    ADDED -> FIRDataEventTypeChildAdded
    CHANGED -> FIRDataEventTypeChildChanged
    MOVED -> FIRDataEventTypeChildMoved
    REMOVED -> FIRDataEventTypeChildRemoved
}

internal actual open class NativeQuery(
    open val ios: FIRDatabaseQuery,
    val persistenceEnabled: Boolean,
)

public val Query.ios: FIRDatabaseQuery get() = nativeQuery.ios

public actual open class Query internal actual constructor(
    internal val nativeQuery: NativeQuery,
) {

    internal constructor(ios: FIRDatabaseQuery, persistenceEnabled: Boolean) : this(NativeQuery(ios, persistenceEnabled))

    internal open val ios: FIRDatabaseQuery = nativeQuery.ios
    public val persistenceEnabled: Boolean = nativeQuery.persistenceEnabled

    public actual fun orderByKey(): Query = Query(ios.queryOrderedByKey(), persistenceEnabled)

    public actual fun orderByValue(): Query = Query(ios.queryOrderedByValue(), persistenceEnabled)

    public actual fun orderByChild(path: String): Query = Query(ios.queryOrderedByChild(path), persistenceEnabled)

    public actual fun startAt(value: String, key: String?): Query = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    public actual fun startAt(value: Double, key: String?): Query = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    public actual fun startAt(value: Boolean, key: String?): Query = Query(ios.queryStartingAtValue(value, key), persistenceEnabled)

    public actual fun endAt(value: String, key: String?): Query = Query(ios.queryEndingAtValue(value, key), persistenceEnabled)

    public actual fun endAt(value: Double, key: String?): Query = Query(ios.queryEndingAtValue(value, key), persistenceEnabled)

    public actual fun endAt(value: Boolean, key: String?): Query = Query(ios.queryEndingAtValue(value, key), persistenceEnabled)

    public actual fun limitToFirst(limit: Int): Query = Query(ios.queryLimitedToFirst(limit.toULong()), persistenceEnabled)

    public actual fun limitToLast(limit: Int): Query = Query(ios.queryLimitedToLast(limit.toULong()), persistenceEnabled)

    public actual fun equalTo(value: String, key: String?): Query = Query(ios.queryEqualToValue(value, key), persistenceEnabled)

    public actual fun equalTo(value: Double, key: String?): Query = Query(ios.queryEqualToValue(value, key), persistenceEnabled)

    public actual fun equalTo(value: Boolean, key: String?): Query = Query(ios.queryEqualToValue(value, key), persistenceEnabled)

    public actual val valueEvents: Flow<DataSnapshot> get() = callbackFlow<DataSnapshot> {
        val handle = ios.observeEventType(
            FIRDataEventTypeValue,
            withBlock = { snapShot ->
                trySend(DataSnapshot(snapShot!!, persistenceEnabled))
            },
        ) { close(DatabaseException(it.toString(), null)) }
        awaitClose { ios.removeObserverWithHandle(handle) }
    }

    public actual fun childEvents(vararg types: Type): Flow<ChildEvent> = callbackFlow<ChildEvent> {
        val handles = types.map { type ->
            ios.observeEventType(
                type.toEventType(),
                andPreviousSiblingKeyWithBlock = { snapShot, key ->
                    trySend(ChildEvent(DataSnapshot(snapShot!!, persistenceEnabled), type, key))
                },
            ) { close(DatabaseException(it.toString(), null)) }
        }
        awaitClose {
            handles.forEach { ios.removeObserverWithHandle(it) }
        }
    }

    override fun toString(): String = ios.toString()
}

internal actual class NativeDatabaseReference internal constructor(
    override val ios: FIRDatabaseReference,
    persistenceEnabled: Boolean,
) : NativeQuery(ios, persistenceEnabled) {

    actual val key get() = ios.key

    actual fun child(path: String) = NativeDatabaseReference(ios.child(path), persistenceEnabled)

    actual fun push() = NativeDatabaseReference(ios.childByAutoId(), persistenceEnabled)
    actual fun onDisconnect() = NativeOnDisconnect(ios, persistenceEnabled)

    actual suspend fun setValueEncoded(encodedValue: Any?) {
        ios.await(persistenceEnabled) { setValue(encodedValue, it) }
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        ios.await(persistenceEnabled) { updateChildValues(encodedUpdate.ios, it) }
    }

    actual suspend fun removeValue() {
        ios.await(persistenceEnabled) { removeValueWithCompletionBlock(it) }
    }

    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit, transactionUpdate: (currentData: T) -> T): DataSnapshot {
        val deferred = CompletableDeferred<DataSnapshot>()
        ios.runTransactionBlock(
            block = { firMutableData ->
                firMutableData?.value = reencodeTransformation(strategy, firMutableData?.value, buildSettings, transactionUpdate)
                FIRTransactionResult.successWithValue(firMutableData!!)
            },
            andCompletionBlock = { error, _, snapshot ->
                if (error != null) {
                    deferred.completeExceptionally(DatabaseException(error.toString(), null))
                } else {
                    deferred.complete(DataSnapshot(snapshot!!, persistenceEnabled))
                }
            },
            withLocalEvents = false,
        )
        return deferred.await()
    }
}

public val DatabaseReference.ios: FIRDatabaseReference get() = nativeReference.ios
public val DataSnapshot.ios: FIRDataSnapshot get() = ios

public actual class DataSnapshot internal constructor(
    internal val ios: FIRDataSnapshot,
    private val persistenceEnabled: Boolean,
) {

    public actual val exists: Boolean get() = ios.exists()

    public actual val key: String? get() = ios.key

    public actual val ref: DatabaseReference get() = DatabaseReference(NativeDatabaseReference(ios.ref, persistenceEnabled))

    public actual val value: Any? get() = ios.value

    public actual inline fun <reified T> value(): T = decode<T>(value = publicIos.value)

    public actual inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit): T = decode(strategy, publicIos.value, buildSettings)

    public actual fun child(path: String): DataSnapshot = DataSnapshot(ios.childSnapshotForPath(path), persistenceEnabled)
    public actual val hasChildren: Boolean get() = ios.hasChildren()
    public actual val children: Iterable<DataSnapshot> get() = ios.children.allObjects.map { DataSnapshot(it as FIRDataSnapshot, persistenceEnabled) }
}

internal actual class NativeOnDisconnect internal constructor(
    val ios: FIRDatabaseReference,
    val persistenceEnabled: Boolean,
) {
    actual suspend fun removeValue() {
        ios.await(persistenceEnabled) { onDisconnectRemoveValueWithCompletionBlock(it) }
    }

    actual suspend fun cancel() {
        ios.await(persistenceEnabled) { cancelDisconnectOperationsWithCompletionBlock(it) }
    }

    actual suspend fun setEncodedValue(encodedValue: Any?) {
        ios.await(persistenceEnabled) { onDisconnectSetValue(encodedValue, it) }
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) {
        ios.await(persistenceEnabled) { onDisconnectUpdateChildValues(encodedUpdate.ios, it) }
    }
}

public val OnDisconnect.ios: FIRDatabaseReference get() = native.ios
public val OnDisconnect.persistenceEnabled: Boolean get() = native.persistenceEnabled

public actual class DatabaseException actual constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause)

internal suspend inline fun <T, reified R> T.awaitResult(whileOnline: Boolean, function: T.(callback: (NSError?, R?) -> Unit) -> Unit): R {
    val job = CompletableDeferred<R?>()
    function { error, result ->
        if (error == null) {
            job.complete(result)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    return job.run { if (whileOnline) awaitWhileOnline() else await() } as R
}

internal suspend inline fun <T> T.await(whileOnline: Boolean, function: T.(callback: (NSError?, FIRDatabaseReference?) -> Unit) -> Unit) {
    val job = CompletableDeferred<Unit>()
    function { error, _ ->
        if (error == null) {
            job.complete(Unit)
        } else {
            job.completeExceptionally(DatabaseException(error.toString(), null))
        }
    }
    job.run { if (whileOnline) awaitWhileOnline() else await() }
}

@FlowPreview
internal suspend fun <T> CompletableDeferred<T>.awaitWhileOnline(): T = coroutineScope {
    val notConnected = Firebase.database
        .reference(".info/connected")
        .valueEvents
        .filter { !it.value<Boolean>() }
        .produceIn(this)

    select {
        onAwait { it.also { notConnected.cancel() } }
        notConnected.onReceive { throw DatabaseException("Database not connected", null) }
    }
}
