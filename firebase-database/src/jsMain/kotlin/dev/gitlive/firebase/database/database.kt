/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.database

import dev.gitlive.firebase.DecodeSettings
import dev.gitlive.firebase.EncodeDecodeSettingsBuilder
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.database.externals.CancelCallback
import dev.gitlive.firebase.database.externals.ChangeSnapshotCallback
import dev.gitlive.firebase.database.externals.Database
import dev.gitlive.firebase.database.externals.child
import dev.gitlive.firebase.database.externals.connectDatabaseEmulator
import dev.gitlive.firebase.database.externals.enableLogging
import dev.gitlive.firebase.database.externals.getDatabase
import dev.gitlive.firebase.database.externals.onChildAdded
import dev.gitlive.firebase.database.externals.onChildChanged
import dev.gitlive.firebase.database.externals.onChildMoved
import dev.gitlive.firebase.database.externals.onChildRemoved
import dev.gitlive.firebase.database.externals.onDisconnect
import dev.gitlive.firebase.database.externals.onValue
import dev.gitlive.firebase.database.externals.push
import dev.gitlive.firebase.database.externals.query
import dev.gitlive.firebase.database.externals.ref
import dev.gitlive.firebase.database.externals.remove
import dev.gitlive.firebase.database.externals.set
import dev.gitlive.firebase.database.externals.update
import dev.gitlive.firebase.internal.EncodedObject
import dev.gitlive.firebase.internal.decode
import dev.gitlive.firebase.internal.js
import dev.gitlive.firebase.internal.reencodeTransformation
import dev.gitlive.firebase.js
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.selects.select
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlin.js.Promise
import kotlin.js.json
import dev.gitlive.firebase.database.externals.DataSnapshot as JsDataSnapshot
import dev.gitlive.firebase.database.externals.DatabaseReference as JsDatabaseReference
import dev.gitlive.firebase.database.externals.OnDisconnect as JsOnDisconnect
import dev.gitlive.firebase.database.externals.Query as JsQuery
import dev.gitlive.firebase.database.externals.endAt as jsEndAt
import dev.gitlive.firebase.database.externals.equalTo as jsEqualTo
import dev.gitlive.firebase.database.externals.goOffline as jsGoOffline
import dev.gitlive.firebase.database.externals.goOnline as jsGoOnline
import dev.gitlive.firebase.database.externals.limitToFirst as jsLimitToFirst
import dev.gitlive.firebase.database.externals.limitToLast as jsLimitToLast
import dev.gitlive.firebase.database.externals.orderByChild as jsOrderByChild
import dev.gitlive.firebase.database.externals.orderByKey as jsOrderByKey
import dev.gitlive.firebase.database.externals.orderByValue as jsOrderByValue
import dev.gitlive.firebase.database.externals.runTransaction as jsRunTransaction
import dev.gitlive.firebase.database.externals.startAt as jsStartAt
import dev.gitlive.firebase.database.js as publicJs

public actual val Firebase.database: FirebaseDatabase
    get() = rethrow { FirebaseDatabase(getDatabase()) }

public actual fun Firebase.database(app: FirebaseApp): FirebaseDatabase = rethrow { FirebaseDatabase(getDatabase(app = app.js)) }

public actual fun Firebase.database(url: String): FirebaseDatabase = rethrow { FirebaseDatabase(getDatabase(url = url)) }

public actual fun Firebase.database(app: FirebaseApp, url: String): FirebaseDatabase = rethrow { FirebaseDatabase(getDatabase(app = app.js, url = url)) }

public val FirebaseDatabase.js get() = js

public actual class FirebaseDatabase internal constructor(internal val js: Database) {

    public actual fun reference(path: String): DatabaseReference = rethrow { DatabaseReference(NativeDatabaseReference(ref(js, path), js)) }
    public actual fun reference(): DatabaseReference = rethrow { DatabaseReference(NativeDatabaseReference(ref(js), js)) }
    public actual fun setPersistenceEnabled(enabled: Boolean) {}
    public actual fun setPersistenceCacheSizeBytes(cacheSizeInBytes: Long) {}
    public actual fun setLoggingEnabled(enabled: Boolean): Unit = rethrow { enableLogging(enabled) }
    public actual fun useEmulator(host: String, port: Int): Unit = rethrow { connectDatabaseEmulator(js, host, port) }

    public actual fun goOffline(): Unit = rethrow { jsGoOffline(js) }

    public actual fun goOnline(): Unit = rethrow { jsGoOnline(js) }
}

internal actual open class NativeQuery(
    open val js: JsQuery,
    val database: Database,
)

public val Query.js: JsQuery get() = nativeQuery.js

public actual open class Query internal actual constructor(
    internal val nativeQuery: NativeQuery,
) {

    internal constructor(js: JsQuery, database: Database) : this(NativeQuery(js, database))

    public val database: Database = nativeQuery.database

    public actual fun orderByKey(): Query = Query(query(publicJs, jsOrderByKey()), database)
    public actual fun orderByValue(): Query = Query(query(publicJs, jsOrderByValue()), database)
    public actual fun orderByChild(path: String): Query = Query(query(publicJs, jsOrderByChild(path)), database)

    public actual val valueEvents: Flow<DataSnapshot>
        get() = callbackFlow {
            val unsubscribe = rethrow {
                onValue(
                    query = publicJs,
                    callback = { trySend(DataSnapshot(it, database)) },
                    cancelCallback = { close(DatabaseException(it)).run { } },
                )
            }
            awaitClose { rethrow { unsubscribe() } }
        }

    public actual fun childEvents(vararg types: ChildEvent.Type): Flow<ChildEvent> = callbackFlow {
        val unsubscribes = rethrow {
            types.map { type ->
                val callback: ChangeSnapshotCallback = { snapshot, previousChildName ->
                    trySend(
                        ChildEvent(
                            DataSnapshot(snapshot, database),
                            type,
                            previousChildName,
                        ),
                    )
                }

                val cancelCallback: CancelCallback = {
                    close(DatabaseException(it)).run { }
                }

                when (type) {
                    ChildEvent.Type.ADDED -> onChildAdded(publicJs, callback, cancelCallback)
                    ChildEvent.Type.CHANGED -> onChildChanged(publicJs, callback, cancelCallback)
                    ChildEvent.Type.MOVED -> onChildMoved(publicJs, callback, cancelCallback)
                    ChildEvent.Type.REMOVED -> onChildRemoved(publicJs, callback, cancelCallback)
                }
            }
        }
        awaitClose { rethrow { unsubscribes.forEach { it.invoke() } } }
    }

    public actual fun startAt(value: String, key: String?): Query = Query(query(publicJs, jsStartAt(value, key ?: undefined)), database)

    public actual fun startAt(value: Double, key: String?): Query = Query(query(publicJs, jsStartAt(value, key ?: undefined)), database)

    public actual fun startAt(value: Boolean, key: String?): Query = Query(query(publicJs, jsStartAt(value, key ?: undefined)), database)

    public actual fun endAt(value: String, key: String?): Query = Query(query(publicJs, jsEndAt(value, key ?: undefined)), database)

    public actual fun endAt(value: Double, key: String?): Query = Query(query(publicJs, jsEndAt(value, key ?: undefined)), database)

    public actual fun endAt(value: Boolean, key: String?): Query = Query(query(publicJs, jsEndAt(value, key ?: undefined)), database)

    public actual fun limitToFirst(limit: Int): Query = Query(query(publicJs, jsLimitToFirst(limit)), database)

    public actual fun limitToLast(limit: Int): Query = Query(query(publicJs, jsLimitToLast(limit)), database)

    public actual fun equalTo(value: String, key: String?): Query = Query(query(publicJs, jsEqualTo(value, key ?: undefined)), database)

    public actual fun equalTo(value: Double, key: String?): Query = Query(query(publicJs, jsEqualTo(value, key ?: undefined)), database)

    public actual fun equalTo(value: Boolean, key: String?): Query = Query(query(publicJs, jsEqualTo(value, key ?: undefined)), database)

    override fun toString(): String = publicJs.toString()
}

internal actual class NativeDatabaseReference internal constructor(
    override val js: JsDatabaseReference,
    database: Database,
) : NativeQuery(js, database) {

    actual val key get() = rethrow { js.key }
    actual fun push() = rethrow { NativeDatabaseReference(push(js), database) }
    actual fun child(path: String) = rethrow { NativeDatabaseReference(child(js, path), database) }

    actual fun onDisconnect() = rethrow { NativeOnDisconnect(onDisconnect(js), database) }

    actual suspend fun removeValue() = rethrow { remove(js).awaitWhileOnline(database) }

    actual suspend fun setValueEncoded(encodedValue: Any?) = rethrow {
        set(js, encodedValue).awaitWhileOnline(database)
    }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) = rethrow { update(js, encodedUpdate.js).awaitWhileOnline(database) }

    actual suspend fun <T> runTransaction(strategy: KSerializer<T>, buildSettings: EncodeDecodeSettingsBuilder.() -> Unit, transactionUpdate: (currentData: T) -> T): DataSnapshot = DataSnapshot(
        jsRunTransaction<Any?>(js, transactionUpdate = { currentData ->
            reencodeTransformation(strategy, currentData ?: json(), buildSettings, transactionUpdate)
        }).awaitWhileOnline(database).snapshot,
        database,
    )
}

public val DataSnapshot.js: JsDataSnapshot get() = js

public actual class DataSnapshot internal constructor(
    internal val js: JsDataSnapshot,
    public val database: Database,
) {
    public actual val value: Any? get() {
        check(!hasChildren) { "DataSnapshot.value can only be used for primitive values (snapshots without children)" }
        return js.`val`()
    }

    public actual inline fun <reified T> value(): T = rethrow { decode<T>(value = publicJs.`val`()) }

    public actual inline fun <T> value(strategy: DeserializationStrategy<T>, buildSettings: DecodeSettings.Builder.() -> Unit): T = rethrow { decode(strategy, publicJs.`val`(), buildSettings) }

    public actual val exists: Boolean get() = rethrow { js.exists() }
    public actual val key: String? get() = rethrow { js.key }
    public actual fun child(path: String): DataSnapshot = DataSnapshot(js.child(path), database)
    public actual val hasChildren: Boolean get() = js.hasChildren()
    public actual val children: Iterable<DataSnapshot> = rethrow {
        ArrayList<DataSnapshot>(js.size).also {
            js.forEach { snapshot ->
                it.add(DataSnapshot(snapshot, database))
                false // don't cancel enumeration
            }
        }
    }
    public actual val ref: DatabaseReference
        get() = DatabaseReference(NativeDatabaseReference(js.ref, database))
}

internal actual class NativeOnDisconnect internal constructor(
    val js: JsOnDisconnect,
    val database: Database,
) {

    actual suspend fun removeValue() = rethrow { js.remove().awaitWhileOnline(database) }
    actual suspend fun cancel() = rethrow { js.cancel().awaitWhileOnline(database) }

    actual suspend fun setEncodedValue(encodedValue: Any?) = rethrow { js.set(encodedValue).awaitWhileOnline(database) }

    actual suspend fun updateEncodedChildren(encodedUpdate: EncodedObject) = rethrow { js.update(encodedUpdate.js).awaitWhileOnline(database) }
}

public val OnDisconnect.js: dev.gitlive.firebase.database.externals.OnDisconnect get() = native.js
public val OnDisconnect.database: Database get() = native.database

public actual class DatabaseException actual constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
    public constructor(error: dynamic) : this("${error.code ?: "UNKNOWN"}: ${error.message}", error.unsafeCast<Throwable>())
}

@PublishedApi
internal inline fun <T, R> T.rethrow(function: T.() -> R): R = dev.gitlive.firebase.database.rethrow { function() }

@PublishedApi
internal inline fun <R> rethrow(function: () -> R): R {
    try {
        return function()
    } catch (e: Exception) {
        throw e
    } catch (e: dynamic) {
        throw DatabaseException(e)
    }
}

@PublishedApi
internal suspend fun <T> Promise<T>.awaitWhileOnline(database: Database): T = coroutineScope {
    val notConnected = FirebaseDatabase(database)
        .reference(".info/connected")
        .valueEvents
        .filter { !it.value<Boolean>() }
        .produceIn(this)

    select {
        this@awaitWhileOnline.asDeferred().onAwait { it.also { notConnected.cancel() } }
        notConnected.onReceive { throw DatabaseException("Database not connected", null) }
    }
}
